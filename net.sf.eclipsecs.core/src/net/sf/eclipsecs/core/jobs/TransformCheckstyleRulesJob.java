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

package net.sf.eclipsecs.core.jobs;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.core.projectconfig.IProjectConfiguration;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationFactory;
import net.sf.eclipsecs.core.transformer.CheckstyleParser;
import net.sf.eclipsecs.core.transformer.CheckstyleTransformer;
import net.sf.eclipsecs.core.transformer.Logger;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.puppycrawl.tools.checkstyle.api.Configuration;

/**
 * Job who starts transforming the checkstyle-rules to
 * eclipse-formatter-settings.
 * 
 * @author Lukas Frena
 * 
 */
public class TransformCheckstyleRulesJob extends WorkspaceJob {
    /** Selected project in workspace. */
    IProject selection;

    /**
     * Job for transforming checkstyle to formatter-rules.
     * 
     * @param selection
     *            The current selected project in the workspace.
     */
    public TransformCheckstyleRulesJob(final IProject selection) {
        super("transformCheckstyle");

        this.selection = selection;
        Logger.initialize(selection);
    }

    /**
     * Method for getting the location of the default
     * checkstyle-configuration-file.
     * 
     * @return InputStream to the checkstyle-config file.
     */
    private InputStream getConfigStream() {
        try {
            final IProjectConfiguration conf = ProjectConfigurationFactory
                .getConfiguration(selection);
            final Iterator<FileSet> it = conf.getFileSets().iterator();

            return it.next().getCheckConfig().getCheckstyleConfiguration()
                .getCheckConfigFileStream();
        }
        catch (final CheckstylePluginException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    /**
     * Method for printing all the found rules.
     * 
     * @param rules
     *            A list of checkstyle-rules.
     */
    private static void printRules(final List<Configuration> rules) {
        Logger.writeln("found these rules: ");

        final Iterator<Configuration> it = rules.iterator();
        while (it.hasNext()) {
            Logger.write(it.next().getName() + ", ");
        }
        Logger.writeln("\n");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStatus runInWorkspace(final IProgressMonitor arg0) {
        InputStream config = null;

        config = getConfigStream();

        CheckstyleParser parser;
        try {
            parser = new CheckstyleParser(config);
            final List<Configuration> rules = parser.parseRules();

            if (rules == null) {
                Logger.close();
                return Status.CANCEL_STATUS;
            }
            printRules(rules);

            final CheckstyleTransformer transformer = new CheckstyleTransformer(
                rules);
            transformer.transformRules();
        }
        catch (final FileNotFoundException e) {
            Logger.writeln("checkstyle-configuration-file does not exist");
            Logger.close();
            return Status.CANCEL_STATUS;
        }

        Logger
            .writeln("\nNew Eclipse-Formatter-Profile got created and activated!");
        Logger.close();
        return Status.OK_STATUS;
    }
}
