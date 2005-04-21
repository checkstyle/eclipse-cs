//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
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

package com.atlassw.tools.eclipse.checkstyle.preferences;

//=================================================
// Imports from java namespace
//=================================================
import java.util.List;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.config.ResolvableProperty;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;

/**
 *  Resolve property values that were specified as variables in a config file.
 */
public class ResolvePropertyValuesDialog extends Dialog
{
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Static class variables.
    //=================================================

    private static final int MAX_LENGTH = 40;

    //=================================================
    // Instance member variables.
    //=================================================

    private Composite mParentComposite;

    private TableViewer mViewer;

    private List mProperties;

    private boolean mOkWasPressed = false;

    //=================================================
    // Constructors & finalizer.
    //=================================================

    /**
     * Constructor.
     * 
     * @param parent  Parent shell.
     * 
     * @param props   Properties being edited.
     * 
     * @throws CheckstyleException  Error during processing.
     */
    ResolvePropertyValuesDialog(Shell parent, List props) throws CheckstylePluginException
    {
        super(parent);
        mProperties = props;
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * @see Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        mParentComposite = parent;

        Composite composite = (Composite)super.createDialogArea(parent);
        Composite dialog = new Composite(composite, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        dialog.setLayout(layout);

        createTablePart(dialog);

        dialog.layout();
        return composite;
    }

    private void createTablePart(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        composite.setLayout(layout);

        Label nameLabel = new Label(composite, SWT.NULL);
        nameLabel.setText(Messages.ResolvePropertyValuesDialog_lblname);

        Table table = new Table(composite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        TableLayout tableLayout = new TableLayout();
        table.setLayout(tableLayout);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = convertWidthInCharsToPixels(160);
        data.heightHint = convertHeightInCharsToPixels(10);
        table.setLayoutData(data);

        TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setText(Messages.ResolvePropertyValuesDialog_colVariable);
        tableLayout.addColumnData(new ColumnWeightData(40));

        TableColumn column2 = new TableColumn(table, SWT.NONE);
        column2.setText(Messages.ResolvePropertyValuesDialog_colValue);
        tableLayout.addColumnData(new ColumnWeightData(120));

        mViewer = new TableViewer(table);
        mViewer.setLabelProvider(new ResolvablePropertyLabelProvider());
        mViewer.setContentProvider(new ArrayContentProvider());
        mViewer.setSorter(new ResolvablePropertyViewerSorter());
        mViewer.setInput(mProperties);

        mViewer.addDoubleClickListener(new IDoubleClickListener()
        {
            public void doubleClick(DoubleClickEvent e)
            {
                editValue();
            }
        });
    }

    private void editValue()
    {
        IStructuredSelection selection = (IStructuredSelection)mViewer.getSelection();
        ResolvableProperty prop = (ResolvableProperty)selection.getFirstElement();
        if (prop == null)
        {
            //
            //  Nothing is selected.
            //
            return;
        }

        ResolvablePropertyEditDialog dialog =
            new ResolvablePropertyEditDialog(mParentComposite.getShell(), prop);
        dialog.open();

        if (dialog.okWasPressed())
        {
            String value = dialog.getValue();
            if (value == null)
            {
                value = ""; //$NON-NLS-1$
            }
            prop.setValue(value);
            mViewer.refresh();
        }
    }


    /**
     *  Over-rides method from Window to configure the 
     *  shell (e.g. the enclosing window).
     * 
     *  @param shell  The shell to configure.
     */
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);
        shell.setText(Messages.ResolvePropertyValuesDialog_titleResolveProperties);
    }
}