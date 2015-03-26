package de.metanome.algorithms.anelosimus.test;

import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;

public class RelationalInputGeneratorMock implements RelationalInputGenerator {

    RelationalInput input;

    public RelationalInputGeneratorMock(RelationalInput input) {
        this.input = input;
    }

    @Override
    public RelationalInput generateNewCopy() throws InputGenerationException {
        RelationalInputMock mock = (RelationalInputMock) input;
        mock.reset();
        return mock;
    }
}
