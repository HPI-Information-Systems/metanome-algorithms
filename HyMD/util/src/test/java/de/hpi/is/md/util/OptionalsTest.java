package de.hpi.is.md.util;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class OptionalsTest {

	@Test
	public void testEmptyStream() {
		assertThat(Optionals.stream(Optional.empty()).count()).isEqualTo(0L);
	}

	@Test
	public void testOfCollection() {
		Assert.assertThat(Optionals.of(Arrays.asList(1, 2)), isPresentAnd(hasSize(2)));
		Assert.assertThat(Optionals.of(Arrays.asList(1, 2)), isPresentAnd(hasItems(1, 2)));
	}

	@Test
	public void testOfEmptyCollection() {
		assertThat(Optionals.of(Collections.emptyList())).isEmpty();
	}

	@Test
	public void testStream() {
		assertThat(Optionals.stream(Optional.of("foo")).count()).isEqualTo(1L);
		assertThat(Optionals.stream(Optional.of("foo")).findFirst()).hasValue("foo");
	}

}
