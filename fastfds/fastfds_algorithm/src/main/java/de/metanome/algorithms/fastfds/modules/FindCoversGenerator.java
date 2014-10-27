package de.metanome.algorithms.fastfds.modules;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.util.OpenBitSet;

import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;
import de.metanome.algorithms.fastfds.fastfds_helper.modules.Algorithm_Group2_Modul;
import de.metanome.algorithms.fastfds.fastfds_helper.modules.container.FunctionalDependencyGroup2;
import de.metanome.algorithms.fastfds.modules.container.DifferenceSet;

public class FindCoversGenerator extends Algorithm_Group2_Modul {

    private FunctionalDependencyResultReceiver receiver;
    private List<String> columnNames;
    private String tableIdentifier;

    public FindCoversGenerator(FunctionalDependencyResultReceiver resultReceiver, List<String> columnNames,
                               String tableIdentifier, int numberOfThreads) {

        super(numberOfThreads, "FindCoversGen");

        this.receiver = resultReceiver;
        this.columnNames = columnNames;
        this.tableIdentifier = tableIdentifier;
    }

    public List<FunctionalDependencyGroup2> execute(List<DifferenceSet> differenceSets, int numberOfAttributes)
            throws CouldNotReceiveResultException {

        if (this.timeMesurement) {
            this.startTime();
        }

        List<FunctionalDependencyGroup2> result = new LinkedList<FunctionalDependencyGroup2>();

        for (int attribute = 0; attribute < numberOfAttributes; attribute++) {

            List<DifferenceSet> tempDiffSet = new LinkedList<DifferenceSet>();

            // Compute DifferenceSet modulo attribute (line 3 - Fig5 - FastFDs)
            for (DifferenceSet ds : differenceSets) {
                OpenBitSet obs = ds.getAttributes().clone();
                if (!obs.get(attribute)) {
                    continue;
                } else {
                    obs.flip(attribute);
                    tempDiffSet.add(new DifferenceSet(obs));
                }
            }

            // check new DifferenceSet (line 4 + 5 - Fig5 - FastFDs)
            if (tempDiffSet.size() == 0) {
                this.addFdToReceivers(new FunctionalDependencyGroup2(attribute, new IntArrayList()));
            } else if (this.checkNewSet(tempDiffSet)) {
                List<DifferenceSet> copy = new LinkedList<DifferenceSet>();
                copy.addAll(tempDiffSet);
                this.doRecusiveCrap(attribute, this.generateInitialOrdering(tempDiffSet), copy, new IntArrayList(), tempDiffSet,
                        result);
            }

        }

        if (this.timeMesurement) {
            this.stopTime();
        }

        return result;

    }

