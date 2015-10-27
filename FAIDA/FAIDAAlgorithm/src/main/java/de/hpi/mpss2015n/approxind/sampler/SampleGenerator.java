package de.hpi.mpss2015n.approxind.sampler;

import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;

public class SampleGenerator implements RelationalInputGenerator {

  RelationalInput input;

  public SampleGenerator(RelationalInput input){
    this.input = input;
  }

  @Override
  public RelationalInput generateNewCopy() throws InputGenerationException {
    return input;
  }
}
