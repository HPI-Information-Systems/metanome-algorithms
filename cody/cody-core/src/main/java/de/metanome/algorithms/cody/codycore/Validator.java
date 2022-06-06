package de.metanome.algorithms.cody.codycore;

import de.metanome.algorithms.cody.codycore.candidate.CheckedColumnCombination;
import de.metanome.algorithms.cody.codycore.candidate.ColumnCombination;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.roaringbitmap.buffer.BufferFastAggregation;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Validator {

    private final Configuration configuration;
    private final int nRows;
    private final int[] rowCounts;
    private final List<List<ImmutableRoaringBitmap>> plis;
    private final List<List<Double>> supports;

    public Validator(@NonNull Configuration configuration, @NonNull List<ImmutableRoaringBitmap> dataset, int nRows,
                     int[] rowCounts) {
        this.configuration = configuration;
        this.nRows = nRows;
        this.rowCounts = rowCounts;

        ArrayList<List<ImmutableRoaringBitmap>> plis = new ArrayList<>(dataset.size());
        ArrayList<List<Double>> supports = new ArrayList<>(dataset.size());

        for (int left = 0; left < dataset.size(); left++) {
            ArrayList<ImmutableRoaringBitmap> currentLevelPlis = new ArrayList<>(dataset.size());
            ArrayList<Double> currentLevelSupports = new ArrayList<>(dataset.size());

            for (int right = 0; right < dataset.size(); right++) {
                if (left > right) {
                    currentLevelPlis.add(plis.get(right).get(left));
                    currentLevelSupports.add(supports.get(right).get(left));
                } else {
                    ImmutableRoaringBitmap pli = ImmutableRoaringBitmap.xor(dataset.get(left), dataset.get(right));
                    currentLevelPlis.add(pli);
                    currentLevelSupports.add(this.calculateSupport(pli));
                }
            }

            plis.add(ImmutableList.copyOf(currentLevelPlis));
            supports.add(ImmutableList.copyOf(currentLevelSupports));
        }

        this.plis = ImmutableList.copyOf(plis);
        this.supports = ImmutableList.copyOf(supports);
    }

    private double calculateSupport(ImmutableRoaringBitmap pli) {
        double support = 0.0;
        if (pli.cardinalityExceeds(0)) {
            for (int index : pli)
                support += this.rowCounts[index];
        }

        return support / (double) this.nRows;
    }

    /**
     * Calculate support of a given ColumnCombination
     *
     * @param c ColumnCombination to check
     * @return CheckedColumnCombination wit support set to respective value
     */
    public CheckedColumnCombination checkColumnCombination(ColumnCombination c) {
        ImmutableRoaringBitmap[] pliList =
                new ImmutableRoaringBitmap[c.getLeft().cardinality() * c.getRight().cardinality()];

        int index = 0;
        for (int left = c.getLeft().nextSetBit(0); left != -1; left = c.getLeft().nextSetBit(left + 1)) {
            for (int right = c.getRight().nextSetBit(0); right != -1; right = c.getRight().nextSetBit(right + 1)) {
                pliList[index] = this.plis.get(left).get(right);
                index++;
            }
        }

        return new CheckedColumnCombination(c,
                this.calculateSupport(BufferFastAggregation.and(pliList)));
    }

    /**
     * Get a 2D adjacency matrix with the support for unary Codys between all columns
     *
     * @return a list of lists (all same length) with the support as a double
     */
    public List<List<Double>> getGraphView() {
        return this.supports;
    }
}
