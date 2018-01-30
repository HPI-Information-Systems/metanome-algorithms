package de.hpi.is.md.hybrid;

import static org.assertj.core.api.Assertions.assertThat;

import de.hpi.is.md.hybrid.impl.md.MDElementImpl;
import org.junit.Test;

public class SimilaritySetTest {

	@Test
	public void testGet() {
		SimilaritySet similaritySet = new SimilaritySet(new double[]{0.2, 0.3});
		assertThat(similaritySet.get(0)).isEqualTo(0.2);
		assertThat(similaritySet.get(1)).isEqualTo(0.3);
	}

	@Test
	public void testIsViolated() {
		SimilaritySet similaritySet = new SimilaritySet(new double[]{0.2, 0.3});
		assertThat(similaritySet.isViolated(new MDElementImpl(0, 0.1))).isFalse();
		assertThat(similaritySet.isViolated(new MDElementImpl(0, 0.2))).isFalse();
		assertThat(similaritySet.isViolated(new MDElementImpl(0, 0.3))).isTrue();
	}

}