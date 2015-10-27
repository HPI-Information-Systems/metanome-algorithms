package de.hpi.mpss2015n.approxind.sampler;

import de.hpi.mpss2015n.approxind.RowSampler;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;

public class ReservoirRowSampler implements RowSampler {

  int sampleSize;

  public ReservoirRowSampler(int sampleSize){
    this.sampleSize = sampleSize;
  }

  @Override
  public RelationalInputGenerator[] createSample(RelationalInputGenerator[] fileInputGenerators) {
    RelationalInputGenerator[] sampleGenerators = new RelationalInputGenerator[fileInputGenerators.length];
    int i = 0;
    for(RelationalInputGenerator generator: fileInputGenerators){
      try {
        ReservoirRowSample sample = new ReservoirRowSample(generator.generateNewCopy(), sampleSize);
        sampleGenerators[i++] = new SampleGenerator(sample);
      } catch (InputGenerationException e) {
        e.printStackTrace();
      }
    }
    return sampleGenerators;
  }
}
