package de.hpi.is.md.hybrid.impl.sampling;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.Sampler;
import de.hpi.is.md.hybrid.SimilaritySet;
import de.hpi.is.md.util.IntArrayPair;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SamplerImplTest {

	@Mock
	private PreprocessedColumnPair col2;
	@Mock
	private PreprocessedColumnPair col1;
	@Mock
	private Iterable<int[]> leftRecords;

	@Test
	public void test() {
		Collection<int[]> left = ImmutableList.<int[]>builder()
			.add(new int[]{0, 0})
			.build();
		when(leftRecords.iterator()).thenReturn(left.iterator());
		Sampler sampler = createSampler();
		doReturn(1.0).when(col1).getSimilarity(new int[]{0, 0}, new int[]{0, 0});
		doReturn(0.0).when(col1).getSimilarity(new int[]{0, 0}, new int[]{1, 1});
		doReturn(0.5).when(col2).getSimilarity(new int[]{0, 0}, new int[]{0, 0});
		doReturn(0.3).when(col2).getSimilarity(new int[]{0, 0}, new int[]{1, 1});
		Optional<Set<SimilaritySet>> similaritySets = sampler.sample();
		Assert.assertThat(similaritySets, isPresentAnd(hasSize(2)));
		Assert.assertThat(similaritySets,
			isPresentAnd(hasItem(new SimilaritySet(new double[]{1.0, 0.5}))));
		Assert.assertThat(similaritySets,
			isPresentAnd(hasItem(new SimilaritySet(new double[]{0.0, 0.3}))));
	}

	@Test
	public void testDuplicateRecommendations() {
		when(leftRecords.iterator()).thenReturn(Collections.emptyIterator());
		Sampler sampler = createSampler();
		doReturn(1.0).when(col1).getSimilarity(new int[]{0, 0}, new int[]{0, 0});
		doReturn(0.5).when(col2).getSimilarity(new int[]{0, 0}, new int[]{0, 0});
		Collection<SimilaritySet> similaritySets = sampler
			.processRecommendations(
				Arrays.asList(new IntArrayPair(new int[]{0, 0}, new int[]{0, 0}),
					new IntArrayPair(new int[]{0, 0}, new int[]{0, 0})));
		assertThat(similaritySets).hasSize(1);
		assertThat(similaritySets).contains(new SimilaritySet(new double[]{1.0, 0.5}));
	}

	@Test
	public void testMultiple() {
		Collection<int[]> left = ImmutableList.<int[]>builder()
			.add(new int[]{0, 0})
			.build();
		when(leftRecords.iterator()).thenReturn(left.iterator());
		Sampler sampler = createSampler();
		doReturn(1.0).when(col1).getSimilarity(new int[]{0, 0}, new int[]{0, 0});
		doReturn(0.0).when(col1).getSimilarity(new int[]{0, 0}, new int[]{1, 1});
		doReturn(0.5).when(col2).getSimilarity(new int[]{0, 0}, new int[]{0, 0});
		doReturn(0.3).when(col2).getSimilarity(new int[]{0, 0}, new int[]{1, 1});
		sampler.sample();
		Optional<Set<SimilaritySet>> similaritySets = sampler.sample();
		assertThat(similaritySets).isEmpty();
	}

	@Test
	public void testRecommendationsAfterSample() {
		Collection<int[]> left = ImmutableList.<int[]>builder()
			.add(new int[]{0, 0})
			.build();
		when(leftRecords.iterator()).thenReturn(left.iterator());
		Sampler sampler = createSampler();
		doReturn(1.0).when(col1).getSimilarity(new int[]{0, 0}, new int[]{0, 0});
		doReturn(0.0).when(col1).getSimilarity(new int[]{0, 0}, new int[]{1, 1});
		doReturn(0.5).when(col2).getSimilarity(new int[]{0, 0}, new int[]{0, 0});
		doReturn(0.3).when(col2).getSimilarity(new int[]{0, 0}, new int[]{1, 1});
		sampler.sample();
		Collection<SimilaritySet> similaritySets = sampler
			.processRecommendations(
				Collections.singletonList(new IntArrayPair(new int[]{0, 0}, new int[]{0, 0})));
		assertThat(similaritySets).hasSize(1);
		assertThat(similaritySets).contains(new SimilaritySet(new double[]{1.0, 0.5}));
	}

	@Test
	public void testWithRecommendations() {
		when(leftRecords.iterator()).thenReturn(Collections.emptyIterator());
		Sampler sampler = createSampler();
		doReturn(1.0).when(col1).getSimilarity(new int[]{0, 0}, new int[]{0, 0});
		doReturn(0.0).when(col1).getSimilarity(new int[]{0, 0}, new int[]{1, 1});
		doReturn(0.5).when(col2).getSimilarity(new int[]{0, 0}, new int[]{0, 0});
		doReturn(0.3).when(col2).getSimilarity(new int[]{0, 0}, new int[]{1, 1});
		Collection<SimilaritySet> similaritySets = sampler
			.processRecommendations(
				Arrays.asList(new IntArrayPair(new int[]{0, 0}, new int[]{0, 0}),
					new IntArrayPair(new int[]{0, 0}, new int[]{1, 1})));
		assertThat(similaritySets).hasSize(2);
		assertThat(similaritySets).contains(new SimilaritySet(new double[]{1.0, 0.5}));
		assertThat(similaritySets).contains(new SimilaritySet(new double[]{0.0, 0.3}));
	}

	private Sampler createSampler() {
		List<PreprocessedColumnPair> columnPairs = Arrays.asList(col1, col2);
		Collection<int[]> right = ImmutableList.<int[]>builder()
			.add(new int[]{0, 0})
			.add(new int[]{1, 1})
			.build();
		DictionaryRecords rightRecords = Mockito.mock(DictionaryRecords.class);
		when(rightRecords.spliterator()).thenReturn(right.spliterator());
		return SamplerImpl.builder()
			.columnPairs(columnPairs)
			.left(leftRecords.iterator())
			.right(rightRecords)
			.build();
	}

}