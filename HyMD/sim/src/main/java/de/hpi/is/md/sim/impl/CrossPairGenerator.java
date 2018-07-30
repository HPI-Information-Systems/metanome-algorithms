package de.hpi.is.md.sim.impl;

import com.bakdata.util.jackson.CPSType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.hpi.is.md.sim.PairGenerator;
import de.hpi.is.md.util.jackson.SingletonDeserializer;
import java.util.Collection;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

@JsonDeserialize(using = SingletonDeserializer.class)
@CPSType(id = "cross", base = PairGenerator.class)
@Slf4j
public enum CrossPairGenerator implements PairGenerator<Object> {

	INSTANCE;

	@SuppressWarnings("unchecked")
	public static <T> PairGenerator<T> getInstance() {
		return (PairGenerator<T>) INSTANCE;
	}

	@Override
	public Result<Object> generate(Collection<Object> left, Collection<Object> right) {
		long expectedComputations = (long) left.size() * right.size();
		log.info("Will compute {} similarities", expectedComputations);
		//avoid Seq here to allow parallelization later
		Stream<Tuple2<Object, Collection<Object>>> pairs = left.stream()
			.map(l -> Tuple.tuple(l, right));
		return new Result<>(pairs, true);
	}

	@Override
	public String toString() {
		return "CrossPairGenerator";
	}
}
