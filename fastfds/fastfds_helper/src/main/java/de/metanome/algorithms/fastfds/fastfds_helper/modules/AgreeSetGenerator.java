package de.metanome.algorithms.fastfds.fastfds_helper.modules;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithms.fastfds.fastfds_helper.modules.container.AgreeSet;
import de.metanome.algorithms.fastfds.fastfds_helper.modules.container.StrippedPartition;
import it.unimi.dsi.fastutil.longs.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

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

        Set<LongList> maxSets;
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

    public Set<AgreeSet> computeAgreeSets(Long2ObjectMap<TupleEquivalenceClassRelation> relationships, Set<LongList> maxSets,
                                          List<StrippedPartition> partitions) throws AlgorithmExecutionException {

        if (this.debugSysout) {
            System.out.println("\tstartet calculation of agree sets");
            int bitsPerSet = (((int) (partitions.size() - 1) / 64) + 1) * 64;
            long setsNeeded = 0;
            for (LongList l : maxSets) {
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
            for (LongList maxEquiClass : maxSets) {
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
        } else {

            Set<AgreeSet> agreeSets = new HashSet<AgreeSet>();

            for (LongList maxEquiClass : maxSets) {
                if (this.debugSysout) {
                    System.out.println(a++);
                }
                for (int i = 0; i < maxEquiClass.size() - 1; i++) {
                    for (int j = i + 1; j < maxEquiClass.size(); j++) {
                        relationships.get(maxEquiClass.getLong(i)).intersectWithAndAddToAgreeSet(
                                relationships.get(maxEquiClass.getLong(j)), agreeSets);
                    }
                }
            }

            return agreeSets;
        }

    }

    public Long2ObjectMap<TupleEquivalenceClassRelation> calculateRelationships(List<StrippedPartition> partitions) {

        if (this.debugSysout) {
            System.out.println("\tstartet calculation of relationships");
        }
        Long2ObjectMap<TupleEquivalenceClassRelation> relationships = new Long2ObjectOpenHashMap<TupleEquivalenceClassRelation>();
        for (StrippedPartition p : partitions) {
            this.calculateRelationship(p, relationships);
        }

        return relationships;
    }

    private void calculateRelationship(StrippedPartition partitions, Long2ObjectMap<TupleEquivalenceClassRelation> relationships) {

        int partitionNr = 0;
        for (LongList partition : partitions.getValues()) {
            if (this.debugSysout)
                System.out.println(".");
            for (long index : partition) {
                if (!relationships.containsKey(index)) {
                    relationships.put(index, new TupleEquivalenceClassRelation());
                }
                relationships.get(index).addNewRelationship(partitions.getAttributeID(), partitionNr);
            }
            partitionNr++;
        }

    }

    public Set<LongList> computeMaximumSets(List<StrippedPartition> partitionsOrig) {

        if (this.debugSysout) {
            System.out.println("\tstartet calculation of maximal partitions");
        }

        List<StrippedPartition> partitions = new LinkedList<StrippedPartition>();
        for (StrippedPartition p : partitionsOrig) {
            partitions.add(p.copy());
        }

        Set<LongList> maxSets = new HashSet<LongList>(partitions.get(partitions.size() - 1).getValues());

        for (int i = partitions.size() - 2; i >= 0; i--) {
            if (this.debugSysout)
                System.out.println(i);
            this.calculateSupersets(maxSets, partitions.get(i).getValues());
        }

        if (this.debugSysout) {
            long count = 0;
            System.out.println("-----\nAnzahl maximaler Partitionen: " + maxSets.size() + "\n-----");
            for (LongList l : maxSets) {
                System.out.println("Partitionsgröße: " + l.size());
                count = count + (l.size() * (l.size() - 1) / 2);
            }
            System.out.println("-----\nBenötigte Anzahl: " + count + "\n-----");
        }

        return maxSets;
    }

    public void calculateSupersets(Set<LongList> maxSets, List<LongList> partitions) {

        List<LongList> toDelete = new LinkedList<LongList>();
        Set<LongList> toAdd = new HashSet<LongList>();
        int deleteFromPartition = -1;

        // List<LongList> remainingSets = new LinkedList<LongList>();
        // remainingSets.addAll(partition);
        for (LongList maxSet : maxSets) {
            for (LongList partition : partitions) {
                // LongList partitionCopy = new LongArrayList(partition);
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

    public Set<LongList> computeMaximumSetsAlternative(List<StrippedPartition> partitions) {

        if (this.debugSysout) {
            System.out.println("\tstartet calculation of maximal partitions");
        }
        long start = System.currentTimeMillis();

        Set<LongList> sortedPartitions = new TreeSet<LongList>(new ListComparator());
        for (StrippedPartition p : partitions) {
            sortedPartitions.addAll(p.getValues());
        }
        Iterator<LongList> it = sortedPartitions.iterator();
        Long2ObjectMap<Set<LongList>> maxSets = new Long2ObjectOpenHashMap<Set<LongList>>();
        long remainingPartitions = sortedPartitions.size();
        if (this.debugSysout) {
            System.out.println("\tNumber of Partitions: " + remainingPartitions);
        }
        if (it.hasNext()) {
            LongList actuelList = it.next();
            long minSize = actuelList.size();
            Set<LongList> set = new HashSet<LongList>();
            set.add(actuelList);
            while ((actuelList = it.next()) != null && (actuelList.size() == minSize)) {
                if (this.debugSysout) {
                    System.out.println("\tremaining: " + --remainingPartitions);
                }
                set.add(actuelList);
            }
            maxSets.put(minSize, set);
            if (actuelList != null) {
                maxSets.put(actuelList.size(), new HashSet<LongList>());
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
                        maxSets.put(actuelList.size(), new HashSet<LongList>());
                    this.handleList(actuelList, maxSets, true);
                }
            }
        }

        long end = System.currentTimeMillis();
        if (this.debugSysout)
            System.out.println("\tTime needed: " + (end - start));

        Set<LongList> max = this.mergeResult(maxSets);
        maxSets.clear();
        sortedPartitions.clear();

        return max;
    }

    // ############################################

    private Set<LongList> mergeResult(Long2ObjectMap<Set<LongList>> maxSets) {

        Set<LongList> max = new HashSet<LongList>();
        for (Set<LongList> set : maxSets.values()) {
            max.addAll(set);
        }
        return max;
    }

    private void handleList(LongList list, Long2ObjectMap<Set<LongList>> maxSets, boolean firstStep) {

        for (int i = 0; i < list.size(); i++) {
            long removedElement = list.removeLong(i);
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

    public Set<LongList> computeMaximumSetsAlternative2(List<StrippedPartition> partitions) throws AlgorithmExecutionException {

        if (this.debugSysout) {
            System.out.println("\tstartet calculation of maximal partitions");
        }
        long start = System.currentTimeMillis();

        Set<LongList> sortedPartitions = this.sortPartitions(partitions, new ListComparator2());

        if (this.debugSysout) {
            System.out.println("\tTime to sort: " + (System.currentTimeMillis() - start));
        }

        Iterator<LongList> it = sortedPartitions.iterator();
        long remainingPartitions = sortedPartitions.size();
        if (this.debugSysout) {
            System.out.println("\tNumber of Partitions: " + remainingPartitions);
        }

        if (this.optimize()) {
            Map<Long, LongSet> index = new ConcurrentHashMap<Long, LongSet>();
            Map<LongList, Object> max = new ConcurrentHashMap<LongList, Object>();

            long actuelIndex = 0;
            LongList actuelList;

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
        } else {
            Long2ObjectMap<LongSet> index = new Long2ObjectOpenHashMap<LongSet>();
            Set<LongList> max = new HashSet<LongList>();

            long actuelIndex = 0;
            LongList actuelList;

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

    }

    private void handlePartitionConcurrent(LongList actuelList, long position, Map<Long, LongSet> index, Map<LongList, Object> max) {

        if (!this.isSubset(actuelList, index)) {
            max.put(actuelList, new Object());
            for (long e : actuelList) {
                if (!index.containsKey(e)) {
                    index.put(e, new LongArraySet());
                }
                index.get(e).add(position);
            }
        }
    }

    // ############## next alternative #################

    private void handlePartition(LongList actuelList, long position, Long2ObjectMap<LongSet> index, Set<LongList> max) {

        if (!this.isSubset(actuelList, index)) {
            max.add(actuelList);
            for (long e : actuelList) {
                if (!index.containsKey(e)) {
                    index.put(e, new LongArraySet());
                }
                index.get(e).add(position);
            }
        }
    }

    private boolean isSubset(LongList actuelList, Map<Long, LongSet> index) {

        boolean first = true;
        LongSet positions = new LongArraySet();
        for (long e : actuelList) {
            if (!index.containsKey(e)) {
                return false;
            }
            if (first) {
                positions.addAll(index.get(e));
                first = false;
            } else {

                this.intersect(positions, index.get(e));
                // FIXME: Throws UnsupportedOperationExeption within fastUtil
                // positions.retainAll(index.get(e));
            }
            if (positions.size() == 0) {
                return false;
            }
        }
        return true;
    }

    private void intersect(LongSet positions, LongSet indexSet) {

        LongSet toRemove = new LongArraySet();
        for (long l : positions) {
            if (!indexSet.contains(l)) {
                toRemove.add(l);
            }
        }
        positions.removeAll(toRemove);
    }

    private Set<LongList> sortPartitions(List<StrippedPartition> partitions, Comparator<LongList> comparator) {

        Set<LongList> sortedPartitions = new TreeSet<LongList>(comparator);
        for (StrippedPartition p : partitions) {
            sortedPartitions.addAll(p.getValues());
        }
        return sortedPartitions;
    }

    private class IntersectWithAndAddToAgreeSetTask implements Runnable {

        private int i;
        private int j;
        private LongList maxEquiClass;
        private Long2ObjectMap<TupleEquivalenceClassRelation> relationships;
        private Map<AgreeSet, Object> agreeSets;

        private IntersectWithAndAddToAgreeSetTask(int i, int j, LongList maxEquiClass,
                                                  Long2ObjectMap<TupleEquivalenceClassRelation> relationships, Map<AgreeSet, Object> agreeSets) {

            this.i = i;
            this.j = j;
            this.maxEquiClass = maxEquiClass;
            this.relationships = relationships;
            this.agreeSets = agreeSets;
        }

        @Override
        public void run() {

            relationships.get(maxEquiClass.getLong(i)).intersectWithAndAddToAgreeSetConcurrent(
                    relationships.get(maxEquiClass.getLong(j)), agreeSets);

        }

    }

    private class ListComparator implements Comparator<LongList> {

        @Override
        public int compare(LongList l1, LongList l2) {

            if (l1.size() - l2.size() != 0)
                return l1.size() - l2.size();
            for (int i = 0; i < l1.size(); i++) {
                if (l1.getLong(i) == l2.getLong(i))
                    continue;
                return (int) (l1.getLong(i) - l2.getLong(i));
            }
            return 0;
        }

    }

    private class HandlePartitionTask implements Runnable {

        private Map<Long, LongSet> index;
        private Map<LongList, Object> max;
        private LongList actuelList;
        private long actuelIndex;

        private HandlePartitionTask(LongList actuelList, long actuelIndex, Map<Long, LongSet> index, Map<LongList, Object> max) {

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

    private class ListComparator2 implements Comparator<LongList> {

        @Override
        public int compare(LongList l1, LongList l2) {

            if (l1.size() - l2.size() != 0)
                return l2.size() - l1.size();
            for (int i = 0; i < l1.size(); i++) {
                if (l1.getLong(i) == l2.getLong(i))
                    continue;
                return (int) (l2.getLong(i) - l1.getLong(i));
            }
            return 0;
        }

    }
}
