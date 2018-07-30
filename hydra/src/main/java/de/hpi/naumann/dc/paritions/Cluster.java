package de.hpi.naumann.dc.paritions;

import java.util.HashMap;
import java.util.Map;

import de.hpi.naumann.dc.input.ParsedColumn;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

public class Cluster {
	private final TIntArrayList array;

	public Cluster() {
		array = new TIntArrayList();
	}

	public Cluster(TIntArrayList array) {
		this.array = array;
	}

	public Cluster(int firstIndex) {
		this();
		array.add(firstIndex);
	}

	public void add(int index) {
		this.array.add(index);
	}

	public void addAll(Cluster c) {
		this.array.addAll(c.array);
	}

	public boolean contains(int value) {
		return array.contains(value);
	}

	public TIntIterator iterator() {
		return array.iterator();
	}

	public int size() {
		return array.size();
	}

	public Map<Object, Cluster> refineBy(ParsedColumn<?> target) {
		Map<Object, Cluster> map = new HashMap<>();
		for (TIntIterator iter = array.iterator(); iter.hasNext();) {
			int line = iter.next();
			Object c = target.getValue(line);
			if (c == null)
				continue;
			if (map.containsKey(c)) {
				map.get(c).add(line);
			} else {
				Cluster p = new Cluster(line);
				map.put(c, p);
			}
		}
		return map;
	}

	public TIntObjectHashMap<Cluster> refineBy(int column, int[][] values) {
		TIntObjectHashMap<Cluster> map = new TIntObjectHashMap<>();
		for (TIntIterator iter = array.iterator(); iter.hasNext();) {
			int line = iter.next();
			int c = values[line][column];
			if (map.containsKey(c)) {
				map.get(c).add(line);
			} else {
				Cluster p = new Cluster(line);
				map.put(c, p);
			}
		}
		return map;
	}

	public static Cluster minus(Cluster c1, Cluster cNot) {
		if (cNot.size() < 1)
			return c1;

		TIntHashSet set = new TIntHashSet(cNot.array);

		Cluster cNew2 = new Cluster();
		TIntIterator iterOld = c1.iterator();
		while (iterOld.hasNext()) {
			int nextOld = iterOld.next();
			if (!set.contains(nextOld)) {
				cNew2.add(nextOld);
			}
		}
		return cNew2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((array == null) ? 0 : array.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cluster other = (Cluster) obj;
		if (array == null) {
			if (other.array != null)
				return false;
		} else if (!array.equals(other.array))
			return false;
		return true;
	}
}
