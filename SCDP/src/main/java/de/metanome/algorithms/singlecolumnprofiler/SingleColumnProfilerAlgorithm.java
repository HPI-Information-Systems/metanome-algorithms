package de.metanome.algorithms.singlecolumnprofiler;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.BasicStatisticsResultReceiver;
import de.metanome.algorithm_integration.results.BasicStatistic;


public class SingleColumnProfilerAlgorithm {
  public static final int Numoftopk = 10;
  protected RelationalInputGenerator inputGenerator = null;
  protected BasicStatisticsResultReceiver resultReceiver = null;
  private RelationalInput input = null;
  // general statistic
  protected String relationName;
  protected int NumofTuples = 0;
  protected List<String> columnNames;
  protected List<ColumnMainProfile> columnsProfile;
  protected String outputPath;
  // statistic Names
  public final String NUMCOLUMN = "# Columns";
  public final String NUMTUPLE = "# Tuples";
  public final String COLUMNNAME = "column Name";
  public final String NUMBEROFNULL = "# Null";
  public final String PERCENTOFNULL = "% Null";
  public final String NUMBEROFDISTINCT = "# Distinct";
  public final String PERCENTODFISTINCT = "% Distinct";
  public final String DISTINCTVALUES = "Distinct Values";
  public final String VALUEDISTRIBUTION = "Value Distribution";
  public final String STRINGLENGTHDISTRIBUTION = "String Length Distribution";
  public final String TOPKITEM = "Top " + Numoftopk + " frquent Items";
  public final String DATATYPE = "Data Type";
  public final String LONGESTSTRING = "Longest String";
  public final String SHORTESTSTRING = "Shortest String";
  public final String MINSTRING = "Min String";
  public final String MAXSTRING = "Max String";
  public final String SEMANTICDATATYPE = "Symantic Data Type";
  public final String MIN = "Min";
  public final String MAX = "Max";
  public final String AVG = "Avg.";
  public final String STDD = "Standard Deviation";
  // Delimiter used in CSV file
  BufferedWriter bufferWritter;
  private static final String COMMA_DELIMITER = ",";
  private static final String NEW_LINE_SEPARATOR = "\n";
  public void execute() throws AlgorithmExecutionException {

    ////////////////////////////////////////////
    // THE DISCOVERY ALGORITHM LIVES HERE :-) //
    ////////////////////////////////////////////
 //just to generate my data
//    try{
//      File file =new File("generateschema.txt");
//      
//      //if file doesnt exists, then create it
//      if(!file.exists()){
//          file.createNewFile();
//      }
//      
//      //true = append file
//      FileWriter fileWritter = new FileWriter(file.getName(),true);
//      bufferWritter = new BufferedWriter(fileWritter);   
//    }
//    catch (IOException e) {
//      //exception handling left as an exercise for the reader
//  }
//=======================================================
    //step 1: initialisation
    InitialiseColumnProfiles();
   
    //step 2: get data types
    getColumnsProfiles();

    //step 3: output
   // JSONObject General = new JSONObject();
   // General.put(NUMCOLUMN, columnNames.size());
   // General.put(NUMTUPLE, NumofTuples);
    String general=NUMCOLUMN+columnNames.size()+"\n"+NUMTUPLE+NumofTuples;
    addStatistic("General", general, "*", relationName);
    for (int i = 0; i < columnsProfile.size(); i++) {
      // System.out.println(columnsProfile.get(i).toString());
      generateColumnStatistic(columnsProfile.get(i));
    }


  }

  private void InitialiseColumnProfiles() throws InputGenerationException, InputIterationException, AlgorithmConfigurationException {
    input = this.inputGenerator.generateNewCopy();
    this.relationName = input.relationName();
    this.columnNames = input.columnNames();
//    outputPath = "io" + File.separator + "measurements" + File.separator
//        + relationName.replaceAll(".csv", "") + "_" + this.getClass().getSimpleName()
//        + File.separator;;
    columnsProfile = new ArrayList<ColumnMainProfile>();
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
    System.out.println("initial values done " + relationName);

  }



