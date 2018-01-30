package de.hpi.is.md.hybrid.impl.sim.threshold;

import de.hpi.is.md.util.Int2Double2ObjectSortedTable;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntLists;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FlatThresholdMap implements ThresholdMap {

	private static final long serialVersionUID = -2517678664969781344L;
	@NonNull
	private final Int2Double2ObjectSortedTable<IntCollection> table;

	@Override
	public IntCollection greaterOrEqual(int valueId, double max) {
		return table.getCeilingValue(valueId, max)
			.orElse(IntLists.EMPTY_LIST);
	}
}
