package de.metanome.algorithms.dvlc;

import java.util.ArrayList;
import java.util.List;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.BasicStatisticsResultReceiver;
import de.metanome.algorithm_integration.results.BasicStatistic;
import de.metanome.algorithm_integration.results.basic_statistic_values.BasicStatisticValueLong;
/**
 * * Implementation of Linear Counting algorithm.
 * * Reference: Whang, K. Y., Vander-Zanden, B. T., & Taylor, H. M. (1990). A linear-time probabilistic counting algorithm for database applications.
 *   ACM Transactions on Database Systems (TODS), 15(2), 208-229 
 * * @author Hazar.Harmouch
 */
public class DVLCAlgorithm {

  protected RelationalInputGenerator inputGenerator = null;
  protected BasicStatisticsResultReceiver resultReceiver = null;

  protected String relationName;
  protected List<String> columnNames;
  private int NUMOFTUPLES;
  private final String NUMBEROFDISTINCT = "Number of Distinct Values";
  private RelationalInput input;
  protected double eps = 0.01;

  public void execute() throws AlgorithmExecutionException {
    ////////////////////////////////////////////
    // THE DISCOVERY ALGORITHM LIVES HERE :-) //
    ////////////////////////////////////////////


    input = this.inputGenerator.generateNewCopy();
    this.relationName = input.relationName();
    this.columnNames = input.columnNames();
    // (1) Specify the desired accuracy (i.e., the standard error)=0.01
    // (2) Measure the relation cardinality (complexity = 0 (q)) if it is not already available
    while (input.hasNext()) {
      input.next();
      NUMOFTUPLES++;
    }


    input = this.inputGenerator.generateNewCopy();
    ArrayList<LinearCounting> Columns = new ArrayList<LinearCounting>();
    if (eps == 0.01) {
      // (3) Read the map size from Table II using the relation cardinality q as n
      for (int i = 0; i < columnNames.size(); i++)
        Columns.add(new LinearCounting(NUMOFTUPLES));
    } else {
            // (3) calculate the map size that implement a Linear Counter with arbitrary standard
            // error and maximum expected cardinality
      for (int i = 0; i < columnNames.size(); i++)
        Columns.add(new LinearCounting(eps, NUMOFTUPLES));
    }
    // (4) Run the basic Linear Counting Algorithm
    // (4.1)scan the data values in each column in the relation
    while (input.hasNext()) {
      List<String> CurrentTuple = input.next();
      // (4.2)uses a hash function to map each data value to a bit in the bitmap and sets this bit
      // to “1”
      for (int i = 0; i < columnNames.size(); i++)
        Columns.get(i).offer(CurrentTuple.get(i));

    }
    // (4.3)estimates the columns cardinality
    for (int i = 0; i < columnNames.size(); i++) {
      addStatistic(NUMBEROFDISTINCT, Columns.get(i).cardinality(), columnNames.get(i),
          relationName);
    }

  }

  private void addStatistic(String StatisticName, long Value, String ColumnName,
      String RelationName) throws AlgorithmExecutionException {
    BasicStatistic result = new BasicStatistic(new ColumnIdentifier(RelationName, ColumnName));
    result.addStatistic(StatisticName, new BasicStatisticValueLong(Value));
    //System.out.println(StatisticName + " of " + ColumnName + " : " + Value);
    resultReceiver.receiveResult(result);
  }

}
