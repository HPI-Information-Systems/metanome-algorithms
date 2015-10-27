package de.hpi.mpss2015n.approxind.utils;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public final class CandidateGenerator {


    public List<SimpleInd> createCombinedCandidates(List<SimpleInd> inds) {
        List<SimpleInd> newInds = new ArrayList<>();
        HashSet<SimpleInd> lastResults = new HashSet<>(inds);
        Collections.sort(inds);
        for (int i = 0; i < inds.size(); i++) {
            for (int j = i + 1; j < inds.size(); j++) {
                SimpleInd a = inds.get(i);
                SimpleInd b = inds.get(j);
                if(a.left.lastColumn() == b.left.lastColumn()){
                    continue; //otherwise symbol in planets everywhere...
                }
                if (a.startsWith(b) && a.right.lastColumn() != b.right.lastColumn()) {
                    SimpleInd newCandidate = a.combineWith(b);
                    if(isPotentiallyValid(newCandidate, lastResults)) newInds.add(newCandidate);
                }
                //Todo: why wrong result when break ... sorting problem?
            }
        }
        return newInds;
    }

    private boolean isPotentiallyValid(SimpleInd newCandidate, HashSet<SimpleInd> lastResults){
        //only allow each column once in LHS and RHS - comment out in case this is not desired!
        if(newCandidate.left.getTable() == newCandidate.right.getTable()) {
          for (int i : newCandidate.left.getColumns()) {
            for (int j : newCandidate.right.getColumns()) {
              if (i == j) {
                return false;
              }
            }
          }
        }
        if (newCandidate.size() <= 2){
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
