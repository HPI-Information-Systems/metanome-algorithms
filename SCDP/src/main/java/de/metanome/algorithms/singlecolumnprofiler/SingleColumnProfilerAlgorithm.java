package de.metanome.algorithms.singlecolumnprofiler;



import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.BasicStatisticsResultReceiver;
import de.metanome.algorithm_integration.results.BasicStatistic;
import de.metanome.algorithm_integration.results.basic_statistic_values.BasicStatisticValueDouble;
import de.metanome.algorithm_integration.results.basic_statistic_values.BasicStatisticValueInteger;
import de.metanome.algorithm_integration.results.basic_statistic_values.BasicStatisticValueIntegerList;
import de.metanome.algorithm_integration.results.basic_statistic_values.BasicStatisticValueLong;
import de.metanome.algorithm_integration.results.basic_statistic_values.BasicStatisticValueString;
import de.metanome.algorithm_integration.results.basic_statistic_values.BasicStatisticValueStringList;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;



public class SingleColumnProfilerAlgorithm {
  public static final int Numoftopk = 10;
  protected RelationalInputGenerator inputGenerator = null;
  protected BasicStatisticsResultReceiver resultReceiver = null;
  private RelationalInput input = null;
  // general statistic
  protected String relationName;
  protected long NumofTuples = 0;
  protected List<String> columnNames;
  protected ObjectArrayList<ColumnMainProfile> columnsProfile;
  protected String outputPath;
  // statistic Names
  public final String NUMCOLUMN = "Number of Columns";
  public final String NUMTUPLE = "Number of Tuples";
  public final String COLUMNNAME = "Column Name";
  public final String NUMBEROFNULL = "Nulls";
  public final String PERCENTOFNULL = "Percentage of Nulls";
  public final String NUMBEROFDISTINCT = "Number of Distinct Values";
  public final String PERCENTODFISTINCT = "Percentage of Distinct Values";
  public final String DISTINCTVALUES = "Distinct Values";
  public final String VALUEDISTRIBUTION = "Value Distribution";
  public final String STRINGLENGTHDISTRIBUTION = "String Length Distribution";
  public final String TOPKITEM = "Top " + Numoftopk + " frequent items";
  public final String TOPKITEMFREQ = "Frequency Of Top " + Numoftopk + " Frequent Items";
  public final String DATATYPE = "Data Type";
  public final String LONGESTSTRING = "Longest String";
  public final String SHORTESTSTRING = "Shortest String";
  public final String MINSTRING = "Min String";
  public final String MAXSTRING = "Max String";
  public final String SEMANTICDATATYPE = "Symantic DataType";
  public final String MIN = "Min";
  public final String MAX = "Max";
  public final String AVG = "Avg.";
  public final String STDD = "Standard Deviation";
  // Delimiter used in CSV file
  BufferedWriter bufferWritter;

  // private static final String COMMA_DELIMITER = ",";
  // private static final String NEW_LINE_SEPARATOR = "\n";
  public void execute() throws AlgorithmExecutionException {

    ////////////////////////////////////////////
    // THE DISCOVERY ALGORITHM LIVES HERE :-) //
    ////////////////////////////////////////////
    // just to generate my data
    // try{
    // File file =new File("generateschema.txt");
    //
    // //if file doesnt exists, then create it
    // if(!file.exists()){
    // file.createNewFile();
    // }
    //
    // //true = append file
    // FileWriter fileWritter = new FileWriter(file.getName(),true);
    // bufferWritter = new BufferedWriter(fileWritter);
    // }
    // catch (IOException e) {
    // //exception handling left as an exercise for the reader
    // }
    // =======================================================
    // step 1: initialisation
    InitialiseColumnProfiles();
    
    // step 2: get data types
    getColumnsProfiles();
  
    // step 3: output
    // JSONObject General = new JSONObject();
    // General.put(NUMCOLUMN, columnNames.size());
    // General.put(NUMTUPLE, NumofTuples);
//    BasicStatistic gbs = new BasicStatistic(new ColumnIdentifier("*", relationName));
//    gbs.addStatistic(NUMCOLUMN, new BasicStatisticValueInteger(columnNames.size()));
//    gbs.addStatistic(NUMTUPLE, new BasicStatisticValueInteger(NumofTuples));
//    resultReceiver.receiveResult(gbs);
   for (int i = 0; i < columnNames.size(); i++) {
      // System.out.println(columnsProfile.get(i).toString());
      generateColumnStatistic(columnsProfile.get(i));
      columnsProfile.set(i,null);
      
    }

   
  }

