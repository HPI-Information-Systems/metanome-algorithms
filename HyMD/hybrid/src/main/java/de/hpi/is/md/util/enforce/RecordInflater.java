package de.hpi.is.md.util.enforce;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RecordInflater {

	private final List<Int2ObjectMap<?>> dictionaries;

	Object[] inflate(int[] record) {
		Object[] inflated = new Object[record.length];
		for (int i = 0; i < record.length; i++) {
			int id = record[i];
			Int2ObjectMap<?> dictionary = dictionaries.get(i);
			inflated[i] = dictionary.get(id);
		}
		return inflated;
	}

}
