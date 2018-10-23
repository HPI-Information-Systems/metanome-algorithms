package de.metanome.algorithms.fdep;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.algorithm_types.FunctionalDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.StringParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.DatabaseConnectionGenerator;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;
import de.metanome.algorithms.tane.algorithm_helper.FDTree;
import de.metanome.algorithms.tane.algorithm_helper.FDTreeElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class FdepAlgorithm implements FunctionalDependencyAlgorithm,
        RelationalInputParameterAlgorithm,
        StringParameterAlgorithm {

    public static final String INPUT_SQL_CONNECTION = "DatabaseConnection";
    public static final String INPUT_TABLE_NAME = "Table_Name";
    public static final String INPUT_TAG = "Relational Input";


    private DatabaseConnectionGenerator databaseConnectionGenerator;
    private RelationalInputGenerator relationalInputGenerator;
    private String tableName;
    private List<String> columnNames;
    private ObjectArrayList<ColumnIdentifier> columnIdentifiers;

    private int numberAttributes;

    private FDTree negCoverTree;
    private FDTree posCoverTree;
    private ObjectArrayList<List<String>> tuples;

    private FunctionalDependencyResultReceiver fdResultReceiver;

    @Override
    public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
        ArrayList<ConfigurationRequirement<?>> requiredConfig = new ArrayList<>();
//		requiredConfig.add(new ConfigurationSpecificationSQLIterator(
//			INPUT_SQL_CONNECTION));
//		requiredConfig.add(new ConfigurationSpecificationString(INPUT_TABLE_NAME));
        requiredConfig.add(new ConfigurationRequirementRelationalInput(INPUT_TAG));

        return requiredConfig;
    }

    @Override
    public void setStringConfigurationValue(String identifier, String... values) {
        if (identifier.equals(INPUT_TABLE_NAME)) {
            this.tableName = values[0];
        }
    }

    @Override
    public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values) {
        if (identifier.equals(INPUT_TAG)) {
            this.relationalInputGenerator = values[0];
        }
    }

    @Override
    public void setResultReceiver(
            FunctionalDependencyResultReceiver resultReceiver) {
        fdResultReceiver = resultReceiver;
    }

    @Override
    public void execute() throws AlgorithmExecutionException {
        initialize();
        negativeCover();
        this.tuples = null;

        posCoverTree = new FDTree(numberAttributes);
        posCoverTree.addMostGeneralDependencies();
        BitSet activePath = new BitSet();
        calculatePositiveCover(negCoverTree, activePath);
//		posCoverTree.filterGeneralizations();
        addAllDependenciesToResultReceiver();
    }

    private void initialize() throws AlgorithmExecutionException, InputGenerationException, InputIterationException {
        loadData();
        setColumnIdentifiers();
    }

    /**
     * Calculate a set of fds, which do not cover the invalid dependency lhs -> a.
     */
    private void specializePositiveCover(BitSet lhs, int a) {
        BitSet specLhs = new BitSet();

        while (posCoverTree.getGeneralizationAndDelete(lhs, a, 0, specLhs)) {
            for (int attr = this.numberAttributes; attr > 0; attr--) {
                if (!lhs.get(attr) && (attr != a)) {
                    specLhs.set(attr);
                    if (!posCoverTree.containsGeneralization(specLhs, a, 0)) {
                        posCoverTree.addFunctionalDependency(specLhs, a);
                    }
                    specLhs.clear(attr);
                }
            }
            specLhs = new BitSet();
        }
    }

    private void calculatePositiveCover(FDTreeElement negCoverSubtree, BitSet activePath) {
        for (int attr = 1; attr <= numberAttributes; attr++) {
            if (negCoverSubtree.isFd(attr - 1)) {
                specializePositiveCover(activePath, attr);
            }
        }

        for (int attr = 1; attr <= numberAttributes; attr++) {
            if (negCoverSubtree.getChild(attr - 1) != null) {
                activePath.set(attr);
                this.calculatePositiveCover(negCoverSubtree.getChild(attr - 1), activePath);
                activePath.clear(attr);
            }
        }
    }


    /**
     * Calculate the negative Cover for the current relation.
     */
    private void negativeCover() {
        negCoverTree = new FDTree(this.numberAttributes);
        for (int i = 0; i < tuples.size(); i++) {
            for (int j = i + 1; j < tuples.size(); j++) {
                violatedFds(tuples.get(i), tuples.get(j));
            }
        }
        this.negCoverTree.filterSpecializations();
    }


    /**
     * Find the least general functional dependencies violated by t1 and t2
     * and add update the negative cover accordingly.<br/>
     * Note: t1 and t2 must have the same length.
     *
     * @param t1 An ObjectArrayList with the values of one entry of the relation.
     * @param t2 An ObjectArrayList with the values of another entry of the relation.
     */
    private void violatedFds(List<String> t1, List<String> t2) {
        BitSet equalAttr = new BitSet();
        equalAttr.set(1, this.numberAttributes + 1);
        BitSet diffAttr = new BitSet();
        for (int i = 0; i < t1.size(); i++) {
            Object val1 = t1.get(i);
            Object val2 = t2.get(i);
            // Handling of null values. Currently assuming NULL values are equal.
            if (val1 == null && val2 == null) {
                continue;
            } else if ((val1 == null && val2 != null) || !(val1.equals(val2))) {
                // BitSet start with 1 for first attribute
                diffAttr.set(i + 1);
            }
        }
        equalAttr.andNot(diffAttr);
        for (int a = diffAttr.nextSetBit(0); a >= 0; a = diffAttr.nextSetBit(a + 1)) {
            negCoverTree.addFunctionalDependency(equalAttr, a);
        }
    }


    /**
     * Fetch the data from the database and keep it as List of Lists.
     *
     * @throws AlgorithmExecutionException
     * @throws AlgorithmConfigurationException
     */
    private void loadData() throws AlgorithmExecutionException, AlgorithmConfigurationException {
        RelationalInput ri = null;
        tuples = new ObjectArrayList<List<String>>();
        if (this.relationalInputGenerator != null) {
            ri = this.relationalInputGenerator.generateNewCopy();
        } else if (this.databaseConnectionGenerator != null && this.tableName != null) {
            String sql = "SELECT * FROM " + this.tableName;
            ri = this.databaseConnectionGenerator.generateRelationalInputFromSql(sql, this.tableName);
        } else {
            throw new AlgorithmConfigurationException("No input Generator set.");
        }
        if (ri != null) {
            this.columnNames = ri.columnNames();
            this.tableName = ri.relationName();
            this.numberAttributes = ri.numberOfColumns();
            while (ri.hasNext()) {
                List<String> row = ri.next();
                tuples.add(row);
            }
        }
    }

    private void setColumnIdentifiers() {
        this.columnIdentifiers = new ObjectArrayList<ColumnIdentifier>(
                this.columnNames.size());
        for (String column_name : this.columnNames) {
            columnIdentifiers.add(new ColumnIdentifier(this.tableName,
                    column_name));
        }
    }

    private void addAllDependenciesToResultReceiver(FDTreeElement fds, BitSet activePath) throws CouldNotReceiveResultException, ColumnNameMismatchException {
        if (this.fdResultReceiver == null) {
            return;
        }
        for (int attr = 1; attr <= numberAttributes; attr++) {
            if (fds.isFd(attr - 1)) {
                int j = 0;
                ColumnIdentifier[] columns = new ColumnIdentifier[activePath.cardinality()];
                for (int i = activePath.nextSetBit(0); i >= 0; i = activePath.nextSetBit(i + 1)) {
                    columns[j++] = this.columnIdentifiers.get(i - 1);
                }
                ColumnCombination colCombination = new ColumnCombination(columns);
                FunctionalDependency fdResult = new FunctionalDependency(colCombination, columnIdentifiers.get(attr - 1));
//				System.out.println(fdResult.toString());
                fdResultReceiver.receiveResult(fdResult);
            }
        }

        for (int attr = 1; attr <= numberAttributes; attr++) {
            if (fds.getChild(attr - 1) != null) {
                activePath.set(attr);
                this.addAllDependenciesToResultReceiver(fds.getChild(attr - 1), activePath);
                activePath.clear(attr);
            }
        }
    }


    /**
     * Add all functional Dependencies to the FunctionalDependencyResultReceiver.
     * Do nothing if the object does not have a result receiver.
     *
     * @throws CouldNotReceiveResultException
     * @throws ColumnNameMismatchException 
     */
    private void addAllDependenciesToResultReceiver() throws CouldNotReceiveResultException, ColumnNameMismatchException {
        if (this.fdResultReceiver == null) {
            return;
        }
        this.addAllDependenciesToResultReceiver(posCoverTree, new BitSet());
    }

	@Override
	public String getAuthors() {
		return "Jannik Marten, Jan-Peer Rudolph";
	}

	@Override
	public String getDescription() {
		return "Dependency Induction-based FD discovery";
	}
}
