package de.metanome.algorithms.binder.structures;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.BitSet;
import java.util.Collection;

public class IntSingleLinkedList {

	private Element first = null;
	private Element last = null;
	
	public class Element {
		
		public int value;
		public Element next = null;
		
		public Element(int value) {
			this.value = value;
		}
	}
	
	public class ElementIterator {
		
		private Element previous = null;
		private Element current = null;
		private Element next = null;
		
		public ElementIterator() {
			this.next = first;
		}
		
		public boolean hasNext() {
			return this.next != null;
		}
		
		public int next() {
			this.previous = this.current;
			this.current = this.next;
			if (this.current != null)
				this.next = this.current.next;
			return this.current.value;
		}
		
		public void remove() {
			if (this.previous == null)
				first = this.next;
			else
				this.previous.next = this.next;
		}
	}

	public Element getFirst() {
		return this.first;
	}

	public Element getLast() {
		return this.last;
	}

	public IntSingleLinkedList() {
	}
	
	public IntSingleLinkedList(Collection<Integer> seed) {
		for (Integer value : seed)
			this.add(value);
	}

	public IntSingleLinkedList(Collection<Integer> seed, int except) {
		for (int value : seed)
			if (value != except)
				this.add(value);
	}

	public IntSingleLinkedList(Collection<Integer> seed, int except, BitSet excepts) {
		for (int value : seed)
			if ((value != except) && (!excepts.get(value)))
				this.add(value);
	}

	public boolean isEmpty() {
		return this.first == null;
	}

	public boolean contains(int value) {
		Element element = this.first;
		while (element != null) {
			if (element.value == value)
				return true;
			element = element.next;
		}
		return false;
	}

	public void add(Integer value) {
		this.add(value.intValue());
	}

	public void add(int value) {
		Element element = new Element(value);
		if (this.last == null)
			this.first = element;
		else
			this.last.next = element;
		this.last = element;
	}
	
	public void setOwnValuesIn(BitSet bitSet) {
		Element element = this.first;
		while (element != null) {
			bitSet.set(element.value);
			element = element.next;
		}
	}

	public void addOwnValuesTo(IntList list) {
		Element element = this.first;
		while (element != null) {
			list.add(element.value);
			element = element.next;
		}
	}
	
	public void retainAll(IntArrayList otherList) {
		Element previous = null;
		Element current = this.first;
		while (current != null) {
			if (otherList.contains(current.value)) {
				previous = current;
				current = current.next;
			}
			else {
				if (previous == null) {
					this.first = current.next;
					current.next = null;
					if (this.first == null)
						current = null;
					else
						current = this.first;
					
				}
				else {
					previous.next = current.next;
					current.next = null;
					current = previous.next;
				}
			}
		}
	}

	public void clear() {
		this.first = null;
		this.last = null;
	}
	
	public ElementIterator elementIterator() {
		return new ElementIterator();
	}
}
