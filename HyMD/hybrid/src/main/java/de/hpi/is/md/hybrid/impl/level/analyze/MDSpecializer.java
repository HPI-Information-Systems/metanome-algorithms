package de.hpi.is.md.hybrid.impl.level.analyze;

import de.hpi.is.md.hybrid.impl.infer.FullSpecializer;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Collection;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MDSpecializer {

	private final FullSpecializer specializer;

	Collection<MD> specialize(MD md) {
		MDSite lhs = md.getLhs();
		MDElement rhs = md.getRhs();
		return specializer.specialize(lhs, rhs, lhs::getOrDefault);
	}

}
