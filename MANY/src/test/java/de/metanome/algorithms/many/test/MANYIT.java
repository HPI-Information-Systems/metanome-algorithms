/*
 * Copyright 2014 by the Metanome project Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package de.metanome.algorithms.many.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithms.many.driver.AnelosimusDriver;
import de.metanome.backend.result_receiver.ResultCache;
import de.metanome.backend.result_receiver.ResultReceiver;

public class MANYIT {

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

    {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "info");
    }
    Logger logger = LoggerFactory.getLogger(MANYIT.class);

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

    }

    @After
    public void tearDown() throws Exception {
    }

    public static List<ColumnIdentifier> getAcceptedColumns(RelationalInputGenerator[] relationalInputGenerators) throws InputGenerationException, AlgorithmConfigurationException {
    	List<ColumnIdentifier> acceptedColumns = new ArrayList<>();
        for (RelationalInputGenerator relationalInputGenerator: relationalInputGenerators) {
        	RelationalInput relationalInput = relationalInputGenerator.generateNewCopy();
        	String tableName = relationalInput.relationName();
        	for (String columnName : relationalInput.columnNames())
        		acceptedColumns.add(new ColumnIdentifier(tableName, columnName));
        }
        return acceptedColumns;
    }
    
    @Test
    @Ignore
    public void test() {
        // conf
        String databaseName = "WIKITABLES";
        String inputFileEnding = ".csv";
        int limit = 20000;
        String inputFolderPath = "/home/fabian/MasterThesis/Development/data/wikitables" + File.separator;
        boolean hasHeader = true;
        char seperator = ',';
        char quote = '"';
        char escape = '\\';

        String[] tableNames = AnelosimusDriver.listFiles(inputFolderPath + databaseName + File.separator,
                inputFileEnding, limit);
        
        m = 650;
        k = 42;

        // nEstStrategy = ANELOSIMUS.N_EST_STRATEGIES.AVERAGE_HALF_DOUBLED.name();
        // p = 1000;

        passes = 2;
        nullValues = new String[] { "—", "-", "–", "N/A", "?", " ", "--" };
        dop = 4;
        refCoverageMinPercentage = 20;
        verify = true;
        filterNonUniqueRefs = true;
        filterNullCols = true;
        filterNumericAndShortCols = true;
        strategyRef2Deps = true;
        filterDependentRefs = true;
        isFastVector = false;
        condenseMatrix = true;
        output = false;

        try {
            RelationalInputGenerator[] relationalInputGenerators = AnelosimusDriver.getRelationalInputs(databaseName, inputFileEnding, inputFolderPath, hasHeader,
                    seperator, quote, escape, tableNames);
        	ResultCache resultReceiver = new ResultCache("test", getAcceptedColumns(relationalInputGenerators));
            AnelosimusDriver.run(resultReceiver, tableNames, inputRowLimit, nEstStrategy, p, m,
                    k, passes, nullValues, dop, refCoverageMinPercentage, verify, output, filterNonUniqueRefs,
                    filterNullCols,
                    filterNumericAndShortCols, filterDependentRefs, isFastVector, condenseMatrix,
                    strategyRef2Deps,
                    relationalInputGenerators);
        } catch (AlgorithmConfigurationException e) {
            e.printStackTrace();
        } catch (AlgorithmExecutionException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Ignore
    public void testFast() {
        // conf
        String databaseName = "WIKITABLES";
        String inputFileEnding = ".csv";
        int limit = 10000;
        String inputFolderPath = "/home/fabian/MasterThesis/Development/data/wikitables" + File.separator;
        boolean hasHeader = true;
        char seperator = ',';
        char quote = '"';
        char escape = '\\';

        String[] tableNames = AnelosimusDriver.listFiles(inputFolderPath + databaseName + File.separator,
                inputFileEnding, limit);

        m = 650;
        k = 6;

        // nEstStrategy = ANELOSIMUS.N_EST_STRATEGIES.AVERAGE_HALF_DOUBLED.name();
        // p = 1000;

        passes = 2;
        nullValues = new String[] { "—", "-", "–", "N/A", "?", " ", "--" };
        dop = 4;
        refCoverageMinPercentage = 20;
        verify = true;
        filterNonUniqueRefs = true;
        filterNullCols = true;
        filterNumericAndShortCols = true;
        strategyRef2Deps = true;
        filterDependentRefs = true;
        isFastVector = true;
        condenseMatrix = true;
        output = false;

        try {
        	RelationalInputGenerator[] relationalInputGenerators = AnelosimusDriver.getRelationalInputs(databaseName, inputFileEnding, inputFolderPath, hasHeader,
                    seperator, quote, escape, tableNames);
                	ResultCache resultReceiver = new ResultCache("test", getAcceptedColumns(relationalInputGenerators));
            AnelosimusDriver.run(resultReceiver, tableNames, inputRowLimit, nEstStrategy, p, m,
                    k, passes, nullValues, dop, refCoverageMinPercentage, verify, output, filterNonUniqueRefs,
                    filterNullCols,
                    filterNumericAndShortCols, filterDependentRefs, isFastVector, condenseMatrix,
                    strategyRef2Deps,
                    relationalInputGenerators);
        } catch (AlgorithmConfigurationException e) {
            e.printStackTrace();
        } catch (AlgorithmExecutionException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Ignore
    public void testWiki() {
        // conf
        String databaseName = "WIKITABLES";
        String inputFileEnding = ".csv";
        int limit = 10000;
        String inputFolderPath = "/home/fabian/MasterThesis/Development/data/wikitables" + File.separator;
        boolean hasHeader = true;
        char seperator = ',';
        char quote = '"';
        char escape = '\\';

        String[] tableNames = AnelosimusDriver.listFiles(inputFolderPath + databaseName + File.separator,
                inputFileEnding, limit);

        m = 512;
        k = 3;

        // nEstStrategy = ANELOSIMUS.N_EST_STRATEGIES.AVERAGE_HALF_DOUBLED.name();
        // p = 1000;

        passes = 1;
        nullValues = new String[] { "—", "-", "–", "N/A", "?", " ", "--" };
        dop = 4;
        refCoverageMinPercentage = 33;
        verify = true;
        filterNonUniqueRefs = true;
        filterNullCols = true;
        filterNumericAndShortCols = true;
        strategyRef2Deps = true;
        filterDependentRefs = true;
        isFastVector = false;
        condenseMatrix = true;

        try {
        	RelationalInputGenerator[] relationalInputGenerators = AnelosimusDriver.getRelationalInputs(databaseName, inputFileEnding, inputFolderPath, hasHeader,
                    seperator, quote, escape, tableNames);
                	ResultCache resultReceiver = new ResultCache("test", getAcceptedColumns(relationalInputGenerators));
            AnelosimusDriver.run(resultReceiver, tableNames, inputRowLimit, nEstStrategy, p, m,
                    k, passes, nullValues, dop, refCoverageMinPercentage, verify, output, filterNonUniqueRefs,
                    filterNullCols,
                    filterNumericAndShortCols, filterDependentRefs, isFastVector, condenseMatrix,
                    strategyRef2Deps,
                    relationalInputGenerators);
        } catch (AlgorithmConfigurationException e) {
            e.printStackTrace();
        } catch (AlgorithmExecutionException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Test
    @Ignore
    public void testPlista() {
        // conf
        String databaseName = "PLISTA";
        String inputFileEnding = ".csv";
        int limit = 1;
        String inputFolderPath = "/home/fabian/MasterThesis/Development/data/plista" + File.separator;
        boolean hasHeader = false;
        char seperator = ';';
        char quote = '"';
        char escape = '\\';

        String[] tableNames = AnelosimusDriver.listFiles(inputFolderPath + databaseName + File.separator,
                inputFileEnding, limit);

        m = 10;
        k = 1;

        try {
        	RelationalInputGenerator[] relationalInputGenerators = AnelosimusDriver.getRelationalInputs(databaseName, inputFileEnding, inputFolderPath, hasHeader,
                    seperator, quote, escape, tableNames);
                	ResultCache resultReceiver = new ResultCache("test", getAcceptedColumns(relationalInputGenerators));
            AnelosimusDriver.run(resultReceiver, tableNames, inputRowLimit, nEstStrategy, p, m,
                    k, passes, nullValues, dop, refCoverageMinPercentage, verify, output, filterNonUniqueRefs,
                    filterNullCols,
                    filterNumericAndShortCols, filterDependentRefs, isFastVector, condenseMatrix,
                    strategyRef2Deps,
                    relationalInputGenerators);
        } catch (AlgorithmConfigurationException e) {
            e.printStackTrace();
        } catch (AlgorithmExecutionException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
