package de.hpi.is.md.util.enforce;

import java.util.Collection;

interface ActualEnforcer {

	Collection<CompressedEnforceMatch> enforce();
}
