package fdiscovery.partitions;

import fdiscovery.general.ColumnFile;
import fdiscovery.general.ColumnFiles;

import fdiscovery.columns.ColumnCollection;
import gnu.trove.map.hash.THashMap;

public class StrippedPartitions extends THashMap<ColumnCollection, StrippedPartition> {

	private static final long serialVersionUID = 3010652008616797722L;

	public StrippedPartitions() {
		
	}
	
	public StrippedPartitions(ColumnFiles columnFiles) {

		int numberOfColumns = columnFiles.size();
		int columnIndex = 0;

		for (ColumnFile columnFile : columnFiles) {
			String[] columnContent = columnFile.getContent();
			ColumnCollection identifier = new ColumnCollection(numberOfColumns);
			identifier.set(columnIndex);
			this.put(identifier, new StrippedPartition(columnContent));
			columnIndex++;
		}
	}
}
