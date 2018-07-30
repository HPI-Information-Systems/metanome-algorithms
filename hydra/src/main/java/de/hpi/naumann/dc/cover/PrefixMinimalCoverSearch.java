package de.hpi.naumann.dc.cover;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;
import ch.javasoft.bitset.search.ITreeSearch;
import ch.javasoft.bitset.search.TranslatingTreeSearch;
import ch.javasoft.bitset.search.TreeSearch;
import de.hpi.naumann.dc.denialcontraints.DenialConstraint;
import de.hpi.naumann.dc.denialcontraints.DenialConstraintSet;
import de.hpi.naumann.dc.evidenceset.IEvidenceSet;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.PredicateBuilder;
import de.hpi.naumann.dc.predicates.sets.PredicateBitSet;
import de.hpi.naumann.dc.predicates.sets.PredicateSetFactory;

public class PrefixMinimalCoverSearch {

	// private BitSetTranslator translator;
	private List<IBitSet> bitsetList = new ArrayList<>();

	private final Collection<IBitSet> startBitsets;
	private TranslatingTreeSearch posCover;

	public PrefixMinimalCoverSearch(PredicateBuilder predicates2) {
		this(predicates2, (TranslatingTreeSearch) null);
		this.startBitsets.add(LongBitSet.FACTORY.create());
	}

	private PrefixMinimalCoverSearch(PredicateBuilder predicates2, TranslatingTreeSearch tree) {
		for (Collection<Predicate> pSet : predicates2.getPredicateGroups()) {
			IBitSet bitset = LongBitSet.FACTORY.create();
			for (Predicate p : pSet) {
				bitset.or(PredicateSetFactory.create(p).getBitset());
			}
			bitsetList.add(bitset);
		}
		this.startBitsets = new ArrayList<IBitSet>();
		this.posCover = tree;
	}

	private Collection<IBitSet> getBitsets(IEvidenceSet evidenceSet) {
		log.info("Evidence Set size: " + evidenceSet.size());
		if (posCover == null) {
			int[] counts = getCounts(evidenceSet);
			posCover = new TranslatingTreeSearch(counts, bitsetList);
		}

		log.info("Building new bitsets..");
		List<IBitSet> sortedNegCover = new ArrayList<IBitSet>();
		for (PredicateBitSet ps : evidenceSet) {
			sortedNegCover.add(ps.getBitset());
		}

		log.info("Sorting new bitsets..");
		sortedNegCover = minimize(sortedNegCover);

		mostGeneralDCs(posCover);

		Collections.sort(sortedNegCover, posCover.getComparator());
		log.info("Finished sorting neg 2. list size:" + sortedNegCover.size());

		for (int i = 0; i < sortedNegCover.size(); ++i) {
			posCover.handleInvalid(sortedNegCover.get(i));
			if (i % 1000 == 0 && i > 0)
				log.info("\r" + i);
		}

		Collection<IBitSet> result = new ArrayList<IBitSet>();
		posCover.forEach(bs -> result.add(bs));

		return result;
	}

	public DenialConstraintSet getDenialConstraints(IEvidenceSet evidenceSet) {

		DenialConstraintSet set = new DenialConstraintSet();

		getBitsets(evidenceSet).forEach(valid -> {
			set.add(new DenialConstraint(PredicateSetFactory.create(valid)));
		});
		log.info("" + set.size());
		return set;
	}

	private int[] getCounts(IEvidenceSet evidenceSet) {

		int[] counts = new int[PredicateBitSet.indexProvider.size()];
		for (PredicateBitSet ps : evidenceSet) {
			IBitSet bitset = ps.getBitset();
			for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
				counts[i]++;
			}
		}
		return counts;
	}

	private List<IBitSet> minimize(final List<IBitSet> sortedNegCover) {
		Collections.sort(sortedNegCover, new Comparator<IBitSet>() {
			@Override
			public int compare(IBitSet o1, IBitSet o2) {
				int erg = Integer.compare(o2.cardinality(), o1.cardinality());
				return erg != 0 ? erg : o2.compareTo(o1);
			}
		});

		log.info("starting inverting size " + sortedNegCover.size());
		TreeSearch neg = new TreeSearch();
		sortedNegCover.stream().forEach(invalid -> addInvalidToNeg(neg, invalid));

		final ArrayList<IBitSet> list = new ArrayList<IBitSet>();
		neg.forEach(invalidFD -> list.add(invalidFD));
		return list;
	}

	private void mostGeneralDCs(ITreeSearch posCover) {
		for (IBitSet start : startBitsets) {
			posCover.add(start);
		}
	}

	private void addInvalidToNeg(TreeSearch neg, IBitSet invalid) {
		if (neg.findSuperSet(invalid) != null)
			return;

		neg.getAndRemoveGeneralizations(invalid);
		neg.add(invalid);
	}

	private static Logger log = LoggerFactory.getLogger(PrefixMinimalCoverSearch.class);

}
