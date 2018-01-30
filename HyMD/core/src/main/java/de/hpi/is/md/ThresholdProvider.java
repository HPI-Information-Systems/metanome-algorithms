package de.hpi.is.md;

import de.hpi.is.md.util.OptionalDouble;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;
import java.util.List;

public interface ThresholdProvider {

	List<DoubleSortedSet> getAll();

	OptionalDouble getNext(int attr, double threshold);

}
