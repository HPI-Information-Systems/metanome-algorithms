package de.metanome.algorithms.singlecolumnprofiler;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import org.json.JSONObject;
import org.json.JSONArray;
import javax.mail.internet.InternetAddress;

public class DataTypes {
  // Data types : http://www.w3schools.com/sql/sql_datatypes_general.asp
  // -----------------------Alphanumeric or Alphabetic-------------------------
  // CHARACTER(n) Character string. Fixed-length n (string)
  final static public String mySTRING = "VARCHAR";

  // VARCHAR(n) or CHARACTER VARYING(n) Character string. Variable length. Maximum length n (string)
  // LONGVARCHAR String Variable-length character string. (string)
  // -----------------------Binary-------------------------------------------------------
  // BIT Stores TRUE or FALSE values (Boolean)
  final static public String myBOOLEAN = "BOOLEAN";
  final static public String myCHAR = "CHAR";
  // BINARY(n) Binary string. Fixed-length n (byte[])
  // VARBINARY(n) or BINARY VARYING(n) Binary string. Variable length. Maximum length n (byte[])
  // LONGVARBINARY Variable-length character string. (byte[])
  // ----------------------Numeric-------------------------------------------------------
  // SMALLINT Integer numerical (no decimal). Precision 5 (short)
  final static public String mySHORT = "SMALLINT";
  // INTEGER Integer numerical (no decimal). Precision 10 (int)
  final static public String myINTEGER = "INT";
  // BIGINT Integer numerical (no decimal). Precision 19 (long)
  final static public String myLONG = "BIGINT";
  // DECIMAL(p,s) Exact numerical, precision p, scale s. Example: decimal(5,2) is a number that has
  // 3 digits before the decimal and 2 digits after the decimal (java.math.BigDecimal)
  final static public String myBIGDECIMAL = "DECIMAL";
  // REAL Approximate numerical, mantissa precision 7 (float)
  final static public String myFLOAT = "REAL";
  // FLOAT Approximate numerical, mantissa precision 16 (double)
  final static public String myDOUBLE = "FLOAT";

  // DOUBLE PRECISION Approximate numerical, mantissa precision 16 (double)
  // NUMERIC(p,s) Exact numerical, precision p, scale s. (Same as DECIMAL) (java.math.BigDecimal)
  // FLOAT(p) Approximate numerical, mantissa precision p. A floating number in base 10 exponential
  // notation. The size argument for this type consists of a single number specifying the minimum
  // precision
  // --------------------Temporal------------------------------------------
  // DATE Stores year, month, and day values (java.sql.Date)
  final static public String myDATE = "DATE";
  // TIME Stores hour, minute, and second values (java.sql.Time)
  final static public String myTIME = "TIME";
  // TIMESTAMP Stores year, month, day, hour, minute, and second values (java.sql.Timestamp)
  final static public String myTIMESTAMP = "TIMESTAMP";

  // INTERVAL Composed of a number of integer fields, representing a period of time, depending on
  // the type of interval
  // ----------------others not used--------------------------------------------
  // ARRAY A set-length and ordered collection of elements
  // MULTISET A variable-length and unordered collection of elements
  // XML Stores XML data
  // binary object
  // Currency
  final static public String UNKOWN = "NA";
 //postgre specific 
 //longer than 255
  final static public String myTEXT="TEXT";
 //varchar with length=36 not null 
  final static public String myUUID="UUID";
 //int 
  final static public String mySERIAL="SERIAL";
  // semantic data types
  final static public String Jsontype = "JSON";
  final static public String XMLtype = "XML";
  final static public String URLtype = "URL";
  final static public String Emailtype = "Email";


  // formats:https://publib.boulder.ibm.com/iseries/v5r2/ic2924/index.htm?info/db2/rbafzmstdtstrng.htm
  private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() {

    private static final long serialVersionUID = 3403956410277087060L;

    {
      // International Standards Organization (*ISO) yyyy-mm-dd
      // Japanese industrial standard Christian era (*JIS)
      put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
      put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
      // IBM USA standard (*USA) mm/dd/yyyy
      put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
      put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
      // IBM European standard (*EUR) dd.mm.yyyy
      put("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}$", "MM.dd.yyyy");
      // with name of month
      put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
      put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
      // Month, day, year (*MDY) mm/dd/yy
      put("^\\d{1,2}/\\d{1,2}/\\d{2}$", "MM/dd/yy");
      // Day, month, year (*DMY) dd/mm/yy
      put("^\\d{1,2}/\\d{1,2}/\\d{2}$", "dd/mm/yy");
      // Year, month, day (*YMD) yy/mm/dd
      put("^\\d{2}/\\d{1,2}/\\d{1,2}$", "yy/mm/dd");
    }
  };

