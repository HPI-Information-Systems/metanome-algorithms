package de.metanome.algorithms.order;

import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.results.OrderDependency.ComparisonOperator;
import de.metanome.algorithm_integration.results.OrderDependency.OrderType;
import de.metanome.algorithms.order.check.DependencyChecker;
import de.metanome.algorithms.order.sorting.partitions.SortedPartition;
import de.metanome.algorithms.order.types.ByteArray;
import de.metanome.algorithms.order.types.ByteArrayPermutations;

/**
 *
 * Finds all minimal, completely non-trivial order dependencies.
 *
 * Candidate permutations are represented as a byte array with one byte per column index, i.e., the
 * maximum number of columns is 256.
 *
 * @author Philipp Langer
 *
 */
public class ORDERLhsRhs extends ORDER {

  Logger logger = LoggerFactory.getLogger(ORDERLhsRhs.class);

  // Data structures for ORDER
  List<byte[]> singleColumnPermutations;
  List<byte[]> previousLevelCandidates;
  List<byte[]> levelCandidates;

  Map<byte[], Set<byte[]>> previousRhsCandidateSet;
  Map<byte[], Set<byte[]>> currentRhsCandidateSet;
  Map<byte[], Set<byte[]>> validDependencies;

  LoadingCache<ByteArray, SortedPartition> partitionsCache;

  private static final int MAX_NUM_COLS = 256;

  @Override
  public void execute() throws AlgorithmExecutionException {
    super.execute();

    this.partitionsCache =
        CacheBuilder.newBuilder().maximumSize(this.partitionCacheSize)
            .build(new CacheLoader<ByteArray, SortedPartition>() {
              @Override
              public SortedPartition load(final ByteArray key) throws Exception {
                return ORDERLhsRhs.this.createPartitionFromSingletons(key.data);
              }
            });

    this.logger.info("Running order dependency detection algorithm (lhs and rhs) on table: "
        + this.tableName + " (" + this.numRows + " rows, " + this.columnIndices.length
        + " columns)");

    if (this.columnNames.size() > MAX_NUM_COLS) {
      throw new AlgorithmConfigurationException("You provided a table with "
          + this.columnNames.size()
          + " columns. This order dependency detection algorithm supports a maximum of "
          + MAX_NUM_COLS + " columns.");
    }

    this.levelCandidates = new ArrayList<>();
    this.currentRhsCandidateSet = new Object2ObjectOpenCustomHashMap<>(ByteArrays.HASH_STRATEGY);
    this.singleColumnPermutations = new ArrayList<>();
    this.validDependencies = new Object2ObjectOpenCustomHashMap<>(ByteArrays.HASH_STRATEGY);

    // initialize level 1
    for (final int lhsColumnIndex : this.columnIndices) {
      final byte[] singleLhsColumnPermutation = {(byte) lhsColumnIndex};

      // columns with only equal values mean that any OD with that lhs holds (under "<")
      // (but there exists no minimal n-ary OD that has lhs on its lhs or rhs,
      // i.e., they can be removed)
      if (this.permutationToPartition.get(singleLhsColumnPermutation).size() == 1) {
        for (final int rhsColumnIndex : this.columnIndices) {
          final byte[] rhs = new byte[] {(byte) rhsColumnIndex};
          if (Arrays.equals(singleLhsColumnPermutation, rhs)) {
            continue;
          }
          this.statistics.setNumFoundDependencies(this.statistics.getNumFoundDependencies() + 1);
          this.statistics.setNumFoundDependenciesInPreCheck(this.statistics
              .getNumFoundDependenciesInPreCheck() + 1);
          this.signalFoundOrderDependency(singleLhsColumnPermutation, rhs,
              ComparisonOperator.STRICTLY_SMALLER, OrderType.LEXICOGRAPHICAL);
        }

        continue;
      }

      this.singleColumnPermutations.add(singleLhsColumnPermutation);

      this.levelCandidates.add(singleLhsColumnPermutation);
      this.currentRhsCandidateSet.put(singleLhsColumnPermutation, new ObjectOpenCustomHashSet<>(
          ByteArrays.HASH_STRATEGY));
    }

    for (final byte[] levelCandidate : this.levelCandidates) {

      for (final byte[] singleColumnRhsCandidate : this.singleColumnPermutations) {
        if (!ByteArrayPermutations.disjoint(levelCandidate, singleColumnRhsCandidate)) {
          continue;
        }
        this.currentRhsCandidateSet.get(levelCandidate).add(singleColumnRhsCandidate);
      }
    }

    this.level = 1;
    while (!this.levelCandidates.isEmpty()) {
      this.computeDependencies();
      this.prune();
      this.logger.debug("Candidate set: {}", this.prettyPrintCurrentRhsCandidates());
      this.generateNextLevel();
      this.logger.info("Statistics after generating level " + (this.level + 1) + " for table #"
          + this.tableName + "#: " + this.statistics.toString());
      this.level++;
    }

    this.logger.info("Statistics (FINAL) for table #" + this.tableName + "#: "
        + this.statistics.toString());

  }

