package de.hpi.naumann.dc.algorithms.hybrid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hpi.naumann.dc.cover.PrefixMinimalCoverSearch;
import de.hpi.naumann.dc.denialcontraints.DenialConstraintSet;
import de.hpi.naumann.dc.evidenceset.HashEvidenceSet;
import de.hpi.naumann.dc.evidenceset.IEvidenceSet;
import de.hpi.naumann.dc.evidenceset.build.sampling.ColumnAwareEvidenceSetBuilder;
import de.hpi.naumann.dc.evidenceset.build.sampling.SystematicLinearEvidenceSetBuilder;
import de.hpi.naumann.dc.input.Input;
import de.hpi.naumann.dc.predicates.PredicateBuilder;

public class Hydra {

	protected int sampleRounds = 20;
	protected double efficiencyThreshold = 0.005d;

	public DenialConstraintSet run(Input input, PredicateBuilder predicates) {

		log.info("Building approximate evidence set...");
		IEvidenceSet sampleEvidenceSet = new SystematicLinearEvidenceSetBuilder(predicates,
				sampleRounds).buildEvidenceSet(input);
		log.info("Estimation size systematic sampling:" + sampleEvidenceSet.size());

		HashEvidenceSet set = new HashEvidenceSet();
		sampleEvidenceSet.getSetOfPredicateSets().forEach(i -> set.add(i));
		IEvidenceSet fullEvidenceSet = new ColumnAwareEvidenceSetBuilder(predicates).buildEvidenceSet(set, input, efficiencyThreshold);
		log.info("Evidence set size deterministic sampler: " + fullEvidenceSet.size());

		DenialConstraintSet dcsApprox = new PrefixMinimalCoverSearch(predicates).getDenialConstraints(fullEvidenceSet);
		log.info("DC count approx:" + dcsApprox.size());
		dcsApprox.minimize();
		log.info("DC count approx after minimize:" + dcsApprox.size());


		IEvidenceSet result = new ResultCompletion(input, predicates).complete(dcsApprox, sampleEvidenceSet,
				fullEvidenceSet);

		DenialConstraintSet dcs = new PrefixMinimalCoverSearch(predicates).getDenialConstraints(result);
		dcs.minimize();

		return dcs;
	}

	private static Logger log = LoggerFactory.getLogger(Hydra.class);

}
