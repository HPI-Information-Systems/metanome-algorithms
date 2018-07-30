package de.hpi.is.md.hybrid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import de.hpi.is.md.hybrid.PositionListIndex.Cluster;
import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import de.hpi.is.md.hybrid.impl.md.MDElementImpl;
import de.hpi.is.md.hybrid.impl.md.MDSiteImpl;
import de.hpi.is.md.hybrid.impl.preprocessed.MapDictionaryRecords;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.IntArrayPair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public abstract class ValidatorTest {

	@Mock
	private DictionaryRecords right;
	@Mock
	private PreprocessedColumnPair column0;
	@Mock
	private PreprocessedColumnPair column1;
	@Mock
	private PreprocessedColumnPair column2;
	@Mock
	private PreprocessedColumnPair column3;
	@Mock
	private PositionListIndex pli0;

	@Test
	public void test() {
		MDSite lhs = new MDSiteImpl(4)
			.set(0, 0.6);
		doReturn(new IntOpenHashSet(Arrays.asList(0, 1))).when(column0)
			.getAllSimilarRightRecords(0, 0.6);
		doReturn(IntSets.singleton(2)).when(column0)
			.getAllSimilarRightRecords(1, 0.6);
		doReturn(0).when(column2).getLeftValue(new int[]{0, 0, 0, 0});
		doReturn(1).when(column2).getLeftValue(new int[]{1, 0, 1, 0});
		doReturn(0).when(column2).getRightValue(new int[]{0, 0, 0, 0});
		doReturn(1).when(column2).getRightValue(new int[]{0, 0, 1, 0});
		doReturn(2).when(column2).getRightValue(new int[]{0, 0, 2, 0});
		doReturn(1.0).when(column2).getSimilarity(0, 0);
		doReturn(0.1).when(column2).getSimilarity(0, 1);
		doReturn(0.5).when(column2).getSimilarity(1, 2);
		doReturn(new int[]{0, 0, 0, 0}).when(right).get(0);
		doReturn(new int[]{0, 0, 1, 0}).when(right).get(1);
		doReturn(new int[]{0, 0, 2, 0}).when(right).get(2);
		when(column0.getLeftPli()).thenReturn(pli0);
		Collection<Cluster> clusters = Arrays.asList(
			new Cluster(0, IntSets.singleton(0)),
			new Cluster(1, IntSets.singleton(1)));
		when(pli0.iterator()).thenReturn(clusters.iterator());
		Validator validator = createValidator();
		Collection<Rhs> rhs = Collections.singletonList(
			Rhs.builder().rhsAttr(2).threshold(1.0).lowerBound(0.0).build());
		ValidationResult results = validator.validate(lhs, rhs);
		RhsResult v2 = Iterables.get(results.getRhsResults(), 0);
		assertThat(results.getLhsResult().getLhs()).isEqualTo(lhs);
		assertThat(v2.getActual()).isEqualTo(new MDElementImpl(2, 0.1));
		assertThat(v2.getOriginal()).isEqualTo(new MDElementImpl(2, 1.0));
		assertThat(v2.getViolations()).isEmpty();
		assertThat(results.getLhsResult().getSupport()).isEqualTo(3L);
	}

	@Test
	public void testBelowLowerBound() {
		MDSite lhs = new MDSiteImpl(4)
			.set(0, 0.6);
		doReturn(new IntOpenHashSet(Arrays.asList(0, 1))).when(column0)
			.getAllSimilarRightRecords(0, 0.6);
		doReturn(IntSets.singleton(2)).when(column0)
			.getAllSimilarRightRecords(1, 0.6);
		doReturn(0).when(column2).getLeftValue(new int[]{0, 0, 0, 0});
		doReturn(0).when(column2).getRightValue(new int[]{0, 0, 0, 0});
		doReturn(1).when(column2).getRightValue(new int[]{0, 0, 1, 0});
		doReturn(1.0).when(column2).getSimilarity(0, 0);
		doReturn(0.7).when(column2).getSimilarity(0, 1);
		doReturn(new int[]{0, 0, 0, 0}).when(right).get(0);
		doReturn(new int[]{0, 0, 1, 0}).when(right).get(1);
		doReturn(new int[]{0, 0, 2, 0}).when(right).get(2);
		when(column0.getLeftPli()).thenReturn(pli0);
		Collection<Cluster> clusters = Arrays.asList(
			new Cluster(0, IntSets.singleton(0)),
			new Cluster(1, IntSets.singleton(1)));
		when(pli0.iterator()).thenReturn(clusters.iterator());
		Validator validator = createValidator();
		Collection<Rhs> rhs = Collections
			.singletonList(Rhs.builder().rhsAttr(2).threshold(1.0).lowerBound(0.7).build());
		ValidationResult results = validator.validate(lhs, rhs);
		RhsResult v2 = Iterables.get(results.getRhsResults(), 0);
		assertThat(results.getLhsResult().getLhs()).isEqualTo(lhs);
		assertThat(v2.getActual().getId()).isEqualTo(2);
		assertThat(v2.getActual().getThreshold()).isLessThanOrEqualTo(0.7);
		assertThat(v2.getOriginal()).isEqualTo(new MDElementImpl(2, 1.0));
		assertThat(v2.isValidAndMinimal()).isFalse();
		Assert.assertThat(v2.getViolations(), hasSize(greaterThanOrEqualTo(1)));
		assertThat(v2.getViolations())
			.contains(new IntArrayPair(new int[]{0, 0, 0, 0}, new int[]{0, 0, 1, 0}));
		assertThat(results.getLhsResult().getSupport()).isEqualTo(3L);
	}

	@Test
	public void testBelowMinThreshold() {
		MDSite lhs = new MDSiteImpl(4)
			.set(0, 0.6);
		doReturn(new IntOpenHashSet(Arrays.asList(0, 1))).when(column0)
			.getAllSimilarRightRecords(0, 0.6);
		doReturn(new IntOpenHashSet(Collections.singletonList(2))).when(column0)
			.getAllSimilarRightRecords(1, 0.6);
		doReturn(0).when(column2).getLeftValue(new int[]{0, 0, 0, 0});
		doReturn(1).when(column2).getLeftValue(new int[]{1, 0, 1, 0});
		doReturn(0).when(column2).getRightValue(new int[]{0, 0, 0, 0});
		doReturn(1).when(column2).getRightValue(new int[]{0, 0, 1, 0});
		doReturn(2).when(column2).getRightValue(new int[]{0, 0, 2, 0});
		doReturn(1.0).when(column2).getSimilarity(0, 0);
		doReturn(0.7).when(column2).getSimilarity(0, 1);
		doReturn(0.7).when(column2).getSimilarity(1, 2);
		doReturn(new int[]{0, 0, 0, 0}).when(right).get(0);
		doReturn(new int[]{0, 0, 1, 0}).when(right).get(1);
		doReturn(new int[]{0, 0, 2, 0}).when(right).get(2);
		when(column0.getLeftPli()).thenReturn(pli0);
		Collection<Cluster> clusters = Arrays.asList(
			new Cluster(0, IntSets.singleton(0)),
			new Cluster(1, IntSets.singleton(1)));
		when(pli0.iterator()).thenReturn(clusters.iterator());
		Validator validator = createValidator(0.8);
		Collection<Rhs> rhs = Collections
			.singletonList(Rhs.builder().rhsAttr(2).threshold(1.0).lowerBound(0.0).build());
		ValidationResult results = validator.validate(lhs, rhs);
		RhsResult v2 = Iterables.get(results.getRhsResults(), 0);
		assertThat(results.getLhsResult().getLhs()).isEqualTo(lhs);
		assertThat(v2.getActual()).isEqualTo(new MDElementImpl(2, 0.7));
		assertThat(v2.getOriginal()).isEqualTo(new MDElementImpl(2, 1.0));
		Assert.assertThat(v2.getViolations(), hasSize(greaterThanOrEqualTo(1)));
		Assert.assertThat(v2.getViolations(),
			either(hasItem(new IntArrayPair(new int[]{0, 0, 0, 0}, new int[]{0, 0, 1, 0})))
				.or(hasItem(new IntArrayPair(new int[]{1, 0, 1, 0}, new int[]{0, 0, 2, 0}))));
		assertThat(results.getLhsResult().getSupport()).isEqualTo(3L);
	}

	@Test
	public void testEmptyLhs() {
		MDSite lhs = new MDSiteImpl(4);
		when(right.getAll()).thenReturn(new IntOpenHashSet(Arrays.asList(0, 1, 2)));
		when(column2.getMinSimilarity()).thenReturn(0.1);
		Validator validator = createValidator();
		Collection<Rhs> rhs = Collections
			.singletonList(Rhs.builder().rhsAttr(2).threshold(1.0).lowerBound(0.0).build());
		ValidationResult results = validator.validate(lhs, rhs);
		RhsResult v2 = Iterables.get(results.getRhsResults(), 0);
		assertThat(results.getLhsResult().getSupport()).isEqualTo(6L);
		assertThat(v2.getActual()).isEqualTo(new MDElementImpl(2, 0.1));
		assertThat(v2.getOriginal()).isEqualTo(new MDElementImpl(2, 1.0));
		assertThat(v2.getViolations()).isEmpty();
	}

	@Test
	public void testLargerLhs() {
		MDSite lhs = new MDSiteImpl(4)
			.set(0, 0.6)
			.set(1, 0.6);
		doReturn(0).when(column1).getLeftValue(new int[]{0, 0, 0, 0});
		doReturn(0).when(column1).getLeftValue(new int[]{1, 0, 1, 0});
		doReturn(new IntOpenHashSet(Arrays.asList(0, 1))).when(column0)
			.getAllSimilarRightRecords(0, 0.6);
		doReturn(IntSets.singleton(2)).when(column0)
			.getAllSimilarRightRecords(1, 0.6);
		doReturn(IntSets.singleton(0)).when(column1)
			.getAllSimilarRightRecords(0, 0.6);
		doReturn(1).when(column2).getLeftValue(new int[]{1, 0, 1, 0});
		doReturn(0).when(column2).getLeftValue(new int[]{0, 0, 0, 0});
		doReturn(0).when(column2).getRightValue(new int[]{0, 0, 0, 0});
		doReturn(1.0).when(column2).getSimilarity(0, 0);
		doReturn(new int[]{0, 0, 0, 0}).when(right).get(0);
		when(column0.getLeftPli()).thenReturn(pli0);
		Collection<Cluster> clusters = Arrays.asList(
			new Cluster(0, IntSets.singleton(0)),
			new Cluster(1, IntSets.singleton(1)));
		when(pli0.spliterator()).thenReturn(clusters.spliterator());
		Validator validator = createValidator();
		Collection<Rhs> rhs = Collections
			.singletonList(Rhs.builder().rhsAttr(2).threshold(1.0).lowerBound(0.0).build());
		ValidationResult results = validator.validate(lhs, rhs);
		RhsResult v2 = Iterables.get(results.getRhsResults(), 0);
		assertThat(results.getLhsResult().getSupport()).isEqualTo(1L);
		assertThat(v2.getActual()).isEqualTo(new MDElementImpl(2, 1.0));
		assertThat(v2.getOriginal()).isEqualTo(new MDElementImpl(2, 1.0));
		assertThat(v2.getViolations()).isEmpty();
	}

	@Test
	public void testLargerLhsEmpty() {
		MDSite lhs = new MDSiteImpl(4)
			.set(0, 0.7)
			.set(1, 0.6);
		doReturn(0).when(column1).getLeftValue(new int[]{0, 0, 0, 0});
		doReturn(0).when(column1).getLeftValue(new int[]{1, 0, 1, 0});
		doReturn(IntSets.EMPTY_SET).when(column0).getAllSimilarRightRecords(0, 0.7);
		doReturn(IntSets.EMPTY_SET).when(column0).getAllSimilarRightRecords(1, 0.7);
		when(column0.getLeftPli()).thenReturn(pli0);
		Collection<Cluster> clusters = Arrays.asList(
			new Cluster(0, IntSets.singleton(0)),
			new Cluster(1, IntSets.singleton(1)));
		when(pli0.spliterator()).thenReturn(clusters.spliterator());
		Validator validator = createValidator();
		Collection<Rhs> rhs = Collections
			.singletonList(Rhs.builder().rhsAttr(2).threshold(1.0).lowerBound(0.0).build());
		ValidationResult results = validator.validate(lhs, rhs);
		RhsResult v2 = Iterables.get(results.getRhsResults(), 0);
		assertThat(results.getLhsResult().getSupport()).isEqualTo(0L);
		assertThat(v2.getActual()).isEqualTo(new MDElementImpl(2, 1.0));
		assertThat(v2.getOriginal()).isEqualTo(new MDElementImpl(2, 1.0));
		assertThat(v2.getViolations()).isEmpty();
	}

	@Test
	public void testNumberOfValidations() {
		MDSite lhs = new MDSiteImpl(4)
			.set(0, 0.6);
		doReturn(new IntOpenHashSet(Arrays.asList(0, 1))).when(column0)
			.getAllSimilarRightRecords(0, 0.6);
		doReturn(IntSets.singleton(2)).when(column0)
			.getAllSimilarRightRecords(1, 0.6);
		doReturn(0).when(column2).getLeftValue(new int[]{0, 0, 0, 0});
		doReturn(1).when(column2).getLeftValue(new int[]{1, 0, 1, 0});
		doReturn(0).when(column2).getRightValue(new int[]{0, 0, 0, 0});
		doReturn(1).when(column2).getRightValue(new int[]{0, 0, 1, 0});
		doReturn(2).when(column2).getRightValue(new int[]{0, 0, 2, 0});
		doReturn(1.0).when(column2).getSimilarity(0, 0);
		doReturn(1.0).when(column2).getSimilarity(0, 1);
		doReturn(1.0).when(column2).getSimilarity(1, 2);
		doReturn(new int[]{0, 0, 0, 0}).when(right).get(0);
		doReturn(new int[]{0, 0, 1, 0}).when(right).get(1);
		doReturn(new int[]{0, 0, 2, 0}).when(right).get(2);
		when(column0.getLeftPli()).thenReturn(pli0);
		Collection<Cluster> clusters = Arrays.asList(
			new Cluster(0, IntSets.singleton(0)),
			new Cluster(1, IntSets.singleton(1)));
		when(pli0.iterator()).thenReturn(clusters.iterator());
		Validator validator = createValidator();
		Collection<Rhs> rhs = Arrays.asList(
			Rhs.builder().rhsAttr(2).threshold(1.0).lowerBound(0.0).build(),
			Rhs.builder().rhsAttr(3).threshold(1.0).lowerBound(0.0).build());
		ValidationResult results = validator.validate(lhs, rhs);
		assertThat(results.getRhsResults()).hasSize(2);
	}

	protected abstract Validator createValidator(List<PreprocessedColumnPair> columnPairs,
		DictionaryRecords left, DictionaryRecords right, double minThreshold);

	private Validator createValidator() {
		return createValidator(0.0);
	}

	private Validator createValidator(double minThreshold) {
		List<PreprocessedColumnPair> columnPairs = Arrays
			.asList(column0, column1, column2, column3);
		DictionaryRecords left = MapDictionaryRecords.builder()
			.add(0, new int[]{0, 0, 0, 0})
			.add(1, new int[]{1, 0, 1, 0})
			.build();
		return createValidator(columnPairs, left, right, minThreshold);
	}
}