  private void getColumnsProfiles() throws InputGenerationException, InputIterationException, AlgorithmConfigurationException {
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
    System.out.println("done with main profiles");
    //////////////////////////////////////////
    // add the calculated values
    for (int i = 0; i < columnsProfile.size(); i++) {
      columnsProfile.get(i).setCalculatedFields(NumofTuples);
//      try {
//       
//        columnsProfile.get(i).writeMapToCsv(outputPath, relationName.replaceAll(".csv", ""),
//        columnsProfile.get(i).getColumnName());
        columnsProfile.get(i).setFreq(null);
        ///////////////////////////////////////////////////////////////////
//        bufferWritter.write(relationName.replaceAll(".csv", ""));
//        bufferWritter.write(COMMA_DELIMITER);
//        bufferWritter.write(columnsProfile.get(i).getColumnName());
//        bufferWritter.write(COMMA_DELIMITER);
//        bufferWritter.write(columnsProfile.get(i).getDataType());
//        bufferWritter.write(COMMA_DELIMITER);
//      if(columnsProfile.get(i).getNumNull()==0)
//        bufferWritter.write("NOTNULL");
//      else
//        bufferWritter.write("NULL");
//        bufferWritter.write(NEW_LINE_SEPARATOR);
//        bufferWritter.flush();
        //////////////////////////////////////////////////////////////////
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
        }
    /////////////////////////////////////
    // add the second pass value
    input = this.inputGenerator.generateNewCopy();
    while (input.hasNext()) {
      // read a tuple
      currentrecord = input.next();
      // for each column in a tuple
      for (int i = 0; i < currentrecord.size(); i++)
        if (currentrecord.get(i) != null) {
          columnsProfile.get(i).updateColumnProfilesecondpass(currentrecord.get(i));
        }

    }
    for (int i = 0; i < columnsProfile.size(); i++)
      columnsProfile.get(i)
          .setStdDev(Math.sqrt((columnsProfile.get(i).getStdDev() / (NumofTuples - 1))));
  }

  private void addStatistic(String StatisticName, Object Value, String ColumnName,
      String RelationName) throws AlgorithmExecutionException {
    BasicStatistic bs =
        new BasicStatistic(StatisticName, Value, new ColumnIdentifier(RelationName, ColumnName));
    // System.out.println(StatisticName + " of " + ColumnName + " : " + Value);
    resultReceiver.receiveResult(bs);
  }

