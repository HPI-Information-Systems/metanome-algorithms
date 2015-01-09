package fdiscovery.partitions;


import fdiscovery.general.ColumnFile;
import fdiscovery.general.ColumnFiles;

import java.util.ArrayList;

import fdiscovery.preprocessing.SVFileProcessor;

public class FileBasedPartitions extends ArrayList<FileBasedPartition> {

	private static final long serialVersionUID = -7785660771108809119L;

	public FileBasedPartitions(SVFileProcessor fileProcessor) {
		
			int numberOfColumns = fileProcessor.getNumberOfColumns();
			int numberOfRows = fileProcessor.getNumberOfRows();
			int columnIndex = 0;
			for (ColumnFile columnFile : fileProcessor.getColumnFiles()) {
				this.add(new FileBasedPartition(columnIndex++, numberOfColumns, numberOfRows, columnFile));
			}
	}
	
	public FileBasedPartitions(ColumnFiles columnFiles, int numberOfRows) {
		
		int numberOfColumns = columnFiles.getNumberOfColumns();
		int columnIndex = 0;
		for (ColumnFile columnFile : columnFiles) {
			this.add(new FileBasedPartition(columnIndex++, numberOfColumns, numberOfRows, columnFile));
		}
	}
}
