package de.hpi.naumann.dc.predicates.operands;

import de.hpi.naumann.dc.input.ParsedColumn;

public class ColumnOperand<T extends Comparable<T>> {
	private ParsedColumn<T> column;
	private int index;

	public ColumnOperand(ParsedColumn<T> column, int index) {
		this.column = column;
		this.index = index;
	}

	public T getValue(int line1, int line2) {
		return column.getValue(index == 0 ? line1 : line2);
	}

	public ParsedColumn<T> getColumn() {
		return column;
	}

	public int getIndex() {
		return index;
	}

	public ColumnOperand<T> getInvT1T2() {
		return new ColumnOperand<>(getColumn(), index == 0 ? 1 : 0);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((column == null) ? 0 : column.hashCode());
		result = prime * result + index;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColumnOperand<?> other = (ColumnOperand<?>) obj;
		if (column == null) {
			if (other.column != null)
				return false;
		} else if (!column.equals(other.column))
			return false;
		if (index != other.index)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "t" + index + "." + column.toString();
	}
}
