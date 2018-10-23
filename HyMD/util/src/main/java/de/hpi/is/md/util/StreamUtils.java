package de.hpi.is.md.util;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jooq.lambda.Seq;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StreamUtils {

	public static <T> Stream<T> parallel(Stream<T> stream, boolean parallel) {
		return parallel ? stream.parallel() : stream.sequential();
	}

	public static <T> Seq<T> seq(Iterable<T> iterable) {
		Stream<T> stream = stream(iterable);
		return Seq.seq(stream);
	}

	public static Seq<Double> seq(double[] doubles) {
		DoubleStream stream = Arrays.stream(doubles);
		return Seq.seq(stream);
	}

	public static <T> Stream<T> stream(Iterable<T> iterable, boolean parallel) {
		Spliterator<T> spliterator = iterable.spliterator();
		return StreamSupport.stream(spliterator, parallel);
	}

	private static <T> Stream<T> stream(Iterable<T> iterable) {
		Spliterator<T> spliterator = iterable.spliterator();
		return Seq.seq(spliterator);
	}
}
