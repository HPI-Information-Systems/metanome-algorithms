package de.metanome.algorithms.aidfd.results;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;
import ch.javasoft.bitset.search.TreeSearch;
import de.metanome.algorithms.aidfd.helpers.ArrayIndexComparator;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;

public class PrefixTreeResultGen {

	private final Set<IBitSet> invalid = new HashSet<IBitSet>();
	private final MetanomeResultReceiver resultReceiver;
	private final int numberAttributes;
	private IBitSet constantColumns;
	private Integer[] indexes;

	public PrefixTreeResultGen(RelationalInput input,
			FunctionalDependencyResultReceiver resultReceiver,
			boolean checkCorrectness) {
		this.resultReceiver = checkCorrectness
				? new CorrectnessMetanomeResultReceiver(input, resultReceiver)
				: new MetanomeResultReceiver(input, resultReceiver);
		this.numberAttributes = input.numberOfColumns();
	}

	public void add(IBitSet set) {
		invalid.add(set);
	}

	public void generateResults() throws ColumnNameMismatchException {
		int[] counts = new int[numberAttributes];
		invalid.stream().forEach(
			bitset -> {
				for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
					counts[i]++;
				}
		});

		ArrayIndexComparator comparator = new ArrayIndexComparator(counts, ArrayIndexComparator.Order.ASCENDING);
		indexes = comparator.createIndexArray();
		Arrays.sort(indexes, comparator);

		int[] invIndexes = new int[numberAttributes];
		for (int i = 0; i < numberAttributes; ++i) {
			invIndexes[indexes[i].intValue()] = i;
		}

		final ArrayList<IBitSet> sortedNegCover = new ArrayList<IBitSet>();
		invalid.stream().forEach(bitset -> {
			IBitSet bitset2 = LongBitSet.FACTORY.create();
			for (Integer i : indexes) {
				if (bitset.get(indexes[i.intValue()].intValue())) {
					bitset2.set(i.intValue());
				}
			}
			sortedNegCover.add(bitset2);
		});

		Collections.sort(sortedNegCover, new Comparator<IBitSet>() {
			@Override
			public int compare(IBitSet o1, IBitSet o2) {
				int erg = Integer.compare(o2.cardinality(), o1.cardinality());
				return erg != 0 ? erg : o2.compareTo(o1);
			}
		});

		for (int target = 0; target < numberAttributes; ++target) {
			if(constantColumns.get(target))
				continue;
			final int targetF = invIndexes[target];
			TreeSearch neg = new TreeSearch();
			sortedNegCover.stream()
					.filter(invalidFD -> !invalidFD.get(targetF))
					.forEach(invalid -> addInvalidToNeg(neg, invalid));

			TreeSearch posCover = mostGeneralFDs(targetF);

			final ArrayList<IBitSet> list = new ArrayList<IBitSet>();
			neg.forEach(invalidFD -> list.add(invalidFD));
			Collections.sort(list, new Comparator<IBitSet>() {
				@Override
				public int compare(IBitSet o1, IBitSet o2) {
					int erg = Integer.compare(o2.cardinality(),
							o1.cardinality());
					return erg != 0 ? erg : o2.compareTo(o1);
				}
			});

			list.forEach(invalidFD -> handleInvalid(invalidFD, posCover, targetF));

			final int finalTarget = target;
			posCover.forEach(bitset -> {
				IBitSet valid = LongBitSet.FACTORY.create();
				for (int i = bitset.nextSetBit(0); i >= 0; i = bitset
						.nextSetBit(i + 1)) {
					valid.set(indexes[i].intValue());
				}
				try {
					resultReceiver.receiveResult(valid, finalTarget);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		}
		
		resultReceiver.finish();
	}

	public int getNegCoverSize() {
		return invalid.size();
	}

	private void addInvalidToNeg(TreeSearch neg, IBitSet invalid) {
		if (neg.findSuperSet(invalid) != null)
			return;

		getAndRemoveGeneralizations(neg, invalid);
		neg.add(invalid);
	}

	private TreeSearch mostGeneralFDs(int target) {
		TreeSearch tree = new TreeSearch();
		// determined by all other, non-constant attributes
		for (int single = 0; single < numberAttributes; ++single) {
			if (single != target && !constantColumns.get(indexes[single].intValue())) {		
				IBitSet bs = LongBitSet.FACTORY.create();
				bs.set(single);
				tree.add(bs);
			}
		}
		return tree;
	}

	private void handleInvalid(IBitSet invalidFD, TreeSearch tree, int target) {
		Set<IBitSet> remove = getAndRemoveGeneralizations(tree, invalidFD);
		for (IBitSet removed : remove) {
			for (int i = 0; i < numberAttributes; ++i) {
				if (i == target || invalidFD.get(i) || constantColumns.get(indexes[i].intValue()))
					continue;
				IBitSet add = LongBitSet.FACTORY.create(removed);
				add.set(i);
				// add can't be subset of other remove bitset:
				// - ith bit is not set in any bitset in remove
				// --> add is valid
				// add can't be subset of other added bitset:
				// - both would need to set bit i
				// - since both weren't subsets of each other before,
				// they are not now
				// --> still minimal
				// only check whether it is a subset of remaining tree
				if (tree.findSubSet(add) == null) {
					tree.add(add);
				}
			}
		}
	}

	private Set<IBitSet> getAndRemoveGeneralizations(TreeSearch tree,
			IBitSet invalidFD) {
		final Set<IBitSet> remove = new HashSet<IBitSet>();
		// search all subsets and remove them from tree
		tree.forEachSubSet(invalidFD, t -> remove.add(t));
		remove.forEach(i -> tree.remove(i));
		return remove;
	}

	public void setConstantColumns(IBitSet constantColumns) throws ColumnNameMismatchException {
		this.constantColumns = constantColumns;
		for (int i = constantColumns.nextSetBit(0); i >= 0; i = constantColumns
				.nextSetBit(i + 1)) {
			resultReceiver.receiveResult(LongBitSet.FACTORY.create(), i);
		}
	}
}
