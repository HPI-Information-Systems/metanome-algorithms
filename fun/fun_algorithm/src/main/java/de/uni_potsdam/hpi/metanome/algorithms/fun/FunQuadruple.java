package de.uni_potsdam.hpi.metanome.algorithms.fun;


import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;

public class FunQuadruple {

    public ColumnCombinationBitset candidate;
    public long count;
    public ColumnCombinationBitset quasiclosure;
    public ColumnCombinationBitset closure;

    public FunQuadruple(ColumnCombinationBitset candidate, long count) {
        this(candidate, count, null, null);
    }

    public FunQuadruple(ColumnCombinationBitset candidate, long count, ColumnCombinationBitset quasiclosure, ColumnCombinationBitset closure) {
        this.candidate = candidate;
        this.count = count;
        this.quasiclosure = quasiclosure;
        this.closure = closure;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((candidate == null) ? 0 : candidate.hashCode());
        result = prime * result + ((closure == null) ? 0 : closure.hashCode());
        result = prime * result + (int) (count ^ (count >>> 32));
        result = prime * result
                + ((quasiclosure == null) ? 0 : quasiclosure.hashCode());
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
        FunQuadruple other = (FunQuadruple) obj;
        if (candidate == null) {
            if (other.candidate != null)
                return false;
        } else if (!candidate.equals(other.candidate))
            return false;
        if (closure == null) {
            if (other.closure != null)
                return false;
        } else if (!closure.equals(other.closure))
            return false;
        if (count != other.count)
            return false;
        if (quasiclosure == null) {
            if (other.quasiclosure != null)
                return false;
        } else if (!quasiclosure.equals(other.quasiclosure))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "FunQuadruple [candidate=" + candidate + "]";
    }

}
