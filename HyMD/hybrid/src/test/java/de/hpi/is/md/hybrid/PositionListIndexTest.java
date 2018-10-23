package de.hpi.is.md.hybrid;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Test;

public abstract class PositionListIndexTest {

	@Test
	public void test() {
		Map<Integer, Integer> values = ImmutableMap.<Integer, Integer>builder()
			.put(Integer.valueOf(1), Integer.valueOf(1))
			.put(Integer.valueOf(2), Integer.valueOf(1))
			.put(Integer.valueOf(3), Integer.valueOf(2))
			.build();
		PositionListIndex pli = createPli(values);
		assertThat(pli.get(1)).contains(Integer.valueOf(1));
		assertThat(pli.get(1)).hasSize(2);
		assertThat(pli.get(1)).contains(Integer.valueOf(2));
		assertThat(pli.get(2)).hasSize(1);
		assertThat(pli.get(2)).contains(Integer.valueOf(3));
		assertThat(pli.get(3)).isEmpty();
	}

	protected abstract PositionListIndex createPli(Map<Integer, Integer> values);

}
