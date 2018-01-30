package de.hpi.is.md.hybrid.impl.infer;

import com.bakdata.util.jackson.CPSType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.jackson.SingletonDeserializer;

@JsonDeserialize(using = SingletonDeserializer.class)
@CPSType(id = "disjoint", base = SpecializationFilter.class)
public enum LhsRhsDisjointnessFilter implements SpecializationFilter {

	INSTANCE;

	@Override
	public boolean filter(MDSite lhs, MDElement rhs) {
		int rhsAttr = rhs.getId();
		return !lhs.get(rhsAttr).isPresent();
	}
}
