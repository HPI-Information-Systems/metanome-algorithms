package de.metanome.algorithms.many.filter;

import java.util.Set;

public class ColumnFilter {

    public static final ColumnFilter INSTANCE = new ColumnFilter();

    public boolean filterColumn(Set<String> column) {

        // ii
        if (column.size() < 3) {
            return true;
        }

        // null values
        if (column.contains(null)) {
            return true;
        }

        // iii
        double avgLength = 0;

        for (String value : column) {
            if (value != null)
                avgLength += value.length();
        }

        if (avgLength / column.size() < 3) {
            return true;
        }

        // i
        double intPercenctages = 0;
        for (String value : column) {
            intPercenctages += numberPercenctage(value);
        }
        if (intPercenctages / column.size() > 0.8) {
            return true;
        }
        return false;
    }

    private static double numberPercenctage(String str) {
        if (str != null) {
            str = str.replaceAll("(?<=\\d)(rd)|(st)|(nd)|(th)\\b", "");
            double numbers = 0;
            int i = 0;
            for (; i < str.length(); i++) {
                char c = str.charAt(i);
                if (c >= '0' && c <= '9') {
                    numbers++;
                }
            }
            return numbers / str.length();
        }
        return 0.0;
    }
}
