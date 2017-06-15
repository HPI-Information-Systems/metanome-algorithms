package de.metanome.algorithms.binder.structures;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.BitSet;
import java.util.Collection;

public class IntSingleLinkedList {

	private Element first = null;
	private Element last = null;
	
	private Collection<Integer> seed = null;
	private int except = -1;
	
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
		this(null, -1);
	}
	
	public IntSingleLinkedList(Collection<Integer> seed) {
		this(seed, -1);
	}

	public IntSingleLinkedList(Collection<Integer> seed, int except) {
		this.seed = seed;
		this.except = except;
	}
	
	private void initialize() {
		if (this.seed != null) {
			for (int value : this.seed)
				if (value != this.except)
					this.selfAdd(value);
			this.seed = null;
		}
	}
	
	private void selfAdd(int value) {
		Element element = new Element(value);
		if (this.last == null)
			this.first = element;
		else
			this.last.next = element;
		this.last = element;
	}

	public void add(int value) {
		this.initialize();
		this.selfAdd(value);
	}
	
	public void addAll(IntSingleLinkedList values) {
		this.initialize();
		ElementIterator iterator = values.elementIterator();
		while (iterator.hasNext())
			this.selfAdd(iterator.next());
	}
	
	public boolean isEmpty() {
		this.initialize();
		
		return this.first == null;
	}

	public boolean contains(int value) {
		this.initialize();
		
		Element element = this.first;
		while (element != null) {
			if (element.value == value)
				return true;
			element = element.next;
		}
		return false;
	}

	public void setOwnValuesIn(BitSet bitSet) {
		this.initialize();
		
		Element element = this.first;
		while (element != null) {
			bitSet.set(element.value);
			element = element.next;
		}
	}
	
	public void retainAll(IntArrayList otherList) {
		this.initialize();
		
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
		this.initialize();
		
		return new ElementIterator();
	}
}
