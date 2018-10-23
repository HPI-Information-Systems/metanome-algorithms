package de.hpi.mpss2015n.approxind.utils;


import java.util.*;
import java.util.stream.Collectors;

public final class CandidateGenerator {


    public List<SimpleInd> createCombinedCandidates(List<SimpleInd> inds, boolean isCombineNull, AbstractColumnStore[] stores) {
        Map<SimpleColumnCombination, SimpleColumnCombination> columnCombinations = new HashMap<>();

        // Check that we are not merging NULL columns if this is not desired (cf. BINDER).
        // The slight difference with ignoring NULL columns is that we still consider them for unary INDs.
        if (!isCombineNull) {
            inds = inds.stream()
                    .filter(ind -> {
                        AbstractColumnStore store = stores[ind.left.getTable()];
                        for (int column : ind.left.getColumns()) {
                            if (store.isNullColumn(column)) return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        }


        List<SimpleInd> newIndCandidates = new ArrayList<>();
        HashSet<SimpleInd> lastResults = new HashSet<>(inds);
        inds.sort(SimpleInd.prefixBlockComparator);
        for (int i = 0; i < inds.size(); i++) {
            SimpleInd a = inds.get(i);

            for (int j = i + 1; j < inds.size(); j++) {
                SimpleInd b = inds.get(j);

                // Check if LHS(a) and LHS(b) belong to the same prefix block.
                if (!a.left.startsWith(b.left) || !a.right.startsWith(b.right)) break;

                // Skip over completely equal LHS or RHS.
                if (a.left.lastColumn() == b.left.lastColumn() || a.right.lastColumn() == b.right.lastColumn()) {
                    continue;
                }

                // Combine the two INDs and test them.
                SimpleInd newCandidate = a.combineWith(b, columnCombinations);
                if (isPotentiallyValid(newCandidate, lastResults)) {
                    newIndCandidates.add(newCandidate);
                }

            }
        }
        return newIndCandidates;
    }

    private boolean isPotentiallyValid(SimpleInd newCandidate, HashSet<SimpleInd> lastResults) {
        //only allow each column once in LHS and RHS - comment out in case this is not desired!
        if (newCandidate.left.getTable() == newCandidate.right.getTable()) {
            for (int i : newCandidate.left.getColumns()) {
                for (int j : newCandidate.right.getColumns()) {
                    if (i == j) {
                        return false;
                    }
                }
            }
        }
        if (newCandidate.size() <= 2) {
            return true;
        }
        List<SimpleInd> toBeTested = new ArrayList<>();
        int checks = newCandidate.size() - 2;
        for (int k = 0; k < checks; k++) {
            toBeTested.add(newCandidate.flipOff(k));
        }
        return lastResults.containsAll(toBeTested);
    }
}
