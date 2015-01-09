package fdiscovery.general;

import java.util.Iterator;
import java.util.TreeSet;

public class CollectionSet<T extends Comparable<? super T>> extends TreeSet<T> implements Comparable<TreeSet<T>> {

	private static final long serialVersionUID = 1839773136406309404L;

	@Override
	public int compareTo(TreeSet<T> other) {
		TreeSet<T> set = other;
		Iterator<T> iterThis = iterator();
		Iterator<T> iterOther = set.iterator();
		
		while (iterThis.hasNext() && iterOther.hasNext()) {
			T first = iterThis.next();
			T second = iterOther.next();
			int cmp = first.compareTo(second);
			if (cmp == 0) {
				continue;
			}
			return cmp;
		}
		if (iterThis.hasNext()) {
			return 1;
		}
		if (iterOther.hasNext()) {
			return -1;
		}
		return 0;
	}

}
