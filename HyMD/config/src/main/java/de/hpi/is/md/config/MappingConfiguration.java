package de.hpi.is.md.config;

import de.hpi.is.md.ColumnMapping;
import de.hpi.is.md.ThresholdFilter;
import de.hpi.is.md.hybrid.ColumnConfiguration;
import de.hpi.is.md.hybrid.MDMapping;
import de.hpi.is.md.hybrid.PreprocessingColumnConfiguration;
import de.hpi.is.md.hybrid.SimilarityIndex.SimilarityIndexBuilder;
import de.hpi.is.md.mapping.SchemaMapper;
import de.hpi.is.md.mapping.impl.TypeSchemaMapper;
import de.hpi.is.md.relational.ColumnPair;
import de.hpi.is.md.relational.Relation;
import de.hpi.is.md.sim.SimilarityComputer;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class MappingConfiguration {

	@NonNull
	private final ThresholdFilterConfiguration thresholdFilters = new ThresholdFilterConfiguration();
	@NonNull
	private final IndexBuilderConfiguration indexBuilders = new IndexBuilderConfiguration();
	@NonNull
	private final MinThresholdConfiguration minThresholds = new MinThresholdConfiguration();
	@NonNull
	private final SimilarityComputerConfiguration similarityComputers = new SimilarityComputerConfiguration();
	@NonNull
	private final SimilarityMeasureConfiguration similarityMeasures = new SimilarityMeasureConfiguration();
	@NonNull
	private SchemaMapper schemaMapper = new TypeSchemaMapper();

	public MDMapping createMapping(Relation relation) {
		Collection<ColumnPair<?>> columnPairs = schemaMapper.create(relation);
		return toMapping(columnPairs);
	}

	public MDMapping createMapping(Relation relation1, Relation relation2) {
		Collection<ColumnPair<?>> columnPairs = schemaMapper.create(relation1, relation2);
		return toMapping(columnPairs);
	}

	private MDMapping toMapping(Collection<ColumnPair<?>> pairs) {
		List<ColumnConfiguration<?>> mappings = pairs.stream()
			.map(this::toMapping)
			.flatMap(Collection::stream)
			.collect(Collectors.toList());
		log.info("Created {} mappings: {}", Integer.valueOf(mappings.size()), mappings);
		return new MDMapping(mappings);
	}

	private <T> Collection<ColumnConfiguration<T>> toMapping(ColumnPair<T> pair) {
		return with(pair).toMapping();
	}

	private <T> WithPair<T> with(ColumnPair<T> pair) {
		return new WithPair<>(pair);
	}

	@RequiredArgsConstructor
	private class WithPair<T> {

		@NonNull
		private final ColumnPair<T> pair;

		private ColumnConfiguration<T> createColumnConfiguration(
			PreprocessingColumnConfiguration<T> preprocessingConfiguration) {
			ThresholdFilter thresholdFilter = thresholdFilters.getThresholdFilter(pair);
			return new ColumnConfiguration<>(thresholdFilter, preprocessingConfiguration);
		}

		private PreprocessingColumnConfiguration<T> createPreprocessingConfiguration(
			SimilarityMeasure<T> similarityMeasure) {
			double minThreshold = minThresholds.getMinThreshold(pair);
			SimilarityIndexBuilder indexBuilder = indexBuilders.getIndexBuilder(pair);
			ColumnMapping<T> columnMapping = new ColumnMapping<>(pair, similarityMeasure);
			SimilarityComputer<T> similarityComputer = similarityComputers
				.getSimilarityComputer(pair, similarityMeasure);
			return PreprocessingColumnConfiguration.<T>builder()
				.minThreshold(minThreshold)
				.similarityComputer(similarityComputer)
				.indexBuilder(indexBuilder)
				.mapping(columnMapping)
				.build();
		}

		private Collection<ColumnConfiguration<T>> toMapping() {
			Collection<SimilarityMeasure<T>> measures = similarityMeasures
				.getSimilarityMeasures(pair);
			return StreamUtils.seq(measures)
				.map(this::createPreprocessingConfiguration)
				.map(this::createColumnConfiguration)
				.toList();
		}
	}

}
