package fdiscovery.partitions;

import fdiscovery.equivalence.EquivalenceGroupTIntHashSet;
import fdiscovery.equivalence.TEquivalence;
import fdiscovery.general.ColumnFile;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class FileBasedPartition extends Partition {

	private static final long serialVersionUID = -6103103864186301L;
	
	public FileBasedPartition(int columnIndex, int numberOfColumns, int numberOfRows, ColumnFile columnFile) {
		super(columnIndex, numberOfColumns, numberOfRows);
		
		TIntObjectHashMap<TEquivalence> equivalenceGroupMap = new TIntObjectHashMap<>();
		
		String[] columnContent = columnFile.getContent();
		TObjectIntHashMap<String> valueToIndex = new TObjectIntHashMap<>();
		int groupIndex = 0;
		for (int rowIndex = 0; rowIndex < columnContent.length; rowIndex++) {
			String value = columnContent[rowIndex];
			// if the value wasn't there yet, the row index becomes the representative 
			// for that equivalence class
			if (!valueToIndex.containsKey(value)) {
				valueToIndex.put(value, rowIndex);
				TEquivalence equivalenceGroup = new EquivalenceGroupTIntHashSet(groupIndex++);
				equivalenceGroup.add(rowIndex);
				equivalenceGroupMap.put(rowIndex, equivalenceGroup);
			} 
			// otherwise find the right equivalence class and add the current element index
			else {
				int equivalenceGroupIndex = valueToIndex.get(value);
				TEquivalence equivalenceClass = equivalenceGroupMap.get(equivalenceGroupIndex);
				equivalenceClass.add(rowIndex);
			}
		}
		// remove equivalence classes with only one element
		for(TIntObjectIterator<TEquivalence> equivalenceGroupIt = equivalenceGroupMap.iterator(); equivalenceGroupIt.hasNext(); ) {
			equivalenceGroupIt.advance(); 
			TEquivalence equivalenceGroup = equivalenceGroupIt.value();
			if (equivalenceGroup.size() > 1) {
				this.add(equivalenceGroupIt.value());
			} 
		 }
	}
	
	public int getIndex() {
		return this.indices.nextSetBit(0);
	}
}
