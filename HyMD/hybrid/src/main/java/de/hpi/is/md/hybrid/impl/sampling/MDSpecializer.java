package de.hpi.is.md.hybrid.impl.sampling;

import de.hpi.is.md.hybrid.SimilaritySet;
import de.hpi.is.md.hybrid.impl.infer.FullSpecializer;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Collection;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class MDSpecializer {

	private final FullSpecializer specializer;

	Collection<MD> specialize(MDSite lhs, MDElement rhs, SimilaritySet similaritySet) {
		return specializer.specialize(lhs, rhs, similaritySet::get);
	}

}
