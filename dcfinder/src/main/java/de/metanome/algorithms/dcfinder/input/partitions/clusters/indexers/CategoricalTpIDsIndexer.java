package de.metanome.algorithms.dcfinder.input.partitions.clusters.indexers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CategoricalTpIDsIndexer implements ITPIDsIndexer {

	Map<Integer, List<Integer>> valueToTpIDsMap;

	// based on hashing values
	public CategoricalTpIDsIndexer(List<List<Integer>> tpIDs, int[] values) {

		valueToTpIDsMap = new HashMap<Integer, List<Integer>>();

		for (List<Integer> ids : tpIDs) {
			Integer value = values[ids.get(0)];
			valueToTpIDsMap.put(value, ids);
		}

	}

	public Set<Integer> getValues() {
		return valueToTpIDsMap.keySet();
	}

	public List<Integer> getTpIDsForValue(Integer value) {
		return valueToTpIDsMap.get(value);
	}

	public int getIndexForValueThatIsLessThan(int value) {
		return -1; // not applied to categorical
	}

	public void setValueToTpIDsMap(Map<Integer, List<Integer>> valueToTpIDsMap) {
		this.valueToTpIDsMap = valueToTpIDsMap;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		valueToTpIDsMap.forEach((k, v) -> {
			sb.append(k + ":" + v + "\n");
		});
		sb.append("\n");

		return sb.toString();
	}

}