package de.metanome.algorithms.ma2013n2.algorithm_helper.data_structures;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public abstract class GraphTraverser {

  protected int OVERFLOW_THRESHOLD = 10000;
  protected int PLI_SEARCH_THRESHOLD = 1000;
  protected Map<ColumnCombinationBitset, PositionListIndex> calculatedPlis;
  protected List<ColumnCombinationBitset> calculatedPliBitsetStack;

  protected ColumnCombinationBitset bitmaskForNonUniqueColumns;

  protected int numberOfColumns;
  protected PruningGraph negativeGraph;
  protected PruningGraph positiveGraph;
  protected Set<ColumnCombinationBitset> minimalPositives;
  protected Set<ColumnCombinationBitset> maximalNegatives;

  protected Deque<ColumnCombinationBitset> randomWalkTrace;
  protected List<ColumnCombinationBitset> seedCandidates;
  protected HoleFinder holeFinder;

  protected Random random = new Random();
  protected int found;

  public GraphTraverser() {
    this.calculatedPlis = new HashMap<>();
    this.calculatedPliBitsetStack = new LinkedList<>();

    this.randomWalkTrace = new LinkedList<>();

    this.minimalPositives = new HashSet<>();
    this.maximalNegatives = new HashSet<>();
  }

  // TODO: find overlappings in init() and buildInitialPlis() in FD and UCC GraphTraverser

  public int traverseGraph() throws CouldNotReceiveResultException {
    this.found = 0;
    //initial PLI
    ColumnCombinationBitset currentColumn = this.getSeed();

    while (null != currentColumn) {
//			System.out.println(String.format("Start from scratch with new column <%s>", currentColumn.toString()));
      this.randomWalk(currentColumn);
      currentColumn = this.getSeed();
    }

    return this.found;
  }

  protected void randomWalk(ColumnCombinationBitset currentColumnCombination)
      throws CouldNotReceiveResultException {
    ColumnCombinationBitset newColumn;
    while (null != currentColumnCombination) {
      // Check currentColumn

      //System.out.println(currentColumnCombination);
      if (this.isSubsetOfMaximalNegativeColumnCombination(currentColumnCombination)) {
        newColumn = null;
      } else if (this.isSupersetOfPositiveColumnCombination(currentColumnCombination)) {
        newColumn = null;
      }
      // FD/UCC? --> get a child
      else if (this.isPositiveColumnCombination(currentColumnCombination)) {
//				System.out.println(String.format("GO DOWN  <%s>  is unique  -> get child", currentColumnCombination.toString()));
        newColumn = getNextChildColumnCombination(currentColumnCombination);
        if (null == newColumn) {
          // No child available --> found minimal positive
          this.addMinimalPositive(currentColumnCombination);
        }
        this.positiveGraph.add(currentColumnCombination);
      }
      // Get next parent and check for maximal negative
      else {
//				System.out.println(String.format("GO UP    <%s>  is non-unique  -> get parent", currentColumnCombination.toString()));
        newColumn = getNextParentColumnCombination(currentColumnCombination);
        // No parent available --> found maximal negative
        if (null == newColumn) {
          this.maximalNegatives.add(currentColumnCombination);
          this.holeFinder.update(currentColumnCombination);
        }
        this.negativeGraph.add(currentColumnCombination);
      }

      // Go to next column
      if (null != newColumn) {
        this.randomWalkTrace.push(currentColumnCombination);
        currentColumnCombination = newColumn;
      } else {
        if (randomWalkTrace.isEmpty()) {
          return;

        }
        currentColumnCombination = this.randomWalkTrace.poll();
      }
    }
  }

  protected abstract boolean isPositiveColumnCombination(
      ColumnCombinationBitset currentColumnCombination);

  protected ColumnCombinationBitset getSeed() {
    ColumnCombinationBitset
        seedCandidate =
        this.findUnprunedSetAndUpdateGivenList(this.seedCandidates, true);
    if (seedCandidate == null) {
      this.seedCandidates = this.getHoles();
      seedCandidate = this.findUnprunedSetAndUpdateGivenList(this.seedCandidates, true);
    }

    return seedCandidate;
  }

  protected List<ColumnCombinationBitset> getHoles() {
    return this.holeFinder.getHolesWithoutGivenColumnCombinations(this.minimalPositives);
  }

  protected PositionListIndex getPLIFor(ColumnCombinationBitset columnCombination) {
    PositionListIndex pli = this.calculatedPlis.get(columnCombination);
    if (pli != null) {
      return this.calculatedPlis.get(columnCombination);
    }

    pli = createPliFromExistingPli(columnCombination);
    return pli;
  }

  protected PositionListIndex createPliFromExistingPli(ColumnCombinationBitset columnCombination) {
    int counter = 0;

    // initialise with worst case
    ColumnCombinationBitset
        currentBestSet =
        columnCombination.getContainedOneColumnCombinations().get(0);
    ColumnCombinationBitset currentBestMinusSet = columnCombination.minus(currentBestSet);

    Iterator<ColumnCombinationBitset> itr = this.calculatedPliBitsetStack.iterator();
    ColumnCombinationBitset currentSet;
    ColumnCombinationBitset currentMinusSet;
    PositionListIndex currentMinusPli;
    while (itr.hasNext() && counter < this.PLI_SEARCH_THRESHOLD) {
      currentSet = itr.next();

      if (currentSet.size() >= columnCombination.size()) {
        continue;
      }

      counter++;
      if (currentSet.isSubsetOf(columnCombination)) {
        currentMinusSet = columnCombination.minus(currentSet);
        currentMinusPli = this.calculatedPlis.get(currentMinusSet);
        if (currentMinusPli != null) {
          PositionListIndex
              intersect =
              this.calculatedPlis.get(currentSet).intersect(currentMinusPli);
          this.addPli(columnCombination, intersect);
          return intersect;
        }

        if (currentBestSet.size() < currentSet.size()) {
          currentBestSet = currentSet;
          currentBestMinusSet = currentMinusSet;
        }
      }
    }

    return this.extendPli(currentBestSet, currentBestMinusSet);
  }

  /**
   * columnCombination may not be null.
   */
  protected PositionListIndex extendPli(ColumnCombinationBitset columnCombination,
                                        ColumnCombinationBitset extendingColumns) {
    PositionListIndex currentPli = this.calculatedPlis.get(columnCombination);

    for (ColumnCombinationBitset currentOneColumnCombination : extendingColumns
        .getContainedOneColumnCombinations()) {
      currentPli = currentPli.intersect(this.calculatedPlis.get(currentOneColumnCombination));
      columnCombination = columnCombination.union(currentOneColumnCombination);
      this.addPli(columnCombination, currentPli);
    }

    return currentPli;
  }

  protected void addPli(ColumnCombinationBitset columnCombination, PositionListIndex pli) {
    this.calculatedPlis.put(columnCombination, pli);
    this.calculatedPliBitsetStack.add(0, columnCombination);
  }

  public Map<ColumnCombinationBitset, PositionListIndex> getCalculatedPlis() {
    return this.calculatedPlis;
  }

  protected ColumnCombinationBitset getNextParentColumnCombination(ColumnCombinationBitset column) {
    if (this.minimalPositives.contains(column)) {
      return null;
    }
    List<ColumnCombinationBitset>
        supersets =
        column.getDirectSupersets(this.bitmaskForNonUniqueColumns);
    return findUnprunedSet(supersets);
  }

  protected ColumnCombinationBitset getNextChildColumnCombination(ColumnCombinationBitset column) {
    if (column.size() == 1) {
      return null;
    }
    if (this.maximalNegatives.contains(column)) {
      return null;
    }
    List<ColumnCombinationBitset> subsets = column.getDirectSubsets();
    return findUnprunedSet(subsets);
  }

  protected ColumnCombinationBitset findUnprunedSet(List<ColumnCombinationBitset> sets) {
    return this.findUnprunedSetAndUpdateGivenList(sets, false);
  }

  protected ColumnCombinationBitset findUnprunedSetAndUpdateGivenList(
      List<ColumnCombinationBitset> sets, boolean setPrunedEntriesToNull) {
    // Randomize order for random walk
    //Collections.shuffle(sets);

    if (sets.isEmpty()) {
      return null;
    }

    int random = this.random.nextInt(sets.size());
    int i;
    int no;

    // TODO: use an iterator to be faster on the list
    for (i = 0; i < sets.size(); i++) {
      no = (i + random) % sets.size();
      ColumnCombinationBitset singleSet = sets.get(no);

      if (singleSet == null) {
        continue;
      }

      if (this.isAdditionalConditionTrueForFindUnprunedSetAndUpdateGivenList(singleSet)) {
        if (setPrunedEntriesToNull) {
          sets.set(no, null);
        }
        continue;
      }

      if (this.positiveGraph.find(singleSet)) {
        if (setPrunedEntriesToNull) {
          sets.set(no, null);
        }
        continue;
      }

      if (this.negativeGraph.find(singleSet)) {
        if (setPrunedEntriesToNull) {
          sets.set(no, null);
        }
        continue;
      }
      return singleSet;
    }
    return null;
  }

  protected boolean isSupersetOfPositiveColumnCombination(
      ColumnCombinationBitset currentColumnCombination) {
    for (ColumnCombinationBitset ccb : this.minimalPositives) {
      if (ccb.isSubsetOf(currentColumnCombination)) {
        return true;
      }
    }
    return false;
  }

  protected boolean isSubsetOfMaximalNegativeColumnCombination(
      ColumnCombinationBitset currentColumnCombination) {
    for (ColumnCombinationBitset ccb : this.maximalNegatives) {
      if (ccb.containsSubset(currentColumnCombination)) {
        return true;
      }
    }

    return false;
  }

  protected void addMinimalPositive(ColumnCombinationBitset positiveColumnCombination)
      throws CouldNotReceiveResultException {
    this.minimalPositives.add(positiveColumnCombination);
    this.found += 1;
  }

  public Collection<ColumnCombinationBitset> getMinimalPositiveColumnCombinations() {
    return minimalPositives;
  }

  abstract protected boolean isAdditionalConditionTrueForFindUnprunedSetAndUpdateGivenList(
      ColumnCombinationBitset singleSet);
}
