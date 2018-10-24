package de.metanome.algorithms.normalize.fdextension;

import java.util.BitSet;
import java.util.Map;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithms.normalize.aspects.NormiPersistence;

public abstract class FdExtender {
	
	private NormiPersistence persister;
	private String tempResultsPath;
	
	public FdExtender(NormiPersistence persister, String tempResultsPath) {
		this.persister = persister;
		this.tempResultsPath = tempResultsPath;
	}
	
	public Map<BitSet, BitSet> calculateClosure(Map<BitSet, BitSet> fds, boolean useResultFile) throws AlgorithmExecutionException {
		Map<BitSet, BitSet> extendedFds = useResultFile ? this.persister.read(this.tempResultsPath) : null;
		
		if (extendedFds == null) {
			long time = System.currentTimeMillis();
			this.executeAlgorithm(fds);
			System.out.println("Extension in " + (System.currentTimeMillis() - time) + " ms");
			
			this.persister.write(fds, this.tempResultsPath, false);
			return fds;
		}
		return extendedFds;
	}
	
	protected abstract void executeAlgorithm(Map<BitSet, BitSet> fds);
}
