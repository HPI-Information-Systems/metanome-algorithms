package de.hpi.is.md.util.enforce;

import de.hpi.is.md.MatchingDependency.ColumnMatchWithThreshold;
import java.util.Collection;

public interface MDEnforcer {

	Collection<EnforceMatch> enforce(Collection<ColumnMatchWithThreshold<?>> lhs);
}
