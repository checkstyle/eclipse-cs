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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.apache.regexp.RECompiler;
import org.apache.regexp.RESyntaxException;

/**
 *  Property page.
 */
public class FileMatchPatternEditDialog extends Dialog
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
    
    
    private Composite               mComposite;
    
    private Text                    mFileMatchPatternText;
    
    private String                  mPattern;
    
    private String                  mValidationErrorMsg;
    
    private boolean                 mOkWasPressed = false;

    //=================================================
    // Constructors & finalizer.
    //=================================================

	/**
	 * Constructor for SamplePropertyPage.
	 */
    FileMatchPatternEditDialog(Shell parent, String pattern)
    {
        super(parent);
        mPattern = pattern;
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
        
        Label nameLabel = new Label(dialog, SWT.NULL);
        nameLabel.setText("Match Regular Expression:");

        mFileMatchPatternText = new Text(dialog, SWT.SINGLE | SWT.BORDER);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = true;
        data.verticalAlignment = GridData.CENTER;
        data.grabExcessVerticalSpace = false;
        data.widthHint = convertWidthInCharsToPixels(MAX_LENGTH);
        mFileMatchPatternText.setLayoutData(data);
        mFileMatchPatternText.setFont(parent.getFont());
        mFileMatchPatternText.setTextLimit(MAX_LENGTH);
        if (mPattern != null)
        {
            mFileMatchPatternText.setText(mPattern);
        }
        
        dialog.layout();
        return composite;
    }
    
    protected void okPressed()
    {
        //
        //  Get the entered pattern.
        //
        mPattern = mFileMatchPatternText.getText();
        if ((mPattern == null) || (mPattern.trim().length() == 0))
        {
            //
            //  Nothing was entered.
            //
            mPattern = null;
            super.okPressed();
            return;
        }
        
        //
        //  Check that the pattern is a valid regular expression pattern.
        //
        if (!validatePattern(mPattern))
        {
            String msg = "The string '" + mPattern 
                         + "' is not a valid regular expression pattern."
                         + System.getProperty("line.separator")
                         + mValidationErrorMsg;
                         
            MessageDialog.openError(mComposite.getShell(),
                        "Pattern Validation Error", msg);
            return;
        }
        
        mOkWasPressed = true;
        super.okPressed();
    }
   
    boolean okWasPressed()
    {
        return mOkWasPressed;
    }
    
    String getPattern()
    {
        return mPattern;
    }
    
    private boolean validatePattern(String pattern)
    {
        boolean result = false;
        
        //
        //  Try compiling the pattern using the regular expression compiler.
        //
        try
        {
            RECompiler compiler = new RECompiler();
            compiler.compile(pattern);
            result = true;
        }
        catch (RESyntaxException e)
        {
            mValidationErrorMsg = e.getMessage();
        }
        
        return result;
    }
    
    /**
     *  Over-rides method from Window to configure the 
     *  shell (e.g. the enclosing window).
     */
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);
        shell.setText("Checkstyle Regular Expression Editor");
    }
    
}