package de.metanome.algorithms.mvddet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class MvD {
	private List<Integer> leftHandSide;
	private List<Integer> rightHandSide;
	private List<Integer> remainingAttributes;
	private int attributeCount;
	
	public MvD(List<Integer> leftSide, List<Integer> rightSide, int attrCount){
		leftHandSide = leftSide;
		Collections.sort(leftHandSide);
		rightHandSide = rightSide;
		Collections.sort(rightHandSide);
		attributeCount = attrCount;
		generateRemainingAttributes();
	}
	
	public MvD(int leftSide, List<Integer> rightSide, int attrCount){
		leftHandSide = new ArrayList<Integer>();
		leftHandSide.add(leftSide);
		rightHandSide = rightSide;
		Collections.sort(rightHandSide);
		attributeCount = attrCount;
		generateRemainingAttributes();
	}
	
	public MvD(List<Integer> leftSide, int rightSide, int attrCount){
		leftHandSide = leftSide;
		Collections.sort(leftHandSide);
		rightHandSide = new ArrayList<Integer>();
		rightHandSide.add(rightSide);
		attributeCount = attrCount;
		generateRemainingAttributes();
	}
	
	public MvD(int leftSide, int rightSide, int attrCount){
		leftHandSide = new ArrayList<Integer>();
		leftHandSide.add(leftSide);
		rightHandSide = new ArrayList<Integer>();
		rightHandSide.add(rightSide);
		attributeCount = attrCount;
		generateRemainingAttributes();
	}
	
	public MvD(String mvdString, int attrCount){
		mvdString = mvdString.substring(1, mvdString.length() - 1);
		String[] parts = mvdString.split(" ->> ");
		String[] lhsParts = parts[0].split(", ");
		String[] rhsParts = parts[1].split(", ");
		
		leftHandSide = new ArrayList<Integer>();
		for (String lhsString : lhsParts)
			leftHandSide.add(Integer.parseInt(lhsString));
		rightHandSide = new ArrayList<Integer>();
		for (String rhsString : rhsParts)
			rightHandSide.add(Integer.parseInt(rhsString));
		attributeCount = attrCount;
		generateRemainingAttributes();
	}
	
	
	private void generateRemainingAttributes(){
		remainingAttributes = new ArrayList<Integer>();
		for (int i = 0; i < attributeCount; i++){
			if (!leftHandSide.contains(i) && !rightHandSide.contains(i))
				remainingAttributes.add(i);
		}
	}
	
	
	
	public List<Integer> getLeftHandSide(){
		return leftHandSide;
	}

	public List<Integer> getRightHandSide(){
		return rightHandSide;
	}
	
	
	public List<Integer> getRemainingAttributes(){
		return remainingAttributes;
	}
	
	public String toString(){
		String lhsString = "";
		String rhsString = "";
		for (int part : leftHandSide)
			lhsString += part + ", ";
		for (int part : rightHandSide)
			rhsString += part + ", ";
		
		if (lhsString == "")
			lhsString = "empty Set, ";
		if (rhsString == "")
			rhsString = "empty Set, ";
		
		return "[" + lhsString.substring(0, lhsString.length()-2) + " ->> " + rhsString.substring(0, rhsString.length()-2) + "]";
	}
	
	public boolean equalsMvD(MvD other){
		return leftHandSide.equals(other.getLeftHandSide()) && rightHandSide.equals(other.getRightHandSide());
	}
	
	@Override
	public boolean equals(Object other){
		if (other == null)
			return false;
	    if (other == this)
	    	return true;
	    if (!(other instanceof MvD))
	    	return false;
	    MvD otherMvd = (MvD)other;
		return equalsMvD(otherMvd);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((leftHandSide == null) ? 0 : leftHandSide.hashCode());
		result = prime * result + ((rightHandSide == null) ? 0 : rightHandSide.hashCode());
		return result;
	}
	
	public boolean isMinimal(HashSet<MvD> others){
		for (MvD other : others)
			if (other.isMoreMeaningfulThan(this))
				return false;
		return true;
	}
	
	public boolean isRefutedBy(HashSet<MvD> others){
		for (MvD other : others)
			if (this.isMoreMeaningfulThan(other))
				return true;
		return false;
	}
	
	public boolean isMoreMeaningfulThan(MvD other){
		if (this.equals(other))
			return false;

		//A ->> BC is more meaningful than ACD ->> B
		if (other.getLeftHandSide().containsAll(leftHandSide)
				&& rightHandSide.containsAll(other.rightHandSide)
				&& getRemainingAttributes().containsAll(other.getRemainingAttributes()))
			return true;
		
		//A ->> B is more meaningful than AC ->> BD
		if (other.getLeftHandSide().containsAll(leftHandSide)
				&& other.rightHandSide.containsAll(rightHandSide))
			return true;
		
		return false;
	}
}
