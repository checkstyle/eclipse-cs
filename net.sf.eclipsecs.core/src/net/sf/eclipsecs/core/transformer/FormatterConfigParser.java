//============================================================================
//
// Copyright (C) 2009 Lukas Frena
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package net.sf.eclipsecs.core.transformer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Class for parsing a eclipse-formatter-configuration-file for formatter-settings.
 * 
 * @author Lukas Frena
 */
public class FormatterConfigParser {
  /** A FormatterConfiguration with all rules that will be found. */
  private final FormatterConfiguration mRules = new FormatterConfiguration();

  /** The stream to the configuration-file of the eclipse-formatter. */
  private final BufferedReader mReader;

  /**
   * Creates a new Instance of Class FormatterConfigParser.
   * 
   * @param configLocation
   *          The configuration-file of eclipse formatter.
   * @throws FileNotFoundException
   *           Gets thrown if config-file can't be found.
   */
  public FormatterConfigParser(final String configLocation) throws FileNotFoundException {

    final FileReader fin = new FileReader(configLocation);
    mReader = new BufferedReader(fin);
  }

  /**
   * Method for starting parsing for formatter-rules.
   * 
   * @return The FormatterConfiguration of formatter-rules found.
   */
  public FormatterConfiguration parseRules() {
    if (mReader == null) {
      return null;
    }

    String line = null;
    String[] tokens = null;

    try {
      while ((line = mReader.readLine()) != null) {
        if (line.startsWith("org.eclipse.jdt.core.formatter.")) {
          tokens = line.split("=");
          mRules.addFormatterSetting(tokens[0], tokens[1]);
        }
      }
    } catch (final IOException e) {
      return null;
    }

    return mRules;
  }
}
