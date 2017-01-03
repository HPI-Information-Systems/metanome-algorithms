package de.metanome.algorithms.binder.io;

import java.util.ArrayList;
import java.util.List;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;

public class FileInputIterator implements InputIterator {

	private RelationalInput inputGenerator = null;
	private List<String> record = null;
	
	private int rowsRead = 0;
	private int inputRowLimit;
	
	public FileInputIterator(RelationalInputGenerator inputGenerator, int inputRowLimit) throws InputGenerationException, AlgorithmConfigurationException {
		this.inputGenerator = inputGenerator.generateNewCopy();
		this.inputRowLimit = inputRowLimit;
	}
	
	@Override
	public boolean next() throws InputIterationException {
		if (this.inputGenerator.hasNext() && ((this.inputRowLimit <= 0) || (this.rowsRead < this.inputRowLimit))) {
			List<String> input = this.inputGenerator.next();
			this.record = new ArrayList<String>(input.size());
			
			for (String value : input) {
				// Replace line breaks with the zero-character, because these line breaks would otherwise split values when later written to plane-text buckets
				if (value != null)
					value = value.replaceAll("\n", "\0");
				this.record.add(value);
			}
			
			this.rowsRead++;
			return true;
		}
		return false;
	}

	@Override
	public String getValue(int columnIndex) {
		return this.record.get(columnIndex);
	}

	@Override
	public List<String> getValues() {
		return this.record;
	}

	@Override
	public void close() throws Exception {
		this.inputGenerator.close();
	}
}
