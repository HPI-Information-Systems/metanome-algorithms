package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import java.util.NoSuchElementException;
import org.junit.Test;

public class OptionalDoubleTest {

	@Test
	public void testBoxed() {
		assertThat(OptionalDouble.empty().boxed()).isEmpty();
		assertThat(OptionalDouble.of(1.0).boxed()).hasValue(1.0);
	}

	@Test
	public void testEmpty() {
		assertThat(OptionalDouble.empty().boxed()).isEmpty();
	}

	@Test
	public void testFilter() {
		assertThat(OptionalDouble.empty().filter(d -> d > 2.0).isPresent()).isFalse();
		assertThat(OptionalDouble.of(2.0).filter(d -> d > 2.0).isPresent()).isFalse();
		assertThat(OptionalDouble.of(3.0).filter(d -> d > 2.0).isPresent()).isTrue();
	}

	@Test
	public void testGet() {
		assertThat(OptionalDouble.of(1.0).getAsDouble()).isEqualTo(1.0);
	}

	@Test(expected = NoSuchElementException.class)
	public void testGetEmpty() {
		OptionalDouble.empty().getAsDouble();
		fail();
	}

	@Test
	public void testIfPresent() {
		DoubleCollection collection = new DoubleArrayList();
		OptionalDouble.of(2.0).ifPresent(collection::add);
		assertThat(collection).hasSize(1);
		assertThat(collection).contains(2.0);
	}

	@Test
	public void testIfPresentEmpty() {
		DoubleCollection collection = new DoubleArrayList();
		OptionalDouble.empty().ifPresent(collection::add);
		assertThat(collection).isEmpty();
	}

	@Test
	public void testIsPresent() {
		assertThat(OptionalDouble.empty().isPresent()).isFalse();
		assertThat(OptionalDouble.of(1.0).isPresent()).isTrue();
	}

	@Test
	public void testMap() {
		assertThat(OptionalDouble.empty().map(d -> 1)).isEmpty();
		assertThat(OptionalDouble.of(2.0).map(d -> 1)).hasValue(1);
	}

	@Test(expected = NullPointerException.class)
	public void testNullConsumer() {
		OptionalDouble.of(2.0).ifPresent(null);
		fail();
	}

	@Test(expected = NullPointerException.class)
	public void testNullMapper() {
		OptionalDouble.of(2.0).map(null);
		fail();
	}

	@Test(expected = NullPointerException.class)
	public void testNullPredicate() {
		OptionalDouble.of(2.0).filter(null);
		fail();
	}

	@Test
	public void testOf() {
		assertThat(OptionalDouble.of(2.0).boxed()).hasValue(2.0);
	}

	@Test
	public void testOfNullable() {
		assertThat(OptionalDouble.ofNullable(null).boxed()).isEmpty();
		assertThat(OptionalDouble.ofNullable(1.0).boxed()).hasValue(1.0);
	}

	@Test
	public void testOrElse() {
		assertThat(OptionalDouble.empty().orElse(2.0)).isEqualTo(2.0);
		assertThat(OptionalDouble.of(1.0).orElse(2.0)).isEqualTo(1.0);
	}
}