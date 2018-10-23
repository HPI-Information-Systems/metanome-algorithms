package de.metanome.algorithms.hyucc.structures;

import java.util.BitSet;

public class UCCTreeElementUCCPair {
	
	private final UCCTreeElement element;
	private final BitSet ucc;
	
	public UCCTreeElement getElement() {
		return this.element;
	}

	public BitSet getUCC() {
		return this.ucc;
	}

	public UCCTreeElementUCCPair(UCCTreeElement element, BitSet ucc) {
		this.element = element;
		this.ucc = ucc;
	}
}

