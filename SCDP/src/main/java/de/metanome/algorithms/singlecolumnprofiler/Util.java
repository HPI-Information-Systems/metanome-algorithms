package de.metanome.algorithms.singlecolumnprofiler;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

public class Util {
  public static double getmaxnumber(String newvalue, double oldvale) {
    try {
      double newdouble = Double.parseDouble(newvalue);
      if (newdouble > oldvale)
        return newdouble;
      else
        return oldvale;
    } catch (NumberFormatException ex) {
      return oldvale;
    }
  }

  public static double getminnumber(String newvalue, double oldvale) {
    try {
      double newdouble = Double.parseDouble(newvalue);
      if (newdouble < oldvale)
        return newdouble;
      else
        return oldvale;
    } catch (NumberFormatException ex) {
      return oldvale;
    }
  }

  public static double getnumberfromstring(String newvalue) {
    try {
      double newdouble = Double.parseDouble(newvalue);
      return newdouble;
    } catch (NumberFormatException ex) {
      return 0;
    }
  }

  public static long roundUp(long n, long m) {
    return n >= 0 ? ((n + m - 1) / m) * m : (n / m) * m;
  }

  public static void printlist(List<String> A) {
    System.out.print("\n");
    for (int i = 0; i < A.size(); i++) {

      System.out.print(A.get(i) + " | ");
    }
  }

  public static String listtoString(List<String> A) {
    String result = "";
    for (int i = 0; i < A.size(); i++) {

      result += A.get(i) + " | ";
    }
    result += "\n";
    return result;
  }

  public static boolean isContainingNumbers(String input) {
    String regex = "(.)*(\\d)(.)*";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(input);
    if (input != null && !input.isEmpty() && matcher.matches())
      return true;
    else
      return false;
  }

  public static <K, V extends Comparable<V>> SortedMap<K, V> sortByValues(final Map<K, V> map) {
    Comparator<K> valueComparator = new Comparator<K>() {
      public int compare(K k1, K k2) {
        int compare = map.get(k2).compareTo(map.get(k1));
        if (compare == 0)
          return 1;
        else
          return compare;
      }
    };

    SortedMap<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
    sortedByValues.putAll(map);
    return sortedByValues;
  }

  public static <K, V extends Comparable<V>> Map<K, V> getTopK(final Map<K, V> map, int k) {
    int count = 1;
    TreeMap<K, V> target = new TreeMap<K, V>();
    Map<K, V> sortedmap=sortByValues(map);
    for (Map.Entry<K, V> entry : sortedmap.entrySet()) {
      if (count >= k)
        break;

      target.put(entry.getKey(), entry.getValue());
      count++;
    }
    return sortByValues(target);



  }

  public static void printprofilelist(List<ColumnMainProfile> A) {
    {
      System.out.print("\n");
      for (int i = 0; i < A.size(); i++) {

        System.out.print(A.get(i) + " | ");
      }
    }
  }


  public static JSONObject mapToJson(Map<?, ?> data) {
    JSONObject object = new JSONObject();

    for (Map.Entry<?, ?> entry : data.entrySet()) {

      String key = (String) entry.getKey();
      if (key == null) {
        throw new NullPointerException("key == null");
      }
      try {
        object.put(key, entry.getValue());
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    return object;
  }

  public static String mapTostring(Map<?, ?> data) {
    String object = "";

    for (Map.Entry<?, ?> entry : data.entrySet()) {
      String key = (String) entry.getKey();
      if (key == null) {
        throw new NullPointerException("key == null");
      }
      try {
        object+=key+"("+entry.getValue()+") ";
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    return object;
  }
  public static JSONObject mapToJsonIntegerKey(Map<?, ?> data) {
    JSONObject object = new JSONObject();

    for (Map.Entry<?, ?> entry : data.entrySet()) {

      Integer key = (Integer) entry.getKey();
      if (key == null) {
        throw new NullPointerException("key == null");
      }
      try {
        object.put(key.toString(), entry.getValue());
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    return object;
  }

}
