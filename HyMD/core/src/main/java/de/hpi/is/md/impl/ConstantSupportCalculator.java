package de.hpi.is.md.impl;

import com.bakdata.util.jackson.CPSType;
import de.hpi.is.md.SupportCalculator;
import de.hpi.is.md.relational.Relation;
import lombok.RequiredArgsConstructor;

@CPSType(id = "constant", base = SupportCalculator.class)
@RequiredArgsConstructor
public class ConstantSupportCalculator implements SupportCalculator {

	private final long support;

	@Override
	public long calculateSupport(Relation relation) {
		return support;
	}

	@Override
	public long calculateSupport(Relation r, Relation s) {
		return support;
	}
}
