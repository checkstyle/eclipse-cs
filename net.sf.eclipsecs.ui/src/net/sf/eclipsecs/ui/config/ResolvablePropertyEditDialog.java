//============================================================================
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
//============================================================================

package net.sf.eclipsecs.ui.config;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;

import net.sf.eclipsecs.core.config.ResolvableProperty;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.util.SWTUtil;

/**
 * Property page.
 */
public class ResolvablePropertyEditDialog extends TitleAreaDialog {

  private Text mTxtName;

  private Text mTxtValue;

  private ResolvableProperty mProperty;

  /**
   * Constructor for SamplePropertyPage.
   *
   * @param parent
   *          Parent shell for the dialog window.
   * @param prop
   *          Property to be edited.
   */
  ResolvablePropertyEditDialog(Shell parent, ResolvableProperty prop) {
    super(parent);
    setHelpAvailable(false);
    setShellStyle(getShellStyle() | SWT.RESIZE);
    mProperty = prop;
  }

  @Override
  protected Control createDialogArea(Composite parent) {

    Composite composite = (Composite) super.createDialogArea(parent);
    this.setTitle(Messages.ResolvablePropertyEditDialog_titleMessageArea);
    this.setMessage(Messages.ResolvablePropertyEditDialog_msgEditProperty);

    Composite dialog = new Composite(composite, SWT.NONE);
    dialog.setLayout(new GridLayout(2, false));
    dialog.setLayoutData(new GridData(GridData.FILL_BOTH));

    Label lblName = new Label(dialog, SWT.NULL);
    lblName.setText(Messages.ResolvablePropertyEditDialog_lblName);
    mTxtName = new Text(dialog, SWT.SINGLE | SWT.BORDER);
    mTxtName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    mTxtName.setText(mProperty.getPropertyName() != null ? mProperty.getPropertyName() : ""); //$NON-NLS-1$

    Label lblValue = new Label(dialog, SWT.NULL);
    lblValue.setText(Messages.ResolvablePropertyEditDialog_lblValue);
    mTxtValue = new Text(dialog, SWT.SINGLE | SWT.BORDER);
    mTxtValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    mTxtValue.setText(mProperty.getValue() != null ? mProperty.getValue() : ""); //$NON-NLS-1$

    // integrate content assist
    ContentAssistHandler.createHandlerForText(mTxtValue, createContentAssistant());

    return composite;
  }

  @Override
  public void create() {
    super.create();
    SWTUtil.addResizeSupport(this, CheckstyleUIPlugin.getDefault().getDialogSettings(),
            ResolvablePropertyEditDialog.class.getName());
  }

  @Override
  protected void okPressed() {

    if (StringUtils.isBlank(mTxtName.getText())) {
      this.setErrorMessage(Messages.ResolvablePropertyEditDialog_msgMissingName);
      return;
    }
    if (StringUtils.isBlank(mTxtValue.getText())) {
      this.setErrorMessage(Messages.ResolvablePropertyEditDialog_msgMissingValue);
      return;
    }

    //
    // Get the entered value.
    //
    mProperty.setPropertyName(mTxtName.getText());
    mProperty.setValue(mTxtValue.getText());

    super.okPressed();
  }

  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText(Messages.ResolvablePropertyEditDialog_titleDialog);
  }

  /**
   * Creates the content assistant.
   *
   * @return the content assistant
   */
  private SubjectControlContentAssistant createContentAssistant() {

    final SubjectControlContentAssistant contentAssistant = new SubjectControlContentAssistant();

    contentAssistant
            .setRestoreCompletionProposalSize(CheckstyleUIPlugin.getDefault().getDialogSettings());

    IContentAssistProcessor processor = new PropertiesContentAssistProcessor();
    contentAssistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
    contentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
    contentAssistant.setInformationControlCreator(new IInformationControlCreator() {
      @Override
      public IInformationControl createInformationControl(Shell parent) {
        return new DefaultInformationControl(parent);
      }
    });

    return contentAssistant;
  }

}
