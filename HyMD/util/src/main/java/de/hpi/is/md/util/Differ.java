package de.hpi.is.md.util;

import java.util.Collection;
import java.util.HashSet;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Differ<T> {

	@NonNull
	private final Collection<T> a;
	@NonNull
	private final Collection<T> b;

	public static <T> DiffResult<T> diff(Collection<T> a, Collection<T> b) {
		return new Differ<>(a, b).diff();
	}

	private static <T> Collection<T> only(Collection<T> a, Collection<T> b) {
		Collection<T> onlyA = new HashSet<>(a);
		onlyA.removeAll(b);
		return onlyA;
	}

	private Collection<T> common() {
		Collection<T> shared = new HashSet<>(a);
		shared.retainAll(b);
		return shared;
	}

	private DiffResult<T> diff() {
		Collection<T> onlyA = onlyA();
		Collection<T> onlyB = onlyB();
		Collection<T> common = common();
		return DiffResult.<T>builder()
			.onlyA(onlyA)
			.onlyB(onlyB)
			.common(common)
			.build();
	}

	private Collection<T> onlyA() {
		return only(a, b);
	}

	private Collection<T> onlyB() {
		return only(b, a);
	}

	@Data
	@Builder
	public static class DiffResult<T> {

		@NonNull
		private final Collection<T> onlyA;
		@NonNull
		private final Collection<T> onlyB;
		@NonNull
		private final Collection<T> common;

		public boolean isSame() {
			return onlyA.isEmpty() && onlyB.isEmpty();
		}
	}

}
