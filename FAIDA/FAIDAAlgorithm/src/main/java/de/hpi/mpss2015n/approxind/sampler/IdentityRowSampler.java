package de.hpi.mpss2015n.approxind.sampler;

import de.hpi.mpss2015n.approxind.RowSampler;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;

public final class IdentityRowSampler implements RowSampler {
    @Override
    public RelationalInputGenerator[] createSample(RelationalInputGenerator[] fileInputGenerators) {
        return fileInputGenerators;
    }

    @Override
    public String toString() {
        return "IdentityRowSampler";
    }
}
