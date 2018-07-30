package de.hpi.is.md.sim;

import com.bakdata.util.jackson.CPSBase;
import com.bakdata.util.jackson.CPSTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import de.hpi.is.md.sim.impl.SimilarityComputerImpl;
import de.hpi.is.md.util.Hashable;
import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Stream;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type", defaultImpl = SimilarityComputerImpl.class)
@JsonTypeIdResolver(CPSTypeIdResolver.class)
@CPSBase
public interface SimilarityComputer<T> extends Serializable, Hashable {

	Result<T> compute(SimilarityMeasure<T> similarityMeasure, Collection<T> left,
		Collection<T> right);

	@Data
	class Result<T> {

		private final Stream<Similarity<T>> similarities;
		private final boolean complete;
	}
}
