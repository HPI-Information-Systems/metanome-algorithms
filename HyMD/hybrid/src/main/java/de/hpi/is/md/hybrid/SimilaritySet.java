package de.hpi.is.md.hybrid;

import de.hpi.is.md.hybrid.md.MDElement;
import java.util.Arrays;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode
public class SimilaritySet {

	@NonNull
	private final double[] similaritySet;

	public double get(int attr) {
		return similaritySet[attr];
	}

	public boolean isViolated(MDElement element) {
		int attr = element.getId();
		return element.getThreshold() > similaritySet[attr];
	}

	public int size() {
		return similaritySet.length;
	}

	@Override
	public String toString() {
		return Arrays.toString(similaritySet);
	}
}
