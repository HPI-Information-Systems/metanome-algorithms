package de.hpi.is.md.util;

import java.util.Collection;
import java.util.Iterator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Trimmer {

	private final int size;

	public void trim(Collection<?> values) {
		double oldSize = values.size();
		if (oldSize <= size) {
			return;
		}
		double increment = size / oldSize;
		with(increment).trim(values);
	}

	private WithIncrement with(double increment) {
		return new WithIncrement(increment);
	}

	@RequiredArgsConstructor
	private class WithIncrement {

		private final double increment;
		private int nextNew = 0;
		private double count = 0.0;

		private boolean hasSpaceLeft() {
			return count < size;
		}

		private boolean retain(int current) {
			return current == nextNew;
		}

		private boolean retain() {
			int current = (int) count;
			return hasSpaceLeft() && retain(current);
		}

		private void trim(Iterable<?> values) {
			Iterator<?> iterator = values.iterator();
			trim(iterator);
		}

		private void trim(Iterator<?> iterator) {
			while (iterator.hasNext()) {
				iterator.next();
				count += increment;
				if (retain()) {
					nextNew++;
				} else {
					iterator.remove();
				}
			}
		}
	}

}
