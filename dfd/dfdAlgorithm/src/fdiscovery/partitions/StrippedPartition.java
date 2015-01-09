package fdiscovery.partitions;

import fdiscovery.equivalence.EquivalenceGroupTIntHashSet;
import fdiscovery.equivalence.TEquivalence;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

public class StrippedPartition extends TreeSet<TEquivalence> {

	private static final long serialVersionUID = -10500424753490842L;

	// constructor for TANEs strippedProduct
	public StrippedPartition() {
		
	}
	
	public StrippedPartition(StrippedPartition base, StrippedPartition additional) {
		
	}
	
	public StrippedPartition(String[] columnContent) {
		TObjectIntHashMap<String> valueToIndex = new TObjectIntHashMap<>();
		LinkedHashMap<Integer, TEquivalence> helpMap = new LinkedHashMap<>();
		
		for (int rowIndex = 0; rowIndex < columnContent.length; rowIndex++) {
			String value = columnContent[rowIndex];
			// if the value wasn't there yet, the row index becomes the representative 
			// for that equivalence class
			if (!valueToIndex.containsKey(value)) {
				valueToIndex.put(value, rowIndex);
				TEquivalence equivalenceGroup = new EquivalenceGroupTIntHashSet();
				equivalenceGroup.add(rowIndex);
				helpMap.put(rowIndex, equivalenceGroup);
			} 
			// otherwise find the right equivalence class and add the current element index
			else {
				int equivalenceGroupIndex = valueToIndex.get(value);
				TEquivalence equivalenceClass = helpMap.get(equivalenceGroupIndex);
				equivalenceClass.add(rowIndex);
			}
		}
		// remove equivalence classes with only one element
		for(Iterator<Map.Entry<Integer, TEquivalence>> it=helpMap.entrySet().iterator(); it.hasNext();) {
		     Map.Entry<Integer, TEquivalence> entry = it.next();
		     if (entry.getValue().size() <= 1) {
		          it.remove();
		     }
		 }

		// sort the stripped partition by equivalence group sizes
		this.addAll(helpMap.values());
	}

	@Override
	public String toString() {
		StringBuilder outputBuilder = new StringBuilder();
		outputBuilder.append("{");

		for(TEquivalence entry : this) {
			outputBuilder.append("{");
			for (TIntIterator valueIt=entry.iterator(); valueIt.hasNext(); ) {
//			for (TIntIteratorInteger value : entry) {
				outputBuilder.append(valueIt.next());
				outputBuilder.append(",");
			}
			outputBuilder.append("}");
		}
		outputBuilder.append("}");

		return outputBuilder.toString();
	}
}
