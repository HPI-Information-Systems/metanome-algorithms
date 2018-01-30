package de.metanome.algorithms.hymd;

import com.google.common.collect.Iterables;
import de.hpi.is.md.relational.InputException;
import de.hpi.is.md.relational.InputOpenException;
import de.hpi.is.md.relational.Relation;
import de.hpi.is.md.relational.RelationalInput;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;

public class MetanomeRelation implements Relation {

	private final RelationalInputGenerator generator;

	MetanomeRelation(RelationalInputGenerator generator) {
		this.generator = generator;
	}

	@Override
	public long getSize() throws InputException {
		return Iterables.size(open());
	}

	@Override
	public RelationalInput open() throws InputOpenException {
		try {
			de.metanome.algorithm_integration.input.RelationalInput input = generator
				.generateNewCopy();
			return new MetanomeRelationalInput(input);
		} catch (InputGenerationException | AlgorithmConfigurationException e) {
			throw new InputOpenException(e);
		}
	}
}
