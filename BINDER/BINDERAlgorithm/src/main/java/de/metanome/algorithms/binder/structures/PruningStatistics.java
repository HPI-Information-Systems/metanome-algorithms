package de.metanome.algorithms.binder.structures;

public class PruningStatistics {
	
	private int prunedCombinations = 0;
//	private int numColumns;
	private int numOfExpectedBucketsPerColumn;
	private int[] bucketsPerColumn = null;
	private String[][] minValues = null;
	private String[][] maxValues = null;
	
	public int getPrunedCombinations() {
		return this.prunedCombinations;
	}

/*	public PruningStatistics(int numColumns, int numBucketsPerColumn) {
		this.numColumns = numColumns;
		this.numOfExpectedBucketsPerColumn = numBucketsPerColumn;
		this.bucketsPerColumn = new int[this.numColumns];
		this.minValues = new String[this.numColumns][this.numOfExpectedBucketsPerColumn];
		this.maxValues = new String[this.numColumns][this.numOfExpectedBucketsPerColumn];
	}
*/	
/*	public void addValue(int column, int bucket, String value) {
		if (this.minValues[column][bucket] == null) {
			this.minValues[column][bucket] = value;
			this.maxValues[column][bucket] = value;
			this.bucketsPerColumn[column]++;
		}
		else if (value.compareTo(this.minValues[column][bucket]) < 0)
			this.minValues[column][bucket] = value;
		else if (value.compareTo(this.maxValues[column][bucket]) > 0)
			this.maxValues[column][bucket] = value;
	}
*/	
	@SuppressWarnings("unused")
	public boolean isValid(int dep, int ref) {
//		if (this.checkBucketCounts(dep, ref) && this.checkBucketCharacteristics(dep, ref)) {
//			this.prunedCombinations++;
			return true; // TODO: Implement statistical pruning here
//		}
//		return false;
	}
	
	protected boolean checkBucketCounts(int dep, int ref) {
		return this.bucketsPerColumn[dep] <= this.bucketsPerColumn[ref];
	}
	
	protected boolean checkBucketCharacteristics(int dep, int ref) {
		for (int bucket = 0; bucket < this.numOfExpectedBucketsPerColumn; bucket++) {
			if (this.minValues[dep][bucket] != null)
				if ((this.minValues[ref][bucket] == null) || (this.minValues[dep][bucket].compareTo(this.minValues[ref][bucket]) < 0))
					return false;
			
			if (this.maxValues[dep][bucket] != null)
				if ((this.maxValues[ref][bucket] == null) || (this.maxValues[dep][bucket].compareTo(this.maxValues[ref][bucket]) > 0))
					return false;
		}
		return true;
	}
	
//    public int parse2Int (final String s) {
//        // Check for a sign.
//        int num  = 0;
//        int sign = -1;
//        final int len  = s.length( );
//        final char ch  = s.charAt( 0 );
//        if ( ch == '-' )
//            sign = 1;
//        else
//            num = '0' - ch;
//        // Build the number.
//        int i = 1;
//        while ( i < len )
//            num = num*10 + '0' - s.charAt( i++ );
//        return sign * num;
//    }
	
}