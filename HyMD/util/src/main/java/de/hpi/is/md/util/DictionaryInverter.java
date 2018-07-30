package de.hpi.is.md.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DictionaryInverter {

	public static <T> Int2ObjectMap<T> invert(Dictionary<T> dictionary) {
		InversionTask<T> task = createTask(dictionary);
		return task.invert(task);
	}

	private static <T> InversionTask<T> createTask(Dictionary<T> dictionary) {
		Collection<T> values = dictionary.values();
		int size = values.size();
		Int2ObjectMap<T> inverted = new Int2ObjectOpenHashMap<>(size);
		return new InversionTask<>(inverted, dictionary);
	}

	@RequiredArgsConstructor
	private static class InversionTask<T> {

		private final Int2ObjectMap<T> inverted;
		private final Dictionary<T> dictionary;

		private void consume(T value) {
			int id = dictionary.getOrAdd(value);
			inverted.put(id, value);
		}

		private Int2ObjectMap<T> invert(InversionTask<T> task) {
			Collection<T> values = dictionary.values();
			values.forEach(task::consume);
			return inverted;
		}
	}

}
