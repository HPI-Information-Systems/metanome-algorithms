package de.metanome.algorithms.mvddet;

public class MvDAlgorithmConfig {
	private boolean removeDuplicates;
	private boolean markUniqueValues;
	private boolean convertToIntTuples;
	private boolean usePLIs;
//	private int maxLhsSize;
	
	public enum PruningType {
		NO_PRUNING,
		RELEVANT_ONLY,
		RELEVANT_NON_COMPLEMENT,
		BOTTOM_UP,
		TOP_DOWN,
		LHS_FIRST
	};
	
	private PruningType pruningType;
	public long totalTime;
	public long loadRelationTime;
	public long removeDuplicatesTime;
	public long otherPreOperationsTime;
	public long totalPruningTime;
	public long timeCheckingMvD;
	public long mvdMinimizingTime;
	
	public long counterMvDChecks;
	public long counterValidMvD;
	public long counterMinimalMvD;
	
	public String minimalMvdString;
	

	public boolean isRemoveDuplicates() {
		return removeDuplicates;
	}

	public void setRemoveDuplicates(boolean removeDuplicates) {
		this.removeDuplicates = removeDuplicates;
	}

	public boolean isMarkUniqueValues() {
		return markUniqueValues;
	}

	public void setMarkUniqueValues(boolean markUniqueValues) {
		this.markUniqueValues = markUniqueValues;
	}

	public boolean isConvertToIntTuples() {
		return convertToIntTuples;
	}

	public void setConvertToIntTuples(boolean convertToIntTuples) {
		this.convertToIntTuples = convertToIntTuples;
	}

	public boolean isUsePLIs() {
		return usePLIs;
	}

	public void setUsePLIs(boolean usePLIs) {
		this.usePLIs = usePLIs;
	}

	public PruningType getPruningType() {
		return pruningType;
	}

	public void setPruningType(PruningType pruningType) {
		this.pruningType = pruningType;
	}
	
	public void setPruningType(int pruningTypeID) {
		if (pruningTypeID == 0)
			this.pruningType = PruningType.NO_PRUNING;
		else if (pruningTypeID == 1)
			this.pruningType = PruningType.RELEVANT_ONLY;
		else if (pruningTypeID == 2)
			this.pruningType = PruningType.RELEVANT_NON_COMPLEMENT;
		else if (pruningTypeID == 3)
			this.pruningType = PruningType.BOTTOM_UP;
		else if (pruningTypeID == 4)
			this.pruningType = PruningType.TOP_DOWN;
		else if (pruningTypeID == 5)
			this.pruningType = PruningType.LHS_FIRST;
	}
	
//	public int getMaxLhsSize(){
//		return maxLhsSize;
//	}
//	
//	public void setMaxLhsSize(int maxLhsSize){
//		this.maxLhsSize = maxLhsSize;
//	}
}
