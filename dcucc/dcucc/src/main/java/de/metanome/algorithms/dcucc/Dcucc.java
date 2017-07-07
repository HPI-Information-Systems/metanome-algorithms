package de.metanome.algorithms.dcucc;

import com.google.common.collect.ImmutableList;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_helper.data_structures.SubSetGraph;
import de.metanome.algorithm_helper.data_structures.SuperSetGraph;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.ConditionalUniqueColumnCombinationAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.FileInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.ListBoxParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementFileInput;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementInteger;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementListBox;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.*;
import de.metanome.algorithm_integration.results.*;
import de.metanome.algorithms.ducc.DuccAlgorithm;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Mockup comment
 *
 * @author Jens Ehrlich
 */


/*TODO:
add use calculate not condition
adjust condition and result to return found not conditions
traverse condition lattice*/
public class Dcucc implements ConditionalUniqueColumnCombinationAlgorithm,
                              FileInputParameterAlgorithm,
                              RelationalInputParameterAlgorithm,
                              IntegerParameterAlgorithm,
                              BooleanParameterAlgorithm,
                              ListBoxParameterAlgorithm {

  public static final String INPUT_FILE_TAG = "csvIterator";
  public static final String FREQUENCY_TAG = "frequency";
  public static final String PERCENTAGE_TAG = "percentage";
  public static final String ALGORITHM_TAG = "algorithm_type";
  public static final String SELFCONDITIONS_TAG = "calculate self conditions";
  public static final String ORCONDITIONLENGHT_TAG = "maximal length of or condition";
  public static int numberOfTuples = -1;
  public Map<String, ConditionLatticeTraverser> algorithmDescriptionMap;
  protected String algorithmDescription = "";
  protected int frequency = -1;
  protected int numberOfColumns = -1;
  protected boolean percentage = false;
  protected boolean calculateSelfConditions = false;
  protected int maxOrConditionLength = 0;
  protected List<PositionListIndex> basePLI;
  protected List<ColumnCombinationBitset> baseColumn;
  protected ConditionLatticeTraverser conditionLatticeTraverser;
  protected ImmutableList<ColumnCombinationBitset> partialUccs;
  protected Map<ColumnCombinationBitset, PositionListIndex> pliMap;
  protected SuperSetGraph lowerPruningGraph;
  protected SubSetGraph upperPruningGraph;
  protected RelationalInput input;
  protected RelationalInputGenerator inputGenerator;
  protected ConditionalUniqueColumnCombinationResultReceiver resultReceiver;
  protected ResultSingleton resultSingleton;


  public Dcucc() {
    algorithmDescriptionMap = new HashMap<>();
    algorithmDescriptionMap.put("SingleValueSingleCondition", new SimpleConditionTraverser(this));
    algorithmDescriptionMap.put("MultipleValueSingleCondition", new OrConditionTraverser(this));
    algorithmDescriptionMap.put("SingleValueMultipleCondition", new AndConditionTraverser(this));
    algorithmDescriptionMap
        .put("MultipleValueMultipleCondition", new AndOrConditionTraverser(this));

  }

  @Override
  public void execute() throws AlgorithmExecutionException {

    if (this.algorithmDescription != "") {
      this.conditionLatticeTraverser = this.algorithmDescriptionMap.get(this.algorithmDescription);
    }
    long start = System.nanoTime();
    RelationalInput input = this.calculateInput();
    this.createBaseColumns(input);
    System.out.println("Build plis: " + ((System.nanoTime() - start) / 1000000));

    start = System.nanoTime();
    UniqueColumnCombinationResultReceiver dummyReceiver = createDummyResultReceiver();

    DuccAlgorithm
        partialUCCalgorithm =
        new DuccAlgorithm(input.relationName(), input.columnNames(), dummyReceiver);
    partialUCCalgorithm.setRawKeyError(this.numberOfTuples - this.frequency);
    partialUCCalgorithm.run(this.basePLI);
    this.partialUccs = partialUCCalgorithm.getMinimalUniqueColumnCombinations();
    this.pliMap = partialUCCalgorithm.getCalculatedPlis();
    System.out.println("Calculate partial uniques: " + ((System.nanoTime() - start) / 1000000));


    start = System.nanoTime();
    this.preparePruningGraphs();
    this.resultSingleton =
        ResultSingleton
            .createResultSingleton(this.inputGenerator.generateNewCopy(), this.partialUccs,
                                   this.resultReceiver);
    System.out.println("Prepare pruning graphs: " + ((System.nanoTime() - start) / 1000000));
    start = System.nanoTime();
    this.iteratePartialUniqueLattice();
    System.out
        .println("Calculated conditional uniques: " + ((System.nanoTime() - start) / 1000000));
    start = System.nanoTime();
    this.returnResult();
    System.out.println("return results: " + ((System.nanoTime() - start) / 1000000));
  }

  @Override
  public String getAuthors() {
    return "Jens Ehrlich";
  }

  @Override
  public String getDescription() {
    return "The DoCu algorithm is used to discover conditional unique column combinations.";
  }


  protected void preparePruningGraphs() {
    this.lowerPruningGraph = new SuperSetGraph(this.numberOfColumns);
    this.upperPruningGraph = new SubSetGraph();

    this.lowerPruningGraph.addAll(this.partialUccs);
  }

  protected void iteratePartialUniqueLattice() throws AlgorithmExecutionException {
    List<ColumnCombinationBitset> currentLevel = this.calculateFirstLevel();
    while (!currentLevel.isEmpty()) {
      for (ColumnCombinationBitset partialUnique : currentLevel) {
        if (calculateSelfConditions) {
          SelfConditionFinder
            .calculateSelfConditions(partialUnique, this.getPLI(partialUnique), this);
        }
        this.conditionLatticeTraverser.iterateConditionLattice(partialUnique);
      }
      currentLevel = calculateNextLevel(currentLevel);
    }
  }

  protected List<ColumnCombinationBitset> calculateNextLevel(
      List<ColumnCombinationBitset> previousLevel) throws AlgorithmExecutionException {
    List<ColumnCombinationBitset> nextLevel = new LinkedList<>();
    Set<ColumnCombinationBitset> unprunedNextLevel = new TreeSet<>();
    for (ColumnCombinationBitset currentColumnCombination : previousLevel) {
      calculateAllParents(currentColumnCombination, unprunedNextLevel);
    }

    for (ColumnCombinationBitset nextLevelBitset : unprunedNextLevel) {
      boolean isTooSmall = this.lowerPruningGraph.containsSuperset(nextLevelBitset);
      boolean isTooBig = this.upperPruningGraph.containsSubset(nextLevelBitset);

//      if ((this.lowerPruningGraph.containsSuperset(nextLevelBitset)) || (this.upperPruningGraph
//                                                                             .containsSubset(
//                                                                                 nextLevelBitset))) {
      if (isTooSmall || isTooBig) {
        continue;
      } else {
        PositionListIndex nextLevelPLI = this.getPLI(nextLevelBitset);
        if (nextLevelPLI.isUnique() || this.checkForFD(nextLevelBitset)) {
          this.upperPruningGraph.add(nextLevelBitset);
        } else {
            nextLevel.add(nextLevelBitset);
          this.lowerPruningGraph.add(nextLevelBitset);
          resultSingleton.conditionMinimalityGraph.add(nextLevelBitset);
//          if (!this.checkForFD(nextLevelBitset)) {
//            nextLevel.add(nextLevelBitset);
//          }
//          this.lowerPruningGraph.add(nextLevelBitset);
//          resultSingleton.conditionMinimalityGraph.add(nextLevelBitset);
        }
      }
    }
    return nextLevel;
  }

  protected boolean checkForFD(ColumnCombinationBitset bitset) {
    //TODO is this check really reasonable (performance)?
    for (ColumnCombinationBitset possibleChild : bitset
        .getNSubsetColumnCombinations(bitset.getSetBits().size() - 1)) {
      if (this.pliMap.containsKey(possibleChild)) {
        if (this.pliMap.get(possibleChild).getRawKeyError() == this.pliMap.get(bitset)
            .getRawKeyError()) {
          //FD found
          this.upperPruningGraph.add(bitset);
          return true;
        }
      }
    }
    return false;
  }

  protected void calculateAllParents(ColumnCombinationBitset child,
                                     Set<ColumnCombinationBitset> set) {
    for (int newColumn : child.getClearedBits(this.numberOfColumns)) {
      set.add(new ColumnCombinationBitset(child).addColumn(newColumn));
    }
  }

  protected void returnResult() throws AlgorithmExecutionException {
/*    RelationalInput input = this.inputGenerator.generateNewCopy();
    List<Map<Long, String>> inputMap = new ArrayList<>(input.numberOfColumns());
    for (int i = 0; i < input.numberOfColumns(); i++) {
      inputMap.add(new HashMap<Long, String>());
    }
    long row = 0;
    while (input.hasNext()) {
      ImmutableList<String> values = input.next();
      for (int i = 0; i < input.numberOfColumns(); i++) {
        inputMap.get(i).put(row, values.get(i));
      }
      row++;
    }*/

/*    for (Condition condition : this.foundConditions) {
      condition.addOrResultToResultReceiver(this.resultReceiver, input, inputMap);
    }*/
  }

  protected PositionListIndex getPLI(ColumnCombinationBitset bitset)
      throws AlgorithmExecutionException {
    PositionListIndex pli = this.pliMap.get(bitset);
    if (null == pli) {
      //the one of the previous plis always exist
      ColumnCombinationBitset previous = null;
      for (ColumnCombinationBitset previousCandidate : bitset
          .getNSubsetColumnCombinations(bitset.size() - 1)) {
        if (this.pliMap.containsKey(previousCandidate)) {
          previous = previousCandidate;
          break;
        }
      }

      if (previous == null) {
        throw new AlgorithmExecutionException("An expected PLI was not found in the hashmap");
      }

      PositionListIndex previousPLI = this.pliMap.get(previous);
      PositionListIndex missingColumn = this.pliMap.get(bitset.minus(previous));

      pli = previousPLI.intersect(missingColumn);
      this.pliMap.put(bitset, pli);
    }
    return pli;
  }

  protected List<ColumnCombinationBitset> calculateFirstLevel() {
    List<ColumnCombinationBitset> firstLevel = new LinkedList<>();
    for (ColumnCombinationBitset columnCombination : this.partialUccs) {
      if (this.pliMap.get(columnCombination).isUnique()) {
        continue;
      }
      firstLevel.add(columnCombination);
    }
    return firstLevel;
  }

  protected RelationalInput calculateInput()
      throws InputGenerationException, InputIterationException, AlgorithmConfigurationException {
    RelationalInput input;
    input = inputGenerator.generateNewCopy();
    PLIBuilder pliBuilder = new PLIBuilder(input);
    basePLI = pliBuilder.getPLIList();
    numberOfTuples = (int) pliBuilder.getNumberOfTuples();
    numberOfColumns = input.numberOfColumns();
    if (percentage) {
      frequency = (int) Math.ceil(numberOfTuples * frequency * 1.0d / 100);
    }
    if (frequency < 0) {
      throw new AlgorithmConfigurationException();
    }

    return input;
  }

  protected void createBaseColumns(RelationalInput input) {
    this.baseColumn = new ArrayList<>();
    for (int i = 0; i < input.numberOfColumns(); i++) {
      this.baseColumn.add(new ColumnCombinationBitset(i));
    }
  }


  @Override
  public void setResultReceiver(ConditionalUniqueColumnCombinationResultReceiver resultReceiver) {
    this.resultReceiver = resultReceiver;

  }

  @Override
  public void setFileInputConfigurationValue(String identifier, FileInputGenerator... values)
      throws AlgorithmConfigurationException {
    if (identifier.equals(INPUT_FILE_TAG)) {
      inputGenerator = values[0];
    } else {
      throw new AlgorithmConfigurationException("Operation should not be called");
    }
  }


  @Override
  public void setRelationalInputConfigurationValue(String identifier,
                                                   RelationalInputGenerator... values)
      throws AlgorithmConfigurationException {
    if (identifier.equals(INPUT_FILE_TAG)) {
      inputGenerator = values[0];
    } else {
      throw new AlgorithmConfigurationException("Operation should not be called");
    }

  }

  protected UniqueColumnCombinationResultReceiver createDummyResultReceiver() {
    return new OmniscientResultReceiver() {
      @Override
      public void receiveResult(MultivaluedDependency multivaluedDependency) throws CouldNotReceiveResultException, ColumnNameMismatchException {

      }

      @Override
      public Boolean acceptedResult(MultivaluedDependency multivaluedDependency) {
        return null;
      }

      @Override
      public void receiveResult(BasicStatistic statistic) throws CouldNotReceiveResultException {

      }

      @Override
      public Boolean acceptedResult(BasicStatistic basicStatistic) {
        return null;
      }

      @Override
      public void receiveResult(
          ConditionalUniqueColumnCombination conditionalUniqueColumnCombination)
          throws CouldNotReceiveResultException {

      }

      @Override
      public Boolean acceptedResult(ConditionalUniqueColumnCombination conditionalUniqueColumnCombination) {
        return null;
      }

      @Override
      public void receiveResult(FunctionalDependency functionalDependency)
          throws CouldNotReceiveResultException {

      }

      @Override
      public Boolean acceptedResult(FunctionalDependency functionalDependency) {
        return null;
      }

      @Override
      public void receiveResult(InclusionDependency inclusionDependency)
          throws CouldNotReceiveResultException {

      }

      @Override
      public Boolean acceptedResult(InclusionDependency inclusionDependency) {
        return null;
      }

      @Override
      public void receiveResult(UniqueColumnCombination uniqueColumnCombination)
          throws CouldNotReceiveResultException {

      }

      @Override
      public Boolean acceptedResult(UniqueColumnCombination uniqueColumnCombination) {
        return null;
      }

      @Override
      public void receiveResult(OrderDependency orderDependency)
          throws CouldNotReceiveResultException {

      }

      @Override
      public Boolean acceptedResult(OrderDependency orderDependency) {
        return null;
      }
    };
  }

  @Override
  public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
    ArrayList<ConfigurationRequirement<?>> spec = new ArrayList<>();
    ConfigurationRequirementFileInput
        csvFile =
        new ConfigurationRequirementFileInput(INPUT_FILE_TAG);
    spec.add(csvFile);

    ConfigurationRequirementInteger
        frequency =
        new ConfigurationRequirementInteger(FREQUENCY_TAG);
    spec.add(frequency);

    ConfigurationRequirementBoolean
        percentage =
        new ConfigurationRequirementBoolean(PERCENTAGE_TAG);
    spec.add(percentage);

//    ConfigurationRequirementInteger
//        conditionLength =
//        new ConfigurationRequirementInteger(ORCONDITIONLENGHT_TAG);
//    spec.add(conditionLength);

    ConfigurationRequirementBoolean
        selfCondition =
        new ConfigurationRequirementBoolean(SELFCONDITIONS_TAG);
    spec.add(selfCondition);

    ArrayList<String> algorithmOptions = new ArrayList<>();
    algorithmOptions.addAll(this.algorithmDescriptionMap.keySet());
    ConfigurationRequirementListBox
        listBox =
        new ConfigurationRequirementListBox(ALGORITHM_TAG, algorithmOptions);
    spec.add(listBox);

    return spec;
  }

  @Override
  public void setListBoxConfigurationValue(String identifier, String... selectedValues)
      throws AlgorithmConfigurationException {
    if (identifier.equals(ALGORITHM_TAG)) {
      this.algorithmDescription = selectedValues[0];
    } else {
      throw new AlgorithmConfigurationException("Operation should not be called");
    }
  }

  @Override
  public void setBooleanConfigurationValue(String s, Boolean... booleans) throws AlgorithmConfigurationException {
    switch (s) {
      case PERCENTAGE_TAG:
        this.percentage = booleans[0];
        break;
      case SELFCONDITIONS_TAG:
        this.calculateSelfConditions = booleans[0];
        break;
      default:
        throw new AlgorithmConfigurationException("Operation should not be called");
    }
  }

  @Override
  public void setIntegerConfigurationValue(String s, Integer... integers) throws AlgorithmConfigurationException {
    switch (s) {
      case FREQUENCY_TAG:
        this.frequency = integers[0];
        break;
      case ORCONDITIONLENGHT_TAG:
        this.maxOrConditionLength = integers[0];
        break;
      default:
        throw new AlgorithmConfigurationException("Operation should not be called");
    }
  }
}

