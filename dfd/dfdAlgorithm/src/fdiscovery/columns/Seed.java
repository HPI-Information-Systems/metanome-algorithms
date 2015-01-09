package fdiscovery.columns;

import fdiscovery.partitions.FileBasedPartition;
import fdiscovery.partitions.Partition;

public class Seed implements Comparable<Seed> {

	private ColumnCollection indices;
	private int additionalColumnIndex;
	private double distinctiveness;
	
	public Seed(Partition a, FileBasedPartition b) {
		this.indices = a.getIndices().orCopy(b.getIndices());
		this.additionalColumnIndex = b.getIndex();
		this.distinctiveness = Partition.estimateDistinctiveness(a, b);
	}
	
	// inverse order
	@Override
	public int compareTo(Seed o) {
		if (this.distinctiveness != o.distinctiveness) {
			if (o.distinctiveness - this.distinctiveness < 0) {
				return -1;
			} else {
				return 1;
			}
		} else {
			return this.indices.compareTo(o.indices);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (!(o instanceof Seed)) {
			return false;
		} else {
			Seed otherSeed = (Seed) o;
			return this.distinctiveness == otherSeed.distinctiveness && this.indices.compareTo(otherSeed.indices) == 0;
		}
	}
	
	public ColumnCollection getBaseIndices() {
		return this.indices.removeColumnCopy(additionalColumnIndex);
	}
	
	public ColumnCollection getIndices() {
		return this.indices;
	}

	public int getAdditionalColumnIndex() {
		return this.additionalColumnIndex;
	}
	
	public String toString() {
		StringBuilder outputBuilder = new StringBuilder();
		outputBuilder.append(String.format("Seed: [%s]\t%f", this.indices, this.distinctiveness));
		return outputBuilder.toString();
	}
}
