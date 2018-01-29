package de.hpi.is.md;

import com.bakdata.util.jackson.CPSBase;
import com.bakdata.util.jackson.CPSTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import de.hpi.is.md.relational.Relation;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeIdResolver(CPSTypeIdResolver.class)
@CPSBase
public interface SupportCalculator {

	long calculateSupport(Relation relation);

	long calculateSupport(Relation r, Relation s);

}
