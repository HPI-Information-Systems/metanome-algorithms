package de.metanome.algorithms.cody.codycore;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import lombok.Data;

@Data
public class Configuration {

    @Parameter(names = {"--path", "-p"}, description = "Relative Path to the CSV file containing the dataset",
            required = true)
    String path;

    @Parameter(names = {"--del", "-d"}, description = "Delimiter used in the dataset", converter =
            StringToCharConverter.class)
    char delimiter = ',';

    @Parameter(names = {"--skip"}, description = "Number of lines to skip when reading the dataset")
    int skipLines = 0;

    @Parameter(names = {"--row-limit"}, description = "Limit number of lines read from dataset")
    int rowLimit = -1;

    @Parameter(names = {"--col-limit"}, description = "Limit number of columns read from dataset")
    int colLimit = -1;

    @Parameter(names = {"--no-header"}, description = "First line already contains data, column names are indices")
    boolean noHeader = false;

    @Parameter(names = {"--null"}, description = "Custom null value in dataset")
    String nullValue = "";

    @Parameter(names = {"--quote"}, description = "Custom quote char in dataset", converter =
            StringToCharConverter.class)
    char quoteChar = '"';

    @Parameter(names = {"--supp", "-s"}, description = "Minimum support, set to 1.0 for exact Cody search")
    double minSupport = 1.0;

    @Parameter(names = {"--no-cliques"}, description = "Disable clique search for approximate Cody discovery")
    boolean noCliqueSearch = false;

    @Parameter(names = {"--help", "-h"}, description = "Show this help page", help = true)
    boolean help;

    public static class StringToCharConverter implements IStringConverter<Character> {
        @Override
        public Character convert(String str) {
            if (str.isEmpty())
                return '\0';

            if (str.startsWith("\\")) {
                switch (str) {
                    case "\\t":
                        return '\t';
                    case "\\0":
                        return '\0';
                    case "\\n":
                        return '\n';
                }
            }

            return str.charAt(0);
        }
    }
}
