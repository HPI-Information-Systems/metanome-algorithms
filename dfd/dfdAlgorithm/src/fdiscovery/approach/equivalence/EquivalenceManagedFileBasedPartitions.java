package fdiscovery.approach.equivalence;


import fdiscovery.general.ColumnFile;
import fdiscovery.general.ColumnFiles;

import java.util.ArrayList;

import fdiscovery.preprocessing.SVFileProcessor;

public class EquivalenceManagedFileBasedPartitions extends ArrayList<EquivalenceManagedFileBasedPartition> {

	private static final long serialVersionUID = -7785660771108809119L;

	public EquivalenceManagedFileBasedPartitions(SVFileProcessor fileProcessor) {
		
			int numberOfColumns = fileProcessor.getNumberOfColumns();
			int numberOfRows = fileProcessor.getNumberOfRows();
			int columnIndex = 0;
			for (ColumnFile columnFile : fileProcessor.getColumnFiles()) {
				this.add(new EquivalenceManagedFileBasedPartition(columnIndex++, numberOfColumns, numberOfRows, columnFile));
			}
	}
	
	public EquivalenceManagedFileBasedPartitions(ColumnFiles columnFiles, int numberOfRows) {
		
		int numberOfColumns = columnFiles.getNumberOfColumns();
		int columnIndex = 0;
		for (ColumnFile columnFile : columnFiles) {
			this.add(new EquivalenceManagedFileBasedPartition(columnIndex++, numberOfColumns, numberOfRows, columnFile));
		}
}
	
	@Override
	public String toString() {
		String output = new String();
		output += "BEG FileBasedPartitions\n";
		for (EquivalenceManagedFileBasedPartition partition : this) {
			output += partition.toString();
			output += "\n";
		}
		output += "END FileBasedPartitions\n";
		
		return output;
	}
}
