package de.hpi.mpss2015n.approxind.sampler;

import de.hpi.mpss2015n.approxind.mocks.RelationalInputBuilder;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ReservoirRowSamplerTest {

  @Test
  public void testCreateSample() throws Exception {
    RelationalInputGenerator[] inputs = new RelationalInputGenerator[1];
    RelationalInputBuilder builder = new RelationalInputBuilder("testTable")
        .setHeader("c0", "c1", "c2");
    for(int i=0; i<20; i++){
      builder.addRow("a", "b", i+"");
    }
    inputs[0] = builder.build();

    int sampleSize = 10;
    ReservoirRowSampler sampler = new ReservoirRowSampler(sampleSize);
    RelationalInput samples = sampler.createSample(inputs)[0].generateNewCopy();
    HashSet<List<String>> set = new HashSet<>();
    while(samples.hasNext()){
      List<String> sample = samples.next();
      //System.out.println(sample);
      assertFalse(set.contains(sample));
      set.add(sample);
    }
    assertEquals(set.size(), sampleSize);
  }
}