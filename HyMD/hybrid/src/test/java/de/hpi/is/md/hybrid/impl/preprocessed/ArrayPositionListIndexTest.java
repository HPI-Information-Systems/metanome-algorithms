package de.hpi.is.md.hybrid.impl.preprocessed;

import de.hpi.is.md.hybrid.PositionListIndex;
import de.hpi.is.md.hybrid.PositionListIndexTest;
import java.util.Map;

public class ArrayPositionListIndexTest extends PositionListIndexTest {

	@Override
	protected PositionListIndex createPli(Map<Integer, Integer> values) {
		PositionListIndex.Builder builder = ArrayPositionListIndex.builder();
		values.forEach(builder::add);
		return builder.build();
	}
}