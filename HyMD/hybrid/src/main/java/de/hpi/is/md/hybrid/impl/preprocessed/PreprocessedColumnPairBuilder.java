package de.hpi.is.md.hybrid.impl.preprocessed;

import static com.google.common.base.Preconditions.checkNotNull;

import com.codahale.metrics.annotation.Timed;
import de.hpi.is.md.ColumnMapping;
import de.hpi.is.md.hybrid.PositionListIndex;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.PreprocessingColumnConfiguration;
import de.hpi.is.md.hybrid.PreprocessingConfiguration;
import de.hpi.is.md.hybrid.SimilarityIndex;
import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.ColumnPair;
import de.hpi.is.md.util.Dictionary;
import io.astefanutti.metrics.aspectj.Metrics;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Setter
@Metrics
class PreprocessedColumnPairBuilder {

	@NonNull
	private PreprocessingConfiguration mappings;
	@NonNull
	private CompressedRelation left;
	@NonNull
	private CompressedRelation right;

	@Timed
	public List<PreprocessedColumnPair> build() {
		checkNotNull(mappings, "Mappings are null");
		checkNotNull(left, "Left relation is null");
		checkNotNull(right, "Right relation is null");
		return mappings.seq()
			.map(this::createBuilder)
			.map(ColumnBuilder::createColumnPair)
			.toList();
	}

	private ColumnBuilder<?> createBuilder(PreprocessingColumnConfiguration<?> mapping) {
		return new ColumnBuilder<>(mapping);
	}

	@RequiredArgsConstructor
	private class ColumnBuilder<T> {

		@NonNull
		private final PreprocessingColumnConfiguration<T> mapping;

		private PreprocessedColumnPair createColumnPair() {
			int leftColumnId = getLeftColumnId();
			int rightColumnId = getRightColumnId();
			PositionListIndex leftPli = getLeftPli(leftColumnId);
			SimilarityIndex similarityIndex = createSimilarityIndex(leftColumnId, rightColumnId);
			return new PreprocessedColumnPairImpl(leftColumnId, rightColumnId, similarityIndex,
				leftPli);
		}

		private SimilarityIndex createSimilarityIndex(int leftColumnId, int rightColumnId) {
			CompressedColumn<T> leftColumn = left.getColumn(leftColumnId);
			CompressedColumn<T> rightColumn = right.getColumn(rightColumnId);
			return createSimilarityIndex(leftColumn, rightColumn);
		}

		private SimilarityIndex createSimilarityIndex(CompressedColumn<T> leftColumn,
			CompressedColumn<T> rightColumn) {
			Dictionary<T> leftDictionary = leftColumn.getDictionary();
			Dictionary<T> rightDictionary = rightColumn.getDictionary();
			PositionListIndex rightIndex = rightColumn.getPli();
			return mapping.createIndex(leftDictionary, rightDictionary, rightIndex);
		}

		private ColumnPair<T> getColumns() {
			ColumnMapping<T> columnMapping = mapping.getMapping();
			return columnMapping.getColumns();
		}

		private int getLeftColumnId() {
			ColumnPair<T> columns = getColumns();
			Column<T> leftColumn = columns.getLeft();
			return left.indexOf(leftColumn);
		}

		private PositionListIndex getLeftPli(int leftColumnId) {
			CompressedColumn<T> leftColumn = left.getColumn(leftColumnId);
			return leftColumn.getPli();
		}

		private int getRightColumnId() {
			ColumnPair<T> columns = getColumns();
			Column<T> rightColumn = columns.getRight();
			return right.indexOf(rightColumn);
		}
	}
}
