package de.hpi.is.md.hybrid;

import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Collection;

public interface Validator {

	ValidationResult validate(MDSite lhs, Collection<Rhs> rhs);
}
