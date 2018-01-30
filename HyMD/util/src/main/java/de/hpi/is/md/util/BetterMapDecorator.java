package de.hpi.is.md.util;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class BetterMapDecorator<K, V> implements BetterMap<K, V> {

	private static final long serialVersionUID = -7785903165560099613L;
	@NonNull
	protected final Map<K, V> map;

	@Override
	public void forEach(BiConsumer<K, V> action) {
		map.forEach(action);
	}

	@Override
	public Optional<V> get(K key) {
		V value = map.get(key);
		return Optional.ofNullable(value);
	}
}
