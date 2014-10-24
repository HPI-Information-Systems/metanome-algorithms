package de.metanome.algorithms.binder.io;

import java.util.ArrayList;
import java.util.List;

import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;

public class FileInputIterator implements InputIterator {

	private RelationalInput inputGenerator = null;
	private List<String> record = null;
	private int rowsRead = 0;
	private int inputRowLimit;
	
	public FileInputIterator(FileInputGenerator inputGenerator, int inputRowLimit) throws InputGenerationException {
		this.inputGenerator = inputGenerator.generateNewCopy();
		this.inputRowLimit = inputRowLimit;
	}
	
	@Override
	public boolean next() throws InputIterationException {
		if (this.inputGenerator.hasNext() && ((this.inputRowLimit <= 0) || (this.rowsRead < this.inputRowLimit))) {
			this.record = this.inputGenerator.next();
			this.rowsRead++;
			return true;
		}
		return false;
	}

	@Override
	public String getValue(int columnIndex) {
		String value = this.record.get(columnIndex);
		return ("".equals(value)) ? null : value;
	}

	@Override
	public List<String> getValues(int numColumns) {
		List<String> values = new ArrayList<String>(numColumns);
		for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {
			String value = this.record.get(columnIndex);
			if ("".equals(value))
				values.add(null);
			else
				values.add(value);
		}
		return values;
	}

	@Override
	public void close() throws Exception {
		this.inputGenerator.close();
	}
}
