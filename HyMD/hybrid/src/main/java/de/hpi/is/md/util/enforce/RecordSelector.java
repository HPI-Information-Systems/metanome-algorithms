package de.hpi.is.md.util.enforce;

import de.hpi.is.md.hybrid.DictionaryRecords;
import it.unimi.dsi.fastutil.ints.IntCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.PrimitiveIterator.OfInt;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class RecordSelector {

	@NonNull
	private final DictionaryRecords records;

	Iterable<int[]> getRecords(IntCollection ids) {
		int size = ids.size();
		Collection<int[]> result = new ArrayList<>(size);
		OfInt it = ids.iterator();
		while (it.hasNext()) {
			int id = it.nextInt();
			int[] record = records.get(id);
			result.add(record);
		}
		return result;
	}
}
