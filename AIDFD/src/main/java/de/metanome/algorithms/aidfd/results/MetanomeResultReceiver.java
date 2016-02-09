package de.metanome.algorithms.aidfd.results;

import java.util.ArrayList;
import ch.javasoft.bitset.IBitSet;
import de.metanome.algorithms.aidfd.helpers.FD;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;

public class MetanomeResultReceiver {

	protected ArrayList<ColumnIdentifier> columnIdents;
	private FunctionalDependencyResultReceiver resultReceiver;

	public MetanomeResultReceiver(RelationalInput input,
			FunctionalDependencyResultReceiver resultReceiver) {
		this.resultReceiver = resultReceiver;
		this.columnIdents = new ArrayList<ColumnIdentifier>();
		for (int i = 0; i < input.numberOfColumns(); ++i) {
			this.columnIdents.add(new ColumnIdentifier(input.relationName(),
					input.columnNames().get(i)));
		}
	}

	public void receiveResult(IBitSet subset, int rhs) throws ColumnNameMismatchException {
		outputFD(new FD(rhs, subset));
	}

	public void finish() throws ColumnNameMismatchException {
	}
	
	protected void outputFD(FD fd) throws ColumnNameMismatchException {
		ColumnIdentifier dependant = columnIdents.get(fd.rhs);

		ColumnIdentifier[] identifierList = new ColumnIdentifier[fd.lhs.cardinality()];
		int j = 0;
		for (int columnIndex = fd.lhs.nextSetBit(0); columnIndex >= 0; columnIndex = fd.lhs
				.nextSetBit(columnIndex + 1)) {
			identifierList[j] = columnIdents.get(columnIndex);
			++j;
		}

		try {
			resultReceiver.receiveResult(new FunctionalDependency(
				new ColumnCombination(identifierList), dependant));
		} catch (CouldNotReceiveResultException e) {
			e.printStackTrace();
		}
	}
}
