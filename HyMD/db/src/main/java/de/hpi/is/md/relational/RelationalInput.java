package de.hpi.is.md.relational;

public interface RelationalInput extends AutoCloseable, Iterable<Row>, HasSchema {

	@Override
	void close() throws InputCloseException;
}
