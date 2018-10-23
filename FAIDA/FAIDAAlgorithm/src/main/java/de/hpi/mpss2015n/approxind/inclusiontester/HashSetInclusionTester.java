package de.hpi.mpss2015n.approxind.inclusiontester;

import de.hpi.mpss2015n.approxind.InclusionTester;
import de.hpi.mpss2015n.approxind.utils.HashedColumnStore;
import de.hpi.mpss2015n.approxind.utils.SimpleColumnCombination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class HashSetInclusionTester implements InclusionTester {

    private final Map<Integer, Map<SimpleColumnCombination, HashSet<List<Long>>>> sets;

    private Map<SimpleColumnCombination, HashSet<List<Long>>> currentTable;

    private int numChecks = 0;

    public HashSetInclusionTester() {
        this.sets = new HashMap<>();
    }

    @Override
    public int[] setColumnCombinations(List<SimpleColumnCombination> combinations) {
        int[] activeTables = combinations.stream().mapToInt(SimpleColumnCombination::getTable).distinct().sorted().toArray();
        sets.clear();
        for (int table : activeTables) {
            sets.put(Integer.valueOf(table), new HashMap<>());
        }
        for (SimpleColumnCombination combination : combinations) {
            sets.get(Integer.valueOf(combination.getTable())).put(combination, new HashSet<>());
        }
        return activeTables;
    }

    @Override
    public void insertRow(long[] values, int rowCount) {
        for (Map.Entry<SimpleColumnCombination, HashSet<List<Long>>> entry : currentTable.entrySet()) {
            SimpleColumnCombination combination = entry.getKey();
            List<Long> combinationValues = new ArrayList<>(combination.getColumns().length);
            boolean anyNull = false;
            for (int c : combination.getColumns()) {
                long value = values[c];
                anyNull |= value == HashedColumnStore.NULLHASH;
                combinationValues.add(Long.valueOf(value));
            }
            if (!anyNull) {
                entry.getValue().add(combinationValues);
            }
        }

    }

    @Override
    public boolean isIncludedIn(SimpleColumnCombination a, SimpleColumnCombination b) {
        HashSet<List<Long>> setA = sets.get(Integer.valueOf(a.getTable())).get(a);
        HashSet<List<Long>> setB = sets.get(Integer.valueOf(b.getTable())).get(b);
        this.numChecks++;
        return setB.containsAll(setA);
    }

	@Override
	public void startInsertRow(int table) {
		currentTable=sets.get(Integer.valueOf(table));
	}

    @Override
    public int getNumCertainChecks() {
        return this.numChecks;
    }

    @Override
    public int getNumUnertainChecks() {
        return 0;
    }
}
