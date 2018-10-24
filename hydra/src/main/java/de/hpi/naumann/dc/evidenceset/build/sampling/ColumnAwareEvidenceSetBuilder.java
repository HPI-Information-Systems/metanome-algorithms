package de.hpi.naumann.dc.evidenceset.build.sampling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hpi.naumann.dc.evidenceset.IEvidenceSet;
import de.hpi.naumann.dc.evidenceset.build.EvidenceSetBuilder;
import de.hpi.naumann.dc.input.ColumnPair;
import de.hpi.naumann.dc.input.Input;
import de.hpi.naumann.dc.input.ParsedColumn;
import de.hpi.naumann.dc.predicates.PredicateBuilder;
import de.hpi.naumann.dc.predicates.sets.PredicateBitSet;
import gnu.trove.iterator.TIntIterator;

public class ColumnAwareEvidenceSetBuilder extends EvidenceSetBuilder {
	private enum SamplingType {
		WITHIN, LATER, BEFORE, OTHER;
	}

	private class SamplingMethod {
		private ColumnData data;
		private SamplingType type;
		private double efficiency;

		public SamplingMethod(ColumnData data, SamplingType type) {
			this.data = data;
			this.type = type;
		}

		public void execute(Input input, IEvidenceSet evidenceSet) {
			int sizePrior = evidenceSet.size();
			forEachLine(data.clusters, (index, line) -> {
				int clusterIndex = index.intValue();
				int line1 = line.intValue();
				PredicateBitSet staticSet = getStatic(pairs, line1);
				switch (type) {
				case BEFORE:
					if (clusterIndex > 0) {
						OrderedCluster randOtherCluster = data.picker.getRandom(0, clusterIndex);
						int line2 = getRandomLine(randOtherCluster);
						evidenceSet.add(getPredicateSet(staticSet, pairs, line1, line2));
					}
					break;
				case LATER:
					if (clusterIndex < data.clusters.size() - 1) {
						OrderedCluster randOtherCluster = data.picker.getRandom(clusterIndex + 1, data.clusters.size());
						int line2 = getRandomLine(randOtherCluster);
						evidenceSet.add(getPredicateSet(staticSet, pairs, line1, line2));
					}
					break;
				case OTHER:
					if (data.clusters.size() > 1) {
						OrderedCluster randOtherCluster = data.picker.getRandom(clusterIndex);
						int line2 = getRandomLine(randOtherCluster);
						evidenceSet.add(getPredicateSet(staticSet, pairs, line1, line2));
					}
					break;
				case WITHIN:
					int line2 = getRandomLine(data.clusters.get(clusterIndex));
					if (line1 != line2)
						evidenceSet.add(getPredicateSet(staticSet, pairs, line1, line2));
					break;
				default:
					break;

				}
			});
			efficiency = (double) (evidenceSet.size() - sizePrior) / input.getLineCount();
		}
	}

	private static class ColumnData {
		public ColumnData(List<OrderedCluster> clusters, WeightedRandomPicker<OrderedCluster> picker, boolean comparable) {
			this.clusters = clusters;
			this.picker = picker;
			this.comparable = comparable;
		}

		public List<OrderedCluster> clusters;
		public WeightedRandomPicker<OrderedCluster> picker;
		public boolean comparable;
	}

	public ColumnAwareEvidenceSetBuilder(PredicateBuilder predicates2) {
		super(predicates2);
	}

	
	public IEvidenceSet buildEvidenceSet(IEvidenceSet evidenceSet, Input input, double efficiencyThreshold) {
//		long start = System.currentTimeMillis();
		pairs = predicates.getColumnPairs();
		createSets(pairs);

		List<ColumnData> columnDatas = new ArrayList<>();

		SamplingType[] comparableTypes = { SamplingType.WITHIN, SamplingType.BEFORE, SamplingType.LATER };
		SamplingType[] otherTypes = { SamplingType.WITHIN, SamplingType.OTHER };

		PriorityQueue<SamplingMethod> methods = new PriorityQueue<>(
				(p1, p2) -> Double.compare(p2.efficiency, p1.efficiency));

		for (ParsedColumn<?> c : input.getColumns()) {
			log.info("Sampling column " + c.getName());
			Map<Object, OrderedCluster> valueMap = new HashMap<>();
			for (int i = 0; i < input.getLineCount(); ++i) {
				OrderedCluster cluster = valueMap.computeIfAbsent(c.getValue(i), (k) -> new OrderedCluster());
				cluster.add(i);
			}

			List<OrderedCluster> clusters;
			if (c.isComparableType())
				clusters = valueMap.keySet().stream().sorted().map(key -> valueMap.get(key)).collect(Collectors.toList());
			else {
				clusters = new ArrayList<>(valueMap.values());
			}

			WeightedRandomPicker<OrderedCluster> picker = new WeightedRandomPicker<>();
			for (OrderedCluster cluster : clusters) {
				cluster.randomize();
				picker.add(cluster, /*cluster.size()*/1);
			}
			ColumnData d = new ColumnData(clusters, picker, c.isComparableType());
			columnDatas.add(d);

			SamplingType[] types = d.comparable ? comparableTypes : otherTypes;
			for (SamplingType type : types) {
				SamplingMethod method = new SamplingMethod(d, type);
				method.execute(input, evidenceSet);
				if (method.efficiency > efficiencyThreshold)
					methods.add(method);
			}
		}

		while (!methods.isEmpty()) {
			SamplingMethod method = methods.poll();
			method.execute(input, evidenceSet);
			if (method.efficiency > efficiencyThreshold)
				methods.add(method);
		}

		return evidenceSet;
	}

	private static void forEachLine(List<OrderedCluster> clusters, BiConsumer<Integer, Integer> consumer) {
		int clusterIndex = 0;
		for (OrderedCluster cluster : clusters) {
			TIntIterator iter = cluster.iterator();
			while (iter.hasNext()) {
				int line1 = iter.next();
				consumer.accept(Integer.valueOf(clusterIndex), Integer.valueOf(line1));
			}
			++clusterIndex;
		}
	}

	private int getRandomLine(OrderedCluster c) {
		return c.nextLine();
	}

	private static Logger log = LoggerFactory.getLogger(ColumnAwareEvidenceSetBuilder.class);
	private Collection<ColumnPair> pairs;
}
