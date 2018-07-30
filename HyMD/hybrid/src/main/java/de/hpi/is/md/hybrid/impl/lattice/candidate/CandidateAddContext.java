package de.hpi.is.md.hybrid.impl.lattice.candidate;

import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.LazyArray;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.NonNull;

final class CandidateAddContext extends LhsContext {

	@NonNull
	private final LazyArray<CandidateThresholdNode> children;
	@NonNull
	private final MDSite rhs;
	@NonNull
	private final LatticeMD latticeMD;
	@NonNull
	private final MDElement rhsElem;
	@NonNull
	private final Consumer<LatticeMD> callback;

	@Builder
	private CandidateAddContext(@NonNull LatticeMD latticeMD,
		@NonNull MDElement rhsElem,
		@NonNull LazyArray<CandidateThresholdNode> children,
		@NonNull Consumer<LatticeMD> callback,
		@NonNull MDSite rhs) {
		super(latticeMD.getLhs());
		this.latticeMD = latticeMD;
		this.children = children;
		this.rhsElem = rhsElem;
		this.callback = callback;
		this.rhs = rhs;
	}

	void add(int currentLhsAttr) {
		Optional<MDElement> next = getNext(currentLhsAttr);
		if (next.isPresent()) {
			next.ifPresent(this::add);
		} else {
			addToThis();
		}
	}

	private void add(MDElement next) {
		int id = next.getId();
		CandidateThresholdNode child = children.getOrCreate(id);
		int nextLhsAttr = id + 1;
		double threshold = next.getThreshold();
		child.add(latticeMD, rhsElem, nextLhsAttr, threshold);
	}

	private void addToThis() {
		setRhs();
		setLatticeMd();
	}

	private void setLatticeMd() {
		callback.accept(latticeMD);
	}

	private void setRhs() {
		int rhsAttr = rhsElem.getId();
		double threshold = rhsElem.getThreshold();
		rhs.set(rhsAttr, threshold);
	}
}
