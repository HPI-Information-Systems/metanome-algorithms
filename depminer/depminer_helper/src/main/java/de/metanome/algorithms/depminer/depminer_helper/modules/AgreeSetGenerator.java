package de.metanome.algorithms.depminer.depminer_helper.modules;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithms.depminer.depminer_helper.modules.container.AgreeSet;
import de.metanome.algorithms.depminer.depminer_helper.modules.container.StrippedPartition;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

public class AgreeSetGenerator extends Algorithm_Group2_Modul {

    private boolean chooseAlternative1 = false;
    private boolean chooseAlternative2 = true;

    public AgreeSetGenerator(int nrOfThreads) {

        super(nrOfThreads, "AgreeSetGen");
    }

    public List<AgreeSet> execute(List<StrippedPartition> partitions) throws AlgorithmExecutionException {

        if (this.timeMesurement) {
            this.startTime();
        }

        if (this.debugSysout) {
            long sum = 0;
            for (StrippedPartition p : partitions) {
                System.out.println("-----");
                System.out.println("Attribut: " + p.getAttributeID());
                System.out.println("Anzahl Partitionen: " + p.getValues().size());
                sum += p.getValues().size();
            }
            System.out.println("-----");
            System.out.println("Summe: " + sum);
            System.out.println("-----");
        }

        Set<IntList> maxSets;
        if (this.chooseAlternative1) {
            maxSets = this.computeMaximumSetsAlternative(partitions);
        } else if (this.chooseAlternative2) {
            maxSets = this.computeMaximumSetsAlternative2(partitions);
        } else {
            maxSets = this.computeMaximumSets(partitions);
        }

        Set<AgreeSet> agreeSets = computeAgreeSets(this.calculateRelationships(partitions), maxSets, partitions);

        List<AgreeSet> result = new LinkedList<AgreeSet>(agreeSets);

        if (this.timeMesurement) {
            this.stopTime();
        }

        return result;
    }

    public Set<AgreeSet> computeAgreeSets(Int2ObjectMap<TupleEquivalenceClassRelation> relationships, Set<IntList> maxSets,
                                          List<StrippedPartition> partitions) throws AlgorithmExecutionException {

        if (this.debugSysout) {
            System.out.println("\tstartet calculation of agree sets");
            int bitsPerSet = (((partitions.size() - 1) / 64) + 1) * 64;
            long setsNeeded = 0;
            for (IntList l : maxSets) {
                setsNeeded += l.size() * (l.size() - 1) / 2;
            }
            System.out
                    .println("Approx. RAM needed to store all agree sets: " + bitsPerSet * setsNeeded / 8 / 1024 / 1024 + " MB");
        }

        partitions.clear();

        if (this.debugSysout) {
            System.out.println(maxSets.size());
        }
        int a = 0;

        if (this.optimize()) {
            Map<AgreeSet, Object> agreeSets = new ConcurrentHashMap<AgreeSet, Object>();

            ExecutorService exec = this.getExecuter();
            for (IntList maxEquiClass : maxSets) {
                if (this.debugSysout) {
                    System.out.println(a++);
                }
                for (int i = 0; i < maxEquiClass.size() - 1; i++) {
                    for (int j = i + 1; j < maxEquiClass.size(); j++) {
                        exec.execute(new IntersectWithAndAddToAgreeSetTask(i, j, maxEquiClass, relationships, agreeSets));
                    }
                }
            }
            this.awaitExecuter(exec);

            return agreeSets.keySet();
        }

        Set<AgreeSet> agreeSets = new HashSet<AgreeSet>();

        for (IntList maxEquiClass : maxSets) {
            if (this.debugSysout) {
                System.out.println(a++);
            }
            for (int i = 0; i < maxEquiClass.size() - 1; i++) {
                for (int j = i + 1; j < maxEquiClass.size(); j++) {
                    relationships.get(maxEquiClass.getInt(i)).intersectWithAndAddToAgreeSet(
                            relationships.get(maxEquiClass.getInt(j)), agreeSets);
                }
            }
        }

        return agreeSets;
    }

    public Int2ObjectMap<TupleEquivalenceClassRelation> calculateRelationships(List<StrippedPartition> partitions) {

        if (this.debugSysout) {
            System.out.println("\tstartet calculation of relationships");
        }
        Int2ObjectMap<TupleEquivalenceClassRelation> relationships = new Int2ObjectOpenHashMap<TupleEquivalenceClassRelation>();
        for (StrippedPartition p : partitions) {
            this.calculateRelationship(p, relationships);
        }

        return relationships;
    }