  private String prettyPrintCurrentRhsCandidates() {
    // prints a Map<byte[], Set<byte[]>>
    final StringBuilder sb = new StringBuilder();
    for (final Entry<byte[], Set<byte[]>> lhsToRhsCandidates : this.currentRhsCandidateSet
        .entrySet()) {
      sb.append(ByteArrayPermutations.permutationToIntegerString(lhsToRhsCandidates.getKey()));
      sb.append(" :- {");
      sb.append(ByteArrayPermutations.permutationListToIntegerString(lhsToRhsCandidates.getValue()));
      sb.append("}");
      sb.append(", ");
    }
    return sb.toString();
  }

  private void prune() {

    long time = System.currentTimeMillis();

    if (this.level < 2) {
      return;
    }

    final List<byte[]> prunedLevelCandidates = new ArrayList<>();
    for (final byte[] candidate : this.levelCandidates) {
      final byte[][] candidatePrefixes = ByteArrayPermutations.prefixes(candidate);
      if (this.allCandidateSetsEmpty(candidatePrefixes)) {
        this.logger.debug("Level {}: Pruned {}.", this.level,
            ByteArrayPermutations.permutationToIntegerString(candidate));
        prunedLevelCandidates.add(candidate);
      }
      for (final byte[] candidatePrefix : candidatePrefixes) {
        if (this.currentRhsCandidateSet.get(candidatePrefix) != null
            && this.currentRhsCandidateSet.get(candidatePrefix).isEmpty()) {
          this.currentRhsCandidateSet.remove(candidatePrefix);
        }

      }
    }
    this.levelCandidates.removeAll(prunedLevelCandidates);

    time = System.currentTimeMillis() - time;
    this.statistics.setPruneTime(this.statistics.getPruneTime() + time);

  }

  private boolean allCandidateSetsEmpty(final byte[][] prefixes) {
    for (final byte[] prefix : prefixes) {
      if (this.currentRhsCandidateSet.get(prefix) != null
          && !this.currentRhsCandidateSet.get(prefix).isEmpty()) {
        return false;
      }
    }
    return true;
  }

