package de.hpi.is.md.hybrid.impl.sim;

import static org.assertj.core.api.Assertions.assertThat;

import de.hpi.is.md.hybrid.impl.sim.PreprocessedSimilarity.To;
import de.hpi.is.md.hybrid.impl.sim.SimilarityTableBuilderImpl.SimilarityRowBuilderFactory;
import de.hpi.is.md.hybrid.impl.sim.SimilarityTableBuilderImpl.SimilarityTableFactory;
import de.hpi.is.md.util.Int2Int2DoubleTable;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.jooq.lambda.Seq;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SimilarityTableBuilderTest {

	@Parameter
	public SimilarityTableFactory tableFactory;
	@Parameter(1)
	public SimilarityRowBuilderFactory rowBuilderFactory;

	@Parameters
	public static Collection<Object[]> data() {
		return Seq.of(new SimilarityHashTableFactory(), new SimilarityArrayTableFactory())
			.crossJoin(
				Seq.of(SimilarityMapRowBuilder.factory(), SimilarityArrayRowBuilder.factory()))
			.map(t -> new Object[]{t.v1(), t.v2()})
			.toList();
	}

	@Test
	public void testGetOrDefault() {
		SimilarityTableBuilder tableBuilder = createTableBuilder(3, 3);
		Collection<To> row1 = Arrays.asList(
			To.builder().similarity(0.5).right(1).records(IntSets.EMPTY_SET).build(),
			To.builder().similarity(0.6).right(2).records(IntSets.EMPTY_SET).build());
		Collection<To> row2 = Collections.singletonList(
			To.builder().similarity(0.4).right(1).records(IntSets.EMPTY_SET).build());
		tableBuilder.add(1, row1);
		tableBuilder.add(2, row2);
		Int2Int2DoubleTable table = tableBuilder.build();
		assertThat(table.getOrDefault(1, 1)).isEqualTo(0.5);
		assertThat(table.getOrDefault(1, 2)).isEqualTo(0.6);
		assertThat(table.getOrDefault(2, 1)).isEqualTo(0.4);
		assertThat(table.getOrDefault(1, 0)).isEqualTo(0.0);
		assertThat(table.getOrDefault(2, 0)).isEqualTo(0.0);
	}

	@Test
	public void testRow() {
		SimilarityTableBuilder tableBuilder = createTableBuilder(3, 3);
		Collection<To> row1 = Arrays.asList(
			To.builder().similarity(0.5).right(1).records(IntSets.EMPTY_SET).build(),
			To.builder().similarity(0.6).right(2).records(IntSets.EMPTY_SET).build());
		Collection<To> row2 = Collections.singletonList(
			To.builder().similarity(0.4).right(1).records(IntSets.EMPTY_SET).build());
		tableBuilder.add(1, row1);
		tableBuilder.add(2, row2);
		Int2Int2DoubleTable table = tableBuilder.build();
		assertThat(table.row(1).asMap()).hasSize(2);
		assertThat(table.row(1).asMap()).containsEntry(Integer.valueOf(1), Double.valueOf(0.5));
		assertThat(table.row(1).asMap()).containsEntry(Integer.valueOf(2), Double.valueOf(0.6));
		assertThat(table.row(2).asMap()).hasSize(1);
		assertThat(table.row(2).asMap()).containsEntry(Integer.valueOf(1), Double.valueOf(0.4));
	}

	@Test
	public void testValues() {
		SimilarityTableBuilder tableBuilder = createTableBuilder(3, 3);
		Collection<To> row1 = Arrays.asList(
			To.builder().similarity(0.5).right(1).records(IntSets.EMPTY_SET).build(),
			To.builder().similarity(0.6).right(2).records(IntSets.EMPTY_SET).build());
		Collection<To> row2 = Collections.singletonList(
			To.builder().similarity(0.4).right(1).records(IntSets.EMPTY_SET).build());
		tableBuilder.add(1, row1);
		tableBuilder.add(2, row2);
		Int2Int2DoubleTable table = tableBuilder.build();
		assertThat(table.values()).hasSize(3);
		assertThat(table.values()).contains(Double.valueOf(0.4), Double.valueOf(0.5), Double.valueOf(0.6));
	}

	private SimilarityTableBuilder createTableBuilder(int height, int width) {
		return SimilarityTableBuilderImpl.factory(tableFactory, rowBuilderFactory)
			.create(height, width);
	}

}
