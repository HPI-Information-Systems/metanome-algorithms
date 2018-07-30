package de.hpi.is.md.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import java.util.Collection;
import java.util.function.IntConsumer;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeadAndTailIntersector {

	@NonNull
	private final IntCollection head;
	@NonNull
	private final Collection<IntCollection> tail;
	@NonNull
	private final IntCollection result;

	public static HeadAndTailIntersector create(IntCollection head,
		Collection<IntCollection> tail) {
		IntArrayList result = createResult(head);
		return new HeadAndTailIntersector(head, tail, result);
	}

	private static IntArrayList createResult(Collection<?> head) {
		int size = head.size();
		return new IntArrayList(size);
	}

	public IntCollection intersect() {
		IntConsumer addIfInTail = this::addIfInTail;
		head.forEach(addIfInTail);
		return result;
	}

	private void addIfInTail(int match) {
		with(match).addIfInTail();
	}

	private WithMatch with(int match) {
		return new WithMatch(match);
	}

	@RequiredArgsConstructor
	private class WithMatch {

		private final int match;

		private void addIfInTail() {
			if (isInTail()) {
				result.add(match);
			}
		}

		private boolean contains(IntCollection cluster) {
			return cluster.contains(match);
		}

		private boolean isInTail() {
			return tail.stream().allMatch(this::contains);
		}
	}

}
