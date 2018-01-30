package de.hpi.is.md.hybrid;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Test;

public abstract class PositionListIndexTest {

	@Test
	public void test() {
		Map<Integer, Integer> values = ImmutableMap.<Integer, Integer>builder()
			.put(1, 1)
			.put(2, 1)
			.put(3, 2)
			.build();
		PositionListIndex pli = createPli(values);
		assertThat(pli.get(1)).contains(1);
		assertThat(pli.get(1)).hasSize(2);
		assertThat(pli.get(1)).contains(2);
		assertThat(pli.get(2)).hasSize(1);
		assertThat(pli.get(2)).contains(3);
		assertThat(pli.get(3)).isEmpty();
	}

	protected abstract PositionListIndex createPli(Map<Integer, Integer> values);

}
