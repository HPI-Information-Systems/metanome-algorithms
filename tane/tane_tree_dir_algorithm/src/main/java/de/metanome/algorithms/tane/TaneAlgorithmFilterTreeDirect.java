package de.metanome.algorithms.tane;

import it.unimi.dsi.fastutil.longs.LongBigArrayBigList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.util.OpenBitSet;

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
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithms.tane.algorithm_helper.FDTree;
import de.metanome.algorithms.tane.algorithm_helper.FDTreeElement;


/**
 * This class implements the TANE algorithm with a small adaptation, to solve
 * a problem with the key pruning. The key pruning might delete elements from the lattice,
 * although the C+ cover is not empty (some of the attributes, which are part of the key, might still be there).
 * If another key is found at a higher level, some C+-covers might not be available and the results cannot be found anymore.
 * Let R = {A,B,C} a relation, where A is a key and BC is a key.
 * In the first level the key A is found and the fd's A -> B and A -> C are added to the result.
 * In the second level the key BC is found. To avoid non-minimal dependencies an additional check is executed.
 * Therefore the C+(AB) and C+(AC) are intersected. Both C+-covers are not available anymore.
 * <p/>
 * To output only minimal fds, the fds are also added to a FDTree.
 * If a key has been found, all possible fds (Elements, which are still in the C+Cover of the key)
 * are checked whether a generalization already exists and are only added, if thats not the case.
 */
