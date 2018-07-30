package de.hpi.is.md;

import de.hpi.is.md.relational.Relation;
import de.hpi.is.md.result.ResultEmitter;

public interface Discoverer extends ResultEmitter<MatchingDependencyResult> {

	default void discover(Relation relation) {
		discover(relation, relation);
	}

	void discover(Relation r, Relation s);
}
