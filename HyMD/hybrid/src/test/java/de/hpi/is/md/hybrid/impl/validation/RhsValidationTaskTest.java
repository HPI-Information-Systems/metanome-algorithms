package de.hpi.is.md.hybrid.impl.validation;

import static de.hpi.is.md.util.CollectionUtils.sizeBelow;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.Rhs;
import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import de.hpi.is.md.hybrid.impl.md.MDElementImpl;
import de.hpi.is.md.util.IntArrayPair;
import it.unimi.dsi.fastutil.ints.IntLists;
import java.util.Collection;
import java.util.function.Predicate;
import org.junit.Test;
import org.mockito.Mock;

@SuppressWarnings("ProtectedField")
public abstract class RhsValidationTaskTest {

	protected double minThreshold = 0.7;
	@Mock
	protected DictionaryRecords rightRecords;
	protected Predicate<Collection<IntArrayPair>> shouldUpdate = sizeBelow(2);
	@Mock
	private PreprocessedColumnPair columnPair;

	@Test
	public void testBelowLowerBound() {
		int[] rec = {0, 0, 0, 0};
		int[] rec0 = {1, 1, 1, 1};
		when(rightRecords.get(0)).thenReturn(rec0);
		when(columnPair.getRightValue(rec0)).thenReturn(1);
		when(columnPair.getLeftValue(rec)).thenReturn(0);
		doReturn(0.7).when(columnPair).getSimilarity(0, 1);
		Rhs rhs = Rhs.builder()
			.lowerBound(0.8)
			.rhsAttr(2)
			.threshold(1.0)
			.build();
		RhsValidationTask task = create(rhs, columnPair);
		task.validate(rec, IntLists.singleton(0));
		RhsResult result = task.createResult();
		assertThat(result.isValidAndMinimal()).isFalse();
		assertThat(result.getActual()).isEqualTo(new MDElementImpl(2, 0.7));
		assertThat(result.getOriginal()).isEqualTo(new MDElementImpl(2, 1.0));
		assertThat(result.getViolations()).hasSize(1);
		assertThat(result.getViolations()).contains(new IntArrayPair(rec, rec0));
	}

	@Test
	public void testBelowMinThreshold() {
		int[] rec = {0, 0, 0, 0};
		int[] rec0 = {1, 1, 1, 1};
		when(rightRecords.get(0)).thenReturn(rec0);
		when(columnPair.getRightValue(rec0)).thenReturn(1);
		when(columnPair.getLeftValue(rec)).thenReturn(0);
		doReturn(0.6).when(columnPair).getSimilarity(0, 1);
		Rhs rhs = Rhs.builder()
			.lowerBound(0.0)
			.rhsAttr(2)
			.threshold(1.0)
			.build();
		RhsValidationTask task = create(rhs, columnPair);
		task.validate(rec, IntLists.singleton(0));
		RhsResult result = task.createResult();
		assertThat(result.isValidAndMinimal()).isFalse();
		assertThat(result.getActual()).isEqualTo(new MDElementImpl(2, 0.6));
		assertThat(result.getOriginal()).isEqualTo(new MDElementImpl(2, 1.0));
		assertThat(result.getViolations()).hasSize(1);
		assertThat(result.getViolations()).contains(new IntArrayPair(rec, rec0));
	}

	@Test
	public void testShouldUpdate() {
		int[] rec = {0, 0, 0, 0};
		int[] rec0 = {1, 1, 1, 1};
		int[] rec1 = {1, 1, 2, 1};
		doReturn(rec0).when(rightRecords).get(0);
		doReturn(rec1).when(rightRecords).get(1);
		doReturn(1).when(columnPair).getRightValue(rec0);
		doReturn(2).when(columnPair).getRightValue(rec1);
		when(columnPair.getLeftValue(rec)).thenReturn(0);
		doReturn(0.6).when(columnPair).getSimilarity(0, 1);
		doReturn(0.5).when(columnPair).getSimilarity(0, 2);
		Rhs rhs = Rhs.builder()
			.lowerBound(0.0)
			.rhsAttr(2)
			.threshold(1.0)
			.build();
		RhsValidationTask task = create(rhs, columnPair);
		task.validate(rec, IntLists.singleton(0));
		RhsResult result = task.createResult();
		assertThat(result.isValidAndMinimal()).isFalse();
		assertThat(result.getActual()).isEqualTo(new MDElementImpl(2, 0.6));
		assertThat(result.getOriginal()).isEqualTo(new MDElementImpl(2, 1.0));
		assertThat(result.getViolations()).hasSize(1);
		assertThat(result.getViolations()).contains(new IntArrayPair(rec, rec0));
		assertThat(task.shouldUpdate()).isTrue();
		task.validate(rec, IntLists.singleton(1));
		result = task.createResult();
		assertThat(result.isValidAndMinimal()).isFalse();
		assertThat(result.getActual()).isEqualTo(new MDElementImpl(2, 0.5));
		assertThat(result.getOriginal()).isEqualTo(new MDElementImpl(2, 1.0));
		assertThat(result.getViolations()).hasSize(2);
		assertThat(result.getViolations())
			.contains(new IntArrayPair(rec, rec0), new IntArrayPair(rec, rec1));
		assertThat(task.shouldUpdate()).isFalse();
		task.validate(rec, IntLists.singleton(2));
		result = task.createResult();
		assertThat(result.isValidAndMinimal()).isFalse();
		assertThat(result.getActual()).isEqualTo(new MDElementImpl(2, 0.5));
		assertThat(result.getOriginal()).isEqualTo(new MDElementImpl(2, 1.0));
		assertThat(result.getViolations()).hasSize(2);
		assertThat(result.getViolations())
			.contains(new IntArrayPair(rec, rec0), new IntArrayPair(rec, rec1));
		assertThat(task.shouldUpdate()).isFalse();
	}

	@Test
	public void testValid() {
		int[] rec = {0, 0, 0, 0};
		int[] rec0 = {1, 1, 1, 1};
		when(rightRecords.get(0)).thenReturn(rec0);
		when(columnPair.getRightValue(rec0)).thenReturn(1);
		when(columnPair.getLeftValue(rec)).thenReturn(0);
		doReturn(0.7).when(columnPair).getSimilarity(0, 1);
		Rhs rhs = Rhs.builder()
			.lowerBound(0.0)
			.rhsAttr(2)
			.threshold(1.0)
			.build();
		RhsValidationTask task = create(rhs, columnPair);
		task.validate(rec, IntLists.singleton(0));
		RhsResult result = task.createResult();
		assertThat(result.isValidAndMinimal()).isTrue();
		assertThat(result.getActual()).isEqualTo(new MDElementImpl(2, 0.7));
		assertThat(result.getOriginal()).isEqualTo(new MDElementImpl(2, 1.0));
		assertThat(result.getViolations()).isEmpty();
	}

	protected abstract RhsValidationTask create(Rhs rhs, PreprocessedColumnPair columnPair);
}
