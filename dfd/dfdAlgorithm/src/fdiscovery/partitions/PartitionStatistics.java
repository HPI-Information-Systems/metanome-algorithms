package fdiscovery.partitions;

import java.util.ArrayList;

import fdiscovery.columns.ColumnCollection;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class PartitionStatistics extends TObjectIntHashMap<ColumnCollection> {

	public String getStatistics() {
		TLongObjectHashMap<TIntObjectHashMap<ArrayList<ColumnCollection>>> statsAndCountsByLevel = new TLongObjectHashMap<>();
		for (ColumnCollection partitionKey : this.keySet()) {
			long keyCardinality = partitionKey.cardinality();
			int usageCount = this.get(partitionKey);
			statsAndCountsByLevel.putIfAbsent(keyCardinality, new TIntObjectHashMap<ArrayList<ColumnCollection>>());
			statsAndCountsByLevel.get(keyCardinality).putIfAbsent(usageCount, new ArrayList<ColumnCollection>());
			statsAndCountsByLevel.get(keyCardinality).get(usageCount).add(partitionKey);
		}
		StringBuilder statisticsBuilder = new StringBuilder();
		statisticsBuilder.append("Statistics:\n");
		for (TLongObjectIterator<TIntObjectHashMap<ArrayList<ColumnCollection>>> statsByLevelIt = statsAndCountsByLevel.iterator(); statsByLevelIt.hasNext(); ) {
			statsByLevelIt.advance();
			long levelCardinality = statsByLevelIt.key();
			statisticsBuilder.append(String.format("%d attributes {\n", Long.valueOf(levelCardinality)));
			for (TIntObjectIterator<ArrayList<ColumnCollection>> countByLevelIt = statsByLevelIt.value().iterator(); countByLevelIt.hasNext(); ) {
				countByLevelIt.advance();
				int usageCount = countByLevelIt.key();
				int numberOfElements = countByLevelIt.value().size();
				statisticsBuilder.append(String.format("\t%d elements used %d times\n", Integer.valueOf(numberOfElements), Integer.valueOf(usageCount)));
			}
			statisticsBuilder.append("}\n");
		}
				
		return statisticsBuilder.toString();		
	}
}
