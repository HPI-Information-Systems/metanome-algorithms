package de.hpi.is.md.util.enforce;

import de.hpi.is.md.relational.Relation;

public interface EnforcerBuilder {

	MDEnforcer create(Relation r, Relation s);

	default MDEnforcer create(Relation r) {
		return create(r, r);
	}
}