  private void InitialiseColumnProfiles()
      throws InputGenerationException, InputIterationException, AlgorithmConfigurationException {
    input = this.inputGenerator.generateNewCopy();
    this.relationName = input.relationName();
    this.columnNames = input.columnNames();
    // outputPath = "io" + File.separator + "measurements" + File.separator
    // + relationName.replaceAll(".csv", "") + "_" + this.getClass().getSimpleName()
    // + File.separator;;
    columnsProfile = new ObjectArrayList<ColumnMainProfile>();
    // generate an initial profiles according to the first record
    if (input.hasNext()) {
      NumofTuples++;
      List<String> firstrecord = input.next();
      // for each column
      for (int i = 0; i < columnNames.size(); i++) {
        ColumnMainProfile profile = new ColumnMainProfile(columnNames.get(i));
        String currentColumnvalue = firstrecord.get(i);

        // data type even if null the type is NA
        profile.setDataType(DataTypes.getDataType(currentColumnvalue));

        // null value
        if (currentColumnvalue == null)
          profile.increaseNumNull();
        else {
          // longest and shortest string
          profile.setLongestString(currentColumnvalue);
          profile.setShortestString(currentColumnvalue);

          // frequency
          profile.addValueforfreq(currentColumnvalue);
          // profile.addValueforlengdist(currentColumnvalue.length());
          ////////////////
          // rest values
          /////////////////////

          // max min sum
          if (DataTypes.isNumeric(profile.getDataType())) {
            profile.setMax(Util.getnumberfromstring(currentColumnvalue));
            profile.setMin(Util.getnumberfromstring(currentColumnvalue));
            profile.setSum(Util.getnumberfromstring(currentColumnvalue));

          }


        }
        columnsProfile.add(i, profile);

      }

    }
   

  }



  private void getColumnsProfiles()
      throws InputGenerationException, InputIterationException, AlgorithmConfigurationException {
    // first pass
    //////////////////////////////////////////////
    List<String> currentrecord = null;
    // for each tuple
    while (input.hasNext()) {
      // read a tuple
      NumofTuples++;
      currentrecord = input.next();
      // for each column in a tuple verify the data type and update if new data type detected
      for (int i = 0; i < currentrecord.size(); i++)
        if (currentrecord.get(i) == null)
          columnsProfile.get(i).increaseNumNull();
        else
          columnsProfile.get(i).updateColumnProfile(currentrecord.get(i));
    }
    //////////////////////////////////////////
    // add the special types of string
    for (int i = 0; i < columnsProfile.size(); i++) {
      if (columnsProfile.get(i).getDataType() == DataTypes.mySTRING)
        if (columnsProfile.get(i).getLongestString().length() > 255)
          columnsProfile.get(i).setDataType(DataTypes.myTEXT);
        else if (columnsProfile.get(i).getDataType() == DataTypes.mySTRING
            && columnsProfile.get(i).getLongestString().length() == columnsProfile.get(i)
                .getShortestString().length()
            && columnsProfile.get(i).getNumNull() == 0
            && DataTypes.isUUID(columnsProfile.get(i).getLongestString())
            && DataTypes.isUUID(columnsProfile.get(i).getShortestString())

        )
          columnsProfile.get(i).setDataType(DataTypes.myUUID);

    }
    // columnsProfile.get(i).setCalculatedFields(NumofTuples);
    // try {
    //
    // columnsProfile.get(i).writeMapToCsv(outputPath, relationName.replaceAll(".csv", ""),
    // columnsProfile.get(i).getColumnName());
    // columnsProfile.get(i).setFreq(null);
    ///////////////////////////////////////////////////////////////////
    // bufferWritter.write(relationName.replaceAll(".csv", ""));
    // bufferWritter.write(COMMA_DELIMITER);
    // bufferWritter.write(columnsProfile.get(i).getColumnName());
    // bufferWritter.write(COMMA_DELIMITER);
    // bufferWritter.write(columnsProfile.get(i).getDataType());
    // bufferWritter.write(COMMA_DELIMITER);
    // if(columnsProfile.get(i).getNumNull()==0)
    // bufferWritter.write("NOTNULL");
    // else
    // bufferWritter.write("NULL");
    // bufferWritter.write(NEW_LINE_SEPARATOR);
    // bufferWritter.flush();
    //////////////////////////////////////////////////////////////////
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    /////////////////////////////////////
    // add the second pass value
    input = this.inputGenerator.generateNewCopy();
    while (input.hasNext()) {
      // read a tuple
      currentrecord = input.next();
      // for each column in a tuple
      for (int i = 0; i < currentrecord.size(); i++)
        if (currentrecord.get(i) != null) {
          columnsProfile.get(i).updateColumnProfilesecondpass(currentrecord.get(i), NumofTuples);
        }

    }
    for (int i = 0; i < columnsProfile.size(); i++)
      columnsProfile.get(i)
          .setStdDev(Math.sqrt((columnsProfile.get(i).getStdDev() / (NumofTuples - 1))));
  }



