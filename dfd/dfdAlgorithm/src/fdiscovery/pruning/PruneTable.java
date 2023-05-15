package fdiscovery.pruning;

import java.util.ArrayList;
import java.util.HashMap;

import fdiscovery.columns.ColumnCollection;

// from rhs to lhs
public abstract class PruneTable extends HashMap<ColumnCollection, HashMap<Integer, ArrayList<ColumnCollection>>> {

	private static final long serialVersionUID = 4470955427882698208L;
	
	public int getCount(ColumnCollection RHS) {
		int count = 0;
		if (this.containsKey(RHS)) {
			for (ArrayList<ColumnCollection> collection : this.get(RHS).values()) {
				count += collection.size();
			}
		}
		return count;
	}
	
	
	public void addValue(ColumnCollection RHS, ColumnCollection LHS) {
		if (!this.containsKey(RHS)) {
			this.put(RHS, new HashMap<Integer, ArrayList<ColumnCollection>>());
		}
		if (!this.get(RHS).containsKey(Integer.valueOf(LHS.cardinality()))) {
			this.get(RHS).put(Integer.valueOf(LHS.cardinality()), new ArrayList<ColumnCollection>());
		}
//		System.out.println(this.get(RHS));
//		System.out.println(String.format("Column:\t%s\t%d", LHS, LHS.cardinality()));
		ArrayList<ColumnCollection> dependencies = this.get(RHS).get(Integer.valueOf(LHS.cardinality()));
//		System.out.println(dependencies);
		dependencies.add(LHS);
	}
}
