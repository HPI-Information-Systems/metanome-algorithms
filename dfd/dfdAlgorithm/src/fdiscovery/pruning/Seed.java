package fdiscovery.pruning;

import fdiscovery.columns.ColumnCollection;

public class Seed {

	private ColumnCollection indices;
	private int additionalColumnIndex;
	
	public Seed(ColumnCollection indices, int addtionalColumnIndex) {
		this.indices = indices.setCopy(addtionalColumnIndex);
		this.additionalColumnIndex = addtionalColumnIndex;
	}
	
	public Seed(ColumnCollection indices) {
		this.indices = indices;
		this.additionalColumnIndex = -1;
	}
	
	public boolean isAtomic() {
		return this.indices.cardinality() == 1;
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
		outputBuilder.append(String.format("Seed: [%s]", this.indices));
		return outputBuilder.toString();
	}
}
