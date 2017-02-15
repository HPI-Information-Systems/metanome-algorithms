package de.hpi.mpss2015n.approxind.mocks;

import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;

class RelationalInputGeneratorMock implements RelationalInputGenerator {

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

    @Override
    public void close() throws Exception {
        if (this.input != null) {
            try {
                this.input.close();
            } finally {
                this.input = null;
            }
        }
    }
}
