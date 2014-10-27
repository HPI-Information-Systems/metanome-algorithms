package de.metanome.algorithms.fastfds.fastfds_helper.modules.container;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.List;

import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.results.FunctionalDependency;

public class FunctionalDependencyGroup2 {

    private int attribute = Integer.MIN_VALUE;
    private IntSet values = new IntArraySet();

    public FunctionalDependencyGroup2(int attributeID, IntList values) {

        this.attribute = attributeID;
        this.values.addAll(values);
    }

    public int getAttributeID() {

        return this.attribute;
    }

    public IntList getValues() {

        IntList returnValue = new IntArrayList();
        returnValue.addAll(this.values);
        return returnValue;

    }

    @Override
    public String toString() {

        return this.values + " --> " + this.attribute;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + attribute;
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FunctionalDependencyGroup2 other = (FunctionalDependencyGroup2) obj;
        if (attribute != other.attribute)
            return false;
        if (values == null) {
            if (other.values != null)
                return false;
        } else if (!values.equals(other.values))
            return false;
        return true;
    }

    public FunctionalDependency buildDependency(String tableIdentifier, List<String> columnNames) {

        ColumnIdentifier[] combination = new ColumnIdentifier[this.values.size()];
        int j = 0;
        for (int i : this.values) {
            // combination[i] = new ColumnIdentifier(tableIdentifier, String.valueOf(determiningCombination.getInt(i)));
            combination[j] = new ColumnIdentifier(tableIdentifier, columnNames.get(i));
            j++;
        }
        ColumnCombination cc = new ColumnCombination(combination);
        // ColumnIdentifier ci = new ColumnIdentifier(tableIdentifier, String.valueOf(dependentAttr));
        ColumnIdentifier ci = new ColumnIdentifier(tableIdentifier, columnNames.get(this.attribute));
        FunctionalDependency fd = new FunctionalDependency(cc, ci);
        return fd;
    }

}
