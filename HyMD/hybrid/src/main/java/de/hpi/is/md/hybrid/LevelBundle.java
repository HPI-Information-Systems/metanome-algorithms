package de.hpi.is.md.hybrid;

import de.hpi.is.md.hybrid.impl.lattice.FullLattice;
import de.hpi.is.md.hybrid.impl.lattice.md.Cardinality;
import de.hpi.is.md.hybrid.impl.lattice.md.Dimensions;
import de.hpi.is.md.hybrid.impl.lattice.md.LevelFunction;
import de.hpi.is.md.hybrid.impl.level.LevelStrategy;
import de.hpi.is.md.hybrid.impl.level.minimal.AlreadyMinimalStrategy;
import de.hpi.is.md.hybrid.impl.level.minimizing.MinimizingLevelStrategy;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum LevelBundle {

	CARDINALITY(MinimizingLevelStrategy::of, Cardinality.factory()),
	DISTANCE(AlreadyMinimalStrategy::of, Dimensions::of);

	private final LevelStrategy.Factory levelStrategyFactory;
	private final LevelFunction.Factory levelFunctionFactory;

	LevelFunction createLevelFunction(List<DoubleSortedSet> thresholds) {
		return levelFunctionFactory.create(thresholds);
	}

	LevelStrategy createLevelStrategy(FullLattice fullLattice, double minThreshold) {
		return levelStrategyFactory.create(fullLattice, minThreshold);
	}
}
