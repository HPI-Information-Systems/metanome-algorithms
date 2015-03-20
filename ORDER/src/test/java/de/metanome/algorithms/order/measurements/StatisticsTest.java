package de.metanome.algorithms.order.measurements;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StatisticsTest {

  @Test
  public void testIncreaseNumApplicationsUniquenessPruning() {
    final Statistics stats = new Statistics("test");
    int level = 3;
    stats.increaseNumApplicationsUniquenessPruning(level);
    stats.increaseNumApplicationsUniquenessPruning(level);
    level += 4;
    stats.increaseNumApplicationsUniquenessPruning(level);
    stats.increaseNumApplicationsUniquenessPruning(level);
    stats.increaseNumApplicationsUniquenessPruning(level);
    stats.increaseNumApplicationsUniquenessPruning(level);
    level++;
    stats.increaseNumApplicationsUniquenessPruning(level);
    stats.increaseNumApplicationsUniquenessPruning(level);
    level = 2;
    stats.increaseNumApplicationsUniquenessPruning(level);
    level++;
    stats.increaseNumApplicationsUniquenessPruning(level);
    assertTrue(stats.toString().contains(
        "numApplicationsUniquenessPruning=[0, 1, 3, 0, 0, 0, 4, 2]"));
  }

}
