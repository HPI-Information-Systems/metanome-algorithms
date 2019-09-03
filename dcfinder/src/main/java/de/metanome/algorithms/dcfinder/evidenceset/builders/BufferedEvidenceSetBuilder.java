package de.metanome.algorithms.dcfinder.evidenceset.builders;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.javasoft.bitset.IBitSet;
import de.metanome.algorithm_integration.Operator;
import de.metanome.algorithms.dcfinder.evidenceset.IEvidenceSet;
import de.metanome.algorithms.dcfinder.evidenceset.TroveEvidenceSet;
import de.metanome.algorithms.dcfinder.input.partitions.clusters.PLI;
import de.metanome.algorithms.dcfinder.predicates.Predicate;
import de.metanome.algorithms.dcfinder.predicates.PredicateBuilder;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;
import edu.stanford.nlp.util.Interval;

public class BufferedEvidenceSetBuilder {

	/*** Common across all instances ***/
	private static int bufferLength;
	private static PredicateBuilder predicateBuilder;
	private static int relSize;

	private static Collection<Collection<Predicate>> numericalSingleColumnPredicates;
	private static Collection<Collection<Predicate>> numericalCrossColumnPredicates;
	private static Collection<Collection<Predicate>> categoricalSingleColumnPredicates;
	private static Collection<Collection<Predicate>> categoricalCrossColumnPredicates;

	private static Map<Predicate, IBitSet> categoricalSingleColumnPredicateMasks;
	private static Map<Predicate, IBitSet> categoricalCrossColumnPredicateMasks;
	private static Map<Predicate, IBitSet> numericalSingleColumnPredicateMasks;
	private static Map<Predicate, IBitSet> numericalCrossColumnPredicateMasks;

	private static PredicateSet cardinalityMask;

	public static void configure(int bufferLength, int relSize, PredicateBuilder predicateBuilder) {

		BufferedEvidenceSetBuilder.bufferLength = bufferLength;
		BufferedEvidenceSetBuilder.relSize = relSize;
		BufferedEvidenceSetBuilder.predicateBuilder = predicateBuilder;

		BufferedEvidenceSetBuilder.numericalSingleColumnPredicates = predicateBuilder
				.getPredicateGroupsNumericalSingleColumn();
		BufferedEvidenceSetBuilder.numericalCrossColumnPredicates = predicateBuilder
				.getPredicateGroupsNumericalCrossColumn();

		BufferedEvidenceSetBuilder.categoricalSingleColumnPredicates = predicateBuilder
				.getPredicateGroupsCategoricalSingleColumn();
		BufferedEvidenceSetBuilder.categoricalCrossColumnPredicates = predicateBuilder
				.getPredicateGroupsCategoricalCrossColumn();

		fillupCardinalityMask();
		fillupCorrectionMask();

	}

	private Interval<Long> currentInterval;
	private long chunkLength;
	private Map<Predicate, BitSet> predicateBitsetMap;

	public BufferedEvidenceSetBuilder(Interval<Long> interval, long chunkLength) {
		this.currentInterval = interval;
		this.chunkLength = chunkLength;
		predicateBitsetMap = new HashMap<>();
		predicateBuilder.getPredicates().forEach(p -> predicateBitsetMap.put(p, new BitSet()));
	}

	public IEvidenceSet buildPartialEvidenceSet() {

		categoricalSingleColumnPredicates.stream().forEach(predicateGroup -> {
			Predicate eq = predicateBuilder.getPredicateByType(predicateGroup, Operator.EQUAL);
			setTPIDsSingleColumnEQ(eq);
		});

		categoricalCrossColumnPredicates.stream().forEach(predicateGroup -> {
			Predicate eq = predicateBuilder.getPredicateByType(predicateGroup, Operator.EQUAL);
			setTPIDsCrossColumnEQ(eq);
		});

		numericalSingleColumnPredicates.stream().forEach(predicateGroup -> {

			Predicate eq = predicateBuilder.getPredicateByType(predicateGroup, Operator.EQUAL);
			setTPIDsSingleColumnEQ(eq);

			Predicate gt = predicateBuilder.getPredicateByType(predicateGroup, Operator.GREATER);
			setTPIDsSingleColumnGT(gt);
		});

		numericalCrossColumnPredicates.stream().forEach(predicateGroup -> {

			Predicate eq = predicateBuilder.getPredicateByType(predicateGroup, Operator.EQUAL);
			setTPIDsCrossColumnEQ(eq);

			Predicate gt = predicateBuilder.getPredicateByType(predicateGroup, Operator.GREATER);
			setTPIDsCrossColumnGT(gt);
		});

		IEvidenceSet evidenceSet = invertEvidenceUsingCorrectionMasks();
		return evidenceSet;

	}

