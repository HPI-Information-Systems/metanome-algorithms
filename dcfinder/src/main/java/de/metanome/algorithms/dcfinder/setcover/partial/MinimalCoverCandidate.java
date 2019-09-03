
package de.metanome.algorithms.dcfinder.setcover.partial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.common.util.concurrent.AtomicLongMap;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.search.ISubsetBackend;
import ch.javasoft.bitset.search.NTreeSearch;
import de.metanome.algorithms.dcfinder.evidenceset.IEvidenceSet;
import de.metanome.algorithms.dcfinder.evidenceset.TroveEvidenceSet;
import de.metanome.algorithms.dcfinder.helpers.BitSetTranslator;
import de.metanome.algorithms.dcfinder.predicates.Predicate;
import de.metanome.algorithms.dcfinder.predicates.sets.Closure;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSetFactory;

public class MinimalCoverCandidate {

	final IEvidenceSet evidenceSet;
	final Collection<Predicate> addablePredicates;
	final Closure closure;
	final PredicateSet current;

	private final static boolean ENABLE_TRANSITIVE_CHECK = false;
	private static final boolean ENABLE_CLOSURE_CHECK = false;

	private static long violationsThreshold;
	public static long alphabetLimit;

	public static long getViolationsThreshold() {
		return violationsThreshold;
	}

	public static void setViolationsThreshold(long violationsThreshold) {
		MinimalCoverCandidate.violationsThreshold = violationsThreshold;
	}

	public MinimalCoverCandidate(IEvidenceSet evidenceSet, Collection<Predicate> addablePredicates) {
		this.evidenceSet = evidenceSet;
		this.addablePredicates = addablePredicates;
		this.closure = new Closure(PredicateSetFactory.create());
		this.current = PredicateSetFactory.create();
	}

	public MinimalCoverCandidate(IEvidenceSet evidenceSet, Collection<Predicate> addablePredicates,
			PredicateSet current, Closure closure) {
		this.evidenceSet = evidenceSet;
		this.addablePredicates = addablePredicates;
		this.closure = closure;
		this.current = current;
	}

	public void searchMinimalCovers(ISubsetBackend mC, BitSetTranslator translator, NTreeSearch priorDCs) {

		long tpCounts = getTpCounts(evidenceSet);

		if (tpCounts <= violationsThreshold) {

			mC.add(translator.bitsetTransform(current.getBitset()));
		} else if (addablePredicates.size() > 0) {
			List<Predicate> pOrder = getSortedPredicates();

			List<Predicate> allowed = new ArrayList<>();
			Iterator<Predicate> iter = pOrder.iterator();
			while (iter.hasNext()) {
				Predicate add = iter.next();
				iter.remove();
				MinimalCoverCandidate c = getCandidate(allowed, add, mC, translator, priorDCs);
				allowed.add(add);
				if (c != null)
					c.searchMinimalCovers(mC, translator, priorDCs);

			}

		}
	}

	public static long getTpCounts(IEvidenceSet evidenceSet) {
		long tpCounts = 0;
		for (PredicateSet ps2 : evidenceSet) {
			tpCounts += evidenceSet.getCount(ps2);

		}
		return tpCounts;
	}

	private MinimalCoverCandidate getCandidate(List<Predicate> pOrder, Predicate addInverse, ISubsetBackend MC,
			BitSetTranslator translator, NTreeSearch priorDCs) {
		PredicateSet newPS = PredicateSetFactory.create(current);
		newPS.add(addInverse);

		boolean contains = MC.containsSubset(translator.bitsetTransform(newPS.getBitset()));
		if (contains)
			return null;

		if (ENABLE_TRANSITIVE_CHECK) {
			for (Predicate p : newPS.convert()) {
				PredicateSet dc2 = PredicateSetFactory.create(newPS);
				dc2.remove(p);
				for (Predicate p2 : p.getInverse().getImplications())
					dc2.add(p2);
				if (MC.containsSubset(translator.bitsetTransform(dc2.getBitset()))
						|| priorDCs != null && priorDCs.containsSubset(dc2.getBitset()))
					return null;
			}
		}

		boolean containsDC = priorDCs != null && priorDCs.containsSubset(newPS.getBitset());
		if (containsDC)
			return null;

		Closure closureNew = null;
		if (ENABLE_CLOSURE_CHECK) {
			closureNew = new Closure(closure, addInverse);
			if (!closureNew.construct())
				return null;

			if (MC.containsSubset(
					translator.bitsetTransform(PredicateSetFactory.create(closureNew.getClosure()).getBitset()))
					|| priorDCs != null
							&& priorDCs.containsSubset(PredicateSetFactory.create(closureNew.getClosure()).getBitset()))
				return null;

		}
		List<Predicate> newOrder = new ArrayList<>(pOrder.size());
		for (Predicate p : pOrder)
			if (!p.getOperand1().equals(addInverse.getOperand1()) || !p.getOperand2().equals(addInverse.getOperand2()))
				if (closureNew == null || !closureNew.getClosure().containsPredicate(p))
					newOrder.add(p);

		IEvidenceSet filtered = getFilteredEvidenceSet(addInverse, newOrder);
		if (filtered == null)
			return null;
		return new MinimalCoverCandidate(filtered, newOrder, newPS, closureNew);
	}

	private IEvidenceSet getFilteredEvidenceSet(Predicate add, Collection<Predicate> addablePredicates) {
		IEvidenceSet filteredEvidence = new TroveEvidenceSet();
		IBitSet addables = PredicateSetFactory.create(addablePredicates).getBitset();
		Iterator<PredicateSet> iter = evidenceSet.iterator();
		int addIndex = PredicateSet.getIndex(add);
		while (iter.hasNext()) {
			PredicateSet pSet = iter.next();
			IBitSet bs = pSet.getBitset();
			if (bs.get(addIndex)) {

				if (addables.isSubSetOf(bs)) {
					return null;
				}

				bs = bs.clone();
				bs.and(addables);
	
				filteredEvidence.add(PredicateSetFactory.create(bs), evidenceSet.getCount(pSet));
			}
		}
		return filteredEvidence;
	}

	private List<Predicate> getSortedPredicates() {

		long[] counts = getPredicateCounts();

		AtomicLongMap<Predicate> pCounts = AtomicLongMap.create();
		for (int i = 0; i < counts.length; ++i) {
			pCounts.put(PredicateSet.getPredicate(i), counts[i]);
		}

		List<Predicate> pOrder = new ArrayList<>(addablePredicates.size());
		for (Predicate p : addablePredicates) {
			pOrder.add(p);
		}

		pOrder.sort(new Comparator<Predicate>() {
			@Override
			public int compare(Predicate o1, Predicate o2) {
				return Long.compare(pCounts.get(o2), pCounts.get(o1));
			}
		});

		return pOrder;
	}

	private long[] getPredicateCounts() {
		long[] counts = new long[PredicateSet.indexProvider.size()];
		for (PredicateSet pSet : evidenceSet) {
			IBitSet bitset = pSet.getBitset();
			long pSetCount = evidenceSet.getCount(pSet);
			for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
				counts[i] += pSetCount;
			}

		}
		return counts;
	}

	
}