  private void computeDependencies() throws AlgorithmExecutionException {

    long time = System.currentTimeMillis();

    if (this.level < 2) {
      return;
    }

    this.updateCandidateSets();

    for (final byte[] levelCandidate : this.levelCandidates) {
      this.checkDependencies(levelCandidate);
    }


    if (this.level < 3) {
      return;
    }

    for (final Entry<byte[], Set<byte[]>> lhsToRhs : this.previousRhsCandidateSet.entrySet()) {
      final byte[] lhs = lhsToRhs.getKey();
      for (final byte[] rhs : lhsToRhs.getValue()) {
        final Set<byte[]> validRhsToLhs = this.validDependencies.get(lhs);
        if (validRhsToLhs != null && validRhsToLhs.contains(rhs)) {
          continue;
        }

        // lhs ~/~> rhs of previous level invalidated by merge

        boolean lhsRhsMergePrunable = true;

        final Set<byte[]> currentRhsForLhs = this.currentRhsCandidateSet.get(lhs);
        if (currentRhsForLhs != null) {
          for (final byte[] currentRhs : currentRhsForLhs) {
            if (this.startsWith(currentRhs, rhs)) {
              lhsRhsMergePrunable = false;
              break;
            }
          }
        }

        // previous 0->1
        // current 0->12

        // if 0->1 invalidated by merge, all 0X-/->1
        // in general, we need to keep only-merge-invalidated ods around, b/c they may generate an
        // OD

        if (lhsRhsMergePrunable) {
          // remove rhs from all C(X) with PREFIX(X) = lhs
          for (final byte[] potentialLhsSuffix : this.currentRhsCandidateSet.keySet()) {
            if (Arrays.equals(ByteArrayPermutations.prefix(potentialLhsSuffix), lhs)) {
              // potentialLhsSuffix is a suffix of lhs
              if (ByteArrayPermutations.disjoint(potentialLhsSuffix, rhs)) {
                // if potentialLhsSuffix and rhs are not disjoint, C(potentialLhsSuffix) did not
                // contain rhs in the first place ...
                this.currentRhsCandidateSet.get(potentialLhsSuffix).remove(rhs);
                this.logger.debug(
                    "Merge pruning: Removed {} from C({}), because {} ~/~> {} was invalidated by merge and "
                        + "{} ~/~> {} are all pruned (invalidated by swap or minimality).",
                    ByteArrayPermutations.permutationToIntegerString(rhs),
                    ByteArrayPermutations.permutationToIntegerString(potentialLhsSuffix),
                    ByteArrayPermutations.permutationToIntegerString(lhs),
                    ByteArrayPermutations.permutationToIntegerString(rhs),
                    ByteArrayPermutations.permutationToIntegerString(lhs),
                    ByteArrayPermutations.permutationToIntegerString(rhs) + "A");

                this.statistics.increaseNumApplicationsMergePruning(this.level);

              }
            }
          }
        }


      }
    }

    time = System.currentTimeMillis() - time;
    this.statistics.setComputeDependenciesTime(this.statistics.getComputeDependenciesTime() + time);
  }