    private void calculateRelationship(StrippedPartition partitions, Int2ObjectMap<TupleEquivalenceClassRelation> relationships) {

        int partitionNr = 0;
        for (IntList partition : partitions.getValues()) {
            if (this.debugSysout)
                System.out.println(".");
            for (int index : partition) {
                if (!relationships.containsKey(index)) {
                    relationships.put(index, new TupleEquivalenceClassRelation());
                }
                relationships.get(index).addNewRelationship(partitions.getAttributeID(), partitionNr);
            }
            partitionNr++;
        }

    }

    public Set<IntList> computeMaximumSets(List<StrippedPartition> partitionsOrig) {

        if (this.debugSysout) {
            System.out.println("\tstartet calculation of maximal partitions");
        }

        List<StrippedPartition> partitions = new LinkedList<StrippedPartition>();
        for (StrippedPartition p : partitionsOrig) {
            partitions.add(p.copy());
        }

        Set<IntList> maxSets = new HashSet<IntList>(partitions.get(partitions.size() - 1).getValues());

        for (int i = partitions.size() - 2; i >= 0; i--) {
            if (this.debugSysout)
                System.out.println(i);
            this.calculateSupersets(maxSets, partitions.get(i).getValues());
        }

        if (this.debugSysout) {
            long count = 0;
            System.out.println("-----\nAnzahl maximaler Partitionen: " + maxSets.size() + "\n-----");
            for (IntList l : maxSets) {
                System.out.println("Partitionsgröße: " + l.size());
                count = count + (l.size() * (l.size() - 1) / 2);
            }
            System.out.println("-----\nBenötigte Anzahl: " + count + "\n-----");
        }

        return maxSets;
    }

    public void calculateSupersets(Set<IntList> maxSets, List<IntList> partitions) {

        List<IntList> toDelete = new LinkedList<IntList>();
        Set<IntList> toAdd = new HashSet<IntList>();
        int deleteFromPartition = -1;

        // List<IntList> remainingSets = new LinkedList<IntList>();
        // remainingSets.addAll(partition);
        for (IntList maxSet : maxSets) {
            for (IntList partition : partitions) {
                // IntList partitionCopy = new LongArrayList(partition);
                if ((maxSet.size() >= partition.size()) && (maxSet.containsAll(partition))) {
                    toAdd.remove(partition);
                    deleteFromPartition = partitions.indexOf(partition);
                    if (this.debugSysout)
                        System.out.println("MaxSet schon vorhanden");
                    break;
                }
                if ((partition.size() >= maxSet.size()) && (partition.containsAll(maxSet))) {
                    toDelete.add(maxSet);
                    if (this.debugSysout)
                        System.out.println("Neues MaxSet");
                }
                toAdd.add(partition);
            }
            if (deleteFromPartition != -1) {
                partitions.remove(deleteFromPartition);
                deleteFromPartition = -1;
            }
        }
        maxSets.removeAll(toDelete);
        maxSets.addAll(toAdd);
    }

    public Set<IntList> computeMaximumSetsAlternative(List<StrippedPartition> partitions) {

        if (this.debugSysout) {
            System.out.println("\tstartet calculation of maximal partitions");
        }
        long start = System.currentTimeMillis();

        Set<IntList> sortedPartitions = new TreeSet<IntList>(new ListComparator());
        for (StrippedPartition p : partitions) {
            sortedPartitions.addAll(p.getValues());
        }
        Iterator<IntList> it = sortedPartitions.iterator();
        Int2ObjectMap<Set<IntList>> maxSets = new Int2ObjectOpenHashMap<Set<IntList>>();
        long remainingPartitions = sortedPartitions.size();
        if (this.debugSysout) {
            System.out.println("\tNumber of Partitions: " + remainingPartitions);
        }
        if (it.hasNext()) {
            IntList actuelList = it.next();
            int minSize = actuelList.size();
            Set<IntList> set = new HashSet<IntList>();
            set.add(actuelList);
            while ((actuelList = it.next()) != null && (actuelList.size() == minSize)) {
                if (this.debugSysout) {
                    System.out.println("\tremaining: " + --remainingPartitions);
                }
                set.add(actuelList);
            }
            maxSets.put(minSize, set);
            if (actuelList != null) {
                maxSets.put(actuelList.size(), new HashSet<IntList>());
                if (this.debugSysout) {
                    System.out.println("\tremaining: " + --remainingPartitions);
                }
                this.handleList(actuelList, maxSets, true);
                while (it.hasNext()) {
                    actuelList = it.next();
                    if (this.debugSysout) {
                        System.out.println("\tremaining: " + --remainingPartitions);
                    }
                    if (!maxSets.containsKey(actuelList.size()))
                        maxSets.put(actuelList.size(), new HashSet<IntList>());
                    this.handleList(actuelList, maxSets, true);
                }
            }
        }

        long end = System.currentTimeMillis();
        if (this.debugSysout)
            System.out.println("\tTime needed: " + (end - start));

        Set<IntList> max = this.mergeResult(maxSets);
        maxSets.clear();
        sortedPartitions.clear();

        return max;
    }

