package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;

public class PollSetTest {

	@Test
	public void test() {
		PollCollection<Integer> collection = new PollSet<>();
		collection.add(1);
		collection.add(2);
		collection.add(2);
		Collection<Integer> polled = collection.poll();
		assertThat(polled).hasSize(2);
		assertThat(polled).contains(1);
		assertThat(polled).contains(2);
		collection.add(3);
		collection.add(1);
		polled = collection.poll();
		assertThat(polled).hasSize(2);
		assertThat(polled).contains(3);
		assertThat(polled).contains(1);
	}

	@Test
	public void testAddAll() {
		PollCollection<Integer> collection = new PollSet<>();
		collection.addAll(Arrays.asList(1, 2, 2));
		Collection<Integer> polled = collection.poll();
		assertThat(polled).hasSize(2);
		assertThat(polled).contains(1);
		assertThat(polled).contains(2);
	}

}