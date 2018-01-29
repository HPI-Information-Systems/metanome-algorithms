package de.hpi.is.md.util;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.BiConsumer;

public interface BetterMap<K, V> extends Serializable {

	void forEach(BiConsumer<K, V> action);

	Optional<V> get(K key);
}
