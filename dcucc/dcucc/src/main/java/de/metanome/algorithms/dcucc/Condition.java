package de.metanome.algorithms.dcucc;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jens Ehrlich
 */
public class Condition {

  protected ColumnCombinationBitset partialUnique;
  protected List<ConditionElement> conditions;
  protected float coverage = Float.NaN;

  public Condition(ColumnCombinationBitset partialUnique,
                   Map<ColumnCombinationBitset, SingleCondition> conditions) {
    this.partialUnique = partialUnique;
    this.conditions = new ArrayList<>();
    for (ColumnCombinationBitset condition : conditions.keySet()) {
      this.conditions.add(new ConditionElement(condition, conditions.get(condition)));
    }
  }

  public Condition(ColumnCombinationBitset partialUnique,
                   List<ConditionEntry> conditions) {
    this.partialUnique = partialUnique;
    this.conditions = new ArrayList<>();
    for (ConditionEntry entry : conditions) {
      SingleCondition conditionValue = new SingleCondition();
      conditionValue.addCluster(entry.cluster.get(0), entry.coverage);
      this.conditions.add(new ConditionElement(entry.condition, conditionValue));
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Condition condition = (Condition) o;

    if (!conditions.equals(condition.conditions)) {
      return false;
    }
    if (!partialUnique.equals(condition.partialUnique)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = partialUnique.hashCode();
    result = 31 * result + conditions.hashCode();
    return result;
  }

  public class ConditionElement {

    public ColumnCombinationBitset condition;
    public SingleCondition value;

    ConditionElement(ColumnCombinationBitset condition, SingleCondition value) {
      this.condition = condition;
      this.value = value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      ConditionElement that = (ConditionElement) o;

      if (condition != null ? !condition.equals(that.condition) : that.condition != null) {
        return false;
      }
      if (value != null ? !value.equals(that.value) : that.value != null) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = condition != null ? condition.hashCode() : 0;
      result = 31 * result + (value != null ? value.hashCode() : 0);
      return result;
    }
  }
}

