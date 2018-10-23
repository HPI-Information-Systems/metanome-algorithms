package de.hpi.is.md.sim.impl;

import com.bakdata.util.jackson.CPSType;
import com.fasterxml.jackson.annotation.JsonCreator;
import de.hpi.is.md.sim.PairGenerator;
import de.hpi.is.md.sim.Similarity;
import de.hpi.is.md.sim.SimilarityComputer;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.Hasher;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Setter;
import org.jooq.lambda.tuple.Tuple2;

@CPSType(id = "default", base = SimilarityComputer.class)
@Setter
@Builder
public final class SimilarityComputerImpl<T> implements SimilarityComputer<T> {

	private static final long serialVersionUID = -7197484583733940857L;
	private static final boolean DEFAULT_PARALLEL = true;
	@NonNull
	private PairGenerator<T> generator;
	@Default
	private boolean parallel = DEFAULT_PARALLEL;

	@JsonCreator
	private SimilarityComputerImpl() {
		this.parallel = DEFAULT_PARALLEL;
	}

	private SimilarityComputerImpl(PairGenerator<T> generator, boolean parallel) {
		this.generator = generator;
		this.parallel = parallel;
	}

	@Override
	public Result<T> compute(SimilarityMeasure<T> similarityMeasure, Collection<T> left,
		Collection<T> right) {
		PairGenerator.Result<T> result = generator.generate(left, right);
		return compute(similarityMeasure, result);
	}

	@Override
	public void hash(Hasher hasher) {
		hasher.put(generator);
	}

	@Override
	public String toString() {
		return generator.toString();
	}

	private Result<T> compute(SimilarityMeasure<T> similarityMeasure,
		PairGenerator.Result<T> result) {
		SimilarityCalculator<T> calculator = new SimilarityCalculator<>(similarityMeasure);
		Stream<Tuple2<T, Collection<T>>> pairs = result.getPairs();
		Stream<Similarity<T>> similarities = StreamUtils.parallel(pairs, parallel)
			.map(t -> t.map(calculator::calculateSimilarities));
		return new Result<>(similarities, result.isComplete());
	}

}
