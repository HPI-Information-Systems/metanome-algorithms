package de.hpi.naumann.dc.algorithms.hybrid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.util.concurrent.AtomicLongMap;

import ch.javasoft.bitset.IBitSet;
import de.hpi.naumann.dc.denialcontraints.DenialConstraint;
import de.hpi.naumann.dc.denialcontraints.DenialConstraintSet;
import de.hpi.naumann.dc.evidenceset.HashEvidenceSet;
import de.hpi.naumann.dc.evidenceset.IEvidenceSet;
import de.hpi.naumann.dc.evidenceset.build.PartitionEvidenceSetBuilder;
import de.hpi.naumann.dc.helpers.IndexProvider;
import de.hpi.naumann.dc.helpers.SuperSetWalker;
import de.hpi.naumann.dc.input.Input;
import de.hpi.naumann.dc.paritions.ClusterPair;
import de.hpi.naumann.dc.paritions.IEJoin;
import de.hpi.naumann.dc.paritions.StrippedPartition;
import de.hpi.naumann.dc.predicates.PartitionRefiner;
import de.hpi.naumann.dc.predicates.Predicate;
import de.hpi.naumann.dc.predicates.PredicateBuilder;
import de.hpi.naumann.dc.predicates.PredicatePair;
import de.hpi.naumann.dc.predicates.sets.PredicateBitSet;

public class ResultCompletion {

	private Input input;
	private PredicateBuilder predicates;
	
	private static BiFunction<AtomicLongMap<PartitionRefiner>,
	Function<PartitionRefiner, Integer>, Comparator<PartitionRefiner>> resultSorter = (
			selectivityCount, counts) -> (r2, r1) -> {

				long s1 = selectivityCount.get(r1);
				long s2 = selectivityCount.get(r2);

				return Double.compare(1.0d * counts.apply(r1).intValue() / s1, 1.0d * counts.apply(r2).intValue() / s2);

			};;
	
	private static BiFunction< Multiset<PredicatePair> ,
	AtomicLongMap<PartitionRefiner>, Function<PredicatePair, Double>> pairWeight = (
			paircountDC, selectivityCount) -> (pair) -> {
				return Double.valueOf(1.0d * selectivityCount.get(pair) / paircountDC.count(pair));
			};

	public ResultCompletion(Input input, PredicateBuilder predicates) {
		this.input = input;
		this.predicates = predicates;
	}

	public HashEvidenceSet complete(DenialConstraintSet set, IEvidenceSet sampleEvidence, IEvidenceSet fullEvidence) {
		log.info("Checking " + set.size() + " DCs.");

		log.info("Building selectivity estimation");


		// frequency estimation predicate pairs
		Multiset<PredicatePair> paircountDC = frequencyEstimationForPredicatePairs(set);

		// selectivity estimation for predicates & predicate pairs
		AtomicLongMap<PartitionRefiner> selectivityCount = createSelectivityEstimation(sampleEvidence,
				paircountDC.elementSet());

		ArrayList<PredicatePair> sortedPredicatePairs = getSortedPredicatePairs(paircountDC, selectivityCount);

		IndexProvider<PartitionRefiner> indexProvider = new IndexProvider<>();

		log.info("Grouping DCs..");
		Map<IBitSet, List<DenialConstraint>> predicateDCMap = groupDCs(set, sortedPredicatePairs, indexProvider,
				selectivityCount);

		int[] refinerPriorities = getRefinerPriorities(selectivityCount, indexProvider, predicateDCMap);

		SuperSetWalker walker = new SuperSetWalker(predicateDCMap.keySet(), refinerPriorities);

		log.info("Calculating partitions..");

		HashEvidenceSet resultEv = new HashEvidenceSet();
		for (PredicateBitSet i : fullEvidence)
			resultEv.add(i);

		ClusterPair startPartition = StrippedPartition.getFullParition(input.getLineCount());
		int[][] values = input.getInts();
		IEJoin iejoin = new IEJoin(values);
		PartitionEvidenceSetBuilder builder = new PartitionEvidenceSetBuilder(predicates, values);

		long startTime = System.nanoTime();
		walker.walk((inter) -> {
			if((System.nanoTime() - startTime) >TimeUnit.MINUTES.toNanos(120))
				return;

			Consumer<ClusterPair> consumer = (clusterPair) -> {
				List<DenialConstraint> currentDCs = predicateDCMap.get(inter.currentBits);
				if (currentDCs != null) {

					// EtmPoint point = etmMonitor.createPoint("EVIDENCES");
					builder.addEvidences(clusterPair, resultEv);
					// point.collect();
				} else {
					inter.nextRefiner.accept(clusterPair);
				}
			};

			PartitionRefiner refiner = indexProvider.getObject(inter.newRefiner);
//			System.out.println(refiner);
			ClusterPair partition = inter.clusterPair != null ? inter.clusterPair : startPartition;
			partition.refine(refiner, iejoin, consumer);

		});

		return resultEv;
		// return output;

	}


	private int[] getRefinerPriorities(AtomicLongMap<PartitionRefiner> selectivityCount,
			IndexProvider<PartitionRefiner> indexProvider, Map<IBitSet, List<DenialConstraint>> predicateDCMap) {
		int[] counts2 = new int[indexProvider.size()];
		for (int i = 0; i < counts2.length; ++i) {
			counts2[i] = 1;
		}
		for (IBitSet bitset : predicateDCMap.keySet()) {
			for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
				counts2[i]++;
			}
		}