public class TaneAlgorithmFilterTreeDirect implements FunctionalDependencyAlgorithm,
//		SqlInputParameterAlgorithm,
        RelationalInputParameterAlgorithm,
        StringParameterAlgorithm {

    public static final String INPUT_SQL_CONNECTION = "DatabaseConnection";
    public static final String INPUT_TABLE_NAME = "Table_Name";
    public static final String INPUT_TAG = "Relational_Input";

    private DatabaseConnectionGenerator databaseConnectionGenerator;
    private RelationalInputGenerator relationalInputGenerator;
    private FDTree dependencies;
    private String tableName;
    private int numberAttributes;
    private long numberTuples;
    private List<String> columnNames;
    private ObjectArrayList<ColumnIdentifier> columnIdentifiers;
    private FunctionalDependencyResultReceiver fdResultReceiver;
    private Object2ObjectOpenHashMap<OpenBitSet, CombinationHelper> level0 = null;
    private Object2ObjectOpenHashMap<OpenBitSet, CombinationHelper> level1 = null;
    private Object2ObjectOpenHashMap<OpenBitSet, ObjectArrayList<OpenBitSet>> prefix_blocks = null;
    private LongBigArrayBigList tTable;

    @Override
    public ArrayList<ConfigurationRequirement> getConfigurationRequirements() {
    	ArrayList<ConfigurationRequirement> requiredConfig = new ArrayList<>();
//		requiredConfig.add(new ConfigurationSpecificationSQLIterator(INPUT_SQL_CONNECTION));
        requiredConfig.add(new ConfigurationRequirementRelationalInput(INPUT_TAG));
//		requiredConfig.add(new ConfigurationSpecificationString(INPUT_TABLE_NAME));

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
        this.fdResultReceiver = resultReceiver;
    }

    @Override
    public void execute() throws AlgorithmExecutionException {

        level0 = new Object2ObjectOpenHashMap<OpenBitSet, CombinationHelper>();
        level1 = new Object2ObjectOpenHashMap<OpenBitSet, CombinationHelper>();
        prefix_blocks = new Object2ObjectOpenHashMap<OpenBitSet, ObjectArrayList<OpenBitSet>>();

        // Get information about table from database
        ObjectArrayList<Object2ObjectOpenHashMap<Object, LongBigArrayBigList>> partitions = loadData();
        setColumnIdentifiers();
        numberAttributes = this.columnNames.size();

        // Initialize table used for stripped partition product
        tTable = new LongBigArrayBigList(numberTuples);
        for (long i = 0; i < numberTuples; i++) {
            tTable.add(-1);
        }
        // Initialize FDTree to store fds and filter them before returning them.
        dependencies = new FDTree(numberAttributes);

        // Initialize Level 0
        CombinationHelper chLevel0 = new CombinationHelper();
        OpenBitSet rhsCandidatesLevel0 = new OpenBitSet();
        rhsCandidatesLevel0.set(1, numberAttributes + 1);
        chLevel0.setRhsCandidates(rhsCandidatesLevel0);
        StrippedPartition spLevel0 = new StrippedPartition(numberTuples);
        chLevel0.setPartition(spLevel0);
        spLevel0 = null;
        level0.put(new OpenBitSet(), chLevel0);
        chLevel0 = null;


        // Initialize Level 1
        for (int i = 1; i <= numberAttributes; i++) {
            OpenBitSet combinationLevel1 = new OpenBitSet();
            combinationLevel1.set(i);

            CombinationHelper chLevel1 = new CombinationHelper();
            OpenBitSet rhsCandidatesLevel1 = new OpenBitSet();
            rhsCandidatesLevel1.set(1, numberAttributes + 1);
            chLevel1.setRhsCandidates(rhsCandidatesLevel0);

            StrippedPartition spLevel1 = new StrippedPartition(partitions.get(i - 1));
            chLevel1.setPartition(spLevel1);

            level1.put(combinationLevel1, chLevel1);
        }
        partitions = null;

        // while loop (main part of TANE)
        int l = 1;
        while (!level1.isEmpty() && l <= numberAttributes) {
            // compute dependencies for a level
            computeDependencies(l);

            // prune the search space
            prune();

            // compute the combinations for the next level
            generateNextLevel();

            l++;
        }
    }

    /**
     * Loads the data from the database or a csv file and
     * creates for each attribute a HashMap, which maps the values to a List of tuple ids.
     *
     * @return A ObjectArrayList with the HashMaps.
     * @throws InputGenerationException
     * @throws InputIterationException
     */
    private ObjectArrayList<Object2ObjectOpenHashMap<Object, LongBigArrayBigList>> loadData() throws InputGenerationException, InputIterationException {
        RelationalInput input = null;
        if (this.relationalInputGenerator != null) {
            input = this.relationalInputGenerator.generateNewCopy();
        } else if (this.databaseConnectionGenerator != null) {
            String sql = "SELECT * FROM " + this.tableName;
            input = this.databaseConnectionGenerator.generateRelationalInputFromSql(sql);
        }
        if (input != null) {
            this.numberAttributes = input.numberOfColumns();
            this.tableName = input.relationName();
            this.columnNames = input.columnNames();
            ObjectArrayList<Object2ObjectOpenHashMap<Object, LongBigArrayBigList>> partitions = new ObjectArrayList<Object2ObjectOpenHashMap<Object, LongBigArrayBigList>>(this.numberAttributes);
            for (int i = 0; i < this.numberAttributes; i++) {
                Object2ObjectOpenHashMap<Object, LongBigArrayBigList> partition = new Object2ObjectOpenHashMap<Object, LongBigArrayBigList>();
                partitions.add(partition);
            }
            long tupleId = 0;
            while (input.hasNext()) {
                List<String> row = input.next();
                for (int i = 0; i < this.numberAttributes; i++) {
                    Object2ObjectOpenHashMap<Object, LongBigArrayBigList> partition = partitions.get(i);
                    String entry = row.get(i);
                    if (partition.containsKey(entry)) {
                        partition.get(entry).add(tupleId);
                    } else {
                        LongBigArrayBigList newEqClass = new LongBigArrayBigList();
                        newEqClass.add(tupleId);
                        partition.put(entry, newEqClass);
                    }
                    ;
                }
                tupleId++;
            }
            this.numberTuples = tupleId;
            return partitions;
        }

        return new ObjectArrayList<Object2ObjectOpenHashMap<Object, LongBigArrayBigList>>(0);
    }

    /**
     * Initialize Cplus (resp. rhsCandidates) for each combination of the level.
     */
    private void initializeCplusForLevel() {
        for (OpenBitSet X : level1.keySet()) {

            ObjectArrayList<OpenBitSet> CxwithoutA_list = new ObjectArrayList<OpenBitSet>();

            // clone of X for usage in the following loop
            OpenBitSet Xclone = (OpenBitSet) X.clone();
            for (int A = X.nextSetBit(0); A >= 0; A = X.nextSetBit(A + 1)) {
                Xclone.clear(A);
                OpenBitSet CxwithoutA = level0.get(Xclone).getRhsCandidates();
                CxwithoutA_list.add(CxwithoutA);
                Xclone.set(A);
            }

            OpenBitSet CforX = new OpenBitSet();

            if (!CxwithoutA_list.isEmpty()) {
                CforX.set(1, numberAttributes + 1); // = (OpenBitSet) CxwithoutA_list.get(0).clone(); // = level1.get(X).getRhsCandidates();
                for (OpenBitSet CxwithoutA : CxwithoutA_list) {
                    CforX.and(CxwithoutA);
                }
            }

            CombinationHelper ch = level1.get(X);
            ch.setRhsCandidates(CforX);
//			level1.put(X, ch);
        }
    }

    private void computeDependencies(int l) throws AlgorithmExecutionException {
        initializeCplusForLevel();

        // iterate through the combinations of the level
        for (OpenBitSet X : level1.keySet()) {
            // Build the intersection between X and C_plus(X)
            OpenBitSet C_plus = level1.get(X).getRhsCandidates();
            OpenBitSet intersection = (OpenBitSet) X.clone();
            intersection.intersect(C_plus);

            // clone of X for usage in the following loop
            OpenBitSet Xclone = (OpenBitSet) X.clone();

            // iterate through all elements (A) of the intersection
            for (int A = intersection.nextSetBit(0); A >= 0; A = intersection.nextSetBit(A + 1)) {
                Xclone.clear(A);

                // check if X\A -> A is valid
                StrippedPartition spXwithoutA = level0.get(Xclone).getPartition();
                StrippedPartition spX = level1.get(X).getPartition();

                if (spX.getError() == spXwithoutA.getError()) {
                    // Add dependency to the result receiver as well as to the FDTree (dependencies),
                    // which is used to check, whether a more general dependency already exists.
                    OpenBitSet XwithoutA = (OpenBitSet) Xclone.clone();
                    addDependencyToResultReceiver(XwithoutA, A);
                    dependencies.addFunctionalDependency(Xclone, A);


                    // remove A from C_plus(X)
                    level1.get(X).getRhsCandidates().clear(A);

                    // remove all B in R\X from C_plus(X)
                    OpenBitSet RwithoutX = new OpenBitSet();
                    // set to R
                    RwithoutX.set(1, numberAttributes + 1);
                    // remove X
                    RwithoutX.andNot(X);

                    for (int i = RwithoutX.nextSetBit(0); i >= 0; i = RwithoutX.nextSetBit(i + 1)) {
                        level1.get(X).getRhsCandidates().clear(i);
                    }

                }
                Xclone.set(A);
            }
        }
    }

    /**
     * Prune the current level (level1) by removing all elements without any rhs candidates
     * and all keys. In case a key is found, dependencies might be added to the result receiver.
     *
     * @throws AlgorithmExecutionException If the result receiver cannot handle the Functional dependency.
     */
    private void prune() throws AlgorithmExecutionException {
        ObjectArrayList<OpenBitSet> elementsToRemove = new ObjectArrayList<OpenBitSet>();
        for (OpenBitSet x : level1.keySet()) {
            if (level1.get(x).getRhsCandidates().isEmpty()) {
                elementsToRemove.add(x);
                continue;
            }
            // Check if x is a key. Thats the case, if the error is 0.
            // See definition of the error on page 104 of the TANE-99 paper.
            if (level1.get(x).getPartition().getError() == 0) {

                // C+(X)\X
                OpenBitSet rhsXwithoutX = (OpenBitSet) level1.get(x).getRhsCandidates().clone();
                rhsXwithoutX.andNot(x);
                for (int a = rhsXwithoutX.nextSetBit(0); a >= 0; a = rhsXwithoutX.nextSetBit(a + 1)) {
                    OpenBitSet lhs = (OpenBitSet) x.clone();
                    if (!dependencies.containsGeneralization(lhs, a, 0)) {
                        addDependencyToResultReceiver(lhs, a);
                        dependencies.addFunctionalDependency(x, a);
                    }
                }
                elementsToRemove.add(x);
            }
        }
        for (OpenBitSet x : elementsToRemove) {
            level1.remove(x);
        }
    }

    /**
     * Calculate the product of two stripped partitions and return the result as a new stripped partition.
     *
     * @param pt1
     * @param pt2
     * @return A new StrippedPartition as the product of the two given StrippedPartitions.
     */
    public StrippedPartition multiply(StrippedPartition pt1, StrippedPartition pt2) {
        ObjectBigArrayBigList<LongBigArrayBigList> result = new ObjectBigArrayBigList<LongBigArrayBigList>();
        ObjectBigArrayBigList<LongBigArrayBigList> pt1List = pt1.getStrippedPartition();
        ObjectBigArrayBigList<LongBigArrayBigList> pt2List = pt2.getStrippedPartition();
        ObjectBigArrayBigList<LongBigArrayBigList> partition = new ObjectBigArrayBigList<LongBigArrayBigList>();
        long noOfElements = 0;
        // iterate over first stripped partition and fill tTable.
        for (long i = 0; i < pt1List.size64(); i++) {
            for (long tId : pt1List.get(i)) {
                tTable.set(tId, i);
            }
            partition.add(new LongBigArrayBigList());
        }
        // iterate over second stripped partition.
        for (long i = 0; i < pt2List.size64(); i++) {
            for (long t_id : pt2List.get(i)) {
                // tuple is also in an equivalence class of pt1
                if (tTable.get(t_id) != -1) {
                    partition.get(tTable.get(t_id)).add(t_id);
                }
            }
            for (long tId : pt2List.get(i)) {
                // if condition not in the paper;
                if (tTable.get(tId) != -1) {
                    if (partition.get(tTable.get(tId)).size64() > 1) {
                        LongBigArrayBigList eqClass = partition.get(tTable.get(tId));
                        result.add(eqClass);
                        noOfElements += eqClass.size64();
                    }
                    partition.set(tTable.get(tId), new LongBigArrayBigList());
                }
            }
        }
        // cleanup tTable to reuse it in the next multiplication.
        for (long i = 0; i < pt1List.size64(); i++) {
            for (long tId : pt1List.get(i)) {
                tTable.set(tId, -1);
            }
        }
        return new StrippedPartition(result, noOfElements);
    }

    private long getLastSetBitIndex(OpenBitSet bitset) {
        int lastSetBit = 0;
        for (int A = bitset.nextSetBit(0); A >= 0; A = bitset.nextSetBit(A + 1)) {
            lastSetBit = A;
        }
        return lastSetBit;
    }

    /**
     * Get prefix of OpenBitSet by copying it and removing the last Bit.
     *
     * @param bitset
     * @return
     */
    private OpenBitSet getPrefix(OpenBitSet bitset) {
        OpenBitSet prefix = (OpenBitSet) bitset.clone();
        prefix.clear(getLastSetBitIndex(prefix));
        return prefix;
    }

    /**
     * Build the prefix blocks for a level. It is a Hashmap containing the
     * prefix as a key and the corresponding attributes as  the value.
     */
    private void buildPrefixBlocks() {
//		System.out.println("Start BuildPrefixBlocks");
        this.prefix_blocks.clear();
        for (OpenBitSet level_iter : level0.keySet()) {
            OpenBitSet prefix = getPrefix(level_iter);

            if (prefix_blocks.containsKey(prefix)) {
                prefix_blocks.get(prefix).add(level_iter);
            } else {
                ObjectArrayList<OpenBitSet> list = new ObjectArrayList<OpenBitSet>();
                list.add(level_iter);
                prefix_blocks.put(prefix, list);
            }
        }
//		System.out.println("Stop BuildPrefixBlocks");
    }

    /**
     * Get all combinations, which can be built out of the elements of a prefix block
     *
     * @param list
     * @return
     */
    private ObjectArrayList<OpenBitSet[]> getListCombinations(ObjectArrayList<OpenBitSet> list) {
        ObjectArrayList<OpenBitSet[]> combinations = new ObjectArrayList<OpenBitSet[]>();
        for (int a = 0; a < list.size(); a++) {
            for (int b = a + 1; b < list.size(); b++) {
                OpenBitSet[] combi = new OpenBitSet[2];
                combi[0] = list.get(a);
                combi[1] = list.get(b);
                combinations.add(combi);
            }
        }
        return combinations;
    }

    /**
     * Checks whether all subsets of X (with length of X - 1) are part of the last level.
     * Only if this check return true X is added to the new level.
     *
     * @param X
     * @return
     */
    private boolean checkSubsets(OpenBitSet X) {
        boolean xIsValid = true;

        // clone of X for usage in the following loop
        OpenBitSet Xclone = (OpenBitSet) X.clone();

        for (int l = X.nextSetBit(0); l >= 0; l = X.nextSetBit(l + 1)) {
            Xclone.clear(l);
            if (!level0.containsKey(Xclone)) {
                xIsValid = false;
                break;
            }
            Xclone.set(l);
        }

        return xIsValid;
    }

    private void generateNextLevel() {
        // System.out.println("Start generateNextLevel");
        level0 = level1;
        level1 = null;
        System.gc();

        Object2ObjectOpenHashMap<OpenBitSet, CombinationHelper> new_level = new Object2ObjectOpenHashMap<OpenBitSet, CombinationHelper>();

        buildPrefixBlocks();

        for (ObjectArrayList<OpenBitSet> prefix_block_list : prefix_blocks.values()) {

            // continue only, if the prefix_block contains at least 2 elements
            if (prefix_block_list.size() < 2) {
                continue;
            }

            ObjectArrayList<OpenBitSet[]> combinations = getListCombinations(prefix_block_list);
            for (OpenBitSet[] c : combinations) {
                OpenBitSet X = (OpenBitSet) c[0].clone();
                X.or(c[1]);

                if (checkSubsets(X)) {
                    StrippedPartition st = multiply(level0.get(c[0]).getPartition(), level0.get(c[1]).getPartition());
                    OpenBitSet rhsCandidates = new OpenBitSet();
//					 rhsCandidates.set(1, numberAttributes+1);

                    CombinationHelper ch = new CombinationHelper();
                    ch.setPartition(st);
                    ch.setRhsCandidates(rhsCandidates);

                    new_level.put(X, ch);
                }
            }
        }

        level1 = new_level;
//		System.out.println("Finished generateNextLevel");
    }

    /**
     * Add the functional dependency to the ResultReceiver.
     *
     * @param X: A OpenBitSet representing the Columns of the determinant.
     * @param a: The number of the dependent column (starting from 1).
     * @throws CouldNotReceiveResultException
     */
    private void addDependencyToResultReceiver(OpenBitSet X, int a) throws CouldNotReceiveResultException {
        if (this.fdResultReceiver == null) {
            return;
        }
        ColumnIdentifier[] columns = new ColumnIdentifier[(int) X.cardinality()];
        int j = 0;
        for (int i = X.nextSetBit(0); i >= 0; i = X.nextSetBit(i + 1)) {
            columns[j++] = this.columnIdentifiers.get(i - 1);
        }
        ColumnCombination colCombination = new ColumnCombination(columns);
        de.metanome.algorithm_integration.results.FunctionalDependency fdResult = new de.metanome.algorithm_integration.results.FunctionalDependency(colCombination, columnIdentifiers.get((int) a - 1));
        this.fdResultReceiver.receiveResult(fdResult);
    }

    private void setColumnIdentifiers() {
        this.columnIdentifiers = new ObjectArrayList<ColumnIdentifier>(this.columnNames.size());
        for (String column_name : this.columnNames) {
            columnIdentifiers.add(new ColumnIdentifier(this.tableName, column_name));
        }
    }

    public void serialize_attribute(OpenBitSet bitset, CombinationHelper ch) {
        String file_name = bitset.toString();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file_name));
            oos.writeObject(ch);
            oos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CombinationHelper deserialize_attribute(OpenBitSet bitset) {
        String file_name = bitset.toString();
        ObjectInputStream is = null;
        CombinationHelper ch = null;
        try {
            is = new ObjectInputStream(new FileInputStream(file_name));
            ch = (CombinationHelper) is.readObject();
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return ch;
    }


    /**
     * Adds all functional dependencies in the subtree fds to the resultReceiver.
     *
     * @param fds
     * @param activePath
     * @throws CouldNotReceiveResultException
     */
    private void addAllDependenciesToResultReceiver(FDTreeElement fds, OpenBitSet activePath) throws CouldNotReceiveResultException {
        if (this.fdResultReceiver == null) {
            return;
        }
        for (int attr = 1; attr <= numberAttributes; attr++) {
            if (fds.isFd(attr - 1)) {
                int j = 0;
                ColumnIdentifier[] columns = new ColumnIdentifier[(int) activePath.cardinality()];
                for (int i = activePath.nextSetBit(0); i >= 0; i = activePath.nextSetBit(i + 1)) {
                    columns[j++] = this.columnIdentifiers.get(i - 1);
                }
                ColumnCombination colCombination = new ColumnCombination(columns);
                de.metanome.algorithm_integration.results.FunctionalDependency fdResult = new de.metanome.algorithm_integration.results.FunctionalDependency(colCombination, columnIdentifiers.get((int) attr - 1));
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
     */
    @SuppressWarnings("unused")
    private void addAllDependenciesToResultReceiver() throws CouldNotReceiveResultException {
        if (this.fdResultReceiver == null) {
            return;
        }
        this.addAllDependenciesToResultReceiver(dependencies, new OpenBitSet());
    }
}
