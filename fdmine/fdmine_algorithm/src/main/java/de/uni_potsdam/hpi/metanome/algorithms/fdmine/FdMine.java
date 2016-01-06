package de.uni_potsdam.hpi.metanome.algorithms.fdmine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.algorithm_types.FunctionalDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;

public class FdMine implements FunctionalDependencyAlgorithm, RelationalInputParameterAlgorithm {

    protected static final String INPUT_FILE_TAG = "Relational Input";

    protected RelationalInputGenerator inputGenerator;
    protected FunctionalDependencyResultReceiver resultReceiver;
    protected ColumnCombinationBitset r;
    protected Map<ColumnCombinationBitset, ColumnCombinationBitset> closure = new HashMap<>();
    protected String relationName;
    protected List<String> columnNames;
    Map<ColumnCombinationBitset, PositionListIndex> plis = new HashMap<>();
    Map<ColumnCombinationBitset, ColumnCombinationBitset> fdSet = new HashMap<>();
    Set<ColumnCombinationBitset> keySet = new HashSet<>();
    Map<ColumnCombinationBitset, HashSet<ColumnCombinationBitset>> eqSet = new HashMap<>();

    @Override
    public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
        ArrayList<ConfigurationRequirement<?>> configurationSpecifications = new ArrayList<>();

        configurationSpecifications.add(new ConfigurationRequirementRelationalInput(INPUT_FILE_TAG));

