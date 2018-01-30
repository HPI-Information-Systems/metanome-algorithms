package de.hpi.is.md.hybrid.impl.preprocessed;

import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.DictionaryRecords.Builder;
import de.hpi.is.md.hybrid.DictionaryRecordsTest;
import java.util.Collection;

public class MapDictionaryRecordsTest extends DictionaryRecordsTest {

	@Override
	protected DictionaryRecords createRecords(Collection<int[]> records) {
		int id = 0;
		Builder builder = MapDictionaryRecords.builder();
		for (int[] record : records) {
			builder.add(id++, record);
		}
		return builder.build();
	}
}