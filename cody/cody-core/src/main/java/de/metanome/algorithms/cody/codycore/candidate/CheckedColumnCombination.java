package de.metanome.algorithms.cody.codycore.candidate;

import lombok.Value;
import lombok.experimental.Delegate;

@Value
public class CheckedColumnCombination {

    /**
     * Holds the respective ColumnCombination object
     */
    @Delegate(types = ColumnCombination.class)
    ColumnCombination columnCombination;

    /**
     * Indicates the ColumnCombinations support as rows where Cody is valid / total rows
     */
    double support;

    public String toString(String[] indexToNameMapping) {
        StringBuilder result = new StringBuilder();
        result.append("CheckedColumnCombination(left=[");
        for (int i = this.getLeft().nextSetBit(0); i != -1; i = this.getLeft().nextSetBit(i + 1)) {
            result.append(indexToNameMapping[i]).append(", ");
        }
        result.delete(result.length() - 2, result.length());
        result.append("], right=[");
        for (int i = this.getRight().nextSetBit(0); i != -1; i = this.getRight().nextSetBit(i + 1)) {
            result.append(indexToNameMapping[i]).append(", ");
        }
        result.delete(result.length() - 2, result.length());
        result.append("], support=");
        result.append(this.getSupport());
        result.append(")");

        return result.toString();
    }
}
