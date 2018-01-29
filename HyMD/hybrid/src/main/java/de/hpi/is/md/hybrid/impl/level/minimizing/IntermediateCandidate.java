package de.hpi.is.md.hybrid.impl.level.minimizing;

import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.md.MDElement;
import java.util.Collection;
import java.util.function.BiConsumer;
import lombok.Data;
import lombok.NonNull;

@Data
class IntermediateCandidate {

	@NonNull
	private final LatticeMD latticeMd;
	@NonNull
	private final Collection<MDElement> rhs;

	public void forEach(BiConsumer<LatticeMD, MDElement> action) {
		rhs.forEach(element -> action.accept(latticeMd, element));
	}

	boolean isNotEmpty() {
		return !rhs.isEmpty();
	}

}
