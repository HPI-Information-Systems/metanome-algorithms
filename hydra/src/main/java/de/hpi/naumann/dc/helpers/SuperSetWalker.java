package de.hpi.naumann.dc.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;
import de.hpi.naumann.dc.paritions.ClusterPair;

public class SuperSetWalker {
	
	public static class InterResult {
		public int newRefiner;
		public IBitSet currentBits;
		public Consumer<ClusterPair> nextRefiner;
		public ClusterPair clusterPair;
	}

	private List<IBitSet> sortedList;
	private BitSetTranslator translator;

	public SuperSetWalker(Collection<IBitSet> keySet, int[] counts) {
		ArrayIndexComparator comparator = new ArrayIndexComparator(counts, ArrayIndexComparator.Order.ASCENDING);
		this.translator = new BitSetTranslator(comparator.createIndexArray());

		this.sortedList = new ArrayList<>(translator.transform(keySet));
		Collections.sort(this.sortedList, new Comparator<IBitSet>() {

			@Override
			public int compare(IBitSet o1, IBitSet o2) {
				return o2.compareTo(o1);
			}
		});
	}

	public void walk(Consumer<InterResult> f) {
		walkChildren(0, LongBitSet.FACTORY.create(), null, -1, f);
	}

	private int max = 0;
	private void walkChildren(int next, IBitSet parent, ClusterPair parentRes, int lastBit, Consumer<InterResult> f) {
		while (next < sortedList.size() && parent.isSubSetOf(sortedList.get(next))) {
			int nextBit = sortedList.get(next).nextSetBit(lastBit + 1);
			if (nextBit < 0) {
				++next;
				if(next > max) {
					max = next;
//					System.out.println(max);
				}
			} else {
				IBitSet toCheck = parent.clone();
				toCheck.set(nextBit);
				final int nextF = next;
				Consumer<ClusterPair> refineFurther = (clusterPair) -> {
					walkChildren(nextF, toCheck, clusterPair, nextBit, f);
				};
				
				InterResult inter = new InterResult();
				inter.clusterPair = parentRes;
				inter.newRefiner = translator.retransform(nextBit);
				inter.nextRefiner = refineFurther;
				inter.currentBits = translator.bitsetRetransform(toCheck);
				f.accept(inter);
				while (next < sortedList.size() && toCheck.isSubSetOf(sortedList.get(next)))
					++next;
				if(next > max) {
					max = next;
//					System.out.println(max);
				}
			}
		}
	}

}