  private void generateColumnStatistic(ColumnMainProfile cs) throws AlgorithmExecutionException {


    BasicStatistic bs = new BasicStatistic(new ColumnIdentifier(relationName, cs.getColumnName()));


    // for all with string
    bs.addStatistic(NUMTUPLE, new BasicStatisticValueLong(NumofTuples));
    bs.addStatistic(NUMBEROFNULL, new BasicStatisticValueLong(cs.getNumNull()));
    bs.addStatistic(PERCENTOFNULL,
        new BasicStatisticValueLong(cs.getNumNull() * 100 / NumofTuples));
    bs.addStatistic(NUMBEROFDISTINCT, new BasicStatisticValueInteger(cs.getFreq().size()));
    bs.addStatistic(PERCENTODFISTINCT,
        new BasicStatisticValueInteger((int) (cs.getFreq().size() * 100 / NumofTuples)));


    // if (cs.getDistinctValues() != null) column.put(DISTINCTVALUES, cs.getDistinctValues());
    // if(cs.getFreq()!=null) column.put(VALUEDISTRIBUTION, Util.mapToJson(cs.getFreq()));

    // just for strings
    if (cs.getDataType() == DataTypes.mySTRING) {
      String stringwithlength =
          cs.getDataType() + "[" + Util.roundUp(cs.getLongestString().length(), 16) + "]";
      bs.addStatistic(DATATYPE, new BasicStatisticValueString(stringwithlength));
      if (cs.getLongestString() != null)
        bs.addStatistic(LONGESTSTRING, new BasicStatisticValueString(cs.getLongestString()));
      if (cs.getShortestString() != null)
        bs.addStatistic(SHORTESTSTRING, new BasicStatisticValueString(cs.getShortestString()));
      bs.addStatistic(MINSTRING, new BasicStatisticValueString(cs.getFreq().firstKey()));
      bs.addStatistic(MAXSTRING, new BasicStatisticValueString(cs.getFreq().lastKey()));

      if (cs.getSemantictype() != null && cs.getSemantictype() != DataTypes.UNKOWN)
        bs.addStatistic(SEMANTICDATATYPE, new BasicStatisticValueString(cs.getSemantictype()));
      // if(cs.getLengthdist()!=null) column.put(STRINGLENGTHDISTRIBUTION,Util.mapToJsonIntegerKey(
      // cs.getLengthdist()));

    } else {
      // all types not string
      bs.addStatistic(DATATYPE, new BasicStatisticValueString(cs.getDataType()));

      // just numbers
      if (DataTypes.isNumeric(cs.getDataType())) {
        if (cs.getMin() != null)
          bs.addStatistic(MIN, new BasicStatisticValueDouble(cs.getMin()));

        if (cs.getMax() != null)
          bs.addStatistic(MAX, new BasicStatisticValueDouble(cs.getMax()));

        bs.addStatistic(AVG, new BasicStatisticValueDouble(cs.getSum() / NumofTuples));

        Double stdev = cs.getStdDev();
        if (!stdev.equals(Double.NaN))
          bs.addStatistic(STDD, new BasicStatisticValueDouble(cs.getStdDev()));

      }

    }

    
    // all
    cs.setFreq(new Object2IntRBTreeMap<String>(Util.sortByValues(cs.getFreq())));
    TreeMap<String, Integer> topk = (TreeMap<String, Integer>) Util.getTopK(cs.getFreq(), SingleColumnProfilerAlgorithm.Numoftopk);
    if(topk!=null){
      bs.addStatistic(TOPKITEM,
          new BasicStatisticValueStringList(new ArrayList<String>(topk.keySet())));
      bs.addStatistic(TOPKITEMFREQ,
          new BasicStatisticValueIntegerList(new ArrayList<Integer>(topk.values())));
    }

    resultReceiver.receiveResult(bs);
    System.out.println(bs);

  }


}
