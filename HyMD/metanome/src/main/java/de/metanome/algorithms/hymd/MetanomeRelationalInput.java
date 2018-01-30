package de.metanome.algorithms.hymd;

import de.hpi.is.md.relational.InputCloseException;
import de.hpi.is.md.relational.RelationalInput;
import de.hpi.is.md.relational.Row;
import de.hpi.is.md.relational.Schema;
import java.util.Iterator;

public class MetanomeRelationalInput implements RelationalInput {

	private final de.metanome.algorithm_integration.input.RelationalInput input;

	MetanomeRelationalInput(de.metanome.algorithm_integration.input.RelationalInput input) {
		this.input = input;
	}

	@Override
	public void close() throws InputCloseException {
		try {
			input.close();
		} catch (Exception e) {
			throw new InputCloseException(e);
		}
	}

	@Override
	public Schema getSchema() {
		return MetanomeSchema.getSchema(input);
	}

	@Override
	public Iterator<Row> iterator() {
		return MetanomeRelationalInputIterator.of(input);
	}

}
