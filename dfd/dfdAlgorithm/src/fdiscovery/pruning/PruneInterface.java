package fdiscovery.pruning;

import fdiscovery.columns.ColumnCollection;

public interface PruneInterface {

	public static final int SPLIT_THRESHOLD = 1000;

	public void rebalance();
	public void rebalanceGroup(ColumnCollection groupKey);
	
}
