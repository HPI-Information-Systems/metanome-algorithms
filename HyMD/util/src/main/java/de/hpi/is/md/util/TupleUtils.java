package de.hpi.is.md.util;

import java.util.Map.Entry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jooq.lambda.tuple.Tuple2;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TupleUtils {

	public static <K, V> Tuple2<K, V> toTuple(Entry<K, V> entry) {
		return new Tuple2<>(entry.getKey(), entry.getValue());
	}

}
