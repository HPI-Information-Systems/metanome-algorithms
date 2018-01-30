package de.hpi.is.md.sim;

import com.bakdata.util.jackson.CPSBase;
import com.bakdata.util.jackson.CPSTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import de.hpi.is.md.util.Hashable;
import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeIdResolver(CPSTypeIdResolver.class)
@CPSBase
public interface DistanceMetric<T> extends Hashable, Serializable {

	long computeDistance(T obj1, T obj2);

}