  private void updateCandidateSets() {
    if (this.level < 3) {
      return;
    }

    // level l: extend all candidate sets for lhs of size 1..(l-2), and
    // set the candidate sets for lhs of size (l-1) to its prefix

    this.previousRhsCandidateSet = this.currentRhsCandidateSet;
    this.currentRhsCandidateSet = new Object2ObjectOpenCustomHashMap<>(ByteArrays.HASH_STRATEGY);

    System.gc();

    for (final byte[] lhs : this.previousRhsCandidateSet.keySet()) {
      // 0, 1, 2, 01, 02, 03, ...
      if (lhs.length != this.level - 1) {
        // extend
        // level 3: length 1 | level 4: 1,2 | level 5: 1,2,3

        final Set<byte[]> rhsCandidates = this.previousRhsCandidateSet.get(lhs);
        final Set<byte[]> extendedRhsCandidates =
            new ObjectOpenCustomHashSet<>(ByteArrays.HASH_STRATEGY);
        for (final byte[] rhs : rhsCandidates) {

          final Set<byte[]> validRhsForLhs = this.validDependencies.get(lhs);
          if (validRhsForLhs != null && validRhsForLhs.contains(rhs)) {
            // don't extend rhs for lhs, if lhs ~> rhs is valid,
            // because then, lhs ~> rhs[X] is also valid
            this.logger.debug("Not extending {} in C({}), because {} ~> {} is valid.",
                ByteArrayPermutations.permutationToIntegerString(rhs),
                ByteArrayPermutations.permutationToIntegerString(lhs),
                ByteArrayPermutations.permutationToIntegerString(lhs),
                ByteArrayPermutations.permutationToIntegerString(rhs));

            this.statistics.increaseNumApplicationsValidRhsPruning(this.level);

            continue;
          }

          for (final byte[] extendedRhsCandidate : this.extend(lhs, rhs)) {

            // e.g.: in the previous level, was 0 ~> 12 invalidated by swap? If so, don't
            // add 12 to C(03)

            // we add the extended rhs, if
            // extended \in C(prefix(lhs)) _OR_
            // one of lhs ~> prefixes(extended) was valid

            final byte[] lhsPrefix = ByteArrayPermutations.prefix(lhs);
            final Set<byte[]> rhsForLhsPrefix = this.previousRhsCandidateSet.get(lhsPrefix);
            if (lhsPrefix.length != 0 // can do no pruning based on empty prefix
                && ((rhsForLhsPrefix == null) || (!rhsForLhsPrefix.contains(extendedRhsCandidate)))) {

              if (!this.oneOfPrefixesOfRhsValid(lhsPrefix, extendedRhsCandidate)) {
                this.logger
                    .debug(
                        "Did not extend {} in C({}) with {}, because {} ~/~>s {} or non-minimality of rhs",
                        ByteArrayPermutations.permutationToIntegerString(rhs),
                        ByteArrayPermutations.permutationToIntegerString(lhs),
                        ByteArrayPermutations.permutationToIntegerString(extendedRhsCandidate),
                        ByteArrayPermutations.permutationToIntegerString(lhsPrefix),
                        ByteArrayPermutations.permutationToIntegerString(rhs));

                continue;
              }

            }

            // don't extend with non-minimal rhs

            if (!this.minimal(extendedRhsCandidate)) {
              this.statistics.increaseNumApplicationsMinimalityRhsPruning(this.level);
              continue;
            }

            extendedRhsCandidates.add(extendedRhsCandidate);

          }

        }
        this.logger.debug("Level {}: Extending rhs candidates for lhs {} to {}", this.level, lhs,
            ByteArrayPermutations.permutationListToIntegerString(extendedRhsCandidates));
        this.currentRhsCandidateSet.put(lhs, extendedRhsCandidates);

      } else {
        // create new

        // is the new lhs not minimal?
        // => C(lhs) = \emptyset
        if (!this.minimal(lhs)) {
          this.logger.debug("{} is not minimal. Setting C({}) := empty set.",
              ByteArrayPermutations.permutationToIntegerString(lhs),
              ByteArrayPermutations.permutationToIntegerString(lhs));

          this.statistics.increaseNumApplicationsMinimalityLhsPruning(this.level);

          continue;
        }

        // set the new candidate set for lhs as the previous level's candidate set of its prefix
        // ex.: lhs = [0, 1] C(lhs) = C([0, 1]) := C_p(prefix([0, 1])) = C_p([0])
        // (where C_p is candidate set of the previous level)
        final byte[] lhsPrefix = ByteArrayPermutations.prefix(lhs);

        final Set<byte[]> rhsForLhsPrefixSet = this.previousRhsCandidateSet.get(lhsPrefix);
        if (rhsForLhsPrefixSet == null) {
          // if C(lhsPrefix) == null, it has been pruned before, so C(lhsPrefix[X]) is empty as
          // well.
          continue;
        }

        for (final byte[] rhsForLhsPrefix : rhsForLhsPrefixSet) {
          if (this.currentRhsCandidateSet.get(lhs) == null) {
            this.currentRhsCandidateSet.put(lhs, new ObjectOpenCustomHashSet<byte[]>(
                ByteArrays.HASH_STRATEGY));
          }

          if (ByteArrayPermutations.disjoint(rhsForLhsPrefix, lhs)) {
            this.currentRhsCandidateSet.get(lhs).add(rhsForLhsPrefix);
          }
        }

        this.logger.debug("Level {}: Created the new rhs candidate set C({}) = {}", this.level,
            ByteArrayPermutations.permutationToIntegerString(lhs), ByteArrayPermutations
                .permutationListToIntegerString(this.currentRhsCandidateSet.get(lhs)));
      }
    }



  }

  private boolean oneOfPrefixesOfRhsValid(final byte[] lhsPrefix, final byte[] extendedRhsCandidate) {
    final Set<byte[]> validRhsToLhsPrefix = this.validDependencies.get(lhsPrefix);
    if (validRhsToLhsPrefix == null) {
      return false;
    }

    final byte[][] extendedRhsPrefixes = ByteArrayPermutations.prefixes(extendedRhsCandidate);
    for (final byte[] extendedRhsPrefix : extendedRhsPrefixes) {
      if (validRhsToLhsPrefix.contains(extendedRhsPrefix)) {
        this.logger.debug(
            "Cannot prune suffix of {} from C({}), because one of {} ~> prefixes({}) is valid.",
            ByteArrayPermutations.permutationToIntegerString(lhsPrefix),
            ByteArrayPermutations.permutationToIntegerString(lhsPrefix),
            ByteArrayPermutations.permutationToIntegerString(lhsPrefix),
            ByteArrayPermutations.permutationToIntegerString(extendedRhsCandidate));
        return true;
      }
    }

    return false;
  }

