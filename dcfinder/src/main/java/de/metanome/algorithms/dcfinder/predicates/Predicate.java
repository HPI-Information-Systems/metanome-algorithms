package de.metanome.algorithms.dcfinder.predicates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.Operator;
import de.metanome.algorithms.dcfinder.predicates.operands.ColumnOperand;


public class Predicate implements PartitionRefiner, de.metanome.algorithm_integration.Predicate {

	private final Operator op;
	private final ColumnOperand operand1;
	private final ColumnOperand operand2;

	public Predicate(Operator op, ColumnOperand<?> operand1, ColumnOperand<?> operand2) {
		if (op == null)
			throw new IllegalArgumentException("Operator must not be null.");

		if (operand1 == null)
			throw new IllegalArgumentException("First operand must not be null.");

		if (operand2 == null)
			throw new IllegalArgumentException("Second operand must not be null.");

		this.op = op;
		this.operand1 = operand1;
		this.operand2 = operand2;
	}

	private Predicate symmetric;

	public Predicate getSymmetric() {
		if (symmetric != null)
			return symmetric;

		symmetric = predicateProvider.getPredicate(op.getSymmetric(), operand2, operand1);
		return symmetric;
	}

	private List<Predicate> implications = null;

	public Collection<Predicate> getImplications() {
		if (this.implications != null)
			return implications;
		Operator[] opImplications = op.getImplications();

		List<Predicate> implications = new ArrayList<>(opImplications.length);
		for (int i = 0; i < opImplications.length; ++i) {
			implications.add(predicateProvider.getPredicate(opImplications[i], operand1, operand2));
		}
		this.implications = Collections.unmodifiableList(implications);
		return implications;
	}

	public boolean implies(Predicate add) {
		if (add.operand1.equals(this.operand1) && add.operand2.equals(this.operand2))
			for (Operator i : op.getImplications())
				if (add.op == i)
					return true;
		return false;
	}

	public Operator getOperator() {
		return op;
	}

	public ColumnOperand<?> getOperand1() {
		return operand1;
	}

	public ColumnOperand<?> getOperand2() {
		return operand2;
	}

	public Predicate getInvT1T2() {
		ColumnOperand<?> invO1 = operand1.getInvT1T2();
		ColumnOperand<?> invO2 = operand2.getInvT1T2();

		return invO1 != null && invO2 != null ? predicateProvider.getPredicate(op, invO1, invO2) : null;
	}

	private Predicate inverse;

	public Predicate getInverse() {
		if (inverse != null)
			return inverse;

		inverse = predicateProvider.getPredicate(op.getInverse(), operand1, operand2);
		return inverse;
	}

	public boolean satisfies(int line1, int line2) {
		return op.eval(operand1.getValue(line1, line2), operand2.getValue(line1, line2));
	}

	@Override
//	public String toString() {
//		return "[Predicate: " + operand1.toString() + " " + op.getShortString() + " " + operand2.toString() + "]";
//	}
	public String toString() {
		return " " + operand1.toString() + " " + op.getShortString() + " " + operand2.toString() + "";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((op == null) ? 0 : op.hashCode());
		result = prime * result + ((operand1 == null) ? 0 : operand1.hashCode());
		result = prime * result + ((operand2 == null) ? 0 : operand2.hashCode());
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
		Predicate other = (Predicate) obj;
		if (op != other.op)
			return false;
		if (operand1 == null) {
			if (other.operand1 != null)
				return false;
		} else if (!operand1.equals(other.operand1))
			return false;
		if (operand2 == null) {
			if (other.operand2 != null)
				return false;
		} else if (!operand2.equals(other.operand2))
			return false;
		return true;
	}

	private static final PredicateProvider predicateProvider = PredicateProvider.getInstance();

	@Override
	public List<ColumnIdentifier> getColumnIdentifiers() {
		List<ColumnIdentifier> list = new ArrayList<>();
		list.add(operand1.getColumn().getColumnIdentifier());
		list.add(operand2.getColumn().getColumnIdentifier());
		return list;
	}
	
	public boolean isCrossColumn() {

		return !operand1.getColumn().getColumnIdentifier().equals(operand2.getColumn().getColumnIdentifier());
	}
}
