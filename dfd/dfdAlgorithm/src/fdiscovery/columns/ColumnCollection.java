package fdiscovery.columns;

import org.apache.lucene.util.OpenBitSet;

public class ColumnCollection extends OpenBitSet implements Comparable<OpenBitSet> {

	private static final long serialVersionUID = -5256272139963505719L;

	private int formatStringWidth;
	protected long numberOfColumns;
	protected int[] setBits;
	
	public ColumnCollection(long numberOfColumns ) {
		this.numberOfColumns = numberOfColumns;
		this.formatStringWidth = (int)Math.ceil(Math.log10(this.numberOfColumns));
	}
	
	public int[] getSetBits() {
		int[] setBits = new int[(int) this.cardinality()];
		
		long bitIndex = 0;
		int currentArrayIndex = 0;
		while (bitIndex < this.numberOfColumns) {
			long currentNextSetBit = this.nextSetBit(bitIndex);
			if (currentNextSetBit != -1) {
				setBits[currentArrayIndex++] = (int) currentNextSetBit; 
				bitIndex = currentNextSetBit + 1;
			} else {
				bitIndex = this.numberOfColumns;

			}
		}
		
		return setBits;
	}
	
	public boolean isAtomic() {
		return this.cardinality() == 1;
	}
	
	public ColumnCollection addColumn(long columnIndex) {
		ColumnCollection copy = (ColumnCollection) this.clone();
		copy.set(columnIndex);
		
		return copy;
	}
	
	public ColumnCollection andCopy(ColumnCollection other) {
		ColumnCollection copy = (ColumnCollection)this.clone();
		copy.and(other);
		
		return copy;
	}
	
	public ColumnCollection clearCopy(int startBit) {
		ColumnCollection copy = (ColumnCollection)this.clone();
		copy.clear(startBit);
		
		return copy;
	}
	
	public ColumnCollection clearAllCopy() {
		ColumnCollection copy = (ColumnCollection)this.clone();
		copy.clear(0, this.numberOfColumns);
		
		return copy;
	}
	
	public ColumnCollection andNotCopy(ColumnCollection other) {
		ColumnCollection copy = (ColumnCollection)this.clone();
		copy.andNot(other);
		
		return copy;
	}
	
	public ColumnCollection removeCopy(ColumnCollection other) {
		ColumnCollection copy = (ColumnCollection)this.clone();
		copy.remove(other);
		
		return copy;
	}
	
	public ColumnCollection orCopy(ColumnCollection other) {
		ColumnCollection copy = (ColumnCollection)this.clone();
		copy.or(other);

		return copy;
	}
	
	public ColumnCollection setCopy(int index) {
		ColumnCollection copy = (ColumnCollection)this.clone();
		copy.set(index);
		
		return copy;
	}
	
	public ColumnCollection xorCopy(ColumnCollection other) {
		ColumnCollection copy = (ColumnCollection)this.clone();
		copy.xor(other);
		
		return copy;
	}
	
	public ColumnCollection complementCopy() {
		ColumnCollection copy = (ColumnCollection)this.clone();
		copy.flip(0, this.numberOfColumns);
		
		return copy;
	}
	
	public ColumnCollection complement() {
		this.flip(0, this.numberOfColumns);
		return this;
	}
	
	public boolean isSubsetOf(ColumnCollection other) {
		return ColumnCollection.unionCount(this, other) == other.cardinality();
	}
	
	public boolean isSupersetOf(ColumnCollection other) {
		return ColumnCollection.unionCount(this, other) == this.cardinality();

	}
	
	public boolean isProperSubsetOf(ColumnCollection other) {
		long cardinality = this.cardinality();
		long otherCardinality = other.cardinality();
		if (cardinality != otherCardinality) {
			if (ColumnCollection.unionCount(this, other) == otherCardinality) {
				return true;
			}
		}
		return false;
	}

	
	public boolean isProperSupersetOf(ColumnCollection other) {
		long cardinality = this.cardinality();
		long otherCardinality = other.cardinality();
		if (cardinality != otherCardinality) {
			if (ColumnCollection.unionCount(this, other) == cardinality) {
				return true;
			}
		}
		return false;
	}

	public boolean isSubsetOrSupersetOf(ColumnCollection other) {
		return isSubsetOf(other) || isSupersetOf(other);
	}
	
	public long getNumberOfColumns() {
		return this.numberOfColumns;
	}

	public long getMostRightBit() {
		long bitIndex = 0;
		while (bitIndex < this.numberOfColumns) {
			long currentNextSetBit = this.nextSetBit(bitIndex);
			if (currentNextSetBit != -1) {
				bitIndex = currentNextSetBit + 1;
			} else {
				return bitIndex - 1;

			}
		}
		return bitIndex;
	}
	
	public ColumnCollection removeColumnCopy(int columnIndex) {
		ColumnCollection copy = (ColumnCollection) this.clone();
		copy.clear(columnIndex);
		
		return copy;
	}
	
	public ColumnCollection removeColumnCopy(long columnIndex) {
		ColumnCollection copy = (ColumnCollection) this.clone();
		copy.clear(columnIndex);
		
		return copy;
	}
	
	@Override
	public int compareTo(OpenBitSet other) {
		ColumnCollection copy = (ColumnCollection) this.clone();
		copy.xor(other);
		int lowestBit = copy.nextSetBit(0);
		if (lowestBit == -1) {
			return 0;
		} else if (this.get(lowestBit)) {
			return -1;
		} else {
			return 1;
		}
	}
	
	public String toString() {
		StringBuilder outputBuilder = new StringBuilder();
		if (this.cardinality() > 0) {
			for (Integer columnIndex : this.getSetBits()) {
				outputBuilder.append(String.format("%0" + formatStringWidth + "d,", columnIndex));

			}
		} else {
			outputBuilder.append("emptyset");
		}
		
		return outputBuilder.toString();
	}
	
}
