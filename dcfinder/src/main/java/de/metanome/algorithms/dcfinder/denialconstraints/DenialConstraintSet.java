package de.metanome.algorithms.dcfinder.denialconstraints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.search.NTreeSearch;
import de.metanome.algorithms.dcfinder.predicates.sets.Closure;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSetFactory;

public class DenialConstraintSet implements Iterable<DenialConstraint> {

	private Set<DenialConstraint> constraints = new HashSet<>();

	public boolean contains(DenialConstraint dc) {
		return constraints.contains(dc);
	}

	private static class MinimalDCCandidate {
		DenialConstraint dc;
		IBitSet bitset;

		public MinimalDCCandidate(DenialConstraint dc) {
			this.dc = dc;
			this.bitset = PredicateSetFactory.create(dc.getPredicateSet()).getBitset();
		}

		public boolean shouldReplace(MinimalDCCandidate prior) {
			if (prior == null)
				return true;

			if (dc.getPredicateCount() < prior.dc.getPredicateCount())
				return true;

			if (dc.getPredicateCount() > prior.dc.getPredicateCount())
				return false;

			return bitset.compareTo(prior.bitset) <= 0;
		}
	}

	public void minimize() {
		Map<PredicateSet, MinimalDCCandidate> constraintsClosureMap = new HashMap<>();
		for (DenialConstraint dc : constraints) {
			PredicateSet predicateSet = dc.getPredicateSet();
			Closure c = new Closure(predicateSet);
			if (c.construct()) {
				MinimalDCCandidate candidate = new MinimalDCCandidate(dc);
				PredicateSet closure = c.getClosure();
				MinimalDCCandidate prior = constraintsClosureMap.get(closure);
				if (candidate.shouldReplace(prior))
					constraintsClosureMap.put(closure, candidate);
			}
		}

		List<Entry<PredicateSet, MinimalDCCandidate>> constraints2 = new ArrayList<>(
				constraintsClosureMap.entrySet());
		// log.info("Sym size created " + constraints2.size());

		constraints2.sort((entry1, entry2) -> {
			int res = Integer.compare(entry1.getKey().size(), entry2.getKey().size());
			if (res != 0)
				return res;
			res = Integer.compare(entry1.getValue().dc.getPredicateCount(), entry2.getValue().dc.getPredicateCount());
			if (res != 0)
				return res;
			return entry1.getValue().bitset.compareTo(entry2.getValue().bitset);
		});

		constraints = new HashSet<>();
		NTreeSearch tree = new NTreeSearch();
		for (Entry<PredicateSet, MinimalDCCandidate> entry : constraints2) {
			if (tree.containsSubset(PredicateSetFactory.create(entry.getKey()).getBitset()))
				continue;

			DenialConstraint inv = entry.getValue().dc.getInvT1T2DC();
			if (inv != null) {
				Closure c = new Closure(inv.getPredicateSet());
				if (!c.construct())
					continue;
				if (tree.containsSubset(PredicateSetFactory.create(c.getClosure()).getBitset()))
					continue;
			}

			constraints.add(entry.getValue().dc);
			tree.add(entry.getValue().bitset);
			if (inv != null)
				tree.add(PredicateSetFactory.create(inv.getPredicateSet()).getBitset());
		}
		// etmMonitor.render(new SimpleTextRenderer());
	}

	public void add(DenialConstraint dc) {
		constraints.add(dc);
	}

	@Override
	public Iterator<DenialConstraint> iterator() {
		return constraints.iterator();
	}

	public int size() {
		return constraints.size();
	}

	private static Logger log = LoggerFactory.getLogger(DenialConstraintSet.class);
}
