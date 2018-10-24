package de.metanome.algorithms.normalize.aspects;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.results.FunctionalDependency;
import de.metanome.algorithm_integration.results.Result;

public class NormiConversion {

	private List<ColumnIdentifier> columnIdentifiers;	
	private Map<ColumnIdentifier, Integer> name2number;
	private Map<Integer, ColumnIdentifier> number2name;
	
	public NormiConversion(List<ColumnIdentifier> columnIdentifiers, Map<ColumnIdentifier, Integer> name2number, Map<Integer, ColumnIdentifier> number2name) {
		this.columnIdentifiers = columnIdentifiers;
		this.name2number = name2number;
		this.number2name = number2name;
	}

	public Map<BitSet, BitSet> toFunctionalDependencyMap(List<Result> allFds) {
		Map<BitSet, BitSet> fds = new HashMap<>(allFds.size());
		for (Result result : allFds) {
			FunctionalDependency fd = (FunctionalDependency) result;
			
			BitSet lhs = this.toBitSet(fd.getDeterminant());
			BitSet rhs = this.toBitSet(fd.getDependant());
			
			fds.merge(lhs, rhs, (a, b) -> {a.or(b); return a;});
		}
		return fds;
	}
	
	public BitSet toBitSet(ColumnCombination columnCombination) {
		BitSet bits = new BitSet(this.columnIdentifiers.size());
		columnCombination.getColumnIdentifiers().stream().forEach(identifier -> bits.set(this.name2number.get(identifier).intValue()));
		return bits;
	}
	
	public BitSet toBitSet(ColumnIdentifier columnIdentifier) {
		BitSet bits = new BitSet(this.columnIdentifiers.size());
		bits.set(this.name2number.get(columnIdentifier).intValue());
		return bits;
	}
	
	public List<FunctionalDependency> toFunctionalDependencies(BitSet lhs, BitSet rhs) {
		List<FunctionalDependency> fds = new ArrayList<>(rhs.cardinality());
		for (int rhsAttr = rhs.nextSetBit(0); rhsAttr >= 0; rhsAttr = rhs.nextSetBit(rhsAttr + 1))
			fds.add(this.toFunctionalDependency(lhs, rhsAttr));
		return fds;
	}
	
	public FunctionalDependency toFunctionalDependency(BitSet lhs, int rhs) {
		return new FunctionalDependency(this.toColumnCombination(lhs), this.toColumnIdentifier(rhs));
	}
	
	public ColumnCombination toColumnCombination(BitSet columns) {
		List<ColumnIdentifier> columnCombination = new ArrayList<>(columns.cardinality());
		for (int attr = columns.nextSetBit(0); attr >= 0; attr = columns.nextSetBit(attr + 1))
			columnCombination.add(this.toColumnIdentifier(attr));
		return new ColumnCombination(columnCombination.toArray(new ColumnIdentifier[0]));
	}
	
	public ColumnIdentifier toColumnIdentifier(int attribute) {
		return this.number2name.get(Integer.valueOf(attribute));
	}
	
	public String format(ColumnCombination columnCombination) {
		List<ColumnIdentifier> columnIdentifiers = new ArrayList<>(columnCombination.getColumnIdentifiers());
		Collections.sort(columnIdentifiers, new Comparator<ColumnIdentifier>() {
			@Override
			public int compare(ColumnIdentifier o1, ColumnIdentifier o2) {
				return NormiConversion.this.name2number.get(o1).intValue() - NormiConversion.this.name2number.get(o2).intValue();
			}
		});
		
		StringBuilder builder = new StringBuilder("[");
		if (columnIdentifiers.size() != 0) {
			columnIdentifiers.stream()
				.map(identifier -> identifier.getColumnIdentifier())
				.forEach(identifier -> builder.append(identifier + ", "));
			builder.setLength(builder.length() - 2);
		}
		builder.append("]");
		return builder.toString();
	}
	
	public String formatSchema(BitSet columnCombination, BitSet key) {
		ColumnCombination attributeColumnCombination = this.toColumnCombination(columnCombination);
		ColumnCombination keyColumnCombination = this.toColumnCombination((key == null) ? new BitSet() : key);
				
		List<ColumnIdentifier> columnIdentifiers = new ArrayList<>(attributeColumnCombination.getColumnIdentifiers());
		Collections.sort(columnIdentifiers, new Comparator<ColumnIdentifier>() {
			@Override
			public int compare(ColumnIdentifier o1, ColumnIdentifier o2) {
				return NormiConversion.this.name2number.get(o1).intValue() - NormiConversion.this.name2number.get(o2).intValue();
			}
		});
		
		StringBuilder builder = new StringBuilder("[");
		columnIdentifiers.stream()
			.map(identifier -> keyColumnCombination.getColumnIdentifiers().contains(identifier) ? identifier.getColumnIdentifier().toUpperCase() : identifier.getColumnIdentifier())
			.forEach(identifier -> builder.append(identifier + ", "));
		builder.setLength(builder.length() - 2);
		builder.append("]");
		return builder.toString();
	}
	
	public String formatKey(BitSet key) {
		return this.format(this.toColumnCombination(key));
	}
	
	public String formatFd(BitSet lhs, BitSet rhs) {
		return this.format(this.toColumnCombination(lhs)) + " --> " + this.format(this.toColumnCombination(rhs));
	}
}
