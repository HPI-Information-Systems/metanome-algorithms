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
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import de.metanome.algorithm_integration.input.InputIterationException;

public class TestCsvFileFixture {

  protected static final char QUOTE_CHAR = '"';
  protected static final char SEPARATOR = ',';
  protected static final char ESCAPE = '\\';
  protected static final boolean STRICT_QUOTES = false;
  protected static final boolean IGNORE_LEADING_WHITESPACES = true;
  protected static final boolean HAS_HEADER = false;

  protected TestFileFixture fileFixture;

  public TestCsvFileFixture() {
    this.fileFixture = new TestFileFixture(this.getCsvFileData());
  }

  public File getTestDataPath(final String fileName) throws FileNotFoundException,
      UnsupportedEncodingException {
    return this.fileFixture.getTestData(fileName);
  }

  protected String getCsvFileData() {
    final String s =
        Joiner.on(SEPARATOR).join(this.quoteStrings(this.expectedFirstLine()))
            + System.getProperty("line.separator")
            + Joiner.on(SEPARATOR).join(this.quoteStrings(this.expectedSecondLine()))
            + System.getProperty("line.separator")
            + Joiner.on(SEPARATOR).join(this.quoteStrings(this.expectedThirdLine()));
    return s;
  }

  public TestFileIterator getTestData(final boolean skipDifferingLines)
      throws InputIterationException {
    return new TestFileIterator("some relation", new StringReader(this.getCsvFileData()),
        SEPARATOR, QUOTE_CHAR, ESCAPE, 0, STRICT_QUOTES, IGNORE_LEADING_WHITESPACES, HAS_HEADER,
        skipDifferingLines);
  }

  protected List<String> quoteStrings(final List<String> unquotedStrings) {
    final List<String> quotedStrings = new LinkedList<>();

    for (final String unquotedString : unquotedStrings) {
      quotedStrings.add(QUOTE_CHAR + unquotedString + QUOTE_CHAR);
    }

    return quotedStrings;
  }

  public ImmutableList<String> expectedFirstLine() {
    return ImmutableList.of("3", "10", "5", "2012-01-30", "2012-03-05", "2014-03-05");
  }

  public ImmutableList<String> expectedSecondLine() {
    return ImmutableList.of("6", "7", "5", "2012-02-05", "2012-01-05", "2014-03-05");
  }

  public ImmutableList<String> expectedThirdLine() {
    return ImmutableList.of("6", "10", "11", "2012-02-05", "2012-03-05", "2017-03-05");
  }

}
