package de.hpi.naumann.dc.predicates;

import java.util.HashMap;
import java.util.Map;

import de.hpi.naumann.dc.predicates.operands.ColumnOperand;
import de.metanome.algorithm_integration.Operator;

public class PredicateProvider {
	private static PredicateProvider instance;

	private Map<Operator, Map<ColumnOperand<?>, Map<ColumnOperand<?>, Predicate>>> predicates;

	private PredicateProvider() {
		predicates = new HashMap<>();
	}
	
	public Predicate getPredicate(Operator op, ColumnOperand<?> op1, ColumnOperand<?> op2) {
		Map<ColumnOperand<?>, Predicate> map = predicates.computeIfAbsent(op,  a -> new HashMap<>()).computeIfAbsent(op1, a -> new HashMap<>());
		Predicate p = map.get(op2);
		if(p == null) {
			p = new Predicate(op, op1, op2);
			map.put(op2, p);
		}
		return p;
	}

	static {
        instance = new PredicateProvider();
	}

    public static PredicateProvider getInstance() {
        return instance;
    }
}
