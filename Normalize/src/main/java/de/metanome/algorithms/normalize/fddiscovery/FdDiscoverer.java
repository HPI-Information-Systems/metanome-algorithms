package de.metanome.algorithms.normalize.fddiscovery;

import java.util.BitSet;
import java.util.Map;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithms.normalize.aspects.NormiConversion;
import de.metanome.algorithms.normalize.aspects.NormiPersistence;
import de.metanome.backend.result_receiver.ResultCache;

public abstract class FdDiscoverer {

	private NormiConversion converter;
	private NormiPersistence persister;
	private String tempResultsPath;
	
	public FdDiscoverer(NormiConversion converter, NormiPersistence persister, String tempResultsPath) {
		this.converter = converter;
		this.persister = persister;
		this.tempResultsPath = tempResultsPath;
	}
	
	public Map<BitSet, BitSet> calculateFds(RelationalInputGenerator inputGenerator, Boolean nullEqualsNull, boolean useResultFile) throws AlgorithmExecutionException {
		Map<BitSet, BitSet> fds = useResultFile ? this.persister.read(this.tempResultsPath) : null;
		
		if (fds == null) {
			ResultCache resultFds = this.executeAlgorithm(inputGenerator, nullEqualsNull);
			
			System.out.println("Transforming FDs into bitsets ...");
			fds = this.converter.toFunctionalDependencyMap(resultFds.fetchNewResults());
			
			this.persister.write(fds, this.tempResultsPath, false);
		}
		
		return fds;
	}
	
	protected abstract ResultCache executeAlgorithm(RelationalInputGenerator inputGenerator, Boolean nullEqualsNull) throws AlgorithmExecutionException;
}
