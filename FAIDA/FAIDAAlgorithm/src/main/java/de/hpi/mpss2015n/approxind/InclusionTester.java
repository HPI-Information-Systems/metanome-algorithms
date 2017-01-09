package de.hpi.mpss2015n.approxind;

import de.hpi.mpss2015n.approxind.utils.SimpleColumnCombination;

import java.util.List;

public interface InclusionTester {

    /**
     * (Step 1) Iteration starter method.
     * In each iteration, only some specific column combinations are considered (all with the same dimension).
     * This method receives a list of these column combinations in order to (re)initialize or (re)structure the necessary data structures as needed.
     *
     * @param combinations List of relevant column combinations that are to be analyzed in this iteration.
     * @return List of tables that need to be read, so that the environment doesn't send unneeded data (i.e. read files unnecessarily).
     */
    int[] setColumnCombinations(List<SimpleColumnCombination> combinations);

    /**
     * step 2a
     *
     * @param table ID of the table where the data is from.
     */
    void startInsertRow(int table);

    /**
     * (Step 2b) Data reader method.
     * Receives the values from one row of one table in order to fill the local data structures.
     *
     * @param values   List of values (as long Hashes)
     * @param rowCount Index of the row. For statistics only.
     */
    void insertRow(long[] values, int rowCount);

    /**
     * (Step 3) Notify InclusionTester that data insertion is finished and querying begins.
     * Final data postprocessing might happen here.
     */
    default void finalizeInsertion() {
        // pass
    }

    /**
     * If data structures are depending on global properties (size, volume, ...) of the tables, these properties can be extracted here.
     * This method should be called directly after InclusionTester construction, before any other operation.
     *
     * @param samples for each table: a list of hashed sample tuples
     */
    default void initialize(List<List<long[]>> samples) {
        // pass
    }

    /**
     * (Step 4) Checks if one  column is included in another one (i.e. one domain is a subset of the other).
     *
     * @param a The left-sided column combination (subset).
     * @param b Right-hand column combination (superset).
     * @return The boolean result (might be approximate).
     */
    boolean isIncludedIn(SimpleColumnCombination a, SimpleColumnCombination b);

    /**
     * Retrieve the number of checks that could be answered exactly (apart from the fact that we operate on hashes).
     *
     * @return the number of certain checks.
     */
    int getNumCertainChecks();

    /**
     * Retrieve the number of checks that could not be answered exactly (apart from the fact that we operate on hashes).
     *
     * @return the number of uncertain checks.
     */
    int getNumUnertainChecks();

}
