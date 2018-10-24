package de.metanome.algorithms.normalize.fddiscovery;

import java.io.IOException;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithms.hyfd.HyFD;
import de.metanome.algorithms.normalize.aspects.NormiConversion;
import de.metanome.algorithms.normalize.aspects.NormiPersistence;
import de.metanome.backend.result_receiver.ResultCache;

public class HyFDFdDiscoverer extends FdDiscoverer {
	
	public HyFDFdDiscoverer(NormiConversion converter, NormiPersistence persister, String tempResultsPath) {
		super(converter, persister, tempResultsPath);
	}

	@Override
	protected ResultCache executeAlgorithm(RelationalInputGenerator inputGenerator, Boolean nullEqualsNull) throws AlgorithmExecutionException {
		ResultCache resultFds = null;
		try {
			HyFD hyFD = new HyFD();
			
			resultFds = new ResultCache("MetanomeMock", null);
			
			hyFD.setRelationalInputConfigurationValue(HyFD.Identifier.INPUT_GENERATOR.name(), inputGenerator);
			hyFD.setBooleanConfigurationValue(HyFD.Identifier.NULL_EQUALS_NULL.name(), nullEqualsNull);
			hyFD.setBooleanConfigurationValue(HyFD.Identifier.VALIDATE_PARALLEL.name(), Boolean.valueOf(true));
			hyFD.setBooleanConfigurationValue(HyFD.Identifier.ENABLE_MEMORY_GUARDIAN.name(), Boolean.valueOf(true));
			hyFD.setIntegerConfigurationValue(HyFD.Identifier.MAX_DETERMINANT_SIZE.name(), Integer.valueOf(-1));
			hyFD.setResultReceiver(resultFds);
			
			hyFD.execute();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new AlgorithmExecutionException(e.getMessage());
		}
		return resultFds;
	}
}
