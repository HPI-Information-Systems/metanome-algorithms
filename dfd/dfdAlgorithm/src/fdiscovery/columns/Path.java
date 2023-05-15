package fdiscovery.columns;

import java.util.ArrayList;


public class Path extends ColumnCollection {

	private static final long serialVersionUID = -6451347203736964695L;

	public Path(long numberOfColumns) {
		super(numberOfColumns);
	}

	public ArrayList<Path> getMaximalSubsets() {
		ArrayList<Path> maximalSubsetPaths = new ArrayList<>();
		
		if (this.isEmpty()) {
			return new ArrayList<>();
		}
		for (int columnIndex : this.getSetBits()) {
			maximalSubsetPaths.add((Path)this.removeColumnCopy(columnIndex));
		}
		
		return maximalSubsetPaths;
	}
}
