package de.hpi.is.md.hybrid.impl.md;

import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import lombok.Data;
import lombok.NonNull;

@Data
public class MDImpl implements MD {

	@NonNull
	private final MDSite lhs;
	@NonNull
	private final MDElement rhs;

	@Override
	public String toString() {
		return lhs + "->" + rhs;
	}


}
