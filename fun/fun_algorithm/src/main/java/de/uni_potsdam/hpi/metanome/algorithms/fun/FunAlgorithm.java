package de.uni_potsdam.hpi.metanome.algorithms.fun;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;
import de.metanome.algorithm_integration.results.UniqueColumnCombination;

public class FunAlgorithm {

    protected Map<ColumnCombinationBitset, PositionListIndex> plis;
    protected ColumnCombinationBitset rDash;

    protected String relationName;
    protected List<String> columnNames;
    protected FunctionalDependencyResultReceiver fdReceiver = null;
    protected UniqueColumnCombinationResultReceiver uccReceiver = null;
    protected int numberOfColumns = -1;

    protected ColumnCombinationBitset allBitSetsColumnCombination;

    public FunAlgorithm(String relationName, List<String> columnNames,
                        FunctionalDependencyResultReceiver fdReceiver) {

        this.fdReceiver = fdReceiver;

        this.relationName = relationName;
        this.columnNames = columnNames;
        this.numberOfColumns = columnNames.size();

        this.allBitSetsColumnCombination = new ColumnCombinationBitset();
        for (int columnIndex = 0; columnIndex < this.numberOfColumns; columnIndex++) {
            this.allBitSetsColumnCombination.addColumn(columnIndex);
        }

        plis = new HashMap<>();
        // add empty set to plis
        plis.put(new ColumnCombinationBitset(), new PositionListIndex());
        rDash = new ColumnCombinationBitset();
    }

    public FunAlgorithm(String relationName, List<String> columnNames,
                        FunctionalDependencyResultReceiver fdReceiver,
                        UniqueColumnCombinationResultReceiver uccReceiver) {

        this(relationName, columnNames, fdReceiver);
        this.uccReceiver = uccReceiver;
    }

    public void run(List<PositionListIndex> pliList)
            throws InputIterationException, CouldNotReceiveResultException, ColumnNameMismatchException {

        LinkedList<FunQuadruple> lkminusOne = new LinkedList<>();
        lkminusOne.add(new FunQuadruple(new ColumnCombinationBitset(), Long.MAX_VALUE,
                new ColumnCombinationBitset(), new ColumnCombinationBitset()));

        LinkedList<FunQuadruple> lk = new LinkedList<>();
        for (int i = 0; i < numberOfColumns; i++) {
            PositionListIndex currentPli = pliList.get(i);
            plis.put(new ColumnCombinationBitset(i), currentPli);
            lk.add(new FunQuadruple(
                    new ColumnCombinationBitset(i), currentPli
                    .getRawKeyError(),
                    new ColumnCombinationBitset(i),
                    new ColumnCombinationBitset(i)));
            if (!currentPli.isUnique()) {
                rDash.addColumn(i);
            }
        }
        while (!lk.isEmpty()) {
            computeClosure(lkminusOne);
            computeQuasiClosure(lk, lkminusOne);
            displayFD(lkminusOne);
            purePrune(lk, lkminusOne);
            lkminusOne = lk;
            lk = generateCandidates(lk);
        }
        computeClosure(lkminusOne);
        displayFD(lkminusOne);
    }

    protected void computeClosure(List<FunQuadruple> lkminus1) {
        for (FunQuadruple l : lkminus1) {
            if (!plis.get(l.candidate).isUnique()) {
                l.closure = l.quasiclosure;
                for (ColumnCombinationBitset a : rDash.minus(l.quasiclosure)
                        .getContainedOneColumnCombinations()) {
                    // enable this for fun like pruning
                    if (!hasGreaterCount(l.candidate.union(a), l.count)) {
                        l.closure = l.closure.union(a);
                    }
                    // enable this for tane like pruning
                    // if
                    // (getOrCalculatePLI(l.candidate.union(a)).getRawKeyError()
                    // == l.count) {
                    // l.closure = l.closure.union(a);
                    // }
                }
            }
        }
    }

    // FIXME this method is never used
    protected PositionListIndex getOrCalculatePLI(
            ColumnCombinationBitset candidate) {
        if (plis.containsKey(candidate)) {
            return plis.get(candidate);
        } else {
            return addPliGenerate(candidate);
        }
    }

    protected void computeQuasiClosure(List<FunQuadruple> lk,
                                       List<FunQuadruple> lkMinus1) throws CouldNotReceiveResultException {
        for (FunQuadruple l : lk) {
            l.quasiclosure = l.candidate;

            for (FunQuadruple s : lkMinus1) {
                if (s.candidate.isProperSubsetOf(l.candidate)) {
                    l.quasiclosure = l.quasiclosure.union(s.closure);
                }
            }
            if (plis.get(l.candidate).isUnique()) {
                l.closure = new ColumnCombinationBitset(
                        this.allBitSetsColumnCombination);
            }
        }
    }

    protected void displayUCC(ColumnCombinationBitset ucc) throws CouldNotReceiveResultException, ColumnNameMismatchException {
        if (uccReceiver != null) {
            uccReceiver.receiveResult(
                    new UniqueColumnCombination(createColumnCombination(ucc)));
        }
    }

