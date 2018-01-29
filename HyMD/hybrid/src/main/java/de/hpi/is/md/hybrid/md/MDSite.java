package de.hpi.is.md.hybrid.md;

import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.OptionalDouble;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;

public interface MDSite extends Iterable<MDElement>, Cloneable {

	int cardinality();

	void clear(int attr);

	MDSite clone();

	OptionalDouble get(int i);

	default double getOrDefault(int i) {
		return get(i).orElse(SimilarityMeasure.MIN_SIMILARITY);
	}

	default boolean isNotEmpty() {
		return cardinality() > 0;
	}

	default boolean isSet(int attr) {
		return get(attr).isPresent();
	}

	@Override
	default Iterator<MDElement> iterator() {
		return new MDSiteIterator(this);
	}

	@Override
	default Spliterator<MDElement> spliterator() {
		return Spliterators.spliterator(iterator(), size(), 0);
	}

	Optional<MDElement> nextElement(int start);

	MDSite set(int attr, double threshold);

	int size();

}
