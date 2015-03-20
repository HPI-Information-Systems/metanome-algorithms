package de.metanome.algorithms.order.check;

import it.unimi.dsi.fastutil.BigList;
import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;
import de.metanome.algorithms.order.types.Permutation;

public class DependencyResult {
  BigList<Permutation> satisfied;
  BigList<Permutation> unsatisfied;

  public DependencyResult() {
    this.satisfied = new ObjectBigArrayBigList<Permutation>();
    this.unsatisfied = new ObjectBigArrayBigList<Permutation>();
  }

  public void addSatisfied(final Permutation satisfiedPermutation) {
    this.satisfied.add(satisfiedPermutation);
  }

  public void addUnsatisfied(final Permutation unsatisfiedPermutation) {
    this.unsatisfied.add(unsatisfiedPermutation);
  }

  public BigList<Permutation> getSatisfied() {
    return this.satisfied;
  }

  public void setSatisfied(final BigList<Permutation> satisfied) {
    this.satisfied = satisfied;
  }

  public BigList<Permutation> getUnsatisfied() {
    return this.unsatisfied;
  }

  public void setUnsatisfied(final BigList<Permutation> unsatisfied) {
    this.unsatisfied = unsatisfied;
  }

}
