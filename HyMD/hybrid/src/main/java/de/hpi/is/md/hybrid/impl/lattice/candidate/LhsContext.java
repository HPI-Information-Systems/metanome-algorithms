package de.hpi.is.md.hybrid.impl.lattice.candidate;

import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class LhsContext {

	@NonNull
	final MDSite lhs;

	Optional<MDElement> getNext(int currentLhsAttr) {
		return lhs.nextElement(currentLhsAttr);
	}

}
