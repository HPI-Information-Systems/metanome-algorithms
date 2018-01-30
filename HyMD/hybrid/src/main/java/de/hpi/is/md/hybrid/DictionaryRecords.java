package de.hpi.is.md.hybrid;

import de.hpi.is.md.util.Hashable;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.Serializable;

public interface DictionaryRecords extends Iterable<int[]>, Serializable {

	int[] get(int id);

	IntSet getAll();

	interface Builder extends Hashable {

		Builder add(int recordId, int[] record);

		DictionaryRecords build();
	}
}
