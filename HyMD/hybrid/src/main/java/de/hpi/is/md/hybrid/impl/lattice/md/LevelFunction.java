package de.hpi.is.md.hybrid.impl.lattice.md;

import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.StreamUtils;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;
import java.util.List;

public interface LevelFunction {

	default int getDistance(MDSite lhs) {
		return StreamUtils.seq(lhs)
			.mapToInt(lhsElem -> getSingleDistance(lhsElem.getId(), lhsElem.getThreshold()))
			.sum();
	}

	int getSingleDistance(int lhsAttr, double threshold);

	int size();

	interface Factory {

		LevelFunction create(List<DoubleSortedSet> thresholds);
	}
}
