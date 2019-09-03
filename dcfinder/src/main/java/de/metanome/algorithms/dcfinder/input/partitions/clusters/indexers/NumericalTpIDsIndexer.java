package de.metanome.algorithms.dcfinder.input.partitions.clusters.indexers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NumericalTpIDsIndexer implements ITPIDsIndexer {

	Map<Integer, List<Integer>> valueToTpIDsMap;
	List<Integer> orderedValues;

	// based on hashing values
	public NumericalTpIDsIndexer(List<List<Integer>> tpIDs, int[] values) {

		valueToTpIDsMap = new HashMap<Integer, List<Integer>>();

		orderedValues = new ArrayList<Integer>(tpIDs.size());

		for (List<Integer> ids : tpIDs) {

			Integer value = values[ids.get(0)];
			valueToTpIDsMap.put(value, ids);
			orderedValues.add(value);

		}

	}

	public List<Integer> getValues() {
		return orderedValues;
	}

	public List<Integer> getTpIDsForValue(Integer value) {

		return valueToTpIDsMap.get(value);

	}

	public int getIndexForValueThatIsLessThanLinear(int value) {

		int ans = -1;

		for (int i = 0; i < orderedValues.size(); i++) {
			if (orderedValues.get(i) < value) {

				return i;
			}
		}

		return ans;
	}

	// using BS
	public int getIndexForValueThatIsLessThan(int value) {
		int start = 0, end = orderedValues.size() - 1;

		int ans = -1;
		while (start <= end) {
			int mid = (start + end) / 2;

			// Move to right side if target is
			// greater.
			if (orderedValues.get(mid) >= value) {
				start = mid + 1;
			}

			// Move left side.
			else {
				ans = mid;
				end = mid - 1;
			}
		}
		return ans;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		valueToTpIDsMap.forEach((k, v) -> {

			sb.append(k + ":" + v + "\n");

		});

		sb.append("\n");

		sb.append(orderedValues);

		return sb.toString();
	}

}
