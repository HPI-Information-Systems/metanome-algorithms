package de.hpi.is.md.hybrid.impl.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ClassifierTest {

	@Test
	public void testLowerBound() {
		Classifier classifier = new Classifier(0.0, 0.5);
		assertThat(classifier.isValidAndMinimal(0.6)).isTrue();
		assertThat(classifier.isValidAndMinimal(0.5)).isFalse();
	}

	@Test
	public void testMinThreshold() {
		Classifier classifier = new Classifier(0.5, 0.0);
		assertThat(classifier.isValidAndMinimal(0.5)).isTrue();
		assertThat(classifier.isValidAndMinimal(0.4)).isFalse();
	}

	@Test
	public void testMinThresholdAndLowerBound() {
		Classifier classifier = new Classifier(0.5, 0.5);
		assertThat(classifier.isValidAndMinimal(0.6)).isTrue();
		assertThat(classifier.isValidAndMinimal(0.5)).isFalse();
	}

}