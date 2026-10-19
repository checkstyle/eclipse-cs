///////////////////////////////////////////////////////////////////////////////////////////////
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch
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
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//
///////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.eclipsecs.ui.properties;

import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.projectconfig.FileMatchPattern;
import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationConfigureDialog;

/**
 * Simple file sets editor producing only one file set that contains all files. Only the check
 * configuration can be chosen.
 *
 */
public class SimpleFileSetsEditor implements IFileSetsEditor {

    /** The property page context. */
    private final PropertyPageContext propertyPageContext;
    /** The shell. */
    private final Shell shell;

    /** The editor view. */
    private SimpleFileSetsEditorView editorView;

    /** The list of file sets. */
    private List<FileSet> fileSets;

    /** The default file set. */
    private FileSet defaultFileSet;

    /**
     * Creates the SimpleFileSetsEditor.
     *
     * @param shell
     *            the shell
     * @param propertyPageContext
     *            the property page context
     */
    public SimpleFileSetsEditor(Shell shell, PropertyPageContext propertyPageContext) {
        this.shell = shell;
        this.propertyPageContext = propertyPageContext;
    }

    @Override
    public void setFileSets(List<FileSet> fileSets) {
        this.fileSets = fileSets;

        ICheckConfiguration config = null;
        if (!fileSets.isEmpty()) {
            config = fileSets.get(0).getCheckConfig();
        }

        if (config == null) {
            final CheckConfigurationWorkingCopy[] allConfigs = propertyPageContext.configuration()
                .getGlobalCheckConfigWorkingSet().getWorkingCopies();
            if (allConfigs.length > 0) {
                config = allConfigs[0];
            }
        }

        defaultFileSet = new FileSet(Messages.SimpleFileSetsEditor_nameAllFileset, config);
        try {
            defaultFileSet.getFileMatchPatterns().add(new FileMatchPattern("."));
        }
        catch (CheckstylePluginException exception) {
            // impossible
        }
        fileSets.clear();
        fileSets.add(defaultFileSet);
    }

    @Override
    public List<FileSet> getFileSets() {
        return fileSets;
    }

    @Override
    public Control createContents(Composite parent) {
        this.editorView = new SimpleFileSetsEditorView(parent, SWT.NONE, this::manageConfig,
            defaultFileSet, propertyPageContext);
        return editorView;
    }

    /**
     * Opens a dialog to configure the check configuration of the default file set.
     */
    private void manageConfig() {
        final ICheckConfiguration config = defaultFileSet.getCheckConfig();
        if (config != null) {
            try {
                config.getCheckstyleConfiguration();
                final CheckConfigurationConfigureDialog dialog =
                    new CheckConfigurationConfigureDialog(shell,
                        (CheckConfigurationWorkingCopy) config);
                dialog.setBlockOnOpen(true);
                dialog.open();
            }
            catch (CheckstylePluginException exception) {
                CheckstyleUIPlugin.warningDialog(shell,
                    NLS.bind(Messages.CheckstylePreferencePage_msgProjectRelativeConfigNoFound,
                        propertyPageContext.project(), config.getLocation()),
                    exception);
            }
        }
    }

    @Override
    public void refresh() {
        editorView.refresh();
    }

}
