package de.hpi.is.md.util;

import java.util.Optional;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import lombok.NonNull;

public final class OptionalDouble {

	private static final OptionalDouble EMPTY = new OptionalDouble();
	private final java.util.OptionalDouble optional;

	private OptionalDouble() {
		this.optional = java.util.OptionalDouble.empty();
	}

	private OptionalDouble(double value) {
		this.optional = java.util.OptionalDouble.of(value);
	}

	public static OptionalDouble empty() {
		return EMPTY;
	}

	public static OptionalDouble of(double value) {
		return new OptionalDouble(value);
	}

	public static OptionalDouble ofNullable(Double value) {
		return value == null ? empty() : of(value.doubleValue());
	}

	public Optional<Double> boxed() {
		return map(d -> Double.valueOf(d));
	}

	public OptionalDouble filter(@NonNull DoublePredicate predicate) {
		if (!optional.isPresent()) {
			return this;
		}
		return predicate.test(optional.getAsDouble()) ? this : empty();
	}

	public double getAsDouble() {
		return optional.getAsDouble();
	}

	public void ifPresent(DoubleConsumer consumer) {
		optional.ifPresent(consumer);
	}

	public boolean isPresent() {
		return optional.isPresent();
	}

	public <T> Optional<T> map(@NonNull DoubleFunction<T> mapper) {
		return optional.isPresent() ? Optional.ofNullable(mapper.apply(optional.getAsDouble()))
			: Optional.empty();
	}

	public double orElse(double other) {
		return optional.orElse(other);
	}
}