  private void generateColumnStatistic(ColumnMainProfile cs) throws AlgorithmExecutionException {

    String column ="";

    // for all with string
    column+=COLUMNNAME+" : "+ cs.getColumnName()+",";
    column+=NUMBEROFNULL+" : "+  cs.getNumofNull()+",";
    column+=PERCENTOFNULL+" : "+ cs.getPercentNull()+",";
    column+=NUMBEROFDISTINCT+" : "+cs.getNumDistinct()+",";
    column+=PERCENTODFISTINCT+" : "+ cs.getPercentDistinct()+",";

    // if (cs.getDistinctValues() != null) column.put(DISTINCTVALUES, cs.getDistinctValues());
    // if(cs.getFreq()!=null) column.put(VALUEDISTRIBUTION, Util.mapToJson(cs.getFreq()));
   
    if (cs.getTopkValues() != null)
      column+=TOPKITEM+" : "+ Util.mapTostring(cs.getTopkValues())+",";

    // just for strings
    if (cs.getDataType() == DataTypes.mySTRING) {
      String stringwithlength =
          cs.getDataType() + "[" + Util.roundUp(cs.getLongestString().length(), 16) + "]";
      column+=DATATYPE+" : "+ stringwithlength+",";
      if (cs.getLongestString() != null)
        column+=LONGESTSTRING+" : "+cs.getLongestString()+",";
      if (cs.getShortestString() != null)
        column+=SHORTESTSTRING+" : "+cs.getShortestString()+",";
      if (cs.getFirstString() != null)
        column+=MINSTRING+" : "+cs.getFirstString()+",";
      if (cs.getLasttString() != null)
        column+=MAXSTRING+" : "+cs.getLasttString()+",";
      if (cs.getSemantictype() != null && cs.getSemantictype() !=DataTypes.UNKOWN)
        column+=SEMANTICDATATYPE+" : "+ cs.getSemantictype()+",";
      // if(cs.getLengthdist()!=null) column.put(STRINGLENGTHDISTRIBUTION,Util.mapToJsonIntegerKey(
      // cs.getLengthdist()));

    } else {
      // all types not string
      column+=DATATYPE+" : "+cs.getDataType()+",";

      // just numbers
      if (DataTypes.isNumeric(cs.getDataType())) {
        if (cs.getMin() != null)
          column+=MIN+" : "+ cs.getMin()+",";
        if (cs.getMax() != null)
          column+=MAX+" : "+ cs.getMax()+",";
        column+=AVG+" : "+cs.getAvg()+",";
        Double stdev = cs.getStdDev();
        if (!stdev.equals(Double.NaN))
          column+=STDD+" : "+ cs.getStdDev();
      }

    }

    addStatistic("Single Column Profiler", column, cs.getColumnName(), relationName);
  }
  
  private void generateColumnStatisticJson(ColumnMainProfile cs) throws AlgorithmExecutionException {

    JSONObject column = new JSONObject();

    // for all with string
    column.put(COLUMNNAME, cs.getColumnName());
    column.put(NUMBEROFNULL, cs.getNumofNull());
    column.put(PERCENTOFNULL, cs.getPercentNull());
    column.put(NUMBEROFDISTINCT, cs.getNumDistinct());
    column.put(PERCENTODFISTINCT, cs.getPercentDistinct());

    // if (cs.getDistinctValues() != null) column.put(DISTINCTVALUES, cs.getDistinctValues());

    // if(cs.getFreq()!=null) column.put(VALUEDISTRIBUTION, Util.mapToJson(cs.getFreq()));
    if (cs.getTopkValues() != null)
      column.put(TOPKITEM, Util.mapToJson(cs.getTopkValues()));

    // just for strings
    if (cs.getDataType() == DataTypes.mySTRING) {
      String stringwithlength =
          cs.getDataType() + "[" + Util.roundUp(cs.getLongestString().length(), 16) + "]";
      column.put(DATATYPE, stringwithlength);
      if (cs.getLongestString() != null)
        column.put(LONGESTSTRING, cs.getLongestString());
      if (cs.getShortestString() != null)
        column.put(SHORTESTSTRING, cs.getShortestString());
      if (cs.getFirstString() != null)
        column.put(MINSTRING, cs.getFirstString());
      if (cs.getLasttString() != null)
        column.put(MAXSTRING, cs.getLasttString());
      if (cs.getSemantictype() != null)
        column.put(SEMANTICDATATYPE, cs.getSemantictype());
      // if(cs.getLengthdist()!=null) column.put(STRINGLENGTHDISTRIBUTION,Util.mapToJsonIntegerKey(
      // cs.getLengthdist()));

    } else {
      // all types not string
      column.put(DATATYPE, cs.getDataType());

      // just numbers
      if (DataTypes.isNumeric(cs.getDataType())) {
        if (cs.getMin() != null)
          column.put(MIN, cs.getMin());
        if (cs.getMax() != null)
          column.put(MAX, cs.getMax());
        column.put(AVG, cs.getAvg());
        Double stdev = cs.getStdDev();
        if (!stdev.equals(Double.NaN))
          column.put(STDD, cs.getStdDev());
      }

    }

    addStatistic("Single Column Profiler", column.toString(), cs.getColumnName(), relationName);
  }

}
