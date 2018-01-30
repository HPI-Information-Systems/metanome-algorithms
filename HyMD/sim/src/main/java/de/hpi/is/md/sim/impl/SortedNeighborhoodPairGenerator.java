package de.hpi.is.md.sim.impl;

import com.bakdata.util.jackson.CPSType;
import com.google.common.collect.EvictingQueue;
import de.hpi.is.md.sim.PairGenerator;
import de.hpi.is.md.util.Hasher;
import de.hpi.is.md.util.StreamUtils;
import de.hpi.is.md.util.ValueWrapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

@CPSType(id = "sn", base = PairGenerator.class)
@Slf4j
@RequiredArgsConstructor
@EqualsAndHashCode
public class SortedNeighborhoodPairGenerator<T> implements PairGenerator<T> {

	private static final long serialVersionUID = -1813245693350420477L;
	@NonNull
	private final Comparator<T> comparator;
	private final int windowSize;

	@Override
	public Result<T> generate(Collection<T> left, Collection<T> right) {
		int expectedComputations = left.size() * Math.min(windowSize, right.size());
		log.info("Will compute approximately {} similarities", expectedComputations);
		Collection<T> sortedLeft = sort(left);
		Collection<T> sortedRight = sort(right);
		Stream<Tuple2<T, Collection<T>>> pairs = new Task(sortedLeft, sortedRight).generate();
		boolean complete = right.size() <= windowSize;
		return new Result<>(pairs, complete);
	}

	@Override
	public void hash(Hasher hasher) {
		hasher
			.putClass(SortedNeighborhoodPairGenerator.class)
			.putInt(windowSize);
	}

	private Collection<T> sort(Collection<T> collection) {
		List<T> list = new ArrayList<>(collection);
		list.sort(comparator);
		return list;
	}

	private final class Task {

		private final Collection<T> left;
		private final Iterator<T> right;
		private final Queue<ValueWrapper<T>> upperWindow = EvictingQueue
			.create(halfWindow(RoundingMode.DOWN));
		private final EvictingQueue<ValueWrapper<T>> lowerWindow = EvictingQueue
			.create(halfWindow(RoundingMode.UP));
		private ValueWrapper<T> current;

		private Task(Collection<T> left, Iterable<T> right) {
			this.left = left;
			this.right = right.iterator();
		}

		private void doShift(T leftValue) {
			while (needsShift(leftValue)) {
				shift();
			}
		}

		private Stream<Tuple2<T, Collection<T>>> generate() {
			initialize();
			return StreamUtils.seq(left)
				.map(this::shiftAndGenerate);
		}

		private Tuple2<T, Collection<T>> generatePairs(T leftValue) {
			Collection<T> window = StreamUtils.seq(upperWindow)
				.concat(getCurrent())
				.concat(lowerWindow)
				.map(ValueWrapper::getValue)
				.toList();
			return Tuple.tuple(leftValue, window);
		}

		private Optional<ValueWrapper<T>> getCurrent() {
			return Optional.ofNullable(current);
		}

		private int halfWindow(RoundingMode roundingMode) {
			return BigDecimal.valueOf(windowSize - 1)
				.divide(BigDecimal.valueOf(2), roundingMode)
				.intValue();
		}

		private void initialize() {
			if (right.hasNext()) {
				current = new ValueWrapper<>(right.next());
			}
			while (lowerWindow.remainingCapacity() > 0 && right.hasNext()) {
				lowerWindow.add(new ValueWrapper<>(right.next()));
			}
		}

		private boolean needsShift(T leftValue) {
			return getCurrent()
				.map(c -> comparator.compare(c.getValue(), leftValue) < 0)
				.orElse(false);
		}

		private void shift() {
			ValueWrapper<T> next = right.hasNext() ? new ValueWrapper<>(right.next()) : null;
			shift(next);
		}

		private void shift(ValueWrapper<T> next) {
			getCurrent()
				.ifPresent(upperWindow::add);
			current = lowerWindow.poll();
			Optional.ofNullable(next)
				.ifPresent(lowerWindow::add);
		}

		private Tuple2<T, Collection<T>> shiftAndGenerate(T leftValue) {
			doShift(leftValue);
			return generatePairs(leftValue);
		}
	}
}
