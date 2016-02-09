/*
 * Copyright 2014 by the Metanome project Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package de.metanome.algorithms.many.test;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.results.Result;
import de.metanome.algorithms.many.driver.AnelosimusDriver;
import de.metanome.backend.result_receiver.ResultCache;
import de.metanome.backend.result_receiver.ResultReceiver;

public class MANYTest {

    // standard conf
    int inputRowLimit;
    String nEstStrategy;
    int p;
    int m;
    int k;
    int passes;
    String[] nullValues;
    int dop;
    int refCoverageMinPercentage;
    boolean verify;
    boolean output;
    boolean filterNonUniqueRefs;
    boolean filterNullCols;
    boolean filterNumericAndShortCols;
    boolean strategyRef2Deps;
    boolean filterDependentRefs;
    boolean isFastVector;
    boolean condenseMatrix;
    String tableName;

    {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");
    }
    Logger logger = LoggerFactory.getLogger(MANYTest.class);

    @Before
    public void setUp() throws Exception {

        inputRowLimit = -1;
        p = 0;
        m = 8;
        k = 1;
        passes = 1;
        nullValues = new String[] {};
        dop = 1;
        refCoverageMinPercentage = 0;
        verify = true;
        output = true;
        filterNonUniqueRefs = false;
        filterNullCols = false;
        filterNumericAndShortCols = false;
        strategyRef2Deps = false;
        filterDependentRefs = false;
        isFastVector = false;
        condenseMatrix = false;
        tableName = "table1";
    }

    @After
    public void tearDown() throws Exception {
    }

    public static List<String> getAcceptedColumns(RelationalInputGenerator[] relationalInputGenerators) throws InputGenerationException, AlgorithmConfigurationException {
    	List<String> acceptedColumns = new ArrayList<>();
        for (RelationalInputGenerator relationalInputGenerator: relationalInputGenerators) {
        	RelationalInput relationalInput = relationalInputGenerator.generateNewCopy();
        	String tableName = relationalInput.relationName();
        	for (String columnName : relationalInput.columnNames())
        		acceptedColumns.add(tableName + ResultReceiver.TABLE_COLUMN_SEPARATOR + columnName);
        }
        return acceptedColumns;
    }
    
    @Test
    public void testNoFilters() {
        // conf
        RelationalInputGenerator[] relationalInputGenerators = new RelationalInputGenerator[1];
        RelationalInputMock table1 = new RelationalInputMock(tableName, new String[] { "foo", "bar" },
                new String[][] { { "", "" }, { "", "" }, { "", "" } });
        relationalInputGenerators[0] = new RelationalInputGeneratorMock(table1);

        try {
            ResultCache resultReceiver = new ResultCache("test", getAcceptedColumns(relationalInputGenerators));
            AnelosimusDriver.run(resultReceiver, new String[] { "table1" }, inputRowLimit, nEstStrategy, p, m,
                    k, passes, nullValues, dop, refCoverageMinPercentage, verify, output, filterNonUniqueRefs,
                    filterNullCols,
                    filterNumericAndShortCols, filterDependentRefs, isFastVector, condenseMatrix,
                    strategyRef2Deps, relationalInputGenerators);

            assertEquals(2, resultReceiver.fetchNewResults().size());
        } catch (AlgorithmConfigurationException e) {
            logger.error("{}", e);
            e.printStackTrace();
        } catch (AlgorithmExecutionException e) {
            logger.error("{}", e);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            logger.error("{}", e);
            e.printStackTrace();
        }

    }

    @Test
    public void testFilterNullCols() {
        // conf
        RelationalInputGenerator[] relationalInputGenerators = new RelationalInputGenerator[1];
        RelationalInputMock table1 = new RelationalInputMock(tableName, new String[] { "foo", "bar", "1", "2" },
                new String[][] { { "", "", "A", "A" }, { "", "", "A", "A" }, { "", "", "A", "A" } });
        relationalInputGenerators[0] = new RelationalInputGeneratorMock(table1);

        filterNullCols = true;

        try {
            ResultCache resultReceiver = new ResultCache("test", getAcceptedColumns(relationalInputGenerators));
            AnelosimusDriver.run(resultReceiver, new String[] { tableName }, inputRowLimit, nEstStrategy, p, m,
                    k, passes, nullValues, dop, refCoverageMinPercentage, verify, output, filterNonUniqueRefs,
                    filterNullCols,
                    filterNumericAndShortCols, filterDependentRefs, isFastVector, condenseMatrix,
                    strategyRef2Deps, relationalInputGenerators);

            assertEquals(2, resultReceiver.fetchNewResults().size());
        } catch (AlgorithmConfigurationException e) {
            logger.error("{}", e);
            e.printStackTrace();
        } catch (AlgorithmExecutionException e) {
            logger.error("{}", e);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            logger.error("{}", e);
            e.printStackTrace();
        }
    }

    @Test
    public void testRefCoverageMinPercentage() {
        // conf
        RelationalInputGenerator[] relationalInputGenerators = new RelationalInputGenerator[1];
        RelationalInputMock table1 = new RelationalInputMock(tableName, new String[] { "foo", "bar", "1" },
                new String[][] { { "A", "A", "A" }, { "B", "A", "A" }, { "C", "A", "B" } });
        relationalInputGenerators[0] = new RelationalInputGeneratorMock(table1);

        refCoverageMinPercentage = 50;

        try {
            ResultCache resultReceiver = new ResultCache("test", getAcceptedColumns(relationalInputGenerators));
            AnelosimusDriver.run(resultReceiver, new String[] { tableName }, inputRowLimit, nEstStrategy, p, m,
                    k, passes, nullValues, dop, refCoverageMinPercentage, verify, output, filterNonUniqueRefs,
                    filterNullCols,
                    filterNumericAndShortCols, filterDependentRefs, isFastVector, condenseMatrix,
                    strategyRef2Deps, relationalInputGenerators);

            assertEquals(2, resultReceiver.fetchNewResults().size());
        } catch (AlgorithmConfigurationException e) {
            logger.error("{}", e);
            e.printStackTrace();
        } catch (AlgorithmExecutionException e) {
            logger.error("{}", e);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            logger.error("{}", e);
            e.printStackTrace();
        }
    }

    @Test
    public void testFilterNonUniqueRefs() {
        // conf
        RelationalInputGenerator[] relationalInputGenerators = new RelationalInputGenerator[1];
        RelationalInputMock table1 = new RelationalInputMock(tableName, new String[] { "foo", "bar", "A" },
                new String[][] { { "A", "A", "A" }, { "D", "A", "B" }, { "D", "A", "C" } });
        relationalInputGenerators[0] = new RelationalInputGeneratorMock(table1);

        filterNonUniqueRefs = true;

        try {
            ResultCache resultReceiver = new ResultCache("test", getAcceptedColumns(relationalInputGenerators));
            AnelosimusDriver.run(resultReceiver, new String[] { tableName }, inputRowLimit, nEstStrategy, p, m,
                    k, passes, nullValues, dop, refCoverageMinPercentage, verify, output, filterNonUniqueRefs,
                    filterNullCols,
                    filterNumericAndShortCols, filterDependentRefs, isFastVector, condenseMatrix,
                    strategyRef2Deps, relationalInputGenerators);

            assertEquals(1, resultReceiver.fetchNewResults().size());
        } catch (AlgorithmConfigurationException e) {
            logger.error("{}", e);
            e.printStackTrace();
        } catch (AlgorithmExecutionException e) {
            logger.error("{}", e);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            logger.error("{}", e);
            e.printStackTrace();
        }
    }

    @Test
    public void testFilterRefCandidates() {
        // conf
        RelationalInputGenerator[] relationalInputGenerators = new RelationalInputGenerator[1];
        RelationalInputMock table1 = new RelationalInputMock(tableName, new String[] { "foo", "bar", "A", "B" },
                new String[][] { { "1", "1", "AAAA", "AAAA" }, { "2", "1", "AAAB", "AAAB" },
                        { "3", "1", "AAAC", "AAAC" }, { "4", "1", "AAAA", "AAAD" } });
        relationalInputGenerators[0] = new RelationalInputGeneratorMock(table1);

        filterNumericAndShortCols = true;

        try {
            ResultCache resultReceiver = new ResultCache("test", getAcceptedColumns(relationalInputGenerators));
            AnelosimusDriver.run(resultReceiver, new String[] { tableName }, inputRowLimit, nEstStrategy, p, m,
                    k, passes, nullValues, dop, refCoverageMinPercentage, verify, output, filterNonUniqueRefs,
                    filterNullCols,
                    filterNumericAndShortCols, filterDependentRefs, isFastVector, condenseMatrix,
                    strategyRef2Deps, relationalInputGenerators);

            assertEquals(1, resultReceiver.fetchNewResults().size());
        } catch (AlgorithmConfigurationException e) {
            logger.error("{}", e);
            e.printStackTrace();
        } catch (AlgorithmExecutionException e) {
            logger.error("{}", e);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            logger.error("{}", e);
            e.printStackTrace();
        }
    }

    @Test
    public void testFilterRefCandidatesWithCoverageFilter() {
        // conf
        RelationalInputGenerator[] relationalInputGenerators = new RelationalInputGenerator[1];
        RelationalInputMock table1 = new RelationalInputMock(tableName, new String[] { "foo", "bar", "A" },
                new String[][] { { "1", "1", "1" }, { "2", "1", "2" },
                        { "3", "1", "1" }, { "4", "1", "2" }, { "5", "1", "2" } });
        relationalInputGenerators[0] = new RelationalInputGeneratorMock(table1);

        filterDependentRefs = true;
        strategyRef2Deps = true;
        refCoverageMinPercentage = 50;

        try {
            ResultCache resultReceiver = new ResultCache("test", getAcceptedColumns(relationalInputGenerators));
            AnelosimusDriver.run(resultReceiver, new String[] { tableName }, inputRowLimit, nEstStrategy, p, m,
                    k, passes, nullValues, dop, refCoverageMinPercentage, verify, output, filterNonUniqueRefs,
                    filterNullCols,
                    filterNumericAndShortCols, filterDependentRefs, isFastVector, condenseMatrix,
                    strategyRef2Deps, relationalInputGenerators);

            assertEquals(1, resultReceiver.fetchNewResults().size());
        } catch (AlgorithmConfigurationException e) {
            logger.error("{}", e);
            e.printStackTrace();
        } catch (AlgorithmExecutionException e) {
            logger.error("{}", e);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            logger.error("{}", e);
            e.printStackTrace();
        }
    }

    @Test
    public void testStrategyRefToDeps() {
        // conf
        RelationalInputGenerator[] relationalInputGenerators = new RelationalInputGenerator[1];
        RelationalInputMock table1 = new RelationalInputMock(tableName, new String[] { "foo", "bar", "boo" },
                new String[][] { { "1", "1", "1" }, { "2", "1", "2" }, { "3", "1", "1" } });
        relationalInputGenerators[0] = new RelationalInputGeneratorMock(table1);

        strategyRef2Deps = true;

        try {
            ResultCache resultReceiver = new ResultCache("test", getAcceptedColumns(relationalInputGenerators));
            AnelosimusDriver.run(resultReceiver, new String[] { tableName }, inputRowLimit, nEstStrategy, p, m,
                    k, passes, nullValues, dop, refCoverageMinPercentage, verify, output, filterNonUniqueRefs,
                    filterNullCols,
                    filterNumericAndShortCols, filterDependentRefs, isFastVector, condenseMatrix,
                    strategyRef2Deps, relationalInputGenerators);

            assertEquals(3, resultReceiver.fetchNewResults().size());
        } catch (AlgorithmConfigurationException e) {
            logger.error("{}", e);
            e.printStackTrace();
        } catch (AlgorithmExecutionException e) {
            logger.error("{}", e);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            logger.error("{}", e);
            e.printStackTrace();
        }
    }

    @Test
    public void testFilterDependentRefs() {
        // conf
        RelationalInputGenerator[] relationalInputGenerators = new RelationalInputGenerator[1];
        RelationalInputMock table1 = new RelationalInputMock(tableName, new String[] { "foo1", "bar", "boo", "foo2" },
                new String[][] { { "1", "1", "1", "1" }, { "2", "1", "1", "3" }, { "3", "1", "2", "2" } });
        relationalInputGenerators[0] = new RelationalInputGeneratorMock(table1);

        strategyRef2Deps = true;
        filterDependentRefs = true;

        try {
            ResultCache resultReceiver = new ResultCache("test", getAcceptedColumns(relationalInputGenerators));
            AnelosimusDriver.run(resultReceiver, new String[] { tableName }, inputRowLimit, nEstStrategy, p, m,
                    k, passes, nullValues, dop, refCoverageMinPercentage, verify, output, filterNonUniqueRefs,
                    filterNullCols,
                    filterNumericAndShortCols, filterDependentRefs, isFastVector, condenseMatrix,
                    strategyRef2Deps, relationalInputGenerators);

            // assertEquals(3, resultReceiver.getNewResults().size());
            for (Result res : resultReceiver.fetchNewResults()) {
                logger.info(res.toString());
            }
        } catch (AlgorithmConfigurationException e) {
            logger.error("{}", e);
            e.printStackTrace();
        } catch (AlgorithmExecutionException e) {
            logger.error("{}", e);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            logger.error("{}", e);
            e.printStackTrace();
        }
    }

}
