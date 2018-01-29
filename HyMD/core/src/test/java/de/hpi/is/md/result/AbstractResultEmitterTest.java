package de.hpi.is.md.result;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class AbstractResultEmitterTest {

	@Test
	public void testEmission() {
		StringResultEmitter resultEmitter = new StringResultEmitter();
		List<String> l1 = new ArrayList<>();
		List<String> l2 = new ArrayList<>();
		assertThat(l1).isEmpty();
		assertThat(l2).isEmpty();
		resultEmitter.register(l1::add);
		resultEmitter.emitResult("foo");
		assertThat(l1).hasSize(1);
		assertThat(l1).contains("foo");
		assertThat(l2).isEmpty();
		resultEmitter.register(l2::add);
		resultEmitter.emitResult("bar");
		assertThat(l1).hasSize(2);
		assertThat(l2).hasSize(1);
		assertThat(l1).contains("foo");
		assertThat(l1).contains("bar");
		assertThat(l2).contains("bar");
	}

	@Test
	public void testUnregister() {
		StringResultEmitter resultEmitter = new StringResultEmitter();
		List<String> l1 = new ArrayList<>();
		assertThat(l1).isEmpty();
		resultEmitter.register(l1::add);
		resultEmitter.emitResult("foo");
		assertThat(l1).hasSize(1);
		assertThat(l1).contains("foo");
		resultEmitter.unregisterAll();
		resultEmitter.emitResult("bar");
		assertThat(l1).hasSize(1);
		assertThat(l1).contains("foo");
	}

	private static class StringResultEmitter extends AbstractResultEmitter<String> {

	}

}
