package de.metanome.algorithms.order;

import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.ColumnPermutation;
import de.metanome.algorithm_integration.algorithm_types.OrderDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.OrderDependencyResultReceiver;
import de.metanome.algorithm_integration.results.OrderDependency;
import de.metanome.algorithms.order.measurements.Statistics;
import de.metanome.algorithms.order.sorting.partitions.RowIndexedDateValue;
import de.metanome.algorithms.order.sorting.partitions.RowIndexedDoubleValue;
import de.metanome.algorithms.order.sorting.partitions.RowIndexedLongValue;
import de.metanome.algorithms.order.sorting.partitions.RowIndexedStringValue;
import de.metanome.algorithms.order.sorting.partitions.RowIndexedValue;
import de.metanome.algorithms.order.sorting.partitions.SortedPartition;
import de.metanome.algorithms.order.sorting.partitions.SortedPartitionCreator;
import de.metanome.algorithms.order.types.ByteArrayPermutations;
import de.metanome.algorithms.order.types.Datatype;
import de.metanome.algorithms.order.types.TypeInferrer;
import de.uni_potsdam.hpi.utils.CollectionUtils;

public class ORDER implements OrderDependencyAlgorithm, RelationalInputParameterAlgorithm {

  protected RelationalInputGenerator inputGenerator;
  protected RelationalInput input;
  protected OrderDependencyResultReceiver resultReceiver;
  protected String tableName;
  protected List<String> columnNames = new ArrayList<String>();
  protected int[] columnIndices;
  protected Statistics statistics;
  protected int partitionCacheSize = 0;

  List<List<RowIndexedValue>> dataByColumns;
  List<Datatype> types;

  public int numRows;
  public int level;

  Map<byte[], SortedPartition> permutationToPartition;
  Map<byte[], List<byte[]>> prefixBlocks;

  // should write out all dependencies when dependencyBatchLimit is reached
  // private static final long dependencyBatchLimit = 100;

  Logger logger = LoggerFactory.getLogger(ORDER.class);

  public enum ConfigIdentifier {
    RELATIONAL_INPUT
  }

