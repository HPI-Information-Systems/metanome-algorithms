package de.metanome.algorithms.dcfinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraintSet;
import de.metanome.algorithms.dcfinder.evidenceset.builders.SplitReconstructEvidenceSetBuilder;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.predicates.PredicateBuilder;
import de.metanome.algorithms.dcfinder.setcover.partial.MinimalCoverSearch;

public class DCFinder {

	protected long chunkLength = 10000 * 5000;
	protected int bufferLength = 5000;
	protected double errorThreshold = 0.01d;
	protected long violationsThreshold = 0L;
	protected long rsize = 0;

	public DenialConstraintSet run(Input input, PredicateBuilder predicates) {

		input.buildPLIs();
		rsize = input.getLineCount();
		
		setViolationsThreshold();

		SplitReconstructEvidenceSetBuilder evidenceSetBuilder = new SplitReconstructEvidenceSetBuilder(input,
				predicates, chunkLength, bufferLength);
		evidenceSetBuilder.buildEvidenceSet();

		DenialConstraintSet dcs = new MinimalCoverSearch(predicates.getPredicates(), violationsThreshold)
				.getDenialConstraints(evidenceSetBuilder.getFullEvidenceSet());

		return dcs;
	}

	private void setViolationsThreshold() {
		long totaltps = rsize * (rsize - 1);
		violationsThreshold = (long) Math.ceil(((double) totaltps * errorThreshold));
		log.info("Error threshold: " + errorThreshold + ".");
		log.info("Discovering DCs with at most " + violationsThreshold + " violating tuple pairs.");
	}

	private static Logger log = LoggerFactory.getLogger(DCFinder.class);

}
