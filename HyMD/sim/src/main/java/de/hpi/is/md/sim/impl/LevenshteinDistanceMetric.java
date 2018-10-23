package de.hpi.is.md.sim.impl;

import com.bakdata.util.jackson.CPSType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.hpi.is.md.sim.DistanceMetric;
import de.hpi.is.md.util.jackson.SingletonDeserializer;
import org.apache.commons.text.similarity.LevenshteinDistance;

@JsonDeserialize(using = SingletonDeserializer.class)
@CPSType(id = "levenshtein", base = DistanceMetric.class)
public enum LevenshteinDistanceMetric implements DistanceMetric<String> {

	INSTANCE;

	private static final LevenshteinDistance LEVENSHTEIN = LevenshteinDistance.getDefaultInstance();

	@Override
	public long computeDistance(String obj1, String obj2) {
		return LEVENSHTEIN.apply(obj1, obj2).longValue();
	}


	@Override
	public String toString() {
		return "Levenshtein";
	}
}