	private void setTPIDsSingleColumnEQ(Predicate eq) {

		BitSet satisfiedEQ = predicateBitsetMap.get(eq);
		satisfiedEQ.clear();

		PLI pli = eq.getOperand1().getColumn().getPli();
		Interval<Integer> currentTIDsInterval = getTIDsInterval();
		List<List<Integer>> clusters = pli.getPlis();

		for (List<Integer> tids : clusters) {
			if (tids.size() == 1) {
				continue;
			}

			for (Integer lowi : tids) {
				if (lowi < currentTIDsInterval.first || lowi > currentTIDsInterval.second) {
					continue;
				}

				for (Integer highi : tids) {
					if (lowi == highi)
						continue;

					int tid = getBitSetIndex(lowi, highi);
					if (tid >= 0) {

						satisfiedEQ.set(tid);
					}
				}

			}

		}

	}

	private void setTPIDsCrossColumnEQ(Predicate eq) {

		BitSet satisfiedEQ = predicateBitsetMap.get(eq);
		satisfiedEQ.clear();

		Interval<Integer> currentTIDsInterval = getTIDsInterval();

		PLI pliPivot = eq.getOperand1().getColumn().getPli();
		PLI pliProbe = eq.getOperand2().getColumn().getPli(); // probing PLI

		Collection<Integer> valuesPivot = pliPivot.getValues();

		for (Integer vPivot : valuesPivot) {
			List<Integer> tidsProbe = pliProbe.getTpIDsForValue(vPivot);
			if (tidsProbe != null) {
				List<Integer> tidsPivot = pliPivot.getTpIDsForValue(vPivot);
				for (Integer tidPivot : tidsPivot) {

					if (tidPivot < currentTIDsInterval.first || tidPivot > currentTIDsInterval.second) {
						continue;
					}
					for (Integer tidProbe : tidsProbe) {
						if (tidPivot == tidProbe)
							continue;
						int tid = getBitSetIndex(tidPivot, tidProbe);
						if (tid >= 0) {
							satisfiedEQ.set(tid);

						}
					}

				}

			}

		}
	}

	private void setTPIDsSingleColumnGT(Predicate gt) {

		BitSet satisfiedGT = predicateBitsetMap.get(gt);
		satisfiedGT.clear();

		PLI pli = gt.getOperand1().getColumn().getPli();
		Interval<Integer> currentTIDsInterval = getTIDsInterval();
		List<List<Integer>> clusters = pli.getPlis();

		for (int i = 0; i < clusters.size() - 1; i++) {

			List<Integer> greaterTids = clusters.get(i);
			for (Integer greaterTid : greaterTids) {

				if (greaterTid < currentTIDsInterval.first || greaterTid > currentTIDsInterval.second) {
					continue;
				}

				for (int j = i + 1; j < clusters.size(); j++) {

					List<Integer> smallerTids = clusters.get(j);
					for (Integer smallerTid : smallerTids) {

						if (greaterTid == smallerTid)
							continue;

						int tid = getBitSetIndex(greaterTid, smallerTid);
						if (tid >= 0) {
							satisfiedGT.set(tid);
						}

					}

				}

			}

		}

	}

	private void setTPIDsCrossColumnGT(Predicate gt) {

		BitSet satisfiedGT = predicateBitsetMap.get(gt);
		satisfiedGT.clear();

		Interval<Integer> currentTIDsInterval = getTIDsInterval();

		PLI pliPivot = gt.getOperand1().getColumn().getPli();
		PLI pliProbe = gt.getOperand2().getColumn().getPli(); // probing PLI

		List<Integer> valuesPivot = (List<Integer>) pliPivot.getValues();
		// Collection<Integer> valuesProbe = pliProbe.getValues();
		List<List<Integer>> tidsListProbe = pliProbe.getPlis();

		for (int indexPivot = 0; indexPivot < valuesPivot.size(); indexPivot++) {

			Integer vPivot = valuesPivot.get(indexPivot);
			Integer indexProbe = pliProbe.getIndexForValueThatIsLessThan(vPivot);

			if (indexProbe >= 0) {

				List<Integer> tidsPivot = pliPivot.getTpIDsForValue(vPivot);
				for (Integer tidPivot : tidsPivot) {

					if (tidPivot < currentTIDsInterval.first || tidPivot > currentTIDsInterval.second) {
						continue;
					}

					for (int j = indexProbe; j < tidsListProbe.size(); j++) {
						List<Integer> tidsProbe = tidsListProbe.get(j);
						for (Integer smallerTid : tidsProbe) {

							if (tidPivot == smallerTid)
								continue;

							int tid = getBitSetIndex(tidPivot, smallerTid);
							if (tid >= 0) {
								satisfiedGT.set(tid);
							}
						}
					}
				}
			} else {
				break;
			}

		}

	}