        return configurationSpecifications;
    }

    @Override
    public void execute() throws InputGenerationException, InputIterationException, CouldNotReceiveResultException, AlgorithmConfigurationException {
        RelationalInput input = inputGenerator.generateNewCopy();

        relationName = input.relationName();
        columnNames = input.columnNames();

        PLIBuilder pliBuilder = new PLIBuilder(input);

        int columnIndex = 0;
        for (PositionListIndex pli : pliBuilder.getPLIList()) {
            plis.put(new ColumnCombinationBitset(columnIndex), pli);

            columnIndex++;
        }

        r = new ColumnCombinationBitset();
        Set<ColumnCombinationBitset> candidateSet = new HashSet<>();
        for (columnIndex = 0; columnIndex < input.numberOfColumns(); columnIndex++) {
            r.addColumn(columnIndex);
            candidateSet.add(new ColumnCombinationBitset(columnIndex));
        }

        for (ColumnCombinationBitset xi : candidateSet) {
            closure.put(xi, new ColumnCombinationBitset());
        }

        while (!candidateSet.isEmpty()) {
            for (ColumnCombinationBitset xi : candidateSet) {
                computeNonTrivialClosure(xi);
                obtainFdAndKey(xi);
            }
            // TODO reflexive?
            obtainEqSet(candidateSet);
            pruneCandidates(candidateSet);
            generateCandidates(candidateSet);
        }
        displayFD();
    }

    protected void computeNonTrivialClosure(ColumnCombinationBitset xi) throws CouldNotReceiveResultException {
        ColumnCombinationBitset xiClosure = closure.get(xi);
        for (int columnIndex : r.minus(xi).minus(xiClosure).getSetBits()) {
            ColumnCombinationBitset y = new ColumnCombinationBitset(columnIndex);
            ColumnCombinationBitset xiy = new ColumnCombinationBitset(xi);
            xiy.addColumn(columnIndex);
            PositionListIndex xiPli = plis.get(xi);
            // TODO optimise do these plis need to be computed?
            PositionListIndex xiyPli = xiPli.intersect(plis.get(y));
            plis.put(xiy, xiyPli);


            if (xiPli.getRawKeyError() == xiyPli.getRawKeyError()) {
                xiClosure.addColumn(columnIndex);

                //not in paper!!!
//				outputFd(xi, columnIndex);
            }
        }
    }

    protected void obtainFdAndKey(ColumnCombinationBitset xi) {
        // TODO is this the only place and add operation happens?
        ColumnCombinationBitset xiClosure = closure.get(xi);
        fdSet.put(xi, xiClosure);
        if (r.equals(xi.union(xiClosure))) {
            // TODO emit keys as results.
            keySet.add(xi);
        }
    }

    protected void obtainEqSet(Set<ColumnCombinationBitset> candidateSet) {
        for (ColumnCombinationBitset xi : candidateSet) {
            for (ColumnCombinationBitset x : fdSet.keySet()) {
                ColumnCombinationBitset z = xi.intersect(x);
                ColumnCombinationBitset xClosure = fdSet.get(x);
                if ((xClosure.containsSubset(xi.minus(z))) && (closure.get(xi).containsSubset(x.minus(z)))) {
                    if (!x.equals(xi))
                        addToEqualSet(x, xi);
                }
            }
        }
    }

    protected void addToEqualSet(ColumnCombinationBitset first, ColumnCombinationBitset second) {
        // TODO optimise do not store twice
        if (!eqSet.containsKey(first)) {
            eqSet.put(first, new HashSet<ColumnCombinationBitset>());
        }
        eqSet.get(first).add(second);

        if (!eqSet.containsKey(second)) {
            eqSet.put(second, new HashSet<ColumnCombinationBitset>());
        }
        eqSet.get(second).add(first);
    }

    protected void pruneCandidates(Set<ColumnCombinationBitset> candidateSet) {

        Iterator<ColumnCombinationBitset> candidateIterator = candidateSet.iterator();
        ColumnCombinationBitset xi;
        outer:
        while (candidateIterator.hasNext()) {
            xi = candidateIterator.next();

            if (eqSet.containsKey(xi)) {
                for (ColumnCombinationBitset xj : eqSet.get(xi)) {
                    if (candidateSet.contains(xj)) {
                        // TODO not in the paper: remove already found fds?
                        //this.fdSet.remove(xj);
                        candidateIterator.remove();
                        continue outer;
                    }
                }
            }
            if (keySet.contains(xi)) {
                candidateIterator.remove();
                continue;
            }
        }
    }


    protected void generateCandidates(Set<ColumnCombinationBitset> candidateSet) {
        List<ColumnCombinationBitset> candidates = new ArrayList<>(candidateSet);
        ColumnCombinationBitset xi;
        ColumnCombinationBitset xj;
        ColumnCombinationBitset xij;
        for (int i = 0; i < candidates.size(); i++) {
            xi = candidates.get(i);
            candidateSet.remove(xi);
            outer:
            for (int j = i + 1; j < candidates.size(); j++) {
                xj = candidates.get(j);
                boolean similarCandidate = true;
                int k;
                // TODO optimise use bitset operation?
                for (k = 0; k < xi.size() - 1; k++) {
                    if (xi.getSetBits().get(k).equals(xj.getSetBits().get(k))) {
                        continue;
                    } else {
                        similarCandidate = false;
                        break;
                    }
                }
                if (similarCandidate) {
                    xij = xi.union(xj);
                    // FIXME xi does not need to be in fdSet
                    if (((!fdSet.containsKey(xi)) || (!this.fdSet.get(xi).containsSubset(xj))) &&
                            ((!fdSet.containsKey(xj)) || (!this.fdSet.get(xj).containsSubset(xi)))) {
                        PositionListIndex xijPLI = plis.get(xi).intersect(plis.get(xj));
                        plis.put(xij, xijPLI);

                        ColumnCombinationBitset xijClosure = new ColumnCombinationBitset();// = this.closure.get(xi).union(this.closure.get(xj));
                        for (ColumnCombinationBitset subset : xij.getDirectSubsets()) {
                            if (!closure.containsKey(subset)) {
                                continue outer;
                            }
                            xijClosure = xijClosure.union(closure.get(subset));
                        }
                        this.closure.put(xij, xijClosure);

                        if (r.equals(xij.union(xijClosure))) {
                            keySet.add(xij);
                        } else {
                            candidateSet.add(xij);
                        }
                    }
                }
            }
        }
    }

