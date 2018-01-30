package de.hpi.is.md.relational;

import de.hpi.is.md.util.Hashable;

public interface Relation extends HasSchema, Hashable {

	@Override
	default Schema getSchema() {
		try (RelationalInput input = open()) {
			return input.getSchema();
		} catch (InputException e) {
			throw new RuntimeException(e);
		}
	}

	long getSize() throws InputException;

	RelationalInput open() throws InputOpenException;

}
