//============================================================================
//
// Copyright (C) 2002-2003  David Schneider
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

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================


//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


/**
 *  Property page.
 */
public class TestResultsDialog extends Dialog
{
    //=================================================
    // Public static final variables.
    //=================================================
  
    //=================================================
    // Static class variables.
    //=================================================
    
    private static final int MAX_LENGTH = 40;
    
    private static final String JAVA_SUFFIX = ".java";
    
    //=================================================
    // Instance member variables.
    //=================================================
    
    private List  mFiles;

    //=================================================
    // Constructors & finalizer.
    //=================================================

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public TestResultsDialog(Shell parent, List files)
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
        Composite composite = (Composite)super.createDialogArea(parent);
        Composite dialog = new Composite(composite, SWT.NONE);
        
     	GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        dialog.setLayout(layout);
        
        Label label = new Label(dialog, SWT.NULL);
        label.setText("The following files are included in the File Set");

        Table table = new Table(dialog, SWT.BORDER );
        TableLayout tableLayout = new TableLayout();
        table.setLayout(tableLayout);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);

        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = convertWidthInCharsToPixels(80);
        data.heightHint = convertHeightInCharsToPixels(10);
        table.setLayoutData(data);

        TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setText("Matching Files");
        tableLayout.addColumnData(new ColumnWeightData(10));

        TableViewer viewer = new TableViewer(table);
        viewer.setLabelProvider(new TestResultLabelProvider());
        viewer.setContentProvider(new TestResultProvider());
        viewer.setSorter(new TestResultViewerSorter());
        viewer.setInput(mFiles);

        return composite;
    }
    
    public boolean okWasPressed()
    {
        return true;
    }
    
    /**
     *  Over-rides method from Window to configure the 
     *  shell (e.g. the enclosing window).
     */
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);
        shell.setText("Checkstyle File Set Test Results");
    }
   
}