    // ############################################

    private Set<IntList> mergeResult(Int2ObjectMap<Set<IntList>> maxSets) {

        Set<IntList> max = new HashSet<IntList>();
        for (Set<IntList> set : maxSets.values()) {
            max.addAll(set);
        }
        return max;
    }

    private void handleList(IntList list, Int2ObjectMap<Set<IntList>> maxSets, boolean firstStep) {

        for (int i = 0; i < list.size(); i++) {
        	int removedElement = list.removeInt(i);
            if (maxSets.containsKey(list.size()) && maxSets.get(list.size()).contains(list))
                maxSets.get(list.size()).remove(list);
            else {
                if (list.size() > 2) {
                    this.handleList(list, maxSets, false);
                }
            }
            list.add(i, removedElement);
        }

        if (firstStep)
            maxSets.get(list.size()).add(list);
    }

    public Set<IntList> computeMaximumSetsAlternative2(List<StrippedPartition> partitions) throws AlgorithmExecutionException {

        if (this.debugSysout) {
            System.out.println("\tstartet calculation of maximal partitions");
        }
        long start = System.currentTimeMillis();

        Set<IntList> sortedPartitions = this.sortPartitions(partitions, new ListComparator2());

        if (this.debugSysout) {
            System.out.println("\tTime to sort: " + (System.currentTimeMillis() - start));
        }

        Iterator<IntList> it = sortedPartitions.iterator();
        long remainingPartitions = sortedPartitions.size();
        if (this.debugSysout) {
            System.out.println("\tNumber of Partitions: " + remainingPartitions);
        }

        if (this.optimize()) {
            Map<Integer, IntSet> index = new ConcurrentHashMap<Integer, IntSet>();
            Map<IntList, Object> max = new ConcurrentHashMap<IntList, Object>();

            int actuelIndex = 0;
            IntList actuelList;

            int currentSize = Integer.MAX_VALUE;
            ExecutorService exec = this.getExecuter();
            while (it.hasNext()) {
                actuelList = it.next();
                if (currentSize != actuelList.size()) {
                    currentSize = actuelList.size();
                    this.awaitExecuter(exec);
                    exec = this.getExecuter();
                }
                exec.execute(new HandlePartitionTask(actuelList, actuelIndex, index, max));
                actuelIndex++;
            }
            this.awaitExecuter(exec);

            long end = System.currentTimeMillis();
            if (this.debugSysout) {
                System.out.println("\tTime needed: " + (end - start));
            }

            index.clear();
            sortedPartitions.clear();

            return max.keySet();
        }
        Int2ObjectMap<IntSet> index = new Int2ObjectOpenHashMap<IntSet>();
        Set<IntList> max = new HashSet<IntList>();

        int actuelIndex = 0;
        IntList actuelList;

        while (it.hasNext()) {
            actuelList = it.next();
            this.handlePartition(actuelList, actuelIndex, index, max);
            actuelIndex++;
        }

        long end = System.currentTimeMillis();
        if (this.debugSysout) {
            System.out.println("\tTime needed: " + (end - start));
        }

        index.clear();
        sortedPartitions.clear();

        return max;

    }

    private void handlePartitionConcurrent(IntList actuelList, int position, Map<Integer, IntSet> index, Map<IntList, Object> max) {

        if (!this.isSubset(actuelList, index)) {
            max.put(actuelList, new Object());
            for (int e : actuelList) {
                if (!index.containsKey(Integer.valueOf(e))) {
                    index.put(Integer.valueOf(e), new IntArraySet());
                }
                index.get(Integer.valueOf(e)).add(position);
            }
        }
    }

