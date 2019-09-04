package de.metanome.algorithms.dcfinder.input.partitions.clusters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.metanome.algorithms.dcfinder.input.partitions.clusters.indexers.CategoricalTpIDsIndexer;
import de.metanome.algorithms.dcfinder.input.partitions.clusters.indexers.ITPIDsIndexer;
import de.metanome.algorithms.dcfinder.input.partitions.clusters.indexers.NumericalTpIDsIndexer;

public class PLI {

	List<List<Integer>> tpIDs;
	ITPIDsIndexer tpIDsIndexer;
	long relSize;
	boolean numerical;

	// descending order for numerical
	public PLI(List<Set<Integer>> setPlis, long relSize, boolean numerical) {
		tpIDs = new ArrayList<>(setPlis.size());
		this.relSize = relSize;
		this.numerical = numerical;

		for (Set<Integer> set : setPlis) {
			List<Integer> tidsList = new ArrayList<>(set);
			tpIDs.add(tidsList);
		}
		Collections.reverse(tpIDs);
	}

	public PLI(List<Set<Integer>> setPlis, long relSize, boolean numerical, int[] values) {
		tpIDs = new ArrayList<>(setPlis.size());
		this.relSize = relSize;
		this.numerical = numerical;

		for (Set<Integer> set : setPlis) {
			List<Integer> tidsList = new ArrayList<>(set);
			tpIDs.add(tidsList);
		}
		Collections.reverse(tpIDs);
		if (numerical) {

			tpIDsIndexer = new NumericalTpIDsIndexer(tpIDs, values);

		} else {

			tpIDsIndexer = new CategoricalTpIDsIndexer(tpIDs, values);
		}
	}

	public Collection<Integer> getValues() {
		return tpIDsIndexer.getValues();
	}

	public boolean isNumerical() {
		return numerical;
	}

	public long getRelSize() {
		return relSize;
	}

	public void setRelSize(long relSize) {
		this.relSize = relSize;
	}

	public List<List<Integer>> getPlis() {
		return tpIDs;
	}

	public void setPlis(List<List<Integer>> plis) {
		this.tpIDs = plis;
	}

	public List<Integer> getTpIDsForValue(Integer value) {
		return tpIDsIndexer.getTpIDsForValue(value);
	}

	public int getIndexForValueThatIsLessThan(int value) {
		return tpIDsIndexer.getIndexForValueThatIsLessThan(value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Iterator iterator = tpIDs.iterator(); iterator.hasNext();) {
			List<Integer> list = (List<Integer>) iterator.next();
			sb.append(list + "\n");

		}
		// sb.append("\n");
		// if (numerical)
		// sb.append((NumericalTpIDsIndexer) tpIDsIndexer);
		// else
		// sb.append((CategoricalTpIDsIndexer) tpIDsIndexer);

		return sb.toString();
	}

}