  private boolean minimal(final byte[] permutation) {

    for (final Entry<byte[], Set<byte[]>> validLhsRhs : this.validDependencies.entrySet()) {
      final byte[] validLhs = validLhsRhs.getKey();
      for (final byte[] validRhs : validLhsRhs.getValue()) {
        final int index = ByteArrayPermutations.findOccurrenceOf(validRhs, permutation, 0);
        if (index == -1) {
          continue;
        }
        // lhs contains validRhs. Does lhs contain the corresponding validLhs after index?
        // this is minimality rule 1
        if (ByteArrayPermutations.findOccurrenceOf(validLhs, permutation, index) != -1) {
          // lhs is not minimal
          this.logger.debug("Attribute list {} is not minimal (1st rule). Matched {} ~> {}",
              ByteArrayPermutations.permutationToIntegerString(permutation),
              ByteArrayPermutations.permutationToIntegerString(validLhs),
              ByteArrayPermutations.permutationToIntegerString(validRhs));
          return false;
        }

        // does lhs contain validLhs before the index?
        // this is minimality rule 2

        // calculate the first index before validRhs. findOccurrenceOf returns the index directly
        // after validRhs. Therefore, substract 1 from index. Then, substract |validRhs| to obtain
        // the first index before validRhs in lhs
        final int beforeIndex = index - 1 - validRhs.length;
        if (beforeIndex < 0) {
          // there can be no validLhs before validRhs
          continue;
        }

        // can validLhs be fully matched to the left of validRhs in permutation?
        if (validLhs.length > (beforeIndex + 1)) {
          continue;
        }

        // try to match validLhs in permutation before validRhs
        boolean matchedValidLhs = true;
        int j = beforeIndex;
        for (int i = validLhs.length - 1; i >= 0; i--) {
          if (validLhs[i] == permutation[j]) {
            j--;
          } else {
            matchedValidLhs = false;
            break;
          }
        }

        if (matchedValidLhs) {
          this.logger.debug("Attribute list {} is not minimal (2nd rule). Matched {} ~> {}",
              ByteArrayPermutations.permutationToIntegerString(permutation),
              ByteArrayPermutations.permutationToIntegerString(validLhs),
              ByteArrayPermutations.permutationToIntegerString(validRhs));
          return false;
        }

      }
    }

    return true;
  }

  private Set<byte[]> extend(final byte[] lhs, final byte[] rhs) {
    final Set<byte[]> extendedRhsCandidates =
        new ObjectOpenCustomHashSet<>(ByteArrays.HASH_STRATEGY);
    if (lhs.length + rhs.length > this.level - 1) {
      // extended candidates would have more columns than this level allows,
      // return empty extended candidates
      return extendedRhsCandidates;
    }
    for (final byte[] singleColumn : this.singleColumnPermutations) {
      if (ByteArrayPermutations.disjoint(singleColumn, lhs)
          && ByteArrayPermutations.disjoint(singleColumn, rhs)) {
        extendedRhsCandidates.add(ByteArrayPermutations.join(rhs, singleColumn));
      }
    }
    return extendedRhsCandidates;
  }

