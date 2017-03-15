package de.metanome.algorithms.mvddet;

import java.util.ArrayList;
import java.util.List;

public class StringTuple {
	String[] values;
	boolean containsUniqueValue = false;
	
	public StringTuple (String valuesString, String splitCharacter) {
		values = valuesString.split(splitCharacter);
	}
	
	public StringTuple (String[] valueArrray){
		values = valueArrray;
	}
	
	public StringTuple (List<String> valueList){
		values = new String[valueList.size()];
		values = valueList.toArray(values);
	}
	
	
	public String[] getValue() {
		return values;
	}
	
	public String getValueAt(int position) {
		return values[position];
	}
	
	public void setValueAt(int position, String value){
		values[position] = value;
	}
	
	public List<String> getValuesAt(List<Integer> positions){
		List<String> returnValues = new ArrayList<String>();
		
		for (int position : positions)
			returnValues.add(values[position]);
		
		return returnValues;
	}
	
	public int size() {
		return values.length;
	}
	
	public String toString() {
		String returnString = "";
		
		for (String value : values){
			returnString += value + ", ";
		}
		return returnString.substring(0, returnString.length() - 2);
	}
	
	public boolean equalsTuple(StringTuple other) {
		try {
			if (values.length != other.size()) {
				return false;
			}
			for (int i = 0; i < values.length; i++) {
				if (!values[i].equals(other.getValueAt(i)))
					return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean equals(Object other){
		if (other == null)
			return false;
	    if (other == this)
	    	return true;
	    if (!(other instanceof StringTuple))
	    	return false;
	    StringTuple otherTuple = (StringTuple)other;
		return equalsTuple(otherTuple);
	}
	
	public boolean containsUniqueValue(){
		return containsUniqueValue;
	}
	
	public void setContainsUniqueValue(boolean containsUniqueValue){
		this.containsUniqueValue = containsUniqueValue;
	}
}
