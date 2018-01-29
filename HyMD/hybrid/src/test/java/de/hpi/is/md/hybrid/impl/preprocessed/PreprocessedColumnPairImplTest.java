package de.hpi.is.md.hybrid.impl.preprocessed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import de.hpi.is.md.ColumnMapping;
import de.hpi.is.md.hybrid.PositionListIndex;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.PreprocessingColumnConfiguration;
import de.hpi.is.md.hybrid.PreprocessingConfiguration;
import de.hpi.is.md.hybrid.SimilarityIndex;
import de.hpi.is.md.hybrid.SimilarityIndex.SimilarityIndexBuilder;
import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.ColumnPair;
import de.hpi.is.md.sim.SimilarityComputer;
import de.hpi.is.md.sim.SimilarityMeasure;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class PreprocessedColumnPairImplTest {

	private static final Column<Integer> A = Column.of("a", Integer.class);
	private static final Column<Integer> B = Column.of("b", Integer.class);
	private static final Column<Integer> C = Column.of("c", Integer.class);

	@Mock
	private SimilarityMeasure<Integer> similarityMeasure;

	@SuppressWarnings("unchecked")
	private static CompressedRelation createLeft() {
		CompressedRelation left = Mockito.mock(CompressedRelation.class);
		when(left.indexOf(A)).thenReturn(0);
		CompressedColumn<Integer> aCompressed = Mockito.mock(CompressedColumn.class);
		when(aCompressed.getPli()).thenReturn(Mockito.mock(PositionListIndex.class));
		when(left.<Integer>getColumn(0)).thenReturn(aCompressed);
		return left;
	}

	@SuppressWarnings("unchecked")
	private static CompressedRelation createRight() {
		CompressedColumn<Integer> bCompressed = Mockito.mock(CompressedColumn.class);
		PositionListIndex pliB = Mockito.mock(PositionListIndex.class);
		when(bCompressed.getPli()).thenReturn(pliB);
		CompressedRelation right = Mockito.mock(CompressedRelation.class);
		doReturn(bCompressed).when(right).<Integer>getColumn(0);
		doReturn(Mockito.mock(CompressedColumn.class)).when(right).<Integer>getColumn(1);
		doReturn(0).when(right).indexOf(B);
		doReturn(1).when(right).indexOf(C);
		return right;
	}

	@SuppressWarnings("unchecked")
	private static <T> PreprocessingColumnConfiguration<T> toConfiguration(
		ColumnMapping<T> columnMapping) {
		SimilarityIndexBuilder indexBuilder = Mockito.mock(SimilarityIndexBuilder.class);
		SimilarityIndex index = Mockito.mock(SimilarityIndex.class);
		doReturn(0.8).when(index).getSimilarity(4, 5);
		doReturn(0.5).when(index).getSimilarity(4, 8);
		doReturn(new IntArrayList(Arrays.asList(51, 52, 81, 82))).when(index)
			.getSimilarRecords(4, 0.5);
		doReturn(new IntArrayList(Arrays.asList(51, 52))).when(index).getSimilarRecords(4, 0.6);
		when(indexBuilder.create(any(), any(), any(), any(), anyDouble(), any())).thenReturn(index);
		SimilarityComputer<T> similarityComputer = Mockito.mock(SimilarityComputer.class);
		return PreprocessingColumnConfiguration.<T>builder()
			.indexBuilder(indexBuilder)
			.mapping(columnMapping)
			.similarityComputer(similarityComputer)
			.build();
	}

	@Test
	public void testGetAllSimilarRightRecords() {
		List<PreprocessedColumnPair> columnPairs = create();
		PreprocessedColumnPair ab = columnPairs.get(0);
		assertThat(ab.getAllSimilarRightRecords(new int[]{4}, 0.5)).hasSize(4);
		assertThat(ab.getAllSimilarRightRecords(new int[]{4}, 0.5)).contains(51);
		assertThat(ab.getAllSimilarRightRecords(new int[]{4}, 0.5)).contains(52);
		assertThat(ab.getAllSimilarRightRecords(new int[]{4}, 0.5)).contains(81);
		assertThat(ab.getAllSimilarRightRecords(new int[]{4}, 0.5)).contains(82);
		assertThat(ab.getAllSimilarRightRecords(new int[]{4}, 0.6)).hasSize(2);
		assertThat(ab.getAllSimilarRightRecords(new int[]{4}, 0.6)).contains(51);
		assertThat(ab.getAllSimilarRightRecords(new int[]{4}, 0.6)).contains(52);
	}

	@Test
	public void testGetSimilarity() {
		List<PreprocessedColumnPair> columnPairs = create();
		PreprocessedColumnPair ab = columnPairs.get(0);
		assertThat(ab.getSimilarity(new int[]{4}, new int[]{5, 0})).isEqualTo(0.8);
		assertThat(ab.getSimilarity(new int[]{4}, new int[]{8, 0})).isEqualTo(0.5);
	}

	@Test
	public void testSize() {
		List<PreprocessedColumnPair> columnPairs = create();
		assertThat(columnPairs).hasSize(2);
	}

	@SuppressWarnings("unchecked")
	private List<PreprocessedColumnPair> create() {
		ColumnPair<Integer> pair1 = new ColumnPair<>(A, B);
		ColumnPair<Integer> pair2 = new ColumnPair<>(A, C);
		List<ColumnMapping<?>> mappings = ImmutableList.<ColumnMapping<?>>builder()
			.add(new ColumnMapping<>(pair1, similarityMeasure))
			.add(new ColumnMapping<>(pair2, similarityMeasure))
			.build();
		CompressedRelation left = createLeft();
		CompressedRelation right = createRight();
		List<PreprocessingColumnConfiguration<?>> configurations = mappings.stream()
			.map(PreprocessedColumnPairImplTest::toConfiguration)
			.collect(Collectors.toList());
		PreprocessingConfiguration configuration = new PreprocessingConfiguration(configurations);
		return PreprocessedColumnPairImpl.builder()
			.left(left)
			.right(right)
			.mappings(configuration)
			.build();
	}

}