    private boolean checkNewSet(List<DifferenceSet> tempDiffSet) {

        for (DifferenceSet ds : tempDiffSet) {
            if (ds.getAttributes().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private IntList generateInitialOrdering(List<DifferenceSet> tempDiffSet) {

        IntList result = new IntArrayList();

        Int2IntMap counting = new Int2IntArrayMap();
        for (DifferenceSet ds : tempDiffSet) {

            int lastIndex = ds.getAttributes().nextSetBit(0);

            while (lastIndex != -1) {
                if (!counting.containsKey(lastIndex)) {
                    counting.put(lastIndex, 1);
                } else {
                    counting.put(lastIndex, counting.get(lastIndex) + 1);
                }
                lastIndex = ds.getAttributes().nextSetBit(lastIndex + 1);
            }
        }

        // TODO: Comperator und TreeMap --> Tommy
        while (true) {

            if (counting.size() == 0) {
                break;
            }

            int biggestAttribute = -1;
            int numberOfOcc = 0;
            for (int attr : counting.keySet()) {

                if (biggestAttribute < 0) {
                    biggestAttribute = attr;
                    numberOfOcc = counting.get(attr);
                    continue;
                }

                int tempOcc = counting.get(attr);
                if (tempOcc > numberOfOcc) {
                    numberOfOcc = tempOcc;
                    biggestAttribute = attr;
                } else if (tempOcc == numberOfOcc) {
                    if (biggestAttribute > attr) {
                        biggestAttribute = attr;
                    }
                }
            }

            if (numberOfOcc == 0) {
                break;
            }

            result.add(biggestAttribute);
            counting.remove(biggestAttribute);
        }

        return result;
    }

    private void doRecusiveCrap(int currentAttribute, IntList currentOrdering, List<DifferenceSet> setsNotCovered,
                                IntList currentPath, List<DifferenceSet> originalDiffSet, List<FunctionalDependencyGroup2> result)
            throws CouldNotReceiveResultException {

        // Basic Case
        // FIXME
        if (!currentOrdering.isEmpty() && /* BUT */setsNotCovered.isEmpty()) {
            if (this.debugSysout)
                System.out.println("no FDs here");
            return;
        }

        if (setsNotCovered.isEmpty()) {

            List<OpenBitSet> subSets = this.generateSubSets(currentPath);
            if (this.noOneCovers(subSets, originalDiffSet)) {
                FunctionalDependencyGroup2 fdg = new FunctionalDependencyGroup2(currentAttribute, currentPath);
                this.addFdToReceivers(fdg);
                result.add(fdg);
            } else {
                if (this.debugSysout) {
                    System.out.println("FD not minimal");
                    System.out.println(new FunctionalDependencyGroup2(currentAttribute, currentPath));
                }
            }

            return;
        }

        // Recusive Case
        for (int i = 0; i < currentOrdering.size(); i++) {

            List<DifferenceSet> next = this.generateNextNotCovered(currentOrdering.getInt(i), setsNotCovered);
            IntList nextOrdering = this.generateNextOrdering(next, currentOrdering, currentOrdering.getInt(i));
            IntList currentPathCopy = new IntArrayList(currentPath);
            currentPathCopy.add(currentOrdering.getInt(i));
            this.doRecusiveCrap(currentAttribute, nextOrdering, next, currentPathCopy, originalDiffSet, result);
        }

    }

    private IntList generateNextOrdering(List<DifferenceSet> next, IntList currentOrdering, int attribute) {

        IntList result = new IntArrayList();

        Int2IntMap counting = new Int2IntArrayMap();
        boolean seen = false;
        for (int i = 0; i < currentOrdering.size(); i++) {

            if (!seen) {
                if (currentOrdering.getInt(i) != attribute) {
                    continue;
                } else {
                    seen = true;
                }
            } else {

                counting.put(currentOrdering.getInt(i), 0);
                for (DifferenceSet ds : next) {

                    if (ds.getAttributes().get(currentOrdering.getInt(i))) {
                        counting.put(currentOrdering.getInt(i), counting.get(currentOrdering.getInt(i)) + 1);
                    }
                }
            }
        }

        // TODO: Comperator und TreeMap --> Tommy
        while (true) {

            if (counting.size() == 0) {
                break;
            }

            int biggestAttribute = -1;
            int numberOfOcc = 0;
            for (int attr : counting.keySet()) {

                if (biggestAttribute < 0) {
                    biggestAttribute = attr;
                    numberOfOcc = counting.get(attr);
                    continue;
                }

                int tempOcc = counting.get(attr);
                if (tempOcc > numberOfOcc) {
                    numberOfOcc = tempOcc;
                    biggestAttribute = attr;
                } else if (tempOcc == numberOfOcc) {
                    if (biggestAttribute > attr) {
                        biggestAttribute = attr;
                    }
                }
            }

            if (numberOfOcc == 0) {
                break;
            }

            result.add(biggestAttribute);
            counting.remove(biggestAttribute);
        }

        return result;
    }

    private List<DifferenceSet> generateNextNotCovered(int attribute, List<DifferenceSet> setsNotCovered) {

        List<DifferenceSet> result = new LinkedList<DifferenceSet>();

        for (DifferenceSet ds : setsNotCovered) {

            if (!ds.getAttributes().get(attribute)) {
                result.add(ds);
            }
        }

        return result;
    }

    private void addFdToReceivers(FunctionalDependencyGroup2 fdg) throws CouldNotReceiveResultException {

        FunctionalDependency fd = fdg.buildDependency(this.tableIdentifier, this.columnNames);
        this.receiver.receiveResult(fd);

    }

    private boolean noOneCovers(List<OpenBitSet> subSets, List<DifferenceSet> originalDiffSet) {

        for (OpenBitSet obs : subSets) {

            if (this.covers(obs, originalDiffSet)) {
                return false;
            }

        }

        return true;
    }

    private boolean covers(OpenBitSet obs, List<DifferenceSet> originalDiffSet) {

        for (DifferenceSet diff : originalDiffSet) {

            if (OpenBitSet.intersectionCount(obs, diff.getAttributes()) == 0) {
                return false;
            }
        }

        return true;
    }

    private List<OpenBitSet> generateSubSets(IntList currentPath) {

        List<OpenBitSet> result = new LinkedList<OpenBitSet>();

        OpenBitSet obs = new OpenBitSet();
        for (int i : currentPath) {
            obs.set(i);
        }

        for (int i : currentPath) {

            OpenBitSet obs_ = obs.clone();
            obs_.flip(i);
            result.add(obs_);

        }

        return result;
    }
}
