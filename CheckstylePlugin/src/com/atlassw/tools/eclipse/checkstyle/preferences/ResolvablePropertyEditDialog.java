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

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.config.ResolvableProperty;

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 *  Property page.
 */
public class ResolvablePropertyEditDialog extends Dialog
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
    
    private String                  mValue;
    
    private Composite               mComposite;
    
    private Text                    mPropValueText;
    
    private ResolvableProperty      mProperty;
    
    private boolean                 mOkWasPressed = false;

    //=================================================
    // Constructors & finalizer.
    //=================================================

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public ResolvablePropertyEditDialog(Shell parent, ResolvableProperty  prop)
	{
		super(parent);
		mProperty = prop;
	}

    //=================================================
    // Methods.
    //=================================================

    /**
     * @see Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {
        mComposite = parent;
        Composite composite = (Composite)super.createDialogArea(parent);
        Composite dialog = new Composite(composite, SWT.NONE);
        
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        dialog.setLayout(layout);
        
		Label label = new Label(dialog, SWT.NULL);
		label.setText("Variable:");
		label = new Label(dialog, SWT.NULL);
		label.setText(mProperty.getVariableName());
        
		label = new Label(dialog, SWT.NULL);
		label.setText("Value:");

		mPropValueText = new Text(dialog, SWT.SINGLE | SWT.BORDER);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.verticalAlignment = GridData.CENTER;
        data.grabExcessVerticalSpace = false;
        data.widthHint = convertWidthInCharsToPixels(MAX_LENGTH);
		mPropValueText.setLayoutData(data);
		mPropValueText.setFont(parent.getFont());
		mPropValueText.setTextLimit(MAX_LENGTH);
        if (mProperty.getValue() != null)
        {
			mPropValueText.setText(mProperty.getValue());
        }
        
        dialog.layout();
        return composite;
    }
    
    protected void okPressed()
    {
        //
        //  Get the entered value.
        //
        mValue = mPropValueText.getText();
                
        mOkWasPressed = true;
        super.okPressed();
    }
   
    public boolean okWasPressed()
    {
        return mOkWasPressed;
    }
    
    public String getValue()
    {
        return mValue;
    }
        
    /**
     *  Over-rides method from Window to configure the 
     *  shell (e.g. the enclosing window).
     */
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);
        shell.setText("Property Variable Value Editor");
    }
    
}