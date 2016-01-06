package de.hpi.mpss2015n.approxind;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;


public interface RowSampler {
    RelationalInputGenerator[] createSample(RelationalInputGenerator[] fileInputGenerators) throws AlgorithmConfigurationException;
}
