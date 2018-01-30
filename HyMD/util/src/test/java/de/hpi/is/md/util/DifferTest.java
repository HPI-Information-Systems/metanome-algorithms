package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import de.hpi.is.md.util.Differ.DiffResult;
import java.util.Arrays;
import org.junit.Test;

public class DifferTest {

	@Test
	public void test() {
		DiffResult<Integer> result = Differ.diff(Arrays.asList(1, 2, 3, 3), Arrays.asList(1, 3, 4));
		assertThat(result.getCommon()).hasSize(2);
		assertThat(result.getCommon()).contains(1, 3);
		assertThat(result.getOnlyA()).hasSize(1);
		assertThat(result.getOnlyA()).contains(2);
		assertThat(result.getOnlyB()).hasSize(1);
		assertThat(result.getOnlyB()).contains(4);
	}

	@Test
	public void testIsSame() {
		assertThat(Differ.diff(Arrays.asList(1, 2, 3), Arrays.asList(1, 2, 3)).isSame()).isTrue();
		assertThat(Differ.diff(Arrays.asList(1, 2, 3), Arrays.asList(1, 3)).isSame()).isFalse();
	}

}