		ArrayList<PartitionRefiner> refiners = new ArrayList<PartitionRefiner>();

		int[] counts3 = new int[indexProvider.size()];

		for (int i = 0; i < counts3.length; ++i) {
			PartitionRefiner refiner = indexProvider.getObject(i);
			refiners.add(refiner);
		}
		refiners.sort(resultSorter.apply(selectivityCount, refiner -> Integer.valueOf(counts2[indexProvider.getIndex(refiner).intValue()])));

		int i = 0;
		for (PartitionRefiner refiner : refiners) {
			counts3[indexProvider.getIndex(refiner).intValue()] = i;
			++i;
		}
		return counts3;
	}

	private Map<IBitSet, List<DenialConstraint>> groupDCs(DenialConstraintSet set,
			ArrayList<PredicatePair> sortedPredicatePairs, IndexProvider<PartitionRefiner> indexProvider,
			AtomicLongMap<PartitionRefiner> selectivityCount) {
		Map<IBitSet, List<DenialConstraint>> predicateDCMap = new HashMap<>();
		HashMap<PredicatePair, Integer> prios = new HashMap<>();
		for (int i = 0; i < sortedPredicatePairs.size(); ++i) {
			prios.put(sortedPredicatePairs.get(i), Integer.valueOf(i));
		}
		for (DenialConstraint dc : set) {
			Set<PartitionRefiner> refinerSet = getRefinerSet(prios, dc);

			predicateDCMap.computeIfAbsent(indexProvider.getBitSet(refinerSet), (Set) -> new ArrayList<>()).add(dc);
		}
		return predicateDCMap;
	}

	private Set<PartitionRefiner> getRefinerSet(HashMap<PredicatePair, Integer> prios, DenialConstraint dc) {
		Set<PartitionRefiner> refinerSet = new HashSet<>();

		Set<Predicate> pairSet = new HashSet<>();
		dc.getPredicateSet().forEach(p -> {
			if (StrippedPartition.isSingleSupported(p)) {
				refinerSet.add(p);
			} else {
				pairSet.add(p);
			}
		});
		while (pairSet.size() > 1) {
			PredicatePair bestP = getBest(prios, pairSet);
			refinerSet.add(bestP);
			pairSet.remove(bestP.getP1());
			pairSet.remove(bestP.getP2());
		}
		if (!pairSet.isEmpty()) {
			refinerSet.add(pairSet.iterator().next());
		}
		return refinerSet;
	}

	private PredicatePair getBest(HashMap<PredicatePair, Integer> prios, Set<Predicate> pairSet) {
		int best = -1;
		PredicatePair bestP = null;
		for (Predicate p1 : pairSet) {
			for (Predicate p2 : pairSet) {
				if (p1 != p2) {
					PredicatePair pair = new PredicatePair(p1, p2);
					int score = prios.get(pair).intValue();
					if (score > best) {
						best = score;
						bestP = pair;
					}
				}
			}
		}
		return bestP;
	}

	private ArrayList<PredicatePair> getSortedPredicatePairs(Multiset<PredicatePair> paircountDC,
			AtomicLongMap<PartitionRefiner> selectivityCount) {
		ArrayList<PredicatePair> sortedPredicatePairs = new ArrayList<>();
		sortedPredicatePairs.addAll(paircountDC.elementSet());
		Function<PredicatePair, Double> weightProv = pairWeight.apply(paircountDC, selectivityCount);
		sortedPredicatePairs.sort(new Comparator<PredicatePair>() {

			@Override
			public int compare(PredicatePair o1, PredicatePair o2) {
				return Double.compare(getPriority(o2), getPriority(o1));
			}

			private double getPriority(PredicatePair o1) {
				return weightProv.apply(o1).doubleValue();
			}
		});
		return sortedPredicatePairs;
	}

	private Multiset<PredicatePair> frequencyEstimationForPredicatePairs(DenialConstraintSet set) {
		Multiset<PredicatePair> paircountDC = HashMultiset.create();
		for (DenialConstraint dc : set) {
			 dc.getPredicateSet().forEach(p1 -> {
				if (StrippedPartition.isPairSupported(p1)) {
					dc.getPredicateSet().forEach(p2 -> {
						if (!p1.equals(p2) && StrippedPartition.isPairSupported(p2)) {
							paircountDC.add(new PredicatePair(p1, p2));
						}
					});
				}
			});
		}
		return paircountDC;
	}

	private AtomicLongMap<PartitionRefiner> createSelectivityEstimation(IEvidenceSet sampleEvidence,
			Set<PredicatePair> predicatePairs) {
		AtomicLongMap<PartitionRefiner> selectivityCount = AtomicLongMap.create();
		for (PredicateBitSet ps : sampleEvidence) {
			int count = (int) sampleEvidence.getCount(ps);
			ps.forEach(p -> {
				selectivityCount.addAndGet(p, count);
			});
			for (PredicatePair pair : predicatePairs)
				if (pair.bothContainedIn(ps)) {
					selectivityCount.addAndGet(pair, sampleEvidence.getCount(ps));
				}
		}
		return selectivityCount;
	}

	private static Logger log = LoggerFactory.getLogger(ResultCompletion.class);

}
