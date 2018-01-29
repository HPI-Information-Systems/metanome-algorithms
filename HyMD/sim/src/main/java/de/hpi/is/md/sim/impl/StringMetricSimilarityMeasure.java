package de.hpi.is.md.sim.impl;

import com.bakdata.util.jackson.CPSType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.Hasher;
import de.hpi.is.md.util.ObjectUtils;
import de.hpi.is.md.util.jackson.EnumNameDeserializer;
import lombok.RequiredArgsConstructor;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;

@JsonDeserialize(using = EnumNameDeserializer.class)
@CPSType(id = "string_metric", base = SimilarityMeasure.class)
@RequiredArgsConstructor
public enum StringMetricSimilarityMeasure implements SimilarityMeasure<String> {

	MONGE_ELKAN(StringMetrics.mongeElkan(), "MongeElkan"),
	LONGEST_COMMON_SUBSEQUENCE(StringMetrics.longestCommonSubsequence(),
		"LongestCommonSubsequence");

	private final StringMetric metric;
	private final String name;

	@Override
	public double calculateSimilarity(String obj1, String obj2) {
		if (ObjectUtils.bothNull(obj1, obj2)) {
			return MAX_SIMILARITY;
		}
		if (ObjectUtils.eitherNull(obj1, obj2)) {
			return MIN_SIMILARITY;
		}
		return metric.compare(obj1, obj2);
	}

	@Override
	public void hash(Hasher hasher) {
		Class<?> clazz = this.getClass();
		hasher
			.putClass(clazz)
			.putUnencodedChars(name);
	}

	@Override
	public String toString() {
		return name;
	}

}
