package de.hpi.is.md.sim;

import com.bakdata.util.jackson.CPSBase;
import com.bakdata.util.jackson.CPSTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import de.hpi.is.md.util.Hashable;
import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Stream;
import lombok.Data;
import org.jooq.lambda.tuple.Tuple2;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeIdResolver(CPSTypeIdResolver.class)
@CPSBase
public interface PairGenerator<T> extends Serializable, Hashable {

	Result<T> generate(Collection<T> left, Collection<T> right);

	@Data
	class Result<T> {

		private final Stream<Tuple2<T, Collection<T>>> pairs;
		private final boolean complete;
	}
}
