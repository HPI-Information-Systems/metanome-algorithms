package de.metanome.algorithms.binder.io;

import java.util.List;

import de.metanome.algorithm_integration.input.InputIterationException;

public interface InputIterator extends AutoCloseable {

	public boolean next() throws InputIterationException;
	public String getValue(int columnIndex) throws InputIterationException;
	public List<String> getValues() throws InputIterationException;
}
