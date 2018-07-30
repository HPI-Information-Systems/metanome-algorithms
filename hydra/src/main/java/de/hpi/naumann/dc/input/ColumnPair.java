package de.hpi.naumann.dc.input;

public class ColumnPair {

	private final ParsedColumn<?> c1;
	private final ParsedColumn<?> c2;
	private final boolean joinable;
	private final boolean comparable;

	public ColumnPair(ParsedColumn<?> c1, ParsedColumn<?> c2, boolean joinable, boolean comparable) {
		super();
		this.c1 = c1;
		this.c2 = c2;
		this.joinable = joinable;
		this.comparable = comparable;
	}

	public ParsedColumn<?> getC1() {
		return c1;
	}

	public ParsedColumn<?> getC2() {
		return c2;
	}

	public boolean isJoinable() {
		return joinable;
	}

	public boolean isComparable() {
		return comparable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((c1 == null) ? 0 : c1.hashCode());
		result = prime * result + ((c2 == null) ? 0 : c2.hashCode());
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
		ColumnPair other = (ColumnPair) obj;
		if (c1 == null) {
			if (other.c1 != null)
				return false;
		} else if (!c1.equals(other.c1))
			return false;
		if (c2 == null) {
			if (other.c2 != null)
				return false;
		} else if (!c2.equals(other.c2))
			return false;
		return true;
	}
}
