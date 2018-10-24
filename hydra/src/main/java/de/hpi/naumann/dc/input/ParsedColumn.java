package de.hpi.naumann.dc.input;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.HashMultiset;

import de.metanome.algorithm_integration.ColumnIdentifier;

public class ParsedColumn<T extends Comparable<T>> {
	private final String tableName;
	private final String name;
	private final HashMultiset<T> valueSet = HashMultiset.create();
	private final List<T> values = new ArrayList<>();
	private final Class<T> type;
	private final int index;

	public ParsedColumn(String tableName, String name, Class<T> type, int index) {
		this.tableName = tableName;
		this.name = name;
		this.type = type;
		this.index = index;
	}

	public void addLine(T value) {
		valueSet.add(value);
		values.add(value);
	}

	public T getValue(int line) {
		return values.get(line);
	}

	public String getTableName() {
		return tableName;
	}

	public String getName() {
		return name;
	}

	public ColumnIdentifier getColumnIdentifier() {
		return new ColumnIdentifier(tableName, name);
	}

	public int getIndex() {
		return index;
	}

	public Class<T> getType() {
		return type;
	}

	@Override
	public String toString() {
		return tableName + "." + name;
	}

	public boolean isComparableType() {
		return getType().equals(Double.class) || getType().equals(Long.class);
	}

	public double getAverage() {
		double avg = 0.0d;
		int size = values.size();
		if (type.equals(Double.class)) {
			for (int i = 0; i < size; i++) {
				Double l = (Double) values.get(i);
				double tmp = l.doubleValue() / size;
				avg += tmp;
			}
		} else if (type.equals(Long.class)) {
			for (int i = 0; i < size; i++) {
				Long l = (Long) values.get(i);
				double tmp = l.doubleValue() / size;
				avg += tmp;
			}
		}

		return avg;
	}

	public double getSharedPercentage(ParsedColumn<?> c2) {
		int totalCount = 0;
		int sharedCount = 0;
		for (T s : valueSet.elementSet()) {
			int thisCount = valueSet.count(s);
			int otherCount = c2.valueSet.count(s);
			sharedCount += Math.min(thisCount, otherCount);
			totalCount += Math.max(thisCount, otherCount);
		}
		return ((double) sharedCount) / ((double) totalCount);
	}

}
