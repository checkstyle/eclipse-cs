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

package com.atlassw.tools.eclipse.checkstyle.properties;

//=================================================
// Imports from java namespace
//=================================================
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.atlassw.tools.eclipse.checkstyle.Messages;

/**
 * Property page.
 */
public class TestResultsDialog extends Dialog
{
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Static class variables.
    //=================================================

    //=================================================
    // Instance member variables.
    //=================================================

    private List mFiles;

    //=================================================
    // Constructors & finalizer.
    //=================================================

    /**
     * Constructor for SamplePropertyPage.
     */
    TestResultsDialog(Shell parent, List files)
    {
        super(parent);
        mFiles = files;
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * @see Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        Composite composite = (Composite) super.createDialogArea(parent);
        Composite dialog = new Composite(composite, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        dialog.setLayout(layout);

        Label label = new Label(dialog, SWT.NULL);
        label.setText(Messages.TestResultsDialog_lblFilterResult);

        Table table = new Table(dialog, SWT.BORDER);
        TableLayout tableLayout = new TableLayout();
        table.setLayout(tableLayout);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);

        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = convertWidthInCharsToPixels(80);
        data.heightHint = convertHeightInCharsToPixels(10);
        table.setLayoutData(data);

        TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setText(Messages.TestResultsDialog_colMatchingFiles);
        tableLayout.addColumnData(new ColumnWeightData(10));

        TableViewer viewer = new TableViewer(table);
        viewer.setLabelProvider(new TestResultLabelProvider());
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setSorter(new TestResultViewerSorter());
        viewer.setInput(mFiles);

        return composite;
    }

    boolean okWasPressed()
    {
        return true;
    }

    /**
     * Over-rides method from Window to configure the shell (e.g. the enclosing
     * window).
     */
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);
        shell.setText(Messages.TestResultsDialog_titleTestResult);
    }

    /**
     * Provides the labels for the test results.
     * 
     * @author David Schneider
     */
    class TestResultLabelProvider extends LabelProvider implements ITableLabelProvider
    {

        /**
         * @see ITableLabelProvider#getColumnText(Object, int)
         */
        public String getColumnText(Object element, int columnIndex)
        {
            String result = element.toString();
            if (element instanceof IFile)
            {
                IFile file = (IFile) element;
                result = file.getProjectRelativePath().toString();
            }
            return result;
        }

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
         *      int)
         */
        public Image getColumnImage(Object element, int columnIndex)
        {
            return null;
        }
    }

    /**
     * Sorts the test result.
     * 
     * @author David Schneider
     */
    private class TestResultViewerSorter extends ViewerSorter
    {

        /**
         * @see ViewerSorter#compare
         */
        public int compare(Viewer viewer, Object e1, Object e2)
        {
            int result = 0;

            if ((e1 instanceof IFile) && (e2 instanceof IFile))
            {
                IFile file1 = (IFile) e1;
                IFile file2 = (IFile) e2;

                String name1 = file1.getProjectRelativePath().toString();
                String name2 = file2.getProjectRelativePath().toString();

                result = name1.compareTo(name2);
            }

            return result;
        }
    }
}