package de.hpi.mpss2015n.approxind.inclusiontester;

import com.google.common.base.MoreObjects;

import de.hpi.mpss2015n.approxind.InclusionTester;
import de.hpi.mpss2015n.approxind.datastructures.HyperLogLog;
import de.hpi.mpss2015n.approxind.datastructures.RegisterSet;
import de.hpi.mpss2015n.approxind.utils.ColumnStore;
import de.hpi.mpss2015n.approxind.utils.HLL.HLLData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import de.hpi.mpss2015n.approxind.utils.SimpleColumnCombination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class HLLInclusionTester implements InclusionTester {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final Int2ObjectMap<Map<SimpleColumnCombination, HLLData>> logMaps;
  private final Object2LongMap<SimpleColumnCombination> cardinalityLookUp;
  private final double error;
  private final int demarchiThreshold = 10000;
  private final DeMarchi deMarchi;
  private Map.Entry<SimpleColumnCombination, HLLData>[] insertHelper;

  public HLLInclusionTester(double error) {
    this.error = error;
    this.logMaps = new Int2ObjectOpenHashMap<>();
    cardinalityLookUp = new Object2LongOpenHashMap<>();
    deMarchi = new DeMarchi(demarchiThreshold);
  }

  @Override
  public void initialize(List<List<long[]>> stores) {

    List<SimpleColumnCombination> combinations = new ArrayList<>();
    for (Map<SimpleColumnCombination, HLLData> data : logMaps.values()) {
      combinations.addAll(data.keySet());
    }
    for (int i = 0; i < combinations.size(); i++) {
      combinations.get(i).setIndex(i);
    }
    deMarchi.setMaxIndex(combinations.size()-1);

    List<Long> samples = new ArrayList<>();
    for(int table=0; table < stores.size(); table++){
    	Map<SimpleColumnCombination, HLLData> logMap = logMaps.get(table);
        if(logMap!=null) {
          List<long[]> tableSamples = stores.get(table);
          for(long[] sampleRow: tableSamples) {
            for (Map.Entry<SimpleColumnCombination, HLLData> entry : logMap.entrySet()) {
              SimpleColumnCombination combination = entry.getKey();
              Long combinedHash = getHash(combination, sampleRow);
              if(combinedHash != null){
                samples.add(combinedHash);
              }
            }
          }
        }
    }
    deMarchi.initialize(samples);
  }

  @Override
  public int[] setColumnCombinations(List<SimpleColumnCombination> combinations) {
    logMaps.clear();
    int[]
        activeTables =
        combinations.stream().mapToInt(SimpleColumnCombination::getTable).distinct().sorted()
            .toArray();
    for (int table : activeTables) {
      logMaps.put(table, new HashMap<>());
    }
    for (SimpleColumnCombination combination : combinations) {
      logMaps.get(combination.getTable()).put(combination, new HLLData());
    }
    return activeTables;
  }

  @Override
  public void finalizeInsertion() {
    for (Map<SimpleColumnCombination, HLLData> logMap : logMaps.values()) {
      for (Map.Entry<SimpleColumnCombination, HLLData> entry : logMap.entrySet()) {
        if (entry.getValue().isBig()) {
          cardinalityLookUp.put(entry.getKey(), entry.getValue().getHll().cardinality());
        }
      }
    }
    deMarchi.finalizeInsertion(logMaps.values());
  }

  @Override
  public void insertRow(long[] values, int rowCount) {

    for (Map.Entry<SimpleColumnCombination, HLLData> entry : insertHelper) {
      SimpleColumnCombination combination = entry.getKey();
      HLLData hllData = entry.getValue();
      Long combinedHash = getHash(combination, values);
      if (combinedHash != null) {
        processHash(combination, hllData, combinedHash);
      }
    }

  }

  public Long getHash(SimpleColumnCombination combination, long[] values){
    long combinedHash = 0;
    boolean allNull = true;
    int[] columns = combination.getColumns();
    for (int i = 0; i < columns.length; i++) {
      long hash = values[columns[i]];
      allNull &= hash == ColumnStore.NULLHASH;
      combinedHash = combinedHash*37 ^ hash;
    }
    if(allNull){
      return null;
    }
    return combinedHash;
  }

  @Override
  public boolean isIncludedIn(SimpleColumnCombination a, SimpleColumnCombination b) {
    //In case combination was removes - Todo: test if ind is valid based on generated ind cover
    if(!logMaps.get(a.getTable()).containsKey(a) || !logMaps.get(b.getTable()).containsKey(b)){
      return false;
    }

    HLLData dataA = logMaps.get(a.getTable()).get(a);
    HLLData dataB = logMaps.get(b.getTable()).get(b);
    /*if (a.getTable() == 7 && a.getColumns()[0] == 4){
      System.out.println(dataA.isBig() + ": " + cardinalityLookUp.get(a) + a.toString());
    }*/
    if (dataA.isBig() && dataB.isBig()) {
      //great performance improvement using look up
      //first test indicate at least around factor 20 speed up
      // due to saving potentially billions of calls to registerSet.get() from HyperLogLog.cardinality!
      long setACardinality = cardinalityLookUp.getLong(a);
      long setBCardinality = cardinalityLookUp.getLong(b);
      /*
        if(setBCardinality > 0 && setACardinality > 0 && error > setACardinality/(double) setBCardinality){
          logger.info("No sufficient statement can be made due to difference in size");
          return false;
        }
      */
      if (setACardinality > setBCardinality) {
        return false;
      }

      return deMarchi.isIncludedIn(a,b) && isIncluded(dataA.getHll(), dataB.getHll());

    } else {
      return deMarchi.isIncludedIn(a,b);
    }
  }

  private int[] getRegisterSetBits(HyperLogLog hll) {
    return hll.registerSet().readOnlyBits();
  }

  //tested to be 25% faster than merging the registerSets
  private boolean isIncluded(HyperLogLog a, HyperLogLog b) {
    int[] aBits = getRegisterSetBits(a);
    int[] bBits = getRegisterSetBits(b);
    for (int bucket = 0; bucket < bBits.length; ++bucket) {
      int aBit = aBits[bucket];
      int bBit = bBits[bucket];
      for (int j = 0; j < RegisterSet.LOG2_BITS_PER_WORD; ++j) {
        int mask = 31 << (RegisterSet.REGISTER_SIZE * j);
        int aVal = aBit & mask;
        int bVal = bBit & mask;
        if (bVal < aVal) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("error", error).toString();
  }

  private void processHash(SimpleColumnCombination combination, HLLData hllData, long longHash) {
    boolean exists = deMarchi.processHash(combination, hllData, longHash);
    if (!exists && hllData.isBig()) {
      HyperLogLog hll = hllData.getHll();
      if (hll == null) {
        hll = new HyperLogLog(error);;
        hllData.setHll(hll);
      }
      hll.offerHashed(longHash);
    }

  }

	@SuppressWarnings("unchecked")
	@Override
	public void startInsertRow(int table) {
		Set<Entry<SimpleColumnCombination, HLLData>> set = logMaps.get(table).entrySet();
		insertHelper=set.toArray(new Entry[set.size()]);
	}

}
