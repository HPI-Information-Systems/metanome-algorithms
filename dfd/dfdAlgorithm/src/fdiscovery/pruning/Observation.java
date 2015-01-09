package fdiscovery.pruning;

public enum Observation {
	DEPENDENCY,
	NON_DEPENDENCY,
	MINIMAL_DEPENDENCY,
	CANDIDATE_MINIMAL_DEPENDENCY,
	MAXIMAL_NON_DEPENDENCY,
	CANDIDATE_MAXIMAL_NON_DEPENDENCY,
	EQUIVALENT;
	
	public boolean isCandidate() {
		return this == CANDIDATE_MAXIMAL_NON_DEPENDENCY || this == CANDIDATE_MINIMAL_DEPENDENCY;
	}
	
	public boolean isDependency() {
		return this == DEPENDENCY || this == MINIMAL_DEPENDENCY || this == CANDIDATE_MINIMAL_DEPENDENCY;
	}
	
	public boolean isNonDependency() {
		return this == NON_DEPENDENCY || this == MAXIMAL_NON_DEPENDENCY || this == CANDIDATE_MAXIMAL_NON_DEPENDENCY;
	}
}
