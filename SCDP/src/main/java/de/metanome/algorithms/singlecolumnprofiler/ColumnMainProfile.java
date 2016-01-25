package de.metanome.algorithms.singlecolumnprofiler;


import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;



public class ColumnMainProfile {
  // Delimiter used in CSV file
  private static final String COMMA_DELIMITER = ",";
  private static final String NEW_LINE_SEPARATOR = "\n";
  //////////////////////////////////////////////////
  // valid for all types
  private String ColumnName;
  private String DataType;
  private long NumNull;
  private String Defaultvalue;// *******
  // %Distinct Percentage of distinct values
  private int percentDistinct;// -------Calculated
  // %NULL Percentage of Null values
  private long percentNull;// -------Calculated
  // #Distinct : the number of distinct values is the size of the hash tree
  private int NumDistinct; // -------Calculated
  // top k value
  // column value distribution (int)
  TreeMap<String, MutableInt> freq;
  TreeMap<String, MutableInt> topk;// ------calculated

  ////////////////////////////////////////////////////////
  // text
  private String LongestString;
  private String ShortestString;
  // max and min for strings are the first and list strings in the tree
  private String firstString;// -------Calculated
  private String lasttString;// -------Calculated
  private String Semantictype;// ------secondpass
  // column length distribution (text)
  // TreeMap<String, MutableInt> lengthdist; ; //------secondpass
  //////////////////////////////////////////////////
  // numbers
  private double Max;
  private double Min;
  private double Sum;
  private double Avg;// ------calculated
  private double stdDev;// ------secondpass

  /////////////////// constructor////////////////////////////////

  public ColumnMainProfile(String ColumnName) {
    setColumnName(ColumnName);
    setMax(Double.MIN_VALUE);
    setMin(Double.MAX_VALUE);
    setLongestString("");
    setSum(0.0);
    setNumofNull(0);
    setStdDev(0);
    freq = new TreeMap<String, MutableInt>();
    topk = new TreeMap<String, MutableInt>();

  }
  /////////////////////// getter and setter///////////////////


  public int getPercentDistinct() {
    return percentDistinct;
  }

  public long getPercentNull() {
    return percentNull;
  }

  public String getFirstString() {
    return firstString;
  }

  public String getLasttString() {
    return lasttString;
  }

  public String getColumnName() {
    return ColumnName;
  }

  public void setColumnName(String columnName) {
    ColumnName = columnName;
  }

  public String getDataType() {
    return DataType;
  }

  public void setDataType(String dataType) {
    DataType = dataType;
  }

  public String getLongestString() {
    return LongestString;
  }

  public void setLongestString(String longestString) {
    LongestString = longestString;
  }

  public String getShortestString() {
    return ShortestString;
  }

  public void setShortestString(String shortestString) {
    ShortestString = shortestString;
  }

  public Double getMax() {
    return Max;
  }

  public void setMax(Double max) {
    Max = max;
  }

  public Double getMin() {
    return Min;
  }

  public void setMin(Double min) {
    Min = min;
  }

  public Double getSum() {
    return Sum;
  }

  public void setSum(Double sum) {
    Sum = sum;
  }

  public long getNumofNull() {
    return NumNull;
  }

  public void setNumofNull(long numofNull) {
    NumNull = numofNull;
  }

  public String getDefaultvalue() {
    return Defaultvalue;
  }

  public void setDefaultvalue(String defaultvalue) {
    Defaultvalue = defaultvalue;
  }

  public ArrayList<String> getDistinctValues() {
    return new ArrayList<String>(freq.keySet());
  }

  public TreeMap<String, MutableInt> getTopkValues() {
    if (topk != null && topk.size() > 0)
      return topk;
    return null;
  }

  public long getNumNull() {
    return NumNull;
  }

  public void setNumNull(long numNull) {
    NumNull = numNull;
  }

  public int getNumDistinct() {
    return NumDistinct;
  }

  public String getSemantictype() {
    return Semantictype;
  }

  public void setSemantictype(String semantictype) {
    Semantictype = semantictype;
  }

  public double getAvg() {
    return Avg;
  }


