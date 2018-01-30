package de.hpi.is.md.demo;

import de.hpi.is.md.util.MathUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContingencyTable {

	private final double recall;
	private final double precision;
	private final double fmeasure;

	static ContingencyTable create(double tp, double fn, double fp) {
		double recall = tp / (tp + fn);
		double precision = tp / (tp + fp);
		double fmeasure = MathUtils.divide(2 * precision * recall, precision + recall);
		return new ContingencyTable(recall, precision, fmeasure);
	}


}
