package de.hpi.is.md.hybrid.impl.lattice.md;

import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class MDContext {

	@NonNull
	final MD md;

	Optional<MDElement> getNext(int currentLhsAttr) {
		MDSite lhs = md.getLhs();
		return lhs.nextElement(currentLhsAttr);
	}

}
