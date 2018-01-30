package de.hpi.is.md.impl;

import com.bakdata.util.jackson.CPSType;
import de.hpi.is.md.SupportCalculator;
import de.hpi.is.md.relational.InputException;
import de.hpi.is.md.relational.Relation;
import lombok.Data;

@CPSType(id = "size", base = SupportCalculator.class)
@Data
public class SizeBasedSupportCalculator implements SupportCalculator {

	private int nonReflexiveMatches = 1;

	private static long getSize(Relation relation) {
		try {
			return relation.getSize();
		} catch (InputException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long calculateSupport(Relation relation) {
		return getSize(relation) + nonReflexiveMatches;
	}

	@Override
	public long calculateSupport(Relation r, Relation s) {
		return r == s ? calculateSupport(r) : nonReflexiveMatches;
	}
}
