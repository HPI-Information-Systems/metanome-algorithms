/*
 * Copyright 2014 by the Metanome project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package de.metanome.algorithms.order.sorting.partitions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.configuration.ConfigurationSettingFileInput;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;

/**
 * Generator for {@link de.metanome.algorithm_integration.input.RelationalInput}s based on csv
 * files.
 *
 * @author Jakob Zwiener
 */
public class TestFileInputGenerator implements FileInputGenerator {

  protected File inputFile;
  protected char separator = CSVParser.DEFAULT_SEPARATOR;
  protected char quotechar = CSVParser.DEFAULT_QUOTE_CHARACTER;
  protected char escape = CSVParser.DEFAULT_ESCAPE_CHARACTER;
  protected int skipLines = CSVReader.DEFAULT_SKIP_LINES;
  protected boolean strictQuotes = CSVParser.DEFAULT_STRICT_QUOTES;
  protected boolean ignoreLeadingWhiteSpace = CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE;
  protected boolean hasHeader = TestFileIterator.DEFAULT_HAS_HEADER;
  protected boolean skipDifferingLines = TestFileIterator.DEFAULT_SKIP_DIFFERING_LINES;

  /**
   * @param inputFile the csv input
   */
  public TestFileInputGenerator(final File inputFile) throws FileNotFoundException {
    this.setInputFile(inputFile);
  }

  /**
   * @param inputFile the csv input
   * @param separator cell separator
   * @param quotechar cell quote character
   * @param escape escape character
   * @param skipLines number of lines to skip
   * @param strictQuotes sets if characters outside the quotes are ignored
   * @param ignoreLeadingWhiteSpace it true, parser should ignore white space before a quote in a
   *        field
   * @param hasHeader set if the csv has a header
   * @param skipDifferingLines set if the csv file should skip lines with differing length
   */
  public TestFileInputGenerator(final File inputFile, final char separator, final char quotechar,
      final char escape, final int skipLines, final boolean strictQuotes,
      final boolean ignoreLeadingWhiteSpace, final boolean hasHeader,
      final boolean skipDifferingLines) throws FileNotFoundException {
    this.setInputFile(inputFile);
    this.separator = separator;
    this.quotechar = quotechar;
    this.escape = escape;
    this.skipLines = skipLines;
    this.strictQuotes = strictQuotes;
    this.ignoreLeadingWhiteSpace = ignoreLeadingWhiteSpace;
    this.hasHeader = hasHeader;
    this.skipDifferingLines = skipDifferingLines;
  }

  /**
   * @param setting the settings to construct new
   *        {@link de.metanome.algorithm_integration.input.RelationalInput}s with
   * @throws AlgorithmConfigurationException thrown if the file cannot be found
   */
  public TestFileInputGenerator(final ConfigurationSettingFileInput setting)
      throws AlgorithmConfigurationException {
    try {
      this.setInputFile(new File(setting.getFileName()));
    } catch (final FileNotFoundException e) {
      throw new AlgorithmConfigurationException("File could not be found.", e);
    }
    this.separator = setting.getSeparatorChar().toCharArray()[0];
    this.quotechar = setting.getQuoteChar().toCharArray()[0];
    this.escape = setting.getEscapeChar().toCharArray()[0];
    this.skipLines = setting.getSkipLines();
    this.strictQuotes = setting.isStrictQuotes();
    this.ignoreLeadingWhiteSpace = setting.isIgnoreLeadingWhiteSpace();
    this.hasHeader = setting.hasHeader();
    this.skipDifferingLines = setting.isSkipDifferingLines();
  }

  @Override
  public RelationalInput generateNewCopy() throws InputGenerationException {
    try {
      return new TestFileIterator(this.inputFile.getName(), new FileReader(this.inputFile),
          this.separator, this.quotechar, this.escape, this.skipLines, this.strictQuotes,
          this.ignoreLeadingWhiteSpace, this.hasHeader, this.skipDifferingLines);
    } catch (final FileNotFoundException e) {
      throw new InputGenerationException("File not found.", e.getCause());
    } catch (final InputIterationException e) {
      throw new InputGenerationException("Could not iterate over the first line of the csv file.",
          e.getCause());
    }
  }

  /**
   * @return inputFile
   */
  @Override
  public File getInputFile() {
    return this.inputFile;
  }

  protected void setInputFile(final File inputFile) throws FileNotFoundException {
    if (!inputFile.isFile()) {
      throw new FileNotFoundException();
    }
    this.inputFile = inputFile;
  }
}
