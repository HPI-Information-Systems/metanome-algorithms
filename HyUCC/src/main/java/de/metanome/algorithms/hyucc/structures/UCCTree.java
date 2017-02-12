package de.metanome.algorithms.hyucc.structures;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.util.OpenBitSet;

import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class UCCTree extends UCCTreeElement {

	protected int depth = 0;
	protected int maxDepth;
	
	public UCCTree(int numAttributes, int maxDepth) {
		super(numAttributes, false);
		this.maxDepth = maxDepth;
		this.children = new UCCTreeElement[numAttributes];
	}

	public int getDepth() {
		return this.depth;
	}

	public int getMaxDepth() {
		return this.maxDepth;
	}

	@Override
	public String toString() {
		return "[" + this.depth + " depth, " + this.maxDepth + " maxDepth]";
	}

	public void trim(int newDepth) {
		this.trimRecursive(0, newDepth);
		this.depth = newDepth;
		this.maxDepth = newDepth;
	}

	public void addMostGeneralUniques() {
		for (int attr = 0; attr < this.numAttributes; attr++)
			this.children[attr] = new UCCTreeElement(this.numAttributes, true);
	}

	public UCCTreeElement addUniqueColumnCombination(OpenBitSet ucc) {
		UCCTreeElement currentNode = this;

		int uccLength = 0;
		for (int i = ucc.nextSetBit(0); i >= 0; i = ucc.nextSetBit(i + 1)) {
			uccLength++;
			
			if (currentNode.getChildren() == null) {
				currentNode.setChildren(new UCCTreeElement[this.numAttributes]);
				currentNode.getChildren()[i] = new UCCTreeElement(this.numAttributes, false);
			}
			else if (currentNode.getChildren()[i] == null) {
				currentNode.getChildren()[i] = new UCCTreeElement(this.numAttributes, false);
			}
				
			currentNode = currentNode.getChildren()[i];
		}
		currentNode.setUCC(true);
		
		this.depth = Math.max(this.depth, uccLength);
		return currentNode;
	}

	public UCCTreeElement addUniqueColumnCombinationGetIfNew(OpenBitSet ucc) {
		UCCTreeElement currentNode = this;

		boolean isNew = false;
		int lhsLength = 0;
		for (int i = ucc.nextSetBit(0); i >= 0; i = ucc.nextSetBit(i + 1)) {
			lhsLength++;
			
			if (currentNode.getChildren() == null) {
				currentNode.setChildren(new UCCTreeElement[this.numAttributes]);
				currentNode.getChildren()[i] = new UCCTreeElement(this.numAttributes, false);
				isNew = true;
			}
			else if (currentNode.getChildren()[i] == null) {
				currentNode.getChildren()[i] = new UCCTreeElement(this.numAttributes, false);
				isNew = true;
			}
				
			currentNode = currentNode.getChildren()[i];
		}
		currentNode.setUCC(true);
		
		this.depth = Math.max(this.depth, lhsLength);
		if (isNew)
			return currentNode;
		return null;
	}

	public int addUniqueColumnCombinationsInto(UniqueColumnCombinationResultReceiver resultReceiver, ObjectArrayList<ColumnIdentifier> columnIdentifiers, List<PositionListIndex> plis) throws CouldNotReceiveResultException, ColumnNameMismatchException {
		return this.addUniqueColumnCombinationsInto(resultReceiver, new OpenBitSet(), columnIdentifiers, plis);
	}

	public void removeUniqueColumnCombination(OpenBitSet ucc) {
		int currentUCCAttr = ucc.nextSetBit(0);
		this.removeRecursive(ucc, currentUCCAttr);
	}

	public List<OpenBitSet> getUCCAndGeneralizations(OpenBitSet ucc) {
		List<OpenBitSet> foundUCCs = new ArrayList<>();
		OpenBitSet currentUCC = new OpenBitSet();
		int nextUCCAttr = ucc.nextSetBit(0);
		this.getUCCAndGeneralizations(ucc, nextUCCAttr, currentUCC, foundUCCs);
		return foundUCCs;
	}

	public boolean containsUCCOrGeneralization(OpenBitSet ucc) {
		int nextUCCAttr = ucc.nextSetBit(0);
		return this.containsUCCOrGeneralization(ucc, nextUCCAttr);
	}
	
	public boolean containsUCCOrSpecialization(OpenBitSet ucc) {
		int nextUCCAttr = ucc.nextSetBit(0);
		return this.containsUCCOrSpecialization(ucc, nextUCCAttr);
	}

	public List<UCCTreeElementUCCPair> getLevel(int level) {
		List<UCCTreeElementUCCPair> result = new ArrayList<>();
		OpenBitSet currentUCC = new OpenBitSet();
		int currentLevel = 0;
		this.getLevel(level, currentLevel, currentUCC, result);
		return result;
	}

}
