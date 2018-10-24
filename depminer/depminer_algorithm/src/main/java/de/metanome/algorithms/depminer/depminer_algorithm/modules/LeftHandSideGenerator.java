package de.metanome.algorithms.depminer.depminer_algorithm.modules;

import java.util.BitSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.metanome.algorithms.depminer.depminer_helper.modules.Algorithm_Group2_Modul;
import de.metanome.algorithms.depminer.depminer_helper.modules.container.CMAX_SET;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Third phase of the Dep-Miner algorithm proposed in:
 * <p/>
 * "Efficient Discovery of Functional Dependencies and Armstrong Relations" Stéphane Lopes, Jean-Marc Petit, Lotfi Lakhal
 * <p/>
 * This module calculates the lefthand sides of functional dependencies from a given set of complements of maximal sets
 *
 * @author Tommy Neubert
 */
public class LeftHandSideGenerator extends Algorithm_Group2_Modul {

    public LeftHandSideGenerator(int numberOfThreads) {

        super(numberOfThreads, "LeftHandSideGen");
    }

    /**
     * Computes the LHS
     *
     * @param maximalSets    The set of the complements of maximal sets (see Phase 2 for further information)
     * @param nrOfAttributes The number attributes in the whole relation
     * @return {@code Int2ObjectMap<List<BitSet>>} (key: dependent attribute, value: set of all lefthand sides)
     */
    public Int2ObjectMap<List<BitSet>> execute(List<CMAX_SET> maximalSets, int nrOfAttributes) {

        if (this.timeMesurement) {
            this.startTime();
        }

        Int2ObjectMap<List<BitSet>> lhs = new Int2ObjectOpenHashMap<List<BitSet>>();

		/* 1: for all attributes A in R do */
        for (int attribute = 0; attribute < nrOfAttributes; attribute++) {

			/* 2: i:=1 */
            // int i = 1;

			/* 3: Li:={B | B in X, X in cmax(dep(r),A)} */
            Set<BitSet> Li = new HashSet<BitSet>();
            CMAX_SET correctSet = this.generateFirstLevelAndFindCorrectSet(maximalSets, attribute, Li);

            List<List<BitSet>> lhs_a = new LinkedList<List<BitSet>>();

			/* 4: while Li != ø do */
            while (!Li.isEmpty()) {

				/*
                 * 5: LHSi[A]:={l in Li | l intersect X != ø, for all X in cmax(dep(r),A)}
				 */
                List<BitSet> lhs_i = findLHS(Li, correctSet);

				/* 6: Li:=Li/LHSi[A] */
                Li.removeAll(lhs_i);

				/*
                 * 7: Li+1:={l' | |l'|=i+1 and for all l subset l' | |l|=i, l in Li}
				 */
                /*
				 * The generation of the next level is, as mentioned in the paper, done with the Apriori gen-function from the
				 * following paper: "Fast algorithms for mining association rules in large databases." - Rakesh Agrawal,
				 * Ramakrishnan Srikant
				 */
                Li = this.generateNextLevel(Li);

				/* 8: i:=i+1 */
                // i++;
                lhs_a.add(lhs_i);
            }

			/* 9: lhs(dep(r),A):= union LHSi[A] */
            if (!lhs.containsKey(attribute)) {
                lhs.put(attribute, new LinkedList<BitSet>());
            }
            for (List<BitSet> lhs_ia : lhs_a) {
                lhs.get(attribute).addAll(lhs_ia);
            }
        }

        if (this.timeMesurement) {
            this.stopTime();
        }

        return lhs;
    }

    private List<BitSet> findLHS(Set<BitSet> Li, CMAX_SET correctSet) {

        List<BitSet> lhs_i = new LinkedList<BitSet>();
        for (BitSet l : Li) {
            boolean isLHS = true;
            for (BitSet x : correctSet.getCombinations()) {
                if (!l.intersects(x)) {
                    isLHS = false;
                    break;
                }
            }
            if (isLHS) {
                lhs_i.add(l);
            }
        }
        return lhs_i;
    }

    private CMAX_SET generateFirstLevelAndFindCorrectSet(List<CMAX_SET> maximalSets, int attribute, Set<BitSet> Li) {

        CMAX_SET correctSet = null;
        for (CMAX_SET set : maximalSets) {
            if (!(set.getAttribute() == attribute)) {
                continue;
            }
            correctSet = set;
            for (BitSet list : correctSet.getCombinations()) {

                BitSet combination;
                int lastIndex = list.nextSetBit(0);
                while (lastIndex != -1) {
                    combination = new BitSet();
                    combination.set(lastIndex);
                    Li.add(combination);
                    lastIndex = list.nextSetBit(lastIndex + 1);
                }
            }
            break;
        }
        return correctSet;
    }

    private Set<BitSet> generateNextLevel(Set<BitSet> li) {

        // Join-Step
        List<BitSet> Ck = new LinkedList<BitSet>();
        for (BitSet p : li) {
            for (BitSet q : li) {
                if (!this.checkJoinCondition(p, q)) {
                    continue;
                }
                BitSet candidate = new BitSet();
                candidate.or(p);
                candidate.or(q);
                Ck.add(candidate);
            }
        }

        // Pruning-Step
        Set<BitSet> result = new HashSet<BitSet>();
        for (BitSet c : Ck) {
            boolean prune = false;
            int lastIndex = c.nextSetBit(0);
            while (lastIndex != -1) {
                c.flip(lastIndex);
                if (!li.contains(c)) {
                    prune = true;
                    break;
                }
                c.flip(lastIndex);
                lastIndex = c.nextSetBit(lastIndex + 1);
            }

            if (!prune) {
                result.add(c);
            }
        }

        return result;

    }

    private boolean checkJoinCondition(BitSet p, BitSet q) {

        if (p.length() >= q.length()) {
            return false;
        }
        
        BitSet intersection = (BitSet) p.clone();
        intersection.and(q);
        
        return p.cardinality() == intersection.cardinality() && 
        		q.cardinality() == intersection.cardinality();
    }

}
