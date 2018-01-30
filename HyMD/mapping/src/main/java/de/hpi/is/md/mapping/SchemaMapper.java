package de.hpi.is.md.mapping;

import com.bakdata.util.jackson.CPSBase;
import com.bakdata.util.jackson.CPSTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import de.hpi.is.md.relational.ColumnPair;
import de.hpi.is.md.relational.Relation;
import de.hpi.is.md.relational.Schema;
import java.util.Collection;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeIdResolver(CPSTypeIdResolver.class)
@CPSBase
public interface SchemaMapper {

	default Collection<ColumnPair<?>> create(Relation relation) {
		return create(relation.getSchema());
	}

	default Collection<ColumnPair<?>> create(Schema schema) {
		return create(schema, schema);
	}

	default Collection<ColumnPair<?>> create(Relation relation1, Relation relation2) {
		Schema schema1 = relation1.getSchema();
		Schema schema2 = relation2.getSchema();
		return create(schema1, schema2);
	}

	Collection<ColumnPair<?>> create(Schema schema1, Schema schema2);
}
