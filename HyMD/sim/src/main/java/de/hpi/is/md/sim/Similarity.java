package de.hpi.is.md.sim;

import java.util.Collection;
import lombok.Data;

@Data
public class Similarity<T> {

	private final T left;
	private final Collection<To<T>> similarities;

	@Data
	public static class To<T> {

		private final T right;
		private final double similarity;

	}

}
