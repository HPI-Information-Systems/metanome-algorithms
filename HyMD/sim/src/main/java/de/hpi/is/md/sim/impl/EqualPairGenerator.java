package de.hpi.is.md.sim.impl;

import com.bakdata.util.jackson.CPSType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.hpi.is.md.sim.PairGenerator;
import de.hpi.is.md.util.CastUtils;
import de.hpi.is.md.util.jackson.SingletonDeserializer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Stream;
import org.jooq.lambda.tuple.Tuple2;

@JsonDeserialize(using = SingletonDeserializer.class)
@CPSType(id = "equal", base = PairGenerator.class)
public enum EqualPairGenerator implements PairGenerator<Object> {

	INSTANCE;

	public static <T> PairGenerator<T> getInstance() {
		return CastUtils.as(INSTANCE);
	}

	private static boolean isComplete(Collection<Object> left, Collection<Object> right) {
		return (left.size() <= 1 || right.size() <= 1) &&
			(left.size() != 1 || right.size() != 1 || right.containsAll(left));
	}

	private static Tuple2<Object, Collection<Object>> toPair(Object l) {
		return new Tuple2<>(l, Collections.singleton(l));
	}

	@Override
	public Result<Object> generate(Collection<Object> left, Collection<Object> right) {
		Collection<Object> rightSet = new HashSet<>(right);
		Stream<Tuple2<Object, Collection<Object>>> pairs = left.stream()
			.filter(rightSet::contains)
			.map(EqualPairGenerator::toPair);
		boolean complete = isComplete(left, right);
		return new Result<>(pairs, complete);
	}

	@Override
	public String toString() {
		return "EqualPairGenerator";
	}
}
