package de.metanome.algorithms.binder.structures;

import java.util.Arrays;
import java.util.List;

import de.uni_potsdam.hpi.utils.CollectionUtils;

public class AttributeCombination implements Comparable<AttributeCombination> {

	private int table;
	private int[] attributes;

	public AttributeCombination(int table, int... attributes) {
		this.table = table;
		this.setAttributes(attributes);
	}

	public AttributeCombination(int table, int[] attributes, int attribute) {
		this.table = table;
		this.setAttributes(attributes, attribute);
	}
	
	public int size() {
		return this.attributes.length;
	}
	
	public int getTable() {
		return this.table;
	}
	
	public int[] getAttributes() {
		return this.attributes;
	}
	
	public void setAttributes(int[] attributes) {
		this.attributes = attributes;
	}
	
	public void setAttributes(int[] attributes, int attribute) {
		this.attributes = Arrays.copyOf(attributes, attributes.length + 1);
		this.attributes[attributes.length] = attribute;
	}
	
	public String[] getAttributes(List<String> names) {
		String[] attributesByName = new String[this.attributes.length];
		for (int i = 0; i < this.attributes.length; i++)
			attributesByName[i] = names.get(this.attributes[i]);
		return attributesByName;
	}
	
	public boolean contains(int attribute) {
		for (int a : this.attributes)
			if (a == attribute)
				return true;
		return false;
	}
	
	@Override
	public int hashCode() {
		int code = 0;
		int multiplier = 1;
		for (int i = 0; i < this.attributes.length; i++) {
			code = code + this.attributes[i] * multiplier;
			multiplier = multiplier * 10;
		}
		return code + this.table * multiplier;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AttributeCombination))
			return false;
		
		AttributeCombination other = (AttributeCombination) obj;
		if (this.table != other.getTable())
			return false;
		
		int[] otherColumns = other.getAttributes();
		if (this.attributes.length != otherColumns.length)
			return false;
		
		for (int i = 0; i < this.attributes.length; i++)
			if (this.attributes[i] != otherColumns[i])
				return false;
		
		return true;
	}

	@Override
	public String toString() {
		return "[" + CollectionUtils.concat(this.attributes, ",") + "]";
	}

	@Override
	public int compareTo(AttributeCombination other) {
		if (this.table == other.getTable()) {
			if (this.attributes.length == other.getAttributes().length) {
				for (int i = 0; i < this.attributes.length; i++) {
					if (this.attributes[i] < other.getAttributes()[i])
						return -1;
					if (this.attributes[i] > other.getAttributes()[i])
						return 1;
				}
				return 0;
			}
			return this.attributes.length - other.getAttributes().length;
		}
		return this.table - other.getTable();
	}
}