  private void checkDependencies(final byte[] levelCandidate) throws AlgorithmExecutionException {
    for (int i = 0; i < levelCandidate.length - 1; i++) {
      final byte[] lhs = new byte[i + 1];
      final byte[] rhs = new byte[levelCandidate.length - (i + 1)];

      for (int l = 0; l < i + 1; l++) {
        lhs[l] = levelCandidate[l];
      }

      int r = 0;
      for (int k = i + 1; k < levelCandidate.length; k++) {
        rhs[r] = levelCandidate[k];
        r++;
      }

      if (this.currentRhsCandidateSet.get(lhs) == null
          || (this.currentRhsCandidateSet.get(lhs) != null && !this.currentRhsCandidateSet.get(lhs)
              .contains(rhs))) {
        // do not need to check lhs ~> rhs
        this.logger.debug("Skipping check for {} ~> {}",
            ByteArrayPermutations.permutationToIntegerString(lhs),
            ByteArrayPermutations.permutationToIntegerString(rhs));
        continue;
      }

      final Set<byte[]> validRhsToLhs = this.validDependencies.get(lhs);
      if (validRhsToLhs != null) {
        final byte[][] rhsPrefixes = ByteArrayPermutations.prefixes(rhs);
        boolean prefixValid = false;
        for (final byte[] rhsPrefix : rhsPrefixes) {
          if (validRhsToLhs.contains(rhsPrefix)) {
            // if 0 ~> 1, don't check 0 ~> 1X
            // TODO this is not needed anymore, remove it
            this.logger.debug("Not checking {} ~> {}, because {} ~> {} is valid.",
                ByteArrayPermutations.permutationToIntegerString(lhs),
                ByteArrayPermutations.permutationToIntegerString(rhs),
                ByteArrayPermutations.permutationToIntegerString(lhs), rhsPrefix);
            prefixValid = true;
            break;
          }
        }
        if (prefixValid) {
          continue;
        }
      }

      SortedPartition lhsPartition = null;
      SortedPartition rhsPartition = null;

      if (lhs.length == 1) {
        lhsPartition = this.permutationToPartition.get(lhs);
      } else if (lhs.length > 1) {
        lhsPartition = this.partitionsCache.getUnchecked(new ByteArray(lhs));
      }

      if (rhs.length == 1) {
        rhsPartition = this.permutationToPartition.get(rhs);
      } else if (rhs.length > 1) {
        rhsPartition = this.partitionsCache.getUnchecked(new ByteArray(rhs));
      }

      if (lhsPartition == null || rhsPartition == null) {
        // should never happen
        throw new AlgorithmExecutionException("Could not create/load sorted partitions "
            + "from cache. Lhs: " + ByteArrayPermutations.permutationToIntegerString(lhs)
            + " Rhs: " + ByteArrayPermutations.permutationToIntegerString(rhs));
      }

      this.statistics.setNumDependencyChecks(this.statistics.getNumDependencyChecks() + 1);

      final boolean[] result =
          DependencyChecker.checkOrderDependencyForSwapStrictlySmaller(lhsPartition, rhsPartition);
      final boolean valid = result[0];
      final boolean swap = result[1];
      if (valid) {
        // order dependency is valid
        this.statistics.setNumFoundDependencies(this.statistics.getNumFoundDependencies() + 1);
        this.signalFoundOrderDependency(rhs, lhs, ComparisonOperator.SMALLER_EQUAL,
            OrderType.LEXICOGRAPHICAL);

        if (this.validDependencies.get(lhs) == null) {
          this.validDependencies.put(lhs, new ObjectOpenCustomHashSet<byte[]>(
              ByteArrays.HASH_STRATEGY));
        }
        this.validDependencies.get(lhs).add(rhs);

        if (lhsPartition.isUnique()) {
          this.logger.debug(
              "{} is unique, and {} ~> {}. All {}[X] ~> {}[Y] are valid. Removing {} from C({})",
              ByteArrayPermutations.permutationToIntegerString(lhs),
              ByteArrayPermutations.permutationToIntegerString(lhs),
              ByteArrayPermutations.permutationToIntegerString(rhs),
              ByteArrayPermutations.permutationToIntegerString(lhs),
              ByteArrayPermutations.permutationToIntegerString(rhs),
              ByteArrayPermutations.permutationToIntegerString(rhs),
              ByteArrayPermutations.permutationToIntegerString(lhs));

          this.statistics.increaseNumApplicationsUniquenessPruning(this.level);

          this.currentRhsCandidateSet.get(lhs).remove(rhs);

        }

      } else if (swap) {
        // order dependency invalidated by swap, prune.
        this.logger.debug("{} ~> {} invalidated by swap.",
            ByteArrayPermutations.permutationToIntegerString(lhs),
            ByteArrayPermutations.permutationToIntegerString(rhs));

        this.statistics.increaseNumApplicationsSwapPruning(this.level);

        this.currentRhsCandidateSet.get(lhs).remove(rhs);
      } else if (!swap) {
        // order dependency invalidated by merge
        this.logger.debug("{} ~> {} invalidated by merge.",
            ByteArrayPermutations.permutationToIntegerString(lhs),
            ByteArrayPermutations.permutationToIntegerString(rhs));


      } else {
        // can't happen
        throw new IllegalStateException("Can't proceed with dependency result "
            + Arrays.toString(result));
      }
    }

  }

