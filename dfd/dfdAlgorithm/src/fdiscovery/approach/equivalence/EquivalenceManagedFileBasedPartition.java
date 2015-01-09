package fdiscovery.approach.equivalence;

import fdiscovery.equivalence.EquivalenceGroupTIntHashSet;
import fdiscovery.equivalence.TEquivalence;
import fdiscovery.general.ColumnFile;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class EquivalenceManagedFileBasedPartition extends EquivalenceManagedPartition {

	private static final long serialVersionUID = -6103103864186301L;
	
	public EquivalenceManagedFileBasedPartition(int columnIndex, int numberOfColumns, int numberOfRows, ColumnFile columnFile) {
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
				hashNumber += rowIndex+1;
			} 
			// otherwise find the right equivalence class and add the current element index
			else {
				int equivalenceGroupIndex = valueToIndex.get(value);
				TEquivalence equivalenceClass = equivalenceGroupMap.get(equivalenceGroupIndex);
				equivalenceClass.add(rowIndex);
				hashNumber += equivalenceGroupIndex;
			}
		}
		// remove equivalence classes with only one element
		for(TIntObjectIterator<TEquivalence> equivalenceGroupIt = equivalenceGroupMap.iterator(); equivalenceGroupIt.hasNext(); ) {
			equivalenceGroupIt.advance(); 
			TEquivalence equivalenceGroup = equivalenceGroupIt.value();
			if (equivalenceGroup.size() > 1) {
				this.add(equivalenceGroupIt.value());
			} else {
				hashNumber -= equivalenceGroup.iterator().next();
			}
		 }
	}
	
	public int getIndex() {
		return this.indices.nextSetBit(0);
	}
}
