package de.hpi.is.md.hybrid;

import de.hpi.is.md.ColumnMapping;
import de.hpi.is.md.MatchingDependency;
import de.hpi.is.md.MatchingDependency.ColumnMatchWithThreshold;
import de.hpi.is.md.MatchingDependencyResult;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.relational.ColumnPair;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ResultTransformer {

	@NonNull
	private final List<ColumnMapping<?>> mappings;

	private static <T> ColumnMatchWithThreshold<T> toColumnMatch(ColumnMapping<T> mapping,
		double threshold) {
		SimilarityMeasure<T> similarityMeasure = mapping.getSimilarityMeasure();
		ColumnPair<T> columns = mapping.getColumns();
		ColumnMapping<T> columnMatch = new ColumnMapping<>(columns, similarityMeasure);
		return new ColumnMatchWithThreshold<>(columnMatch, threshold);
	}

	MatchingDependencyResult transform(SupportedMD supportedMD) {
		return with(supportedMD).toResult();
	}

	private ColumnMatchWithThreshold<?> toColumnMatch(MDElement element) {
		int id = element.getId();
		ColumnMapping<?> mapping = mappings.get(id);
		double threshold = element.getThreshold();
		return toColumnMatch(mapping, threshold);
	}

	private WithSupportedMD with(SupportedMD supportedMD) {
		return new WithSupportedMD(supportedMD);
	}

	@RequiredArgsConstructor
	private class WithSupportedMD {

		@NonNull
		private final SupportedMD supportedMd;

		private MatchingDependency buildDependency() {
			Collection<ColumnMatchWithThreshold<?>> lhs = createLhs();
			ColumnMatchWithThreshold<?> rhs = createRhs();
			return new MatchingDependency(lhs, rhs);
		}

		private Collection<ColumnMatchWithThreshold<?>> createLhs() {
			MD md = supportedMd.getMd();
			MDSite lhs = md.getLhs();
			return StreamUtils.seq(lhs)
				.map(ResultTransformer.this::toColumnMatch)
				.collect(Collectors.toList());
		}

		private ColumnMatchWithThreshold<?> createRhs() {
			MD md = supportedMd.getMd();
			MDElement rhs = md.getRhs();
			return toColumnMatch(rhs);
		}

		private MatchingDependencyResult toResult() {
			MatchingDependency md = buildDependency();
			long support = supportedMd.getSupport();
			return new MatchingDependencyResult(md, support);
		}
	}

}
