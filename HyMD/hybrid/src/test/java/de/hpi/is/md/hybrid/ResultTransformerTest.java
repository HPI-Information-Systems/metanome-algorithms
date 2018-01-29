package de.hpi.is.md.hybrid;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import de.hpi.is.md.ColumnMapping;
import de.hpi.is.md.MatchingDependency;
import de.hpi.is.md.MatchingDependency.ColumnMatchWithThreshold;
import de.hpi.is.md.MatchingDependencyResult;
import de.hpi.is.md.hybrid.impl.md.MDElementImpl;
import de.hpi.is.md.hybrid.impl.md.MDImpl;
import de.hpi.is.md.hybrid.impl.md.MDSiteImpl;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.ColumnPair;
import de.hpi.is.md.sim.SimilarityComputer;
import de.hpi.is.md.sim.SimilarityMeasure;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ResultTransformerTest {

	private static final Column<Integer> A = Column.of("a", Integer.class);
	private static final Column<Integer> B = Column.of("b", Integer.class);
	private static final Column<Integer> C = Column.of("c", Integer.class);
	private static final ColumnPair<Integer> AB = new ColumnPair<>(A, B);
	private static final ColumnPair<Integer> AC = new ColumnPair<>(A, C);

	@Mock
	private SimilarityMeasure<Integer> similarityMeasure;
	@Mock
	private SimilarityComputer<Integer> computer;

	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		ResultTransformer transformer = createConsumer();
		MD md = new MDImpl(new MDSiteImpl(2).set(0, 0.8), new MDElementImpl(1, 0.7));
		SupportedMD result = new SupportedMD(md, 10);
		MatchingDependencyResult transformed = transformer.transform(result);
		MatchingDependency dependency = new MatchingDependency(
			Collections.singletonList(
				new ColumnMatchWithThreshold<>(new ColumnMapping<>(AB, similarityMeasure), 0.8)),
			new ColumnMatchWithThreshold<>(new ColumnMapping<>(AC, similarityMeasure), 0.7)
		);
		assertThat(transformed).isEqualTo(new MatchingDependencyResult(dependency, 10));
	}

	private ResultTransformer createConsumer() {
		List<ColumnMapping<?>> mappings = ImmutableList.<ColumnMapping<?>>builder()
			.add(new ColumnMapping<>(AB, similarityMeasure))
			.add(new ColumnMapping<>(AC, similarityMeasure))
			.build();
		return new ResultTransformer(mappings);
	}

}