	private IEvidenceSet invertEvidenceUsingCorrectionMasks() {

		IEvidenceSet evidenceSet = new TroveEvidenceSet();

		/** buffers **/
		List<Interval<Long>> bufferIntervals = new ArrayList<>();
		for (long i = 0; i < currentIntervalLength(); i += bufferLength) {
			bufferIntervals.add(Interval.toInterval(i, (i + bufferLength), Interval.INTERVAL_OPEN_END));
		}

		/** transposition **/
		for (Interval<Long> interval : bufferIntervals) {

			List<PredicateSet> evidences = new ArrayList<>(bufferLength);
			for (int i = 0; i < bufferLength; i++) {
				evidences.add(new PredicateSet(cardinalityMask));
			}

			int begin = interval.getBegin().intValue();
			int end = interval.getEnd().intValue();

			/** evidence correction **/
			for (Map.Entry<Predicate, IBitSet> correction : numericalSingleColumnPredicateMasks.entrySet()) {
				correctEvidence(evidences, begin, end, correction);
			}

			for (Map.Entry<Predicate, IBitSet> correction : numericalCrossColumnPredicateMasks.entrySet()) {
				correctEvidence(evidences, begin, end, correction);
			}

			for (Map.Entry<Predicate, IBitSet> correction : categoricalSingleColumnPredicateMasks.entrySet()) {
				correctEvidence(evidences, begin, end, correction);
			}

			for (Map.Entry<Predicate, IBitSet> correction : categoricalCrossColumnPredicateMasks.entrySet()) {
				correctEvidence(evidences, begin, end, correction);
			}

			/** evidence mapping **/
			for (PredicateSet e : evidences) {
				evidenceSet.add(e);
			}

		}

		return evidenceSet;
	}

	private void correctEvidence(List<PredicateSet> evidences, int begin, int end,
			Map.Entry<Predicate, IBitSet> correction) {
		Predicate p = correction.getKey();
		BitSet bs = predicateBitsetMap.get(p);
		IBitSet mask = correction.getValue();

		for (int i = bs.nextSetBit(begin); i >= 0; i = bs.nextSetBit(i + 1)) {
			if (i >= end) {
				break;
			}
			evidences.get(i - begin).getBitset().xor(mask);
		}

	}

	private static void fillupCardinalityMask() {

		PredicateSet cardinalityPredicateBitset = new PredicateSet();

		for (Collection<Predicate> predicateGroup : numericalSingleColumnPredicates) {

			Predicate neq = predicateBuilder.getPredicateByType(predicateGroup, Operator.UNEQUAL);
			Predicate lt = predicateBuilder.getPredicateByType(predicateGroup, Operator.LESS);
			Predicate lte = predicateBuilder.getPredicateByType(predicateGroup, Operator.LESS_EQUAL);

			cardinalityPredicateBitset.add(neq);
			cardinalityPredicateBitset.add(lt);
			cardinalityPredicateBitset.add(lte);
		}

		for (Collection<Predicate> predicateGroup : numericalCrossColumnPredicates) {

			Predicate neq = predicateBuilder.getPredicateByType(predicateGroup, Operator.UNEQUAL);
			Predicate lt = predicateBuilder.getPredicateByType(predicateGroup, Operator.LESS);
			Predicate lte = predicateBuilder.getPredicateByType(predicateGroup, Operator.LESS_EQUAL);

			cardinalityPredicateBitset.add(neq);
			cardinalityPredicateBitset.add(lt);
			cardinalityPredicateBitset.add(lte);
		}

		for (Collection<Predicate> predicateGroup : categoricalSingleColumnPredicates) {

			Predicate neq = predicateBuilder.getPredicateByType(predicateGroup, Operator.UNEQUAL);
			cardinalityPredicateBitset.add(neq);
		}

		for (Collection<Predicate> predicateGroup : categoricalCrossColumnPredicates) {

			Predicate neq = predicateBuilder.getPredicateByType(predicateGroup, Operator.UNEQUAL);
			cardinalityPredicateBitset.add(neq);
		}

		cardinalityMask = cardinalityPredicateBitset;
	}

