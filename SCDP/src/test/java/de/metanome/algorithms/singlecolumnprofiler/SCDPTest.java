package de.metanome.algorithms.singlecolumnprofiler;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.backend.input.file.DefaultFileInputGenerator;
import de.metanome.backend.result_receiver.ResultCounter;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

/**
 * Tests for the SCDP algorithm.
 */
public class SCDPTest {

    @Test
    public void testWorksWithNoAggregation() throws AlgorithmExecutionException, URISyntaxException, FileNotFoundException {
        SingleColumnProfiler scdp = new SingleColumnProfiler();
        scdp.setBooleanConfigurationValue(
                SingleColumnProfiler.Identifier.NO_AGGREGATION.name(),
                true
        );
        DefaultFileInputGenerator fileInputGenerator = new DefaultFileInputGenerator(
                new File(Thread.currentThread().getContextClassLoader().getResource("small.csv").toURI())
        );
        scdp.setRelationalInputConfigurationValue(
                SingleColumnProfiler.Identifier.INPUT_GENERATOR.name(),
                fileInputGenerator
        );
        scdp.setResultReceiver(new ResultCounter("SCDP", true));

        scdp.execute();
    }

    @Test
    public void testWorksWithoutNoAggregation() throws AlgorithmExecutionException, URISyntaxException, FileNotFoundException {
        SingleColumnProfiler scdp = new SingleColumnProfiler();
        scdp.setBooleanConfigurationValue(
                SingleColumnProfiler.Identifier.NO_AGGREGATION.name(),
                false
        );
        DefaultFileInputGenerator fileInputGenerator = new DefaultFileInputGenerator(
                new File(Thread.currentThread().getContextClassLoader().getResource("small.csv").toURI())
        );
        scdp.setRelationalInputConfigurationValue(
                SingleColumnProfiler.Identifier.INPUT_GENERATOR.name(),
                fileInputGenerator
        );
        scdp.setResultReceiver(new ResultCounter("SCDP", true));

        scdp.execute();
    }

}
