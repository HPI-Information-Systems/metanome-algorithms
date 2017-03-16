package de.metanome.algorithms.singlecolumnprofiler;


import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Objects;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;




public class ColumnMainProfile {
  // Delimiter used in CSV file
   private static final String COMMA_DELIMITER = ",";
   private static final String NEW_LINE_SEPARATOR = "\n";
  //////////////////////////////////////////////////
  // valid for all types
  private String ColumnName;
  private String DataType;
  private long NumNull;
 // private String Defaultvalue;// *******
  // %Distinct Percentage of distinct values
 // private int percentDistinct;// -------Calculated
  // %NULL Percentage of Null values
 //private long percentNull;// -------Calculated
  // #Distinct : the number of distinct values is the size of the hash tree
  //private int NumDistinct; // -------Calculated
  // top k value
  // column value distribution (int)
  Object2IntMap<String> freq;
 // TreeMap<String,Integer> topk;// ------calculated

  ////////////////////////////////////////////////////////
  // text
  private String LongestString;
  private String ShortestString;
  // max and min for strings are the first and list strings in the tree
  //private String firstString;// -------Calculated
 // private String lasttString;// -------Calculated
  private String Semantictype;// ------secondpass
  // column length distribution (text)
  // TreeMap<String, MutableInt> lengthdist; ; //------secondpass
  //////////////////////////////////////////////////
  // numbers
  private double Max;
  private double Min;
  private double Sum;
  //private double Avg;// ------calculated
  private double stdDev;// ------secondpass

  /////////////////// constructor////////////////////////////////

  public ColumnMainProfile(String ColumnName) {
    setColumnName(ColumnName);
    setMax(Double.MIN_VALUE);
    setMin(Double.MAX_VALUE);
    setLongestString("");
    setSum(0.0);
    setNumNull(0);
    setStdDev(0);
    freq = new Object2IntRBTreeMap<String>();
    freq.defaultReturnValue(0);
    //topk = new TreeMap<String,Integer>();

  }
  /////////////////////// getter and setter///////////////////


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

 

  public ArrayList<String> getDistinctValues() {
    return new ArrayList<String>(freq.keySet());
  }

 
  public long getNumNull() {
    return NumNull;
  }

  public void setNumNull(long numNull) {
    NumNull = numNull;
  }


  public String getSemantictype() {
    return Semantictype;
  }

  public void setSemantictype(String semantictype) {
    Semantictype = semantictype;
  }

  public double getStdDev() {
    return stdDev;
  }


  public void setStdDev(double stdDev) {
    this.stdDev = stdDev;
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

 
  public void addValueforfreq(String newvalue) {
    if (newvalue != null) {
    int oldvalue= freq.getInt(newvalue);
    freq.put(newvalue, oldvalue+1);
     // freq.addTo(newvalue, 1);
      
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
      if (!Objects.equals(newtype, DataTypes.UNKOWN)) {
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
      else if (!Objects.equals(newvalue, " ") && !newvalue.isEmpty())
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

  public void updateColumnProfilesecondpass(String newvalue, long numberoftuples) { // string values
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
          (Sum/numberoftuples - Util.getnumberfromstring(newvalue)) * (Sum/numberoftuples - Util.getnumberfromstring(newvalue));

  }

  public Object2IntMap<String> getFreq() {
    return freq;
  }


  public void setFreq( Object2IntRBTreeMap<String> freq) {
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

      for (Entry<String, Integer> entry : freq.entrySet()) {
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


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ColumnName == null) ? 0 : ColumnName.hashCode());
    result = prime * result + ((DataType == null) ? 0 : DataType.hashCode());
    result = prime * result + ((LongestString == null) ? 0 : LongestString.hashCode());
    long temp;
    temp = Double.doubleToLongBits(Max);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(Min);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (int) (NumNull ^ (NumNull >>> 32));
    result = prime * result + ((Semantictype == null) ? 0 : Semantictype.hashCode());
    result = prime * result + ((ShortestString == null) ? 0 : ShortestString.hashCode());
    temp = Double.doubleToLongBits(Sum);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((freq == null) ? 0 : freq.hashCode());
    temp = Double.doubleToLongBits(stdDev);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ColumnMainProfile other = (ColumnMainProfile) obj;
    if (ColumnName == null) {
      if (other.ColumnName != null)
        return false;
    } else if (!ColumnName.equals(other.ColumnName))
      return false;
    if (DataType == null) {
      if (other.DataType != null)
        return false;
    } else if (!DataType.equals(other.DataType))
      return false;
    if (LongestString == null) {
      if (other.LongestString != null)
        return false;
    } else if (!LongestString.equals(other.LongestString))
      return false;
    if (Double.doubleToLongBits(Max) != Double.doubleToLongBits(other.Max))
      return false;
    if (Double.doubleToLongBits(Min) != Double.doubleToLongBits(other.Min))
      return false;
    if (NumNull != other.NumNull)
      return false;
    if (Semantictype == null) {
      if (other.Semantictype != null)
        return false;
    } else if (!Semantictype.equals(other.Semantictype))
      return false;
    if (ShortestString == null) {
      if (other.ShortestString != null)
        return false;
    } else if (!ShortestString.equals(other.ShortestString))
      return false;
    if (Double.doubleToLongBits(Sum) != Double.doubleToLongBits(other.Sum))
      return false;
    if (freq == null) {
      if (other.freq != null)
        return false;
    } else if (!freq.equals(other.freq))
      return false;
    if (Double.doubleToLongBits(stdDev) != Double.doubleToLongBits(other.stdDev))
      return false;
    return true;
  }

 }
