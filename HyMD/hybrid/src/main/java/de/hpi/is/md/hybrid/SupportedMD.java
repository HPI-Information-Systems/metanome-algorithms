package de.hpi.is.md.hybrid;

import de.hpi.is.md.hybrid.md.MD;
import lombok.Data;
import lombok.NonNull;

@Data
public class SupportedMD {

	@NonNull
	private final MD md;
	private final long support;
}
