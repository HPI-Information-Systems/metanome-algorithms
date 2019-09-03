package de.metanome.algorithms.dcfinder.setcover.partial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.search.ISubsetBackend;
import ch.javasoft.bitset.search.NTreeSearch;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraint;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraintSet;
import de.metanome.algorithms.dcfinder.evidenceset.IEvidenceSet;
import de.metanome.algorithms.dcfinder.helpers.ArrayIndexComparator;
import de.metanome.algorithms.dcfinder.helpers.BitSetTranslator;
import de.metanome.algorithms.dcfinder.helpers.LongArrayIndexComparator;
import de.metanome.algorithms.dcfinder.predicates.Predicate;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSetFactory;
import de.metanome.algorithms.dcfinder.setcover.IMinimalCoverSearch;

public class MinimalCoverSearch implements IMinimalCoverSearch {

	protected Set<Predicate> predicates;
	private BitSetTranslator translator;
	protected long violationsThreshold;

	public MinimalCoverSearch(Set<Predicate> predicates, long maxViolationsThreshold) {
		this.predicates = predicates;
		this.violationsThreshold = maxViolationsThreshold;
		MinimalCoverCandidate.setViolationsThreshold(maxViolationsThreshold);
	}

	public DenialConstraintSet getDenialConstraints(IEvidenceSet evidenceSet) {

		Collection<PredicateSet> MC = searchMinimalCovers(evidenceSet);
		log.info("Building denial constraints...");
		DenialConstraintSet dcs = new DenialConstraintSet();
		for (PredicateSet s : MC) {
			dcs.add(new DenialConstraint(s));
		}
		dcs.minimize();

		return dcs;
	}

	public static long[] initialPredicateTpCounts;

	public Collection<PredicateSet> searchMinimalCovers(IEvidenceSet evidenceSet) {

		log.info("Finding Minimal Covers for the Evidence Set...");
		
		long[] predicateTpCounts = getPredicateTpCounts(evidenceSet);
		initialPredicateTpCounts = predicateTpCounts;

		LongArrayIndexComparator comparator = new LongArrayIndexComparator(predicateTpCounts,
				LongArrayIndexComparator.Order.ASCENDING);
		Integer[] initialPredicateOrdering = comparator.createIndexArray();

		this.translator = new BitSetTranslator(initialPredicateOrdering);

		ISubsetBackend MC = new NTreeSearch();
		MinimalCoverCandidate c = new MinimalCoverCandidate(evidenceSet, predicates);
		c.searchMinimalCovers(MC, translator, null);

		Collection<PredicateSet> result = new ArrayList<PredicateSet>();
		MC.forEach(bitset -> result.add(PredicateSetFactory.create(translator.bitsetRetransform(bitset)).convert()));

		return result;
	}

	public static long[] getPredicateTpCounts(IEvidenceSet evidenceSet) {

		long[] counts = new long[PredicateSet.indexProvider.size()];

		for (PredicateSet ps : evidenceSet) {
			IBitSet bitset = ps.getBitset();
			for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
				counts[i] += evidenceSet.getCount(ps);
			}
		}

		return counts;
	}

	public Collection<IBitSet> getBitsets(IEvidenceSet evidenceSet, NTreeSearch priorDCs) {
		System.out.println(PredicateSet.indexProvider.size());
		int[] counts = new int[PredicateSet.indexProvider.size()];
		for (PredicateSet ps : evidenceSet) {
			IBitSet bitset = ps.getBitset();
			for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
				counts[i]++;
			}
		}
		ArrayIndexComparator comparator = new ArrayIndexComparator(counts, ArrayIndexComparator.Order.ASCENDING);
		this.translator = new BitSetTranslator(comparator.createIndexArray());

		ISubsetBackend MC = new NTreeSearch();
		MinimalCoverCandidate c = new MinimalCoverCandidate(evidenceSet, predicates);
		c.searchMinimalCovers(MC, translator, priorDCs);

		Collection<IBitSet> result = new ArrayList<>();
		MC.forEach(bitset -> result.add(translator.bitsetRetransform(bitset)));

		return result;
	}

	private static Logger log = LoggerFactory.getLogger(MinimalCoverSearch.class);
}
