package de.hpi.is.md.mapping.impl;

import com.fasterxml.jackson.databind.util.StdConverter;
import com.google.common.collect.Multimap;
import de.hpi.is.md.relational.Column;
import de.hpi.is.md.util.jackson.Converters;
import de.hpi.is.md.util.jackson.Entry;
import java.util.List;

class ColumnMappingsConverter extends
	StdConverter<List<Entry<Column<?>, Column<?>>>, Multimap<Column<?>, Column<?>>> {

	@Override
	public Multimap<Column<?>, Column<?>> convert(List<Entry<Column<?>, Column<?>>> value) {
		return Converters.toMultimap(value);
	}
}
