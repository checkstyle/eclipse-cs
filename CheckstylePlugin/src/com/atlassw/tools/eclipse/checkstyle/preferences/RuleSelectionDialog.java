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

package com.atlassw.tools.eclipse.checkstyle.preferences;

//=================================================
// Imports from java namespace
//=================================================
import java.util.Iterator;
import java.util.List;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.config.RuleMetadata;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.dialogs.Dialog;


/**
 * Edit dialog for property values.
 */
public class RuleSelectionDialog extends Dialog
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

    private Composite     mParentComposite;
    
    private boolean      mOkWasPressed = false;
    
    private List          mMetadataList;

	private Combo         mComboItem;
    
    private RuleMetadata  mFinalSelection = null;

	//=================================================
	// Constructors & finalizer.
	//=================================================

	/**
	 * Constructor
	 * 
	 * @param parent     Parent shell.
	 * 
	 * @param metadata   List of <code>RuleMetadata</code> objects to select from.
	 */
	public RuleSelectionDialog(Shell parent, List metadata)
        throws CheckstylePluginException
	{
		super(parent);
        mMetadataList = metadata;
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
        mParentComposite = dialog;
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		dialog.setLayout(layout);
        
        Label label = new Label(dialog, SWT.NULL);
        label.setText(" Select Rule:");
        
        //
        //  Build an array of rule names.
        //
        String labels[] = new String[mMetadataList.size()];
        Iterator iter = mMetadataList.iterator();
        for (int i = 0; iter.hasNext(); i++)
        {
            RuleMetadata meta = (RuleMetadata)iter.next();
            labels[i] = " " + meta.getRuleName() + " ";
        }
        
        //
        //  Create a combo box for selecting the rule.
        //
        mComboItem = new Combo(dialog, SWT.NONE | SWT.DROP_DOWN | SWT.READ_ONLY);
        mComboItem.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        mComboItem.setItems(labels);
        mComboItem.select(0);

		dialog.layout();
		return composite;
	}

	/**
	 *  Notification that the OK button was selected.
	 */
	protected void okPressed()
	{        
        mOkWasPressed = true;
        int index = mComboItem.getSelectionIndex();
        mFinalSelection = (RuleMetadata)mMetadataList.get(index);
        
		super.okPressed();
	}
    
    /**
     *  Query after dialog is close to see if it cloased because the
     *  OK button was pressed rather then the Cancel button.
     */
    boolean okWasPressed()
    {
        return mOkWasPressed;
    }
    
    RuleMetadata getSelectedRule()
    {
        return mFinalSelection;
    }
    
    /**
     *  Over-rides method from Window to configure the 
     *  shell (e.g. the enclosing window).
     */
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);
        shell.setText("Checkstyle Rule Selection");
    }
}