    protected void displayFD(List<FunQuadruple> lkMinus1)
            throws CouldNotReceiveResultException, ColumnNameMismatchException {
        for (FunQuadruple quadruple : lkMinus1) {
            if (quadruple.candidate.size() == 0) {
                continue;
            }
            // FIXME write testcase, check if all UCCs are found
            if (quadruple.closure.equals(this.allBitSetsColumnCombination)) {
                displayUCC(quadruple.candidate);
            }

            // TODO need to check if quadruple.count == 0 ? ->key ???

            ColumnCombinationBitset fdBitSet = quadruple.closure
                    .minus(quadruple.quasiclosure);

            if (fdBitSet.size() != 0) {
                ColumnCombination baseColumn = createColumnCombination(quadruple.candidate);
                for (ColumnCombinationBitset singleColumn : fdBitSet
                        .getContainedOneColumnCombinations()) {
                    fdReceiver.receiveResult(new FunctionalDependency(
                            baseColumn, new ColumnIdentifier(this.relationName,
                            this.columnNames.get(singleColumn
                                    .getSetBits().get(0)))));
                }
            }
        }
    }

    // TODO put into ColumnCombinationBitset
    protected ColumnCombination createColumnCombination(
            ColumnCombinationBitset candidate) {
        ColumnIdentifier[] identifierList = new ColumnIdentifier[candidate
                .size()];
        int i = 0;
        for (Integer columnIndex : candidate.getSetBits()) {
            identifierList[i] = new ColumnIdentifier(this.relationName,
                    this.columnNames.get(columnIndex));
            i++;
        }
        return new ColumnCombination(identifierList);
    }

    protected void purePrune(List<FunQuadruple> lk, List<FunQuadruple> lkMinus1) {
        FunQuadruple l;
        Iterator<FunQuadruple> lkIterator = lk.iterator();
        while (lkIterator.hasNext()) {
            l = lkIterator.next();
            for (FunQuadruple s : lkMinus1) {
                if (s.candidate.isProperSubsetOf(l.candidate)) {
                    if (l.count == s.count) {
                        lkIterator.remove();
                        break;
                    }
                }
            }
        }
    }

    protected LinkedList<FunQuadruple> generateCandidates(List<FunQuadruple> lk) {

        // what about keys (last statement in set expression from paper)

        LinkedList<FunQuadruple> lkPlus1 = new LinkedList<>();
        if (lk.isEmpty()) {
            return lkPlus1;
        }
        Set<ColumnCombinationBitset> subsets = new HashSet<>();
        int k = lk.get(0).candidate.size();
        ColumnCombinationBitset union = new ColumnCombinationBitset();
        for (FunQuadruple subsetQuadruple : lk) {
            // TODO optimise: all bits are set? --> break
            // If subsetQuadruple represents a unique do not add it to subsets and union (it should be pruned).
            if (subsetQuadruple.count == 0) {
                continue;
            }
            union = subsetQuadruple.candidate.union(union);
            subsets.add(subsetQuadruple.candidate);
        }

        Map<ColumnCombinationBitset, Integer> candidateGenerationCount = new HashMap<>();

        List<ColumnCombinationBitset> lkPlus1Candidates;
        FunQuadruple lkPlus1Member;
        for (ColumnCombinationBitset subset : subsets) {
            lkPlus1Candidates = union.getNSubsetColumnCombinationsSupersetOf(
                    subset, k + 1);
            // FIXME add key conditional here
            // Removed key conditional - should not be triggerable?
            for (ColumnCombinationBitset candidate : lkPlus1Candidates) {
                if (candidateGenerationCount.containsKey(candidate)) {
                    int count = candidateGenerationCount.get(candidate);
                    count++;
                    candidateGenerationCount.put(candidate, count);
                } else {
                    candidateGenerationCount.put(candidate, 1);
                }
            }
        }

        for (ColumnCombinationBitset candidate : candidateGenerationCount
                .keySet()) {
            if (candidateGenerationCount.get(candidate) == (k + 1)) {
                lkPlus1Member = new FunQuadruple(candidate, addPliGenerate(
                        candidate).getRawKeyError());
                lkPlus1.add(lkPlus1Member);
            }
        }
        return lkPlus1;
    }

    protected PositionListIndex addPliGenerate(ColumnCombinationBitset candidate) {

        ColumnCombinationBitset subset = new ColumnCombinationBitset(candidate);
        int firstSetColumnIndex = subset.getSetBits().get(0);
        subset.removeColumn(firstSetColumnIndex);

        ColumnCombinationBitset additionalColumn = new ColumnCombinationBitset(firstSetColumnIndex);
        // intersect plis
        PositionListIndex candidatePli = plis.get(subset).intersect(
                plis.get(additionalColumn));
        // store new pli
        plis.put(candidate, candidatePli);

        return candidatePli;
    }

    protected boolean hasGreaterCount(ColumnCombinationBitset lCandidate, long count) {
        if (plis.containsKey(lCandidate)) {
            return (plis.get(lCandidate).getRawKeyError() != count);
        }
        return recursiveFastCount(lCandidate, count);
    }

    protected boolean recursiveFastCount(ColumnCombinationBitset lCandidate, long count) {
        Set<ColumnCombinationBitset> fastCountChildren = new HashSet<>();
        fastCountChildren.addAll(lCandidate.getDirectSubsets());

        while (!fastCountChildren.isEmpty()) {
            Iterator<ColumnCombinationBitset> childIterator = fastCountChildren.iterator();
            while (childIterator.hasNext()) {
                ColumnCombinationBitset child = childIterator.next();
                if (plis.containsKey(child)) {
                    childIterator.remove();
                    long currentCount = plis.get(child).getRawKeyError();
                    if (currentCount < count) {
                        return true;
                    }
                }
            }

            Set<ColumnCombinationBitset> newChildren = new HashSet<>();
            for (ColumnCombinationBitset child : fastCountChildren) {
                newChildren.addAll(child.getDirectSubsets());
            }
            fastCountChildren.clear();
            fastCountChildren.addAll(newChildren);
        }
        return false;
    }

}
