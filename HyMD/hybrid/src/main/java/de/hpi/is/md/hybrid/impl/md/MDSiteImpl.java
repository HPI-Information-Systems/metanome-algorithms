package de.hpi.is.md.hybrid.impl.md;

import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.OptionalDouble;
import de.hpi.is.md.util.StreamUtils;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MDSiteImpl implements MDSite {

	private static final double MISSING = 0.0;
	private final double[] thresholds;
	private int cardinality;

	public MDSiteImpl(int columnPairs) {
		this(new double[columnPairs], 0);
	}

	private static boolean isPresent(double threshold) {
		return threshold != MISSING;
	}

	private static String toString(double threshold) {
		return isPresent(threshold) ? String.valueOf(threshold) : "";
	}

	@Override
	public int cardinality() {
		return cardinality;
	}

	@Override
	public void clear(int attr) {
		double old = thresholds[attr];
		thresholds[attr] = MISSING;
		if (isPresent(old)) {
			cardinality--;
		}
	}

	@Override
	public OptionalDouble get(int i) {
		if (i >= size()) {
			return OptionalDouble.empty();
		}
		double threshold = thresholds[i];
		return OptionalDouble.of(threshold)
			.filter(MDSiteImpl::isPresent);
	}

	@Override
	public Optional<MDElement> nextElement(int start) {
		for (int i = start; i < size(); i++) {
			OptionalDouble threshold = get(i);
			if (threshold.isPresent()) {
				int id = i;
				return threshold.map(t -> new MDElementImpl(id, t));
			}
		}
		return Optional.empty();
	}

	@Override
	public MDSite set(int attr, double threshold) {
		double old = thresholds[attr];
		thresholds[attr] = threshold;
		if (!isPresent(old)) {
			cardinality++;
		}
		return this;
	}

	@Override
	public int size() {
		return thresholds.length;
	}

	@Override
	public MDSite clone() {
		return new MDSiteImpl(thresholds.clone(), cardinality);
	}

	@Override
	public String toString() {
		return "[" + StreamUtils.seq(thresholds)
			.map(MDSiteImpl::toString)
			.toString(",") + "]";
	}
}
