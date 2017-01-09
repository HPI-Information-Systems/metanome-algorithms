package de.hpi.metanome.algorithms.hyucc.structures;

import org.apache.lucene.util.OpenBitSet;

public class UCCTreeElementUCCPair {
	
	private final UCCTreeElement element;
	private final OpenBitSet ucc;
	
	public UCCTreeElement getElement() {
		return this.element;
	}

	public OpenBitSet getUCC() {
		return this.ucc;
	}

	public UCCTreeElementUCCPair(UCCTreeElement element, OpenBitSet ucc) {
		this.element = element;
		this.ucc = ucc;
	}
}

