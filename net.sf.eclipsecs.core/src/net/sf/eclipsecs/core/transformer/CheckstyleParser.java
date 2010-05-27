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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

/**
 * Class for parsing a checkstyle xml-file for checkstyle-rules.
 * 
 * @author Lukas Frena
 */
public class CheckstyleParser {
    /** The list of found checkstyle-rules. */
    private final List<Configuration> mRules = new ArrayList<Configuration>();

    /** The AST. */
    private final Configuration[] mTree;

    /**
     * Creates a new instance of CheckstyleParser.
     * 
     * @param file
     *            The checkstyle xml-file.
     * @throws FileNotFoundException
     *             Exception gets thrown if config-file does not exist
     */
    public CheckstyleParser(final InputStream in) throws FileNotFoundException {
        Configuration conf = null;

        try {
            conf = ConfigurationLoader.loadConfiguration(in, null, false);
        }
        catch (final CheckstyleException e) {
            Logger
                .writeln("there is a syntax-error in the checkstyle-config-file");
        }
        mTree = conf.getChildren();

        try {
            in.close();
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for starting parsing for checkstyle-rules.
     * 
     * @return The list of checkstyle-rules found.
     */
    public List<Configuration> parseRules() {
        if (mTree == null) {
            return null;
        }
        for (final Configuration rule : mTree) {
            if (rule.getChildren().length == 0) {
                mRules.add(rule);
            }
            else {
                parseModule(rule.getChildren());
            }
        }

        return mRules;
    }

    /**
     * Method for parsing a checkstyle-modul for rules. Gets called by the
     * method parseRules().
     * 
     * @param module
     *            A array of rules in the module.
     */
    private void parseModule(final Configuration[] module) {
        for (final Configuration rule : module) {
            mRules.add(rule);
        }
    }
}