  public void setAvg(double avg) {
    Avg = avg;
  }


  public double getStdDev() {
    return stdDev;
  }


  public void setStdDev(double stdDev) {
    this.stdDev = stdDev;
  }



  public void setPercentDistinct(int percentDistinct) {
    this.percentDistinct = percentDistinct;
  }


  public void setPercentNull(long percentNull) {
    this.percentNull = percentNull;
  }


  public void setNumDistinct(int numDistinct) {
    NumDistinct = numDistinct;
  }


  public void setFirstString(String firstString) {
    this.firstString = firstString;
  }


  public void setLasttString(String lasttString) {
    this.lasttString = lasttString;
  }


  public void setMax(double max) {
    Max = max;
  }


  public void setMin(double min) {
    Min = min;
  }


  public void setSum(double sum) {
    Sum = sum;
  }


  //////////////////////////////////// Special setter for calculated values from others

  public void setCalculatedFields(int numoftuples) {
    if (numoftuples != 0 && freq != null) {
      NumDistinct = freq.size();
      percentDistinct = NumDistinct * 100 / numoftuples;
      percentNull = NumNull * 100 / numoftuples;

      // for strings
      if (DataType == DataTypes.mySTRING) {
        firstString = freq.firstKey().toString();
        lasttString = freq.lastKey().toString();
        if(LongestString.length()>255)
          DataType=DataTypes.myTEXT;
        else if( DataType==DataTypes.mySTRING && 
            LongestString.length()==ShortestString.length() &&
            NumNull==0 &&
            DataTypes.isUUID(LongestString)&&
            DataTypes.isUUID(ShortestString)&&
            DataTypes.isUUID(firstString)&&
            DataTypes.isUUID(lasttString)
            )
        {DataType=DataTypes.myUUID;}
      }
      // for numbers
      if (DataTypes.isNumeric(DataType))
        Avg = Sum / numoftuples;

      // all
      freq = (TreeMap<String, MutableInt>) Util.sortByValues(freq);
      if (DataType==DataTypes.myINTEGER && NumDistinct==numoftuples && Min>0 && Max<=2147483647)
      {
        Iterator it = freq.entrySet().iterator();
        MutableInt prev=new MutableInt(),current=new MutableInt();
        if(it.hasNext())
          {Map.Entry pair = (Map.Entry)it.next();
          prev=(MutableInt)pair.getValue();
          }
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            current=(MutableInt)pair.getValue();
            if(prev.getMutableInt()-current.getMutableInt()!=1)
              break;
                    }
        
      }
      topk =
          (TreeMap<String, MutableInt>) Util.getTopK(freq, SingleColumnProfilerAlgorithm.Numoftopk);

    }
  }

  public void addValueforfreq(String newvalue) {
    if (newvalue != null) {
      MutableInt count = freq.get(newvalue);
      if (count == null) {
        freq.put(newvalue, new MutableInt());
      } else {
        count.increment();
      }
    }
  }

  // public void addValueforlengdist(int newvalue)
  // {
  // if(lengthdist==null)
  // lengthdist=new TreeMap<String,MutableInt>();
  // MutableInt count = lengthdist.get(newvalue+"");
  // if (count == null) {
  // lengthdist.put(newvalue+"", new MutableInt());
  // } else {
  // count.increment();
  // }
  // }

  public void increaseNumNull() {
    NumNull = NumNull + 1;
  }

  public void updateColumnProfile(String newvalue) {

    if (!DataTypes.isSameDataType(DataType, newvalue)) {
      String newtype = DataTypes.getDataType(newvalue);
      if (newtype != DataTypes.UNKOWN) {
        DataType = newtype;
      }
    }
    if (newvalue != null) { // update strings
      if (LongestString == null)
        LongestString = newvalue;
      else
        LongestString = (LongestString.length() > newvalue.length()) ? LongestString : newvalue;
      if (ShortestString == null)
        ShortestString = newvalue;
      else if (newvalue != " " && !newvalue.isEmpty())
        ShortestString = (ShortestString.length() < newvalue.length()) ? ShortestString : newvalue;

      // update numbers
      if (DataTypes.isNumeric(DataType)) {
        Max = Util.getmaxnumber(newvalue, getMax());
        Min = Util.getminnumber(newvalue, getMin());
        Sum += Util.getnumberfromstring(newvalue);
      }
      // frequency
      addValueforfreq(newvalue);

      //////////////////////////////////////
      ///// update others
      ///////////////////////////////////////
    }


  }

  public void updateColumnProfilesecondpass(String newvalue) { // string values
    if (DataType == DataTypes.mySTRING || DataType == DataTypes.myTEXT) { 
      // length distribution
      // addValueforlengdist(newvalue.length());

      // Verifies same semantic type
      if (getSemantictype() == null)
        Semantictype = DataTypes.getSemanticDataType(newvalue);
      else if (!DataTypes.isSameSemanticType(DataType, newvalue)) {
        Semantictype = DataTypes.UNKOWN;
      }
    }

    if (DataTypes.isNumeric(DataType))
      stdDev +=
          (Avg - Util.getnumberfromstring(newvalue)) * (Avg - Util.getnumberfromstring(newvalue));

  }

  public TreeMap<String, MutableInt> getFreq() {
    return freq;
  }


  public void setFreq(TreeMap<String, MutableInt> freq) {
    this.freq = freq;
  }


  // public TreeMap<String, MutableInt> getLengthdist() {
  // return lengthdist;
  // }
  //
  //
  // public void setLengthdist(TreeMap<String, MutableInt> lengthdist) {
  // this.lengthdist = lengthdist;
  // }


  @Override
  public String toString() {
    return "ColumnMainProfile [ColumnName=" + ColumnName + ", DataType=" + DataType + ", NumNull="
        + NumNull + ", Defaultvalue=" + Defaultvalue + ", percentDistinct=" + percentDistinct
        + ", percentNull=" + percentNull + ", NumDistinct=" + NumDistinct + ", freq=" + freq
        + ", topk=" + topk + ", LongestString=" + LongestString + ", ShortestString="
        + ShortestString + ", firstString=" + firstString + ", lasttString=" + lasttString
        + ", Semantictype=" + Semantictype + ", Max=" + Max + ", Min=" + Min + ", Sum=" + Sum
        + ", Avg=" + Avg + ", stdDev=" + stdDev + "]";
  }

  public void writeMapToCsv(String path, String relation, String columnname) throws Exception {
    if (freq != null) {
      File file = new File(path + "value_" + relation + "_" + columnname);
      File folder = file.getParentFile();

      if (!folder.exists()) {
        folder.mkdirs();
        while (!folder.exists()) {
        }
      }

      FileWriter fileWriter = new FileWriter(path + "value_" + relation + "_" + columnname);

      for (Map.Entry<String, MutableInt> entry : freq.entrySet()) {
        fileWriter.append(entry.getKey().replace(',', ' '));
        fileWriter.append(COMMA_DELIMITER);
        fileWriter.append(String.valueOf(entry.getValue()));
        fileWriter.append(NEW_LINE_SEPARATOR);
      }

      fileWriter.close();
    }
    // if (lengthdist!=null)
    // {FileWriter fileWriter = new FileWriter("stringlength_"+relation+"_"+columnname);
    //
    // for (Map.Entry<String,MutableInt> entry : lengthdist.entrySet()){
    // fileWriter.append(entry.getKey());
    // fileWriter.append(COMMA_DELIMITER);
    // fileWriter.append(String.valueOf(entry.getValue()));
    // fileWriter.append(NEW_LINE_SEPARATOR);
    //
    // }
    // fileWriter.close();
    // }


  }

  private class MutableInt implements Comparable<MutableInt> {
    int value = 1;

    public void increment() {
      ++value;
    }

    public int getMutableInt() {
      return value;
    }

    @Override
    public String toString() {
      return value + "";
    }

    public int compareTo(MutableInt arg0) {
      if (value == arg0.getMutableInt())
        return 0;
      else if (value > arg0.getMutableInt())
        return 1;
      else
        return -1;
    }


  }
}
