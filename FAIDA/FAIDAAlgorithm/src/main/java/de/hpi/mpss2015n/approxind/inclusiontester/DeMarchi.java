package de.hpi.mpss2015n.approxind.inclusiontester;

import de.hpi.mpss2015n.approxind.utils.HLL.HLLData;
import de.hpi.mpss2015n.approxind.utils.SimpleColumnCombination;
import de.hpi.mpss2015n.approxind.utils.SimpleInd;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class DeMarchi {
    private final Long2ObjectOpenHashMap<BitSet> deMarchiRelation;
    private final int threshold;
    private final Set<SimpleInd> partialResult;
    private int maxIndex;

    public DeMarchi(int threshold) {
        this.threshold = threshold;
        deMarchiRelation = new Long2ObjectOpenHashMap<>();
        partialResult = new HashSet<>();
    }

    public void finalizeInsertion(Collection<Map<SimpleColumnCombination, HLLData>> hllData) {
        partialResult.clear();
        Map<SimpleColumnCombination, Set<SimpleColumnCombination>> LHStoRHSMap = new HashMap<>();
        SimpleColumnCombination[] columnCombinations = new SimpleColumnCombination[maxIndex+1];
        List<SimpleColumnCombination> columnCombinationList = new ArrayList<>();
        for (Map<SimpleColumnCombination, HLLData> logMap : hllData) {
            for (SimpleColumnCombination scc : logMap.keySet()) {
                columnCombinations[scc.getIndex()] = scc;
                columnCombinationList.add(scc);
            }
        }
        for (Map<SimpleColumnCombination, HLLData> logMap : hllData) {
            for (Map.Entry<SimpleColumnCombination, HLLData> entry : logMap.entrySet()) {
                    LHStoRHSMap.put(entry.getKey(), new HashSet<>(columnCombinationList));
            }
        }

        for (BitSet candidates : deMarchiRelation.values()) {
            for (int lhs : candidates.stream().toArray()) {
                Set<SimpleColumnCombination> RHSs = LHStoRHSMap.get(columnCombinations[lhs]);
                if (RHSs != null) {
                    List<SimpleColumnCombination> copy = new ArrayList<>(RHSs);
                    for (SimpleColumnCombination rhs : copy) {
                        if(!candidates.get(rhs.getIndex())){
                            RHSs.remove(rhs);
                        }
                    }
                }
            }
        }
        for (Map.Entry<SimpleColumnCombination, Set<SimpleColumnCombination>> entry : LHStoRHSMap.entrySet()) {
            SimpleColumnCombination lhs = entry.getKey();
            for (SimpleColumnCombination rhs : entry.getValue()) {
                partialResult.add(new SimpleInd(lhs, rhs));
            }
        }
        deMarchiRelation.clear();
    }

    public boolean isIncludedIn(SimpleColumnCombination a, SimpleColumnCombination b) {
        return partialResult.contains(new SimpleInd(a, b));
    }

    public void initialize(List<Long> values){
      for(Long longHash: values){
        BitSet set = new BitSet(maxIndex+1);
        deMarchiRelation.put(longHash, set);
      }

    }

    /**
     * @return true if the processed hash already exists in the relation
     */
    public boolean processHash(SimpleColumnCombination combination, HLLData hllData, long longHash) {
      BitSet set = deMarchiRelation.get(longHash);

      if (set != null && set.get(combination.getIndex())) {
        return true;
      }

      if(set == null){
        hllData.setBig(true);
        return false;
      }

      //if set != null
      if (!hllData.isBig()) {
        hllData.incrementCounter();
        if (hllData.getCounter() >= threshold) {
          hllData.setBig(true);
          return false;
        }
      }
      set.set(combination.getIndex());
      return true;

    }

    public Stream<Long> getValues(SimpleColumnCombination combination) {
        return deMarchiRelation.entrySet().stream()
                .filter(tuple -> tuple.getValue().get(combination.getIndex()))
                .map(Map.Entry::getKey);
    }

    public void setMaxIndex(int maxIndex) {
        this.maxIndex = maxIndex;
    }
}