	private static void fillupCorrectionMask() {

		numericalSingleColumnPredicateMasks = new HashMap<>();
		numericalCrossColumnPredicateMasks = new HashMap<>();
		categoricalSingleColumnPredicateMasks = new HashMap<>();
		categoricalCrossColumnPredicateMasks = new HashMap<>();

		for (Collection<Predicate> predicateGroup : numericalSingleColumnPredicates) {

			PredicateSet equalityMask = new PredicateSet();

			Predicate eq = predicateBuilder.getPredicateByType(predicateGroup, Operator.EQUAL);
			Predicate neq = predicateBuilder.getPredicateByType(predicateGroup, Operator.UNEQUAL);
			Predicate lt = predicateBuilder.getPredicateByType(predicateGroup, Operator.LESS);
			Predicate gte = predicateBuilder.getPredicateByType(predicateGroup, Operator.GREATER_EQUAL);

			equalityMask.add(eq);
			equalityMask.add(neq);
			equalityMask.add(lt);
			equalityMask.add(gte);

			numericalSingleColumnPredicateMasks.put(eq, equalityMask.getBitset());

			PredicateSet gtMask = new PredicateSet();

			Predicate gt = predicateBuilder.getPredicateByType(predicateGroup, Operator.GREATER);
			Predicate lte = predicateBuilder.getPredicateByType(predicateGroup, Operator.LESS_EQUAL);

			gtMask.add(lt);
			gtMask.add(lte);
			gtMask.add(gt);
			gtMask.add(gte);

			numericalSingleColumnPredicateMasks.put(gt, gtMask.getBitset());

		}

		for (Collection<Predicate> predicateGroup : numericalCrossColumnPredicates) {

			PredicateSet equalityMask = new PredicateSet();

			Predicate eq = predicateBuilder.getPredicateByType(predicateGroup, Operator.EQUAL);
			Predicate neq = predicateBuilder.getPredicateByType(predicateGroup, Operator.UNEQUAL);
			Predicate lt = predicateBuilder.getPredicateByType(predicateGroup, Operator.LESS);
			Predicate gte = predicateBuilder.getPredicateByType(predicateGroup, Operator.GREATER_EQUAL);

			equalityMask.add(eq);
			equalityMask.add(neq);
			equalityMask.add(lt);
			equalityMask.add(gte);

			numericalCrossColumnPredicateMasks.put(eq, equalityMask.getBitset());

			PredicateSet gtMask = new PredicateSet();

			Predicate gt = predicateBuilder.getPredicateByType(predicateGroup, Operator.GREATER);
			Predicate lte = predicateBuilder.getPredicateByType(predicateGroup, Operator.LESS_EQUAL);

			gtMask.add(lt);
			gtMask.add(lte);
			gtMask.add(gt);
			gtMask.add(gte);

			numericalCrossColumnPredicateMasks.put(gt, gtMask.getBitset());

		}

		for (Collection<Predicate> predicateGroup : categoricalSingleColumnPredicates) {

			PredicateSet equalityMask = new PredicateSet();

			Predicate eq = predicateBuilder.getPredicateByType(predicateGroup, Operator.EQUAL);
			Predicate neq = predicateBuilder.getPredicateByType(predicateGroup, Operator.UNEQUAL);

			equalityMask.add(eq);
			equalityMask.add(neq);

			categoricalSingleColumnPredicateMasks.put(eq, equalityMask.getBitset());

		}

		for (Collection<Predicate> predicateGroup : categoricalCrossColumnPredicates) {

			PredicateSet equalityMask = new PredicateSet();

			Predicate eq = predicateBuilder.getPredicateByType(predicateGroup, Operator.EQUAL);
			Predicate neq = predicateBuilder.getPredicateByType(predicateGroup, Operator.UNEQUAL);

			equalityMask.add(eq);
			equalityMask.add(neq);

			categoricalCrossColumnPredicateMasks.put(eq, equalityMask.getBitset());

		}

	}

	public int currentIntervalLength() {
		long length = currentInterval.getEnd().longValue() - currentInterval.getBegin().longValue();
		return (int) length;

	}

	private int getBitSetIndex(Integer lowi, Integer highi) {
		return (int) (((lowi * (long) relSize) + highi) - (currentInterval.first().longValue()));
	}

	private int translateChunkRangeToTpID(long i) {
		long tpID = i / ((long) relSize);
		return (int) tpID;
	}

	private Interval<Integer> getTIDsInterval() {
		int low = translateChunkRangeToTpID((long) currentInterval.getBegin());
		int high = translateChunkRangeToTpID((long) currentInterval.getEnd());

		return Interval.toInterval(low, high);
	}

	public Interval<Integer> getTIDsInterval(Interval<Long> interval) {
		int low = translateChunkRangeToTpID(interval.getBegin());
		int high = translateChunkRangeToTpID(interval.getEnd());

		return Interval.toInterval(low, high);
	}

	public Map<Predicate, BitSet> getPredicateBitsetMap() {
		return predicateBitsetMap;
	}

	public long getChunkLength() {
		return chunkLength;
	}

	public int getBufferlength() {
		return bufferLength;
	}

	public static PredicateSet getCardinalityMask() {
		return cardinalityMask;
	}

}
