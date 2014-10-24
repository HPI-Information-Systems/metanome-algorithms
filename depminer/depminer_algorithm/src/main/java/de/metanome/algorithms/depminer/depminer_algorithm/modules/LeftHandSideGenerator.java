package de.metanome.algorithms.depminer.depminer_algorithm.modules;

import de.metanome.algorithms.depminer.depminer_helper.modules.Algorithm_Group2_Modul;
import de.metanome.algorithms.depminer.depminer_helper.modules.container.CMAX_SET;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.lucene.util.OpenBitSet;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
     * @return {@code Int2ObjectMap<List<OpenBitSet>>} (key: dependent attribute, value: set of all lefthand sides)
     */
    public Int2ObjectMap<List<OpenBitSet>> execute(List<CMAX_SET> maximalSets, int nrOfAttributes) {

        if (this.timeMesurement) {
            this.startTime();
        }

        Int2ObjectMap<List<OpenBitSet>> lhs = new Int2ObjectOpenHashMap<List<OpenBitSet>>();

		/* 1: for all attributes A in R do */
        for (int attribute = 0; attribute < nrOfAttributes; attribute++) {

			/* 2: i:=1 */
            // int i = 1;

			/* 3: Li:={B | B in X, X in cmax(dep(r),A)} */
            Set<OpenBitSet> Li = new HashSet<OpenBitSet>();
            CMAX_SET correctSet = this.generateFirstLevelAndFindCorrectSet(maximalSets, attribute, Li);

            List<List<OpenBitSet>> lhs_a = new LinkedList<List<OpenBitSet>>();

			/* 4: while Li != ø do */
            while (!Li.isEmpty()) {

				/*
                 * 5: LHSi[A]:={l in Li | l intersect X != ø, for all X in cmax(dep(r),A)}
				 */
                List<OpenBitSet> lhs_i = findLHS(Li, correctSet);

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
                lhs.put(attribute, new LinkedList<OpenBitSet>());
            }
            for (List<OpenBitSet> lhs_ia : lhs_a) {
                lhs.get(attribute).addAll(lhs_ia);
            }
        }

        if (this.timeMesurement) {
            this.stopTime();
        }

        return lhs;
    }

    private List<OpenBitSet> findLHS(Set<OpenBitSet> Li, CMAX_SET correctSet) {

        List<OpenBitSet> lhs_i = new LinkedList<OpenBitSet>();
        for (OpenBitSet l : Li) {
            boolean isLHS = true;
            for (OpenBitSet x : correctSet.getCombinations()) {
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

    private CMAX_SET generateFirstLevelAndFindCorrectSet(List<CMAX_SET> maximalSets, int attribute, Set<OpenBitSet> Li) {

        CMAX_SET correctSet = null;
        for (CMAX_SET set : maximalSets) {
            if (!(set.getAttribute() == attribute)) {
                continue;
            }
            correctSet = set;
            for (OpenBitSet list : correctSet.getCombinations()) {

                OpenBitSet combination;
                int lastIndex = list.nextSetBit(0);
                while (lastIndex != -1) {
                    combination = new OpenBitSet();
                    combination.set(lastIndex);
                    Li.add(combination);
                    lastIndex = list.nextSetBit(lastIndex + 1);
                }
            }
            break;
        }
        return correctSet;
    }

    private Set<OpenBitSet> generateNextLevel(Set<OpenBitSet> li) {

        // Join-Step
        List<OpenBitSet> Ck = new LinkedList<OpenBitSet>();
        for (OpenBitSet p : li) {
            for (OpenBitSet q : li) {
                if (!this.checkJoinCondition(p, q)) {
                    continue;
                }
                OpenBitSet candidate = new OpenBitSet();
                candidate.or(p);
                candidate.or(q);
                Ck.add(candidate);
            }
        }

        // Pruning-Step
        Set<OpenBitSet> result = new HashSet<OpenBitSet>();
        for (OpenBitSet c : Ck) {
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

    private boolean checkJoinCondition(OpenBitSet p, OpenBitSet q) {

        if (p.prevSetBit(p.length()) >= q.prevSetBit(p.length())) {
            return false;
        }
        for (int i = 0; i < p.prevSetBit(p.length()); i++) {
            if (p.getBit(i) != q.getBit(i)) {
                return false;
            }
        }
        return true;
    }

}
