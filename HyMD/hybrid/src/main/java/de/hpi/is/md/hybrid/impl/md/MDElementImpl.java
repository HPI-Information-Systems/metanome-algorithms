package de.hpi.is.md.hybrid.impl.md;

import de.hpi.is.md.hybrid.md.MDElement;
import lombok.Data;

@Data
public class MDElementImpl implements MDElement {

	private final int id;
	private final double threshold;

	@Override
	public String toString() {
		return id + "@" + threshold;
	}
}
