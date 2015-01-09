package fdiscovery.approach.equivalence;

import fdiscovery.partitions.Partition;

public class EquivalenceManagedPartition extends Partition {

	private static final long serialVersionUID = 3864482946944185511L;
	
	protected long hashNumber;
	
	public EquivalenceManagedPartition(int columnIndex, int numberOfColumns, int numberOfRows) {
		super(columnIndex, numberOfColumns, numberOfRows);
	}

	public EquivalenceManagedPartition(EquivalenceManagedPartition base, EquivalenceManagedPartition additional) {
		super(base, additional);
	}

	public long getHashNumber() {
		return hashNumber;
	}
	
}
