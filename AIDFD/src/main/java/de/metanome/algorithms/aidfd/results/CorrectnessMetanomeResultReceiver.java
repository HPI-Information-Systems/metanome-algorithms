package de.metanome.algorithms.aidfd.results;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;

import de.metanome.algorithms.aidfd.helpers.ArrayIndexComparator;
import de.metanome.algorithms.aidfd.helpers.FD;
import de.metanome.algorithms.aidfd.helpers.StrippedPartition;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;

public class CorrectnessMetanomeResultReceiver extends MetanomeResultReceiver {

	private List<FD> fds = new ArrayList<FD>();
	private int nextPos = 0;
	private Integer[] indexes;

	public CorrectnessMetanomeResultReceiver(RelationalInput input,
			FunctionalDependencyResultReceiver resultReceiver) {
		super(input, resultReceiver);
	}

	@Override
	public void receiveResult(IBitSet subset, int i) {
		fds.add(new FD(i, subset));
	}

	@Override
	public void finish() throws ColumnNameMismatchException {
		long time = System.currentTimeMillis();
		System.out.println("Filtering incorrect FDs");

		int[] counts = new int[this.columnIdents.size()];
		for(FD fd : fds) {
			for(int i = fd.lhs.nextSetBit(0); i >= 0; i = fd.lhs.nextSetBit(i + 1)) {
				counts[i]++;
			}
		}
		for (int i = 0; i < columnIdents.size(); ++i) {
			counts[i] *= StrippedPartition.columns[i].size();
		}

		ArrayIndexComparator comparator = new ArrayIndexComparator(counts, ArrayIndexComparator.Order.DESCENDING);
		indexes = comparator.createIndexArray();
		Arrays.sort(indexes, comparator);
		for (FD fd : fds) {
			fd.setSort(indexes);
		}

		System.out.println("Reordering & sorting took " + (System.currentTimeMillis() - time) + " ms");

		Collections.sort(fds);

		StrippedPartition current = StrippedPartition.EMPTY;
		check(current, 0);

		System.out.println("Checked " + nextPos + " FDs in " + (System.currentTimeMillis() - time) + " ms");
	}
	
	public void check(StrippedPartition current, int currentPos) throws ColumnNameMismatchException {

		int pos = fds.get(nextPos).lhsSort.nextSetBit(currentPos);
		if (pos < 0) {
			if (!current.isRefindedBy(fds.get(nextPos).rhs))
				outputFD(fds.get(nextPos));

			nextPos += 1;
			while (nextPos < fds.size()
					&& current.currentBS.isSubSetOf(fds.get(nextPos).lhs)) {
				if (!current.isRefindedBy(fds.get(nextPos).rhs))
					outputFD(fds.get(nextPos));

				nextPos += 1;
			}
			return;
		}
		IBitSet newBS = LongBitSet.FACTORY.create(current.currentBS);
		newBS.set(indexes[pos]);
		check(current.getNew(newBS), pos + 1);
		if (nextPos < fds.size()
				&& current.currentBS.isSubSetOf(fds.get(nextPos).lhs))
			check(current, pos + 1);
	}
}
