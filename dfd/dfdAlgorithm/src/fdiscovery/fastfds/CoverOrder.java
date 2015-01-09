package fdiscovery.fastfds;

public class CoverOrder implements Comparable<CoverOrder> {

	private int columnIndex;
	private int appearances;
	
	public CoverOrder(int columnIndex) {
		this.columnIndex = columnIndex;
		this.appearances = 0;
	}

	public CoverOrder(int columnIndex, int appearances) {
		this.columnIndex = columnIndex;
		this.appearances = appearances;
	}
	
	public int getColumnIndex() {
		return this.columnIndex;
	}
	
	public int getAppearances() {
		return this.appearances;
	}
	
	@Override
	public int compareTo(CoverOrder o) {
		if (this.appearances < o.appearances) {
			return -1;
		}
		if (this.appearances == o.appearances) {
			// guarantee lexical order (reversed because the list gets reversed in the end)
			if (this.columnIndex < o.columnIndex) {
				return 1;
			}
			if (this.columnIndex == o.columnIndex) {
				return 0;
			}
			return -1;
		}
		return 1;
	}
	
	public String toString() {
		StringBuilder outputBuilder = new StringBuilder();
		outputBuilder.append(String.format("[%s:%d]", (char)(this.columnIndex+65), this.appearances));

		return outputBuilder.toString();
	}
}
