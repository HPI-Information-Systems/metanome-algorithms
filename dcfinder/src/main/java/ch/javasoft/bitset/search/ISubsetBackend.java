package ch.javasoft.bitset.search;

import java.util.Set;
import java.util.function.Consumer;

import ch.javasoft.bitset.IBitSet;

public interface ISubsetBackend {

	boolean add(IBitSet bs);

	Set<IBitSet> getAndRemoveGeneralizations(IBitSet invalidFD);

	boolean containsSubset(IBitSet add);

	void forEach(Consumer<IBitSet> consumer);

}
