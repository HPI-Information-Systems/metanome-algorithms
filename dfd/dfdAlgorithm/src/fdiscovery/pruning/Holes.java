package fdiscovery.pruning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

import fdiscovery.columns.ColumnCollection;

public class Holes extends TreeSet<ColumnCollection> {
	
	private static final long serialVersionUID = 178933903410138407L;

	public static Comparator<ColumnCollection> compEliminateSuperset = new Comparator<ColumnCollection>() {
		
		@Override
		public int compare(ColumnCollection o1, ColumnCollection o2) {
			if (o1.isProperSupersetOf(o2)) {
				return 0;
			} else {
				return o1.compareTo(o2);
			}
		}
	}; 
	
	public static Comparator<ColumnCollection> orderComp = new Comparator<ColumnCollection>() {
		
		@Override
		public int compare(ColumnCollection o1, ColumnCollection o2) {
			long o1Cardinality = o1.cardinality();
			long o2Cardinality = o2.cardinality();
			if (o1Cardinality < o2Cardinality) {
				return -1;
			} else if (o1Cardinality > o2Cardinality) {
				return 1;
			} else {
				return o1.compareTo(o2);
			}
		}
	}; 

	public Holes() {
		super(compEliminateSuperset);
	}
	
	public void minimize() {
		ArrayList<ColumnCollection> ordered = new ArrayList<>(this.size());
		ordered.addAll(this);
		Collections.sort(ordered, orderComp);
		this.clear();
		
		for (ColumnCollection collection : ordered) {
			this.add(collection);
		}
//		this.addAll(ordered);
	}
}
