package de.hpi.is.md.sim.impl;

import de.hpi.is.md.sim.DistanceMetric;
import java.sql.Date;
import java.time.temporal.ChronoUnit;

public class SqlDateSimilarity implements DistanceMetric<Date> {

	private static final long serialVersionUID = 1210121832046884960L;
	private final DateSimilarity dateSimilarity;

	public SqlDateSimilarity(ChronoUnit unit) {
		dateSimilarity = new DateSimilarity(unit);
	}

	@Override
	public long computeDistance(Date obj1, Date obj2) {
		return dateSimilarity.computeDistance(obj1.toLocalDate(), obj2.toLocalDate());
	}

}
