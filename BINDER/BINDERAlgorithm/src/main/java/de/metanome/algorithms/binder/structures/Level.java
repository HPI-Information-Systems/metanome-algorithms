package de.metanome.algorithms.binder.structures;

public class Level implements Comparable<Level> {
	
	private final int number;
	private final int emptyBuckets;
	
	public int getNumber() {
		return number;
	}

	public int getEmptyBuckets() {
		return emptyBuckets;
	}

	public Level(int number, int emptyBuckets) {
		this.number = number;
		this.emptyBuckets = emptyBuckets;
	}

	@Override
	public int compareTo(Level other) {
		if (this.number == other.number)
			return 0;
		if (this.emptyBuckets > other.emptyBuckets)
			return 1;
		if (this.emptyBuckets < other.emptyBuckets)
			return -1;
		return 0;
	}

	@Override
	public int hashCode() {
		return this.number;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Level))
			return false;
		Level other = (Level) obj;
		return (this.number == other.number) || (this.emptyBuckets == other.emptyBuckets);
	}

	@Override
	public String toString() {
		return "Level(" + this.number + "," + this.emptyBuckets + ")";
	}
}
