package de.hpi.is.md.impl.threshold;

import de.hpi.is.md.util.IteratorUtils;
import de.hpi.is.md.util.OptionalDouble;
import java.util.PrimitiveIterator.OfDouble;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniformTrimmer {

	private final double intervalSize;

	public void trim(OfDouble iterator) {
		OptionalDouble first = IteratorUtils.next(iterator);
		first.map(this::with)
			.ifPresent(w -> w.trim(iterator));
	}

	private WithIncrement with(double first) {
		return new WithIncrement(first);
	}

	@RequiredArgsConstructor
	private class WithIncrement {

		private final double first;
		private int count = 1;

		private void trim(OfDouble iterator) {
			while (iterator.hasNext()) {
				double current = iterator.nextDouble();
				if (current <= first - count * intervalSize) {
					double diff = first - current;
					count = Math.max(count, (int) (diff / intervalSize)) + 1;
				} else {
					iterator.remove();
				}
			}
		}
	}

}
