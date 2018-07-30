package de.hpi.is.md.hybrid.impl.level.minimizing;

import de.hpi.is.md.hybrid.impl.level.Candidate;
import java.util.Collection;

interface Minimizer {

	Collection<Candidate> toCandidates(Iterable<IntermediateCandidate> preCandidates);
}
