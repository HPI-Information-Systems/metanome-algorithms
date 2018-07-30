package de.hpi.is.md.sim.impl;

import de.hpi.is.md.sim.DistanceMetric;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DateSimilarity implements DistanceMetric<Temporal> {

	private static final long serialVersionUID = -1193708279189367428L;
	private final ChronoUnit unit;

	@Override
	public long computeDistance(Temporal temporal1, Temporal temporal2) {
		return unit.between(temporal1, temporal2);
	}

}
