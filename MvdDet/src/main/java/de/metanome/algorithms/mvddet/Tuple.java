package de.metanome.algorithms.mvddet;

import java.util.ArrayList;
import java.util.List;

public class Tuple {
	private int[] values;
	private boolean containsUniqueValue = false;
	
	public Tuple (int[] valueArrray){
		values = valueArrray;
		for (int i = 0; i < values.length; i++)
			if (values[i] == 0){
				containsUniqueValue = true;
				continue;
			}
	}	
	
	public int[] getValue() {
		return values;
	}
	
	public int getValueAt(int position) {
		return values[position];
	}
	
	public List<Integer> getValuesAt(List<Integer> positions){
		List<Integer> returnValues = new ArrayList<Integer>();
		
		for (int position : positions)
			returnValues.add(values[position]);
		
		return returnValues;
	}
	
	public boolean containsUniqueValue(){
		return containsUniqueValue;
	}
	
	public int size() {
		return values.length;
	}
	
	public String toString() {
		String returnString = "";
		
		for (int value : values){
			returnString += value + ", ";
		}
		return returnString.substring(0, returnString.length() - 2);
	}
	
	public boolean equalsTuple(Tuple other) {
		if (containsUniqueValue || other.containsUniqueValue())
			return false;
		for (int i = 0; i < values.length; i++){
			if (values[i] != other.getValueAt(i))
				return false;
		}
		return true;
	}
	
	public boolean equals(Object other){
		if (other == null)
			return false;
	    if (other == this)
	    	return true;
	    if (!(other instanceof Tuple))
	    	return false;
	    Tuple otherTuple = (Tuple)other;
		return equalsTuple(otherTuple);
	}
}
