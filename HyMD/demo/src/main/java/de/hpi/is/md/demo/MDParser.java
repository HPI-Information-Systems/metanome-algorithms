package de.hpi.is.md.demo;

import de.hpi.is.md.ColumnMapping;
import de.hpi.is.md.MatchingDependency.ColumnMatchWithThreshold;
import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.ColumnPair;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.sim.impl.EqualsSimilarityMeasure;
import de.hpi.is.md.sim.impl.LevenshteinSimilarity;
import de.hpi.is.md.sim.impl.StringMetricSimilarityMeasure;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MDParser {

	private static final Pattern ELEM_PATTERN = Pattern
		.compile("\\[(\\w+)\\.(\\w+)\\]\\((\\w+)\\)@([\\d\\.]+)");
	private static final String IMPLICATION = "->";
	private static final Pattern LIST_PATTERN = Pattern.compile("\\[(.*)\\]");

	static Collection<ColumnMatchWithThreshold<?>> parseMd(String md) {
		String fullLhs = md.split(IMPLICATION)[0];
		Matcher matcher = LIST_PATTERN.matcher(fullLhs);
		if (matcher.find()) {
			String lhs = matcher.group(1);
			return parseLhs(lhs);
		}
		throw new IllegalArgumentException("Cannot parse md: " + md);
	}

	private static Collection<ColumnMatchWithThreshold<?>> parseLhs(String lhs) {
		String[] elements = lhs.split(",");
		return Arrays.stream(elements)
			.map(MDParser::parseLhsElement)
			.collect(Collectors.toList());
	}

	private static ColumnMatchWithThreshold<String> parseLhsElement(String lhsElem) {
		Matcher matcher = ELEM_PATTERN.matcher(lhsElem);
		if (matcher.find()) {
			String tableName = matcher.group(1);
			String columnName = matcher.group(2);
			String simName = matcher.group(3);
			String threshold = matcher.group(4);
			SimilarityMeasure<String> similarityMeasure = parseSimilarityMeasure(simName);
			double t = Double.parseDouble(threshold);
			return new ColumnMatchWithThreshold<>(new ColumnMapping<>(
				new ColumnPair<>(
					Column.of(columnName, String.class, tableName),
					Column.of(columnName, String.class, tableName)),
				similarityMeasure), t);
		}
		throw new IllegalArgumentException("Cannot parse LHS elem: " + lhsElem);
	}

	private static SimilarityMeasure<String> parseSimilarityMeasure(String simName) {
		switch (simName) {
			case "Levenshtein":
				return LevenshteinSimilarity.FAST;
			case "MongeElkan":
				return StringMetricSimilarityMeasure.MONGE_ELKAN;
			case "Equal":
				return EqualsSimilarityMeasure.getInstance();
			case "LongestCommonSubsequence":
				return StringMetricSimilarityMeasure.LONGEST_COMMON_SUBSEQUENCE;
			default:
				throw new IllegalArgumentException("Unknown similarity measure: " + simName);
		}
	}
}
