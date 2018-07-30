package de.hpi.naumann.dc.predicates.sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.PredicateProvider;
import de.metanome.algorithm_integration.Operator;

public class Closure {

	private static class TrivialPredicateSetException extends Exception {
		private static final long serialVersionUID = -6515524472203224770L;
	}

	private PredicateBitSet start;
	private PredicateBitSet closure;
	private boolean added;
	private Map<Operator, List<Predicate>> grouped;

	public Closure(PredicateBitSet start) {
		this.start = start;
		this.grouped = new HashMap<>();
		this.closure = PredicateSetFactory.create();
	}
	
	public Closure(Closure closure, Predicate add) {
		this.closure = PredicateSetFactory.create(closure.closure);
		this.start = PredicateSetFactory.create(add);
		this.grouped = new HashMap<>(closure.grouped);
	}

	public boolean construct() {
		try {
			for(Predicate p : start) {
				addAll(p.getImplications());
				if (p.getSymmetric() != null)
					addAll(p.getSymmetric().getImplications());
			}

			added = true;
			while (added) {
				added = false;
				transitivityStep();
			}
			return true;
		} catch (TrivialPredicateSetException e) {
			return false;
		}
	}
	
	public PredicateBitSet getClosure() {
		return closure;
	}
	
	private void transitivityStep() throws TrivialPredicateSetException {
		Set<Predicate> additions = new HashSet<Predicate>();
		closure.forEach(p -> {
			if (p.getSymmetric() != null)
				additions.addAll(p.getSymmetric().getImplications());
			additions.addAll(p.getImplications());
//			additions.add(predicateProvider.getPredicate(Operator.EQUAL, p.getOperand1(), p.getOperand1()));
//			additions.add(predicateProvider.getPredicate(Operator.EQUAL, p.getOperand2(), p.getOperand2()));
//			additions.add(predicateProvider.getPredicate(Operator.GREATER_EQUAL, p.getOperand1(), p.getOperand1()));
//			additions.add(predicateProvider.getPredicate(Operator.GREATER_EQUAL, p.getOperand2(), p.getOperand2()));
//			additions.add(predicateProvider.getPredicate(Operator.LESS_EQUAL, p.getOperand1(), p.getOperand1()));
//			additions.add(predicateProvider.getPredicate(Operator.LESS_EQUAL, p.getOperand2(), p.getOperand2()));
		});

		for (Entry<Operator, List<Predicate>> entry : grouped.entrySet()) {
			Operator op = entry.getKey();
			List<Predicate> list = entry.getValue();
			for(Operator opTrans : op.getTransitives()) {
				List<Predicate> pTrans = grouped.get(opTrans);
				if(pTrans == null)
					continue;
			
				for (Predicate p : list) {
					for(Predicate p2 : pTrans) {
						if(p == p2)
							continue;
						// A -> B ; B -> C
						if(p.getOperand2().equals(p2.getOperand1())) {
							Predicate newPred = predicateProvider.getPredicate(op, p.getOperand1(), p2.getOperand2());
							additions.add(newPred);
						}
						// C -> A ; A -> B
						if(p2.getOperand2().equals(p.getOperand1())) {
							Predicate newPred = predicateProvider.getPredicate(op, p2.getOperand1(), p.getOperand2());
							additions.add(newPred);
						}
					}
				}
			}
		}

		
		List<Predicate> uneqList = grouped.get(Operator.UNEQUAL);
		if(uneqList != null) {
			for(Predicate p : uneqList) {
				if(closure.containsPredicate(predicateProvider.getPredicate(Operator.LESS_EQUAL, p.getOperand1(), p.getOperand2())))
					additions.add(predicateProvider.getPredicate(Operator.LESS, p.getOperand1(), p.getOperand2()));
				if(closure.containsPredicate(predicateProvider.getPredicate(Operator.GREATER_EQUAL, p.getOperand1(), p.getOperand2())))
					additions.add(predicateProvider.getPredicate(Operator.GREATER, p.getOperand1(), p.getOperand2()));
			}
		}
		List<Predicate> leqList = grouped.get(Operator.LESS_EQUAL);
		if(leqList != null) {
			for(Predicate p : leqList) {
				if(closure.containsPredicate(predicateProvider.getPredicate(Operator.GREATER_EQUAL, p.getOperand1(), p.getOperand2())))
					additions.add(predicateProvider.getPredicate(Operator.EQUAL, p.getOperand1(), p.getOperand2()));
			}
		}
		addAll(additions);
	}

	private void addAll(Collection<Predicate> predicates) throws TrivialPredicateSetException {
		for (Predicate p : predicates) {
			if (closure.add(p)) {
//				if((p.getOperator() == Operator.GREATER || p.getOperator() == Operator.LESS || p.getOperator() == Operator.UNEQUAL) && p.getOperand1().equals(p.getOperand2()))
//					throw new TrivialPredicateSetException();
				if (closure.containsPredicate(p.getInverse()))
					throw new TrivialPredicateSetException();
				grouped.computeIfAbsent(p.getOperator(), op -> new ArrayList<>()).add(p);
				added = true;
			}
		}
	}
	
	static final PredicateProvider predicateProvider = PredicateProvider.getInstance();  
}