    // ############## next alternative #################

    private void handlePartition(IntList actuelList, int position, Int2ObjectMap<IntSet> index, Set<IntList> max) {

        if (!this.isSubset(actuelList, index)) {
            max.add(actuelList);
            for (int e : actuelList) {
                if (!index.containsKey(e)) {
                    index.put(e, new IntArraySet());
                }
                index.get(e).add(position);
            }
        }
    }

    private boolean isSubset(IntList actuelList, Map<Integer, IntSet> index) {

        boolean first = true;
        IntSet positions = new IntArraySet();
        for (int e : actuelList) {
            if (!index.containsKey(Integer.valueOf(e))) {
                return false;
            }
            if (first) {
                positions.addAll(index.get(Integer.valueOf(e)));
                first = false;
            } else {

                this.intersect(positions, index.get(Integer.valueOf(e)));
                // FIXME: Throws UnsupportedOperationExeption within fastUtil
                // positions.retainAll(index.get(e));
            }
            if (positions.size() == 0) {
                return false;
            }
        }
        return true;
    }

    private void intersect(IntSet positions, IntSet indexSet) {

        IntSet toRemove = new IntArraySet();
        for (int l : positions) {
            if (!indexSet.contains(l)) {
                toRemove.add(l);
            }
        }
        positions.removeAll(toRemove);
    }

    private Set<IntList> sortPartitions(List<StrippedPartition> partitions, Comparator<IntList> comparator) {

        Set<IntList> sortedPartitions = new TreeSet<IntList>(comparator);
        for (StrippedPartition p : partitions) {
            sortedPartitions.addAll(p.getValues());
        }
        return sortedPartitions;
    }

    private class IntersectWithAndAddToAgreeSetTask implements Runnable {

        private int i;
        private int j;
        private IntList maxEquiClass;
        private Int2ObjectMap<TupleEquivalenceClassRelation> relationships;
        private Map<AgreeSet, Object> agreeSets;

        private IntersectWithAndAddToAgreeSetTask(int i, int j, IntList maxEquiClass,
                                                  Int2ObjectMap<TupleEquivalenceClassRelation> relationships, Map<AgreeSet, Object> agreeSets) {

            this.i = i;
            this.j = j;
            this.maxEquiClass = maxEquiClass;
            this.relationships = relationships;
            this.agreeSets = agreeSets;
        }

        @Override
        public void run() {

            relationships.get(maxEquiClass.getInt(i)).intersectWithAndAddToAgreeSetConcurrent(
                    relationships.get(maxEquiClass.getInt(j)), agreeSets);

        }

    }

    private class ListComparator implements Comparator<IntList> {

        @Override
        public int compare(IntList l1, IntList l2) {

            if (l1.size() - l2.size() != 0)
                return l1.size() - l2.size();
            for (int i = 0; i < l1.size(); i++) {
                if (l1.getInt(i) == l2.getInt(i))
                    continue;
                return l1.getInt(i) - l2.getInt(i);
            }
            return 0;
        }

    }

    private class HandlePartitionTask implements Runnable {

        private Map<Integer, IntSet> index;
        private Map<IntList, Object> max;
        private IntList actuelList;
        private int actuelIndex;

        private HandlePartitionTask(IntList actuelList, int actuelIndex, Map<Integer, IntSet> index, Map<IntList, Object> max) {

            this.index = index;
            this.max = max;
            this.actuelList = actuelList;
            this.actuelIndex = actuelIndex;
        }

        @Override
        public void run() {

            try {
                handlePartitionConcurrent(actuelList, actuelIndex, index, max);
            } catch (Exception ex) {
                // System.out.print("f");
                ex.printStackTrace();
            }

        }

    }

    private class ListComparator2 implements Comparator<IntList> {

        @Override
        public int compare(IntList l1, IntList l2) {

            if (l1.size() - l2.size() != 0)
                return l2.size() - l1.size();
            for (int i = 0; i < l1.size(); i++) {
                if (l1.getInt(i) == l2.getInt(i))
                    continue;
                return l2.getInt(i) - l1.getInt(i);
            }
            return 0;
        }

    }
}
