package de.hpi.is.md.hybrid.impl.lattice.md;

import de.hpi.is.md.hybrid.md.MDSite;
import lombok.Data;
import lombok.NonNull;

@Data
class LhsRhsPair {

	@NonNull
	private final MDSite lhs;
	@NonNull
	private final MDSite rhs;
}
