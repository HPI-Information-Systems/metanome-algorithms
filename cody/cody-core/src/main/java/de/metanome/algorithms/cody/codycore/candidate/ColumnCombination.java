package de.metanome.algorithms.cody.codycore.candidate;

import ch.javasoft.bitset.LongBitSet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

@Value
public class ColumnCombination {

    /**
     * Indicates left-hand side columns
     */
    @NonNull LongBitSet left;

    /**
     * Indicates right-hand side columns
     */
    @NonNull LongBitSet right;

    /**
     * Convenience access to left join right for hashing
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Getter(lazy = true)
    LongBitSet columns = this.joinLeftRight();

    private LongBitSet joinLeftRight() {
        return LongBitSet.getOr(this.getLeft(), this.getRight());
    }

    public String toString() {
        return "ColumnCombination(left=" + this.getLeft().toBitSet() + ", right=" + this.getRight().toBitSet() + ")";
    }
}
