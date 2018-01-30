package de.hpi.is.md.util;

import static com.google.common.base.Preconditions.checkElementIndex;

import java.util.Optional;
import java.util.function.BiConsumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LazyArray<T> implements LazyMap<Integer, T> {

	private static final long serialVersionUID = -5993169624858742922L;
	@NonNull
	private final T[] elements;
	@NonNull
	private final BetterSupplier<T> factory;

	public void forEach(IntObjectBiConsumer<T> action) {
		for (int i = 0; i < size(); i++) {
			with(i).accept(action);
		}
	}

	@Deprecated
	@Override
	public void forEach(BiConsumer<Integer, T> action) {
		IntObjectBiConsumer<T> consumer = action::accept;
		forEach(consumer);
	}

	@Deprecated
	@Override
	public Optional<T> get(Integer key) {
		int k = key;
		return get(k);
	}

	public Optional<T> get(int id) {
		return with(id).get();
	}

	@Deprecated
	@Override
	public T getOrCreate(Integer key) {
		int k = key;
		return getOrCreate(k);
	}

	public T getOrCreate(int id) {
		return with(id).getOrCreate();
	}

	public int size() {
		return elements.length;
	}

	private WithId with(int i) {
		return new WithId(i);
	}

	@RequiredArgsConstructor
	private class WithId {

		private final int id;

		private void accept(IntObjectBiConsumer<T> action) {
			get().ifPresent(element -> action.accept(id, element));
		}

		private T createElement() {
			checkElementIndex(id, size());
			T element = factory.get();
			elements[id] = element;
			return element;
		}

		private Optional<T> get() {
			checkElementIndex(id, size());
			T element = elements[id];
			return Optional.ofNullable(element);
		}

		private T getOrCreate() {
			return get().orElseGet(this::createElement);
		}
	}
}
