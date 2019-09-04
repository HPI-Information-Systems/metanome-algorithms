package de.metanome.algorithms.dcfinder.setcover;

import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraintSet;
import de.metanome.algorithms.dcfinder.evidenceset.IEvidenceSet;

public interface IMinimalCoverSearch {
	public DenialConstraintSet getDenialConstraints(IEvidenceSet evidenceSet);
}
