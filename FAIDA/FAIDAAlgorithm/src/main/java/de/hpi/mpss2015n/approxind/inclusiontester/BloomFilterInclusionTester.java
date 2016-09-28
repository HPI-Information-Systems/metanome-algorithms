package de.hpi.mpss2015n.approxind.inclusiontester;

import de.hpi.mpss2015n.approxind.datastructures.bloomfilter.BitVector;
import de.hpi.mpss2015n.approxind.datastructures.bloomfilter.BitVectorFactory;
import de.hpi.mpss2015n.approxind.datastructures.bloomfilter.BloomFilter;

import de.hpi.mpss2015n.approxind.InclusionTester;
import de.hpi.mpss2015n.approxind.utils.ColumnStore;
import de.hpi.mpss2015n.approxind.utils.SimpleColumnCombination;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BloomFilterInclusionTester implements InclusionTester {

    private final Map<Integer, Map<SimpleColumnCombination, BloomFilter<Long>>> filters;
    private final BitVectorFactory bitVectorFactory;
    private RelationalInputGenerator[] fileInputGenerators;
    private Map<SimpleColumnCombination, BloomFilter<Long>> currentTable;

    public BloomFilterInclusionTester() {
        this.bitVectorFactory = new BitVectorFactory(true); // assuming TRUE is default
        this.filters = new HashMap<>();
        // Use reflection to access bits of a Bloom filter
        /*try {
            bloomFilterBitAccess = BloomFilter.class.getDeclaredField("bits");
            bloomFilterBitAccess.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }*/
    }



    @Override
    public void initialize(List<List<long[]>> stores){
        // pass
    }



    @Override
    public int[] setColumnCombinations(List<SimpleColumnCombination> combinations) {

        // Get list of all active tables
        int[] activeTables = combinations.stream().mapToInt(SimpleColumnCombination::getTable).distinct().sorted().toArray();

        // Clear all BloomFilters
        filters.clear();

        // Create a list of filters for each table
        for (int table : activeTables) {
            filters.put(table, new HashMap<>());
        }

        // For each column combination, add a Bloom Filter
        for (SimpleColumnCombination combination : combinations) {
            // TODO: Set parameters m and k
            filters.get(combination.getTable()).put(combination, new BloomFilter<Long>(32,2,bitVectorFactory));
        }

        // Return
        return activeTables;
    }


    @Override
    public void insertRow(long[] values, int rowCount) {
        // FOR EACH COLUMN COMBINATION
        for (Map.Entry<SimpleColumnCombination, BloomFilter<Long>> entry : currentTable.entrySet()) {

            // Prepare loop variables
            boolean anyNull = true;
            int[] columns = entry.getKey().getColumns();
            long combinedHash = 0;

            // Loop to concatenate all hashes!
            for (int i = 0; i < columns.length; i++) {
                long hash = values[columns[i]];
                if (anyNull = hash == ColumnStore.NULLHASH) break;
                combinedHash = combinedHash*37 ^ hash;
            }

            // Insert into bloom filter
            if (!anyNull) {
                entry.getValue().add(combinedHash);
            }
        }

    }


    @Override
    public boolean isIncludedIn(SimpleColumnCombination a, SimpleColumnCombination b) {
        BitVector<?> filterbits_a = filters.get(a.getTable()).get(a).getBits();
        BitVector<?> filterbits_b = filters.get(b.getTable()).get(b).getBits();

        BitVector<?> and_bits = filterbits_a.copy().and(filterbits_b);
        BitVector<?> or_bits = filterbits_a.copy().or(filterbits_b);

        if(and_bits.equals(filterbits_a) && or_bits.equals(filterbits_b)){
            return true;
        }
        return false;
    }



	@Override
	public void startInsertRow(int table) {
		currentTable=filters.get(table);		
	}
}