  private static final Map<String, String> Time_FORMAT_REGEXPS = new HashMap<String, String>() {
    private static final long serialVersionUID = 1025905562879271083L;

    {
      // International Standards Organization (*ISO)
      // IBM European standard (*EUR) hh.mm.ss
      put("^\\d{1,2}\\.\\d{2}\\.\\d{2}$", "HH.mm.ss");
      // Japanese industrial standard Christian era (*JIS)
      // Hours, minutes, seconds (*HMS) hh:mm:ss
      put("^\\d{1,2}:\\d{2}:\\d{2}$", "HH:mm:ss");
      // IBM USA standard (*USA) hh:mm AM or PM
      put("^\\d{1,2}:\\d{2}\\sAM|PM|pm|am$", "HH:mm");

    }
  };

  private static final Map<String, String> TimeStamp_FORMAT_REGEXPS =
      new HashMap<String, String>() {
        /**
         * 
         */
        private static final long serialVersionUID = 4243311933781674070L;

        {
          put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
          put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
          put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm");
          put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
          put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
          put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
          put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
          put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
          put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
          put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.?(\\d)*\\+\\d{2}$",
              "yyyy-MM-dd HH:mm:ss");
          put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss");
          put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
          put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
          put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");

        }
      };

  public static boolean isBoolean(String input) {
    if (input != null && !input.isEmpty() && input.equals("f") || input.equals("t")
        || input.equals("true") || input.equals("false") || input.equals("yes")
        || input.equals("no"))
      return true;
    else
      return false;
  }

  public static boolean isChar(String input) {
    if (input != null && !input.isEmpty() && input.length() == 1)
      return true;
    else
      return false;
  }

  public static boolean isShort(String input) {
    try {
      Short.parseShort(input);
      return true;
    } catch (NumberFormatException ex) {
      return false;
    }
  }

  public static boolean isInteger(String input) {
    try {
      Integer.parseInt(input);
      return true;
    } catch (NumberFormatException ex) {
      return false;
    }
  }

  public static boolean isLong(String input) {
    try {
      Long.parseLong(input);
      return true;
    } catch (NumberFormatException ex) {
      return false;
    }
  }

  public static boolean isUUID(String input) {
    try {
      UUID.fromString(input);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }
  public static boolean isDecimal(String input) {
    try {
      DecimalFormatSymbols symbols = new DecimalFormatSymbols();
      symbols.setGroupingSeparator(',');
      symbols.setDecimalSeparator('.');
      String pattern = "#,##0.0#";
      DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
      BigDecimal bigDecimal = (BigDecimal) decimalFormat.parse(input);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  public static boolean isFlaot(String input) {
    try {
      Float.parseFloat(input);
      return true;
    } catch (NumberFormatException ex) {
      return false;
    }
  }

  public static boolean isDouble(String input) {
    try {
      Double.parseDouble(input);
      return true;
    } catch (NumberFormatException ex) {
      return false;
    }
  }

  public static boolean isDate(String input) {
    String dateFormat = null;
    if (input != null && !input.isEmpty()) {
      for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {
        if (input.toLowerCase().matches(regexp))
          dateFormat = DATE_FORMAT_REGEXPS.get(regexp);
      }
      if (dateFormat == null) {
        return false;
      } else
        return true;
    } else
      return false;
  }

  public static boolean isTime(String input) {
    String dateFormat = null;
    if (input != null && !input.isEmpty()) {
      for (String regexp : Time_FORMAT_REGEXPS.keySet()) {
        if (input.toLowerCase().matches(regexp))
          dateFormat = Time_FORMAT_REGEXPS.get(regexp);
      }
      if (dateFormat == null) {
        return false;
      } else
        return true;
    } else
      return false;
  }

  public static boolean isTimeStamp(String input) {
    String dateFormat = null;
    if (input != null && !input.isEmpty()) {
      for (String regexp : TimeStamp_FORMAT_REGEXPS.keySet()) {
        if (input.toLowerCase().matches(regexp))
          dateFormat = TimeStamp_FORMAT_REGEXPS.get(regexp);
      }
      if (dateFormat == null) {
        return false;
      } else
        return true;
    } else
      return false;
  }

  public static String getDataType(String value) {
    if (value == null || value.isEmpty() || value.equals(" "))
      return DataTypes.UNKOWN;
    else if (isBoolean(value))
      return DataTypes.myBOOLEAN;
//    else if (isChar(value))
//      return DataTypes.myCHAR;
    else if (!Util.isContainingNumbers(value))
      return DataTypes.mySTRING;
    else if (isShort(value))
      return DataTypes.mySHORT;
    else if (isInteger(value))
      return DataTypes.myINTEGER;
    else if (isLong(value))
      return DataTypes.myLONG;
    else if (isDecimal(value))
      return DataTypes.myBIGDECIMAL;
    else if (isFlaot(value))
      return DataTypes.myFLOAT;
    else if (isDouble(value))
      return DataTypes.myDOUBLE;
    else if (isDate(value))
      return DataTypes.myDATE;
    else if (isTime(value))
      return DataTypes.myTIME;
    else if (isTimeStamp(value))
      return DataTypes.myTIMESTAMP;
    return DataTypes.mySTRING;
  }

  public static boolean isSameDataType(String currrentdatatype, String newvalue) {
    switch (currrentdatatype) {
      case UNKOWN:
        return false;
      case myBOOLEAN:
        return isBoolean(newvalue);
      case myCHAR:
        return isChar(newvalue);
      case myDATE:
        return isDate(newvalue);
      case myTIME:
        return isTime(newvalue);
      case myTIMESTAMP:
        return isTimeStamp(newvalue);
      case mySHORT:
        return isShort(newvalue);
      case myINTEGER:
        return isInteger(newvalue);
      case myLONG:
        return isLong(newvalue);
      case myBIGDECIMAL:
        return isDecimal(newvalue);
      case myFLOAT:
        return isFlaot(newvalue);
      case myDOUBLE:
        return isDouble(newvalue);
      case mySTRING:
        return true;
    }
    return false;
  }

  public static boolean isSameSemanticType(String currrentdatatype, String newvalue) {
    switch (currrentdatatype) {
      case UNKOWN:
        return false;
      case URLtype:
        return isURL(newvalue);
      case Emailtype:
        return isEmail(newvalue);
      case Jsontype:
        return isJSON(newvalue);
      case XMLtype:
        return isXML(newvalue);
    }
    return false;
  }

  public static boolean isNumeric(String DataType) {
    switch (DataType) {
      case mySHORT:
      case myINTEGER:
      case myLONG:
      case myBIGDECIMAL:
      case myFLOAT:
      case myDOUBLE:
        return true;
      default:
        return false;
    }
  }

  /////////////////////////////// semantic data types////////////////////////////
  public static boolean isXML(String inXMLStr) {

    boolean retBool = false;
    Pattern pattern;
    Matcher matcher;
    final String XML_PATTERN_STR = "<(\\S+?)(.*?)>(.*?)</\\1>";
    if (inXMLStr != null && inXMLStr.trim().length() > 0) {
      if (inXMLStr.trim().startsWith("<")) {
        pattern = Pattern.compile(XML_PATTERN_STR,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
        // RETURN TRUE IF IT HAS PASSED BOTH TESTS
        matcher = pattern.matcher(inXMLStr);
        retBool = matcher.matches();
      }
    }

    return retBool;
  }

  public static boolean isJSON(String test) {
    if (test != null && test.trim().length() > 0) {
      try {
        new JSONObject(test);
      } catch (Exception ex) {
        try {
          new JSONArray(test);
        } catch (Exception ex1) {
          return false;
        }
      }
      return true;
    }

    return false;
  }

  public static boolean isEmail(String email) {

    try {

      // Create InternetAddress object and validated the email address.

      InternetAddress internetAddress = new InternetAddress(email);

      internetAddress.validate();


    } catch (Exception e) {

      return false;

    }

    return true;
  }

  public static boolean isURL(String URL) {

    try {

      new URL(URL);


    } catch (Exception e) {

      return false;

    }

    return true;
  }

  public static String getSemanticDataType(String value) {
    if (value == null || value.isEmpty() || value.equals(" "))
      return DataTypes.UNKOWN;
    else if (isXML(value))
      return XMLtype;
    else if (isJSON(value))
      return Jsontype;
    else if (isEmail(value))
      return Emailtype;
    else if (isURL(value))
      return URLtype;
    return UNKOWN;
  }
}
