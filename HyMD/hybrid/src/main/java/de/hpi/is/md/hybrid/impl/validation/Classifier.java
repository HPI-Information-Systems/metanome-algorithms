package de.hpi.is.md.hybrid.impl.validation;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Classifier {

	private final double minThreshold;
	private final double lowerBound;

	public boolean isValidAndMinimal(double similarity) {
		return isValid(similarity) && isMinimal(similarity);
	}

	private boolean isMinimal(double similarity) {
		return similarity > lowerBound;
	}

	private boolean isValid(double similarity) {
		return similarity >= minThreshold;
	}

}