//	protected void outputFd(ColumnCombinationBitset determinant, int dependent) throws CouldNotReceiveResultException {
//
//		List<ColumnCombinationBitset> leftHandSideEQ = new LinkedList<ColumnCombinationBitset>();
//		leftHandSideEQ.add(determinant);
//		List<ColumnCombinationBitset> rightHandSideEQ = new LinkedList<ColumnCombinationBitset>();
//		rightHandSideEQ.add(new ColumnCombinationBitset().setColumns(dependent));
//
//		for (int i = 0; i<determinant.size(); i++) {
//			for (ColumnCombinationBitset subset : determinant.getNSubsetColumnCombinations(i)) {
//				if (this.eqSet.containsKey(subset)) {
//					for (ColumnCombinationBitset eqBitset : this.eqSet.get(subset)) {
//						leftHandSideEQ.add(determinant.minus(subset).union(eqBitset));
//					}
//				}
//			}
//		}
//		if (this.eqSet.containsKey(dependent)) {
//			for (ColumnCombinationBitset eqBitset : this.eqSet.get(new ColumnCombinationBitset().setColumns(dependent))) {
//				rightHandSideEQ.add(eqBitset);
//			}
//		}
//		for (ColumnCombinationBitset leftHandSide : leftHandSideEQ) {
//			for (ColumnCombinationBitset rightHandSide : rightHandSideEQ) {
//				ColumnCombination determinantOutput = createColumnCombination(leftHandSide);
//				resultReceiver.receiveResult(new FunctionalDependency(determinantOutput, new ColumnIdentifier(relationName, columnNames.get(rightHandSide.getSetBits().get(0)))));
//			}
//		}
//
//	}

    protected void displayFD() throws CouldNotReceiveResultException {
        for (ColumnCombinationBitset leftHandSide : fdSet.keySet()) {
            Queue<ColumnCombinationBitset> queue = new LinkedList<>();
            Set<ColumnCombinationBitset> leftHandSides = new HashSet<>();
            leftHandSides.add(leftHandSide);
            queue.add(leftHandSide);
            //Iterator<ColumnCombinationBitset> queueIterator = queue.iterator();
            ColumnCombinationBitset rightHandSide = fdSet.get(leftHandSide);
            // iterate over all left hand sides
            while (!queue.isEmpty()) {
                ColumnCombinationBitset currentLeftHandSide = queue.remove();
                for (ColumnCombinationBitset subsetEqualCC : eqSet.keySet()) {
                    // only evaluate subsets of current left hand side
                    if (subsetEqualCC.isSubsetOf(currentLeftHandSide)) {
                        // generate equivalent left hand sides
                        for (ColumnCombinationBitset equalCC : eqSet.get(subsetEqualCC)) {
                            ColumnCombinationBitset generatedLeftHandSide = currentLeftHandSide.minus(subsetEqualCC).union(equalCC);
                            if (!leftHandSides.contains(generatedLeftHandSide)) {
                                leftHandSides.add(generatedLeftHandSide);
                                queue.add(generatedLeftHandSide);
                            }
                        }
                    }
                    // only evaluate subsets of current right hand side
                    if (subsetEqualCC.isSubsetOf(rightHandSide)) {
                        // generate equivalent right hand sides
                        for (ColumnCombinationBitset equalCC : eqSet.get(subsetEqualCC)) {
                            rightHandSide = rightHandSide.union(equalCC);
                        }
                    }
                }
            }

            for (ColumnCombinationBitset outputLeftHandSide : leftHandSides) {
                for (Integer rightHandSideIndex : rightHandSide.getSetBits()) {
                    resultReceiver.receiveResult(
                            new FunctionalDependency(
                                    outputLeftHandSide.createColumnCombination(relationName, columnNames),
                                    new ColumnIdentifier(relationName, columnNames.get(rightHandSideIndex))));
                }
            }

        }

    }

    protected ColumnCombination createColumnCombination(ColumnCombinationBitset candidate) {
        ColumnIdentifier[] identifierList = new ColumnIdentifier[candidate.size()];
        int i = 0;
        for (Integer columnIndex : candidate.getSetBits()) {
            identifierList[i] = new ColumnIdentifier(this.relationName, this.columnNames.get(columnIndex));
            i++;
        }
        return new ColumnCombination(identifierList);
    }

    @Override
    public void setResultReceiver(FunctionalDependencyResultReceiver resultReceiver) {
        this.resultReceiver = resultReceiver;
    }

    @Override
    public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values) {
        if (identifier.equals(INPUT_FILE_TAG)) {
            this.inputGenerator = values[0];
        }
    }

}