  @Override
  public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
    final ArrayList<ConfigurationRequirement<?>> config = new ArrayList<>(3);
    config.add(new ConfigurationRequirementRelationalInput(ORDER.ConfigIdentifier.RELATIONAL_INPUT.name()));
    return config;
  }

  @Override
  public void execute() throws AlgorithmExecutionException {
    try {
      this.initialize();
    } catch (final InputGenerationException i) {
      throw new AlgorithmConfigurationException("Could not initialize ORDER: " + i.getMessage(), i);
    }
  }

  protected String prettyPrintPrefixBlocks() {
    final StringBuilder sb = new StringBuilder();

    for (final Entry<byte[], List<byte[]>> samePrefix : this.prefixBlocks.entrySet()) {
      sb.append("\n");
      sb.append(ByteArrayPermutations.permutationToIntegerString(samePrefix.getKey()));
      sb.append(" := ");
      for (final byte[] samePrefixList : samePrefix.getValue()) {
        sb.append(ByteArrayPermutations.permutationToIntegerString(samePrefixList));
      }
    }
    return sb.toString();
  }

  protected void initialize() throws AlgorithmExecutionException {

    this.statistics = new Statistics("ORDER");

    this.input = this.inputGenerator.generateNewCopy();
    
    this.tableName = this.input.relationName();

    this.columnNames = this.input.columnNames();

    // get column indices
    this.columnIndices = new int[this.columnNames.size()];
    for (int i = 0; i < this.columnNames.size(); i++) {
      this.columnIndices[i] = i;
    }

    long time = System.currentTimeMillis();
    this.inferTypes();
    time = System.currentTimeMillis() - time;
    this.statistics.setTypeInferralTime(time);

    time = System.currentTimeMillis();
    this.loadData();
    time = System.currentTimeMillis() - time;
    this.statistics.setLoadDataTime(time);

    time = System.currentTimeMillis();
    this.initializePartitions();
    time = System.currentTimeMillis() - time;
    this.statistics.setInitPartitionsTime(time);
  }

  protected void inferTypes() throws InputGenerationException {
    final TypeInferrer inferrer = new TypeInferrer(this.inputGenerator);
    this.types = inferrer.inferTypes();
  }

  protected void loadData() throws InputIterationException, InputGenerationException {
    this.dataByColumns = new ArrayList<List<RowIndexedValue>>();

    for (int i = 0; i < this.columnIndices.length; i++) {
      this.dataByColumns.add(new ArrayList<RowIndexedValue>());
    }

    try (final RelationalInput input = this.inputGenerator.generateNewCopy()) {

      long tupleId = 0;
      while (this.input.hasNext()) {
        final List<String> currentRow = this.input.next();
        for (final int column : this.columnIndices) {
          this.storeData(tupleId, currentRow, column);
        }
        tupleId++;
        this.numRows++;
      }

    } catch (final Exception e) {
      throw new InputGenerationException("Error while loading data.", e);
    }

    if (this.dataByColumns == null || this.dataByColumns.isEmpty()) {
      throw new InputGenerationException("Did not find any data in " + this.tableName + ".");
    }

  }

  private void storeData(final long tupleId, final List<String> currentRow, final int column)
      throws ParseException {

    final String stringValue = currentRow.get(column);
    switch (this.types.get(column).getSpecificType()) {
      case DOUBLE:
        final Double doubleValue = (stringValue == null) ? null : Double.parseDouble(stringValue);
        this.dataByColumns.get(column).add(new RowIndexedDoubleValue(tupleId, doubleValue));
        break;
      case DATE:
        final Date dateValue =
        (stringValue == null) ? null : DateUtils.parseDate(stringValue,
            TypeInferrer.dateFormats);
        this.dataByColumns.get(column).add(new RowIndexedDateValue(tupleId, dateValue));
        break;
      case LONG:
        final Long longValue = (stringValue == null) ? null : Long.parseLong(stringValue);
        this.dataByColumns.get(column).add(new RowIndexedLongValue(tupleId, longValue));
        break;
      case STRING:
        this.dataByColumns.get(column).add(new RowIndexedStringValue(tupleId, stringValue));
        break;
      default:
        this.dataByColumns.get(column).add(new RowIndexedStringValue(tupleId, stringValue));
        break;
    }
  }

  protected void initializePartitions() throws AlgorithmExecutionException {
    this.permutationToPartition = new Object2ObjectOpenCustomHashMap<>(ByteArrays.HASH_STRATEGY);

    // create partitions for level 1
    for (final int columnIndex : this.columnIndices) {
      final byte[] singleColumnPermutation = {(byte) columnIndex};

      final SortedPartition partition =
          SortedPartitionCreator.createPartition(this.dataByColumns.get(columnIndex),
              this.types.get(columnIndex));

      this.permutationToPartition.put(singleColumnPermutation, partition);
    }

  }

  public String permutationToColumnNames(final byte[] permutation) {
    if (permutation.length == 0) {
      return "[]";
    }
    final int[] colIndices = new int[permutation.length];
    final StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < colIndices.length; i++) {
      sb.append(this.columnNames.get(permutation[i]));
      sb.append(",");
    }
    sb.deleteCharAt(sb.length() - 1);
    sb.append("]");
    return sb.toString();
  }

  public void signalFoundOrderDependency(final byte[] lhs, final byte[] rhs,
      final OrderDependency.ComparisonOperator comparisonOperator,
      final OrderDependency.OrderType orderType) throws ColumnNameMismatchException {
    this.logger.info(
        "Table {} contains a valid OD: {} ~> {} (Comparison operator: {}, ordering: {})",
        this.tableName, this.permutationToColumnNames(lhs), this.permutationToColumnNames(rhs),
        comparisonOperator.name(), orderType.name());
    final OrderDependency orderDependency =
        new OrderDependency(new ColumnPermutation(new ColumnIdentifier(this.tableName,
            this.permutationToColumnNames(lhs))), new ColumnPermutation(new ColumnIdentifier(
            this.tableName, this.permutationToColumnNames(rhs))), orderType, comparisonOperator);
    try {
      this.resultReceiver.receiveResult(orderDependency);
    } catch (final CouldNotReceiveResultException e) {
      this.logger.error("Metanome Framework could not receive order dependency: {}",
          orderDependency.toString());
      e.printStackTrace();
    }
  }

  @Override
  public void setResultReceiver(final OrderDependencyResultReceiver resultReceiver) {
    this.resultReceiver = resultReceiver;
  }

  @Override
  public void setRelationalInputConfigurationValue(final String identifier,
      final RelationalInputGenerator... values) throws AlgorithmConfigurationException {
    if (identifier.equals(ORDER.ConfigIdentifier.RELATIONAL_INPUT.name())) {
      this.inputGenerator = values[0];
    } else {
      throw new AlgorithmConfigurationException("Unknown configuration identifier: " + identifier
          + "->" + CollectionUtils.concat(values, ","));
    }
  }

}
