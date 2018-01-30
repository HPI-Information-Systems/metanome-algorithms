package de.hpi.is.md.hybrid;

import de.hpi.is.md.hybrid.PositionListIndex.Cluster;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.Serializable;
import lombok.Data;

public interface PositionListIndex extends Serializable, Iterable<Cluster> {

	IntSet get(int valueId);

	interface Builder {

		void add(int recordId, int value);

		PositionListIndex build();
	}

	@Data
	class Cluster {

		private final int value;
		private final IntSet records;
	}
}