  /**
   *
   * are rhsCandidate's first rhs.length entries equal to rhs, i.e., does rhsCandidate start with
   * rhs?
   *
   * @param rhsCandidate
   * @param rhs
   * @return
   */
  private boolean startsWith(final byte[] rhsCandidate, final byte[] rhs) {
    for (int i = 0; i < rhs.length; i++) {
      if (rhs[i] != rhsCandidate[i]) {
        return false;
      }
    }
    return true;
  }

  private SortedPartition createPartitionFromSingletons(final byte[] permutation) {

    if (permutation.length < 2) {
      // we have all singleton partitions from the initialization step
      return this.permutationToPartition.get(permutation);
    }

    this.logger.debug("Could not find the partition for {}. Creating it from singletons.",
        ByteArrayPermutations.permutationToIntegerString(permutation));

    this.statistics.increaseNumPartitionCombinationsBySize(permutation.length);

    // incrementally multiply the singleton partitions to get the full partition
    SortedPartition resultPartition = this.permutationToPartition.get(new byte[] {permutation[0]});
    for (int i = 1; i < permutation.length; i++) {
      final SortedPartition nextPartition =
          this.permutationToPartition.get(new byte[] {permutation[i]});
      resultPartition = resultPartition.multiply(nextPartition);
    }
    return resultPartition;
  }

  private void generateNextLevel() {

    long time = System.currentTimeMillis();

    this.previousLevelCandidates = this.levelCandidates;
    this.levelCandidates = new ArrayList<>();

    this.buildPrefixBlocks();

    System.gc();

    this.logger.debug("PREFIX_BLOCKS in level {}: {} ", this.level, this.prettyPrintPrefixBlocks());

    for (final List<byte[]> candidatesWithSamePrefix : this.prefixBlocks.values()) {
      if (candidatesWithSamePrefix.size() < 2) {
        break;
      }
      for (final byte[] candidate : candidatesWithSamePrefix) {
        for (final byte[] candidateForJoin : candidatesWithSamePrefix) {
          if (Arrays.equals(candidate, candidateForJoin)) {
            continue;
          }

          final byte[] joinedCandidate = ByteArrayPermutations.join(candidate, candidateForJoin);
          this.levelCandidates.add(joinedCandidate);
        }
      }
    }
    this.logger.debug("Generated level {}", this.level + 1);
    this.logger.debug("Level {} candidates: {}", (this.level + 1),
        ByteArrayPermutations.permutationListToIntegerString(this.levelCandidates));
    if (this.level > 1 && !this.levelCandidates.isEmpty()) {
      for (final byte[] newLhs : this.previousLevelCandidates) {
        this.logger
            .debug(
                "After generating level {}. Adding {} as an lhs to the candidate set for the next level.",
                this.level + 1, ByteArrayPermutations.permutationToIntegerString(newLhs));
        this.currentRhsCandidateSet.put(newLhs, new ObjectOpenCustomHashSet<byte[]>(
            ByteArrays.HASH_STRATEGY));
      }
    }

    time = System.currentTimeMillis() - time;
    this.statistics.setGenNextTime(this.statistics.getGenNextTime() + time);

  }

  private void buildPrefixBlocks() {
    this.prefixBlocks = new Object2ObjectOpenCustomHashMap<>(ByteArrays.HASH_STRATEGY);

    for (final byte[] permutation : this.previousLevelCandidates) {
      // prefix is the emtpy array for permutations of size 1 (level 1)
      final byte[] prefix = ByteArrayPermutations.prefix(permutation);

      if (this.prefixBlocks.get(prefix) == null) {
        this.prefixBlocks.put(prefix, new ArrayList<byte[]>());
      }
      this.prefixBlocks.get(prefix).add(permutation);
    }
  }
}
