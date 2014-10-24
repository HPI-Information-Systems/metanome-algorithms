package de.metanome.algorithms.binder.structures;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntListIterator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.uni_potsdam.hpi.utils.DatabaseUtils;

public class Attribute implements Comparable<Attribute> {

	protected int attributeId;
	protected String currentValue = null;
	protected List<String> values = null;
	protected IntLinkedOpenHashSet referenced;
	protected IntLinkedOpenHashSet dependents;

	public Attribute(int attributeId, List<String> attributeTypes, List<String> values, PruningStatistics pruningStatistics) {
		this(attributeId, attributeTypes, pruningStatistics);
		this.setValues(values);
	}
	
	public Attribute(int attributeId, List<String> attributeTypes, PruningStatistics pruningStatistics) {
		this.attributeId = attributeId;
		
		int numAttributes = attributeTypes.size();
		this.referenced = new IntLinkedOpenHashSet(numAttributes);
		this.dependents = new IntLinkedOpenHashSet(numAttributes);
		
		for (int i = 0; i < numAttributes; i++) {
			if ((i != this.attributeId) && (DatabaseUtils.matchSameDataTypeClass(attributeTypes.get(i), attributeTypes.get(this.attributeId)))) {
				if (pruningStatistics.isValid(i, this.attributeId))
					this.dependents.add(i);
				if (pruningStatistics.isValid(this.attributeId, i))
					this.referenced.add(i);
			}
		}
	}
	
	public void setValues(List<String> values) {
		this.values = values;
		Collections.sort(this.values, new Comparator<String>() {
				@Override
				public int compare(String first, String second) {
					return first.compareTo(second) * (-1);
				}
			}); // Sort in reverse order to be able to remove values in reverse order that then results in a correct removal order
		this.nextValue();
	}
	
	public int getAttributeId() {
		return this.attributeId;
	}

	public String getCurrentValue() {
		return this.currentValue;
	}
	
	public IntLinkedOpenHashSet getReferenced() {
		return this.referenced;
	}

	public IntLinkedOpenHashSet getDependents() {
		return this.dependents;
	}

	public void nextValue() {
		String nextValue = (this.values.isEmpty()) ? null : this.values.remove(this.values.size() - 1);
		
		while ((nextValue != null) && nextValue.equals(this.currentValue)) // Ignore duplicates
			nextValue = (this.values.isEmpty()) ? null : this.values.remove(this.values.size() - 1);
		
		this.currentValue = nextValue;
	}
	
	public void intersectReferenced(IntArrayList referencedAttributes, Int2ObjectOpenHashMap<Attribute> attributeMap) {
		IntListIterator referencedIterator = this.referenced.iterator();
		while (referencedIterator.hasNext()) {
			int referenced = referencedIterator.nextInt();
			
			if (referencedAttributes.contains(referenced))
				continue;
			
			referencedIterator.remove();
			attributeMap.get(referenced).removeDependent(this.attributeId);
		}
	}
	
	public void removeDependent(int dependent) {
		this.dependents.rem(dependent);
	}
	
	public boolean hasFinished() {
		if (this.currentValue == null)
			return true;
		return false;
	}

	public boolean isPruneable() {
		if (this.referenced.isEmpty() && this.dependents.isEmpty())
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		return this.attributeId;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Attribute))
			return false;
		Attribute other = (Attribute) obj;
		return this.compareTo(other) == 0;
	}

	@Override
	public String toString() {
		return "(" + this.attributeId + " - \"" + this.currentValue + "\")";
	}

	@Override
	public int compareTo(Attribute other) {
		if ((this.getCurrentValue() == null) && (other.getCurrentValue() == null)) {
			if (this.getAttributeId() > other.getAttributeId())
				return 1;
			if (this.getAttributeId() < other.getAttributeId())
				return -1;
			return 0;
		}
		
		if (this.getCurrentValue() == null)
			return 1;
		if (other.getCurrentValue() == null)
			return -1;
		
		int order = this.getCurrentValue().compareTo(other.getCurrentValue());
		if (order == 0) {
			if (this.getAttributeId() > other.getAttributeId())
				return 1;
			if (this.getAttributeId() < other.getAttributeId())
				return -1;
			return 0;
		}
		return order;
	}
}
