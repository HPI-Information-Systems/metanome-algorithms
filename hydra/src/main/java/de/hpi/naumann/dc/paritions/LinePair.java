package de.hpi.naumann.dc.paritions;

public class LinePair {

	private int line1;
	private int line2;

	public LinePair(int line1, int line2) {
		this.line1 = line1;
		this.line2 = line2;
	}

	public int getLine1() {
		return line1;
	}

	public int getLine2() {
		return line2;
	}

	@Override
	public String toString() {
		return "(" + line1 + "," + line2 + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + line1;
		result = prime * result + line2;
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
		LinePair other = (LinePair) obj;
		if (line1 != other.line1)
			return false;
		if (line2 != other.line2)
			return false;
		return true;
	}
}
