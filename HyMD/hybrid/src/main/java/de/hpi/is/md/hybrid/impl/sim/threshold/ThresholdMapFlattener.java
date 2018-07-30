package de.hpi.is.md.hybrid.impl.sim.threshold;

import com.bakdata.util.jackson.CPSBase;
import com.bakdata.util.jackson.CPSTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import de.hpi.is.md.util.Hashable;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;

public interface ThresholdMapFlattener {

	ThresholdMap build();

	void flatten(int valueId, Double2ObjectMap<IntSet> sortedMap);

	@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
	@JsonTypeIdResolver(CPSTypeIdResolver.class)
	@CPSBase
	interface Factory extends Hashable {

		ThresholdMapFlattener create(int leftSize);
	}
}
