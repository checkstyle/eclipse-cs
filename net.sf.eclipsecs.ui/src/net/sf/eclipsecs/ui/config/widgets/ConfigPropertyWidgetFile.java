//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.ui.config.widgets;

import net.sf.eclipsecs.core.config.ConfigProperty;
import net.sf.eclipsecs.core.config.meta.ConfigPropertyMetadata;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.PropertiesContentAssistProcessor;

import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;

/**
 * A string property configuration widget.
 */
public class ConfigPropertyWidgetFile extends ConfigPropertyWidgetAbstractBase {

  private Composite mContents;

  private Text mTextWidget;

  private Button mBtnBrowse;

  /**
   * Creates the widget.
   * 
   * @param parent
   *          the parent composite
   * @param prop
   *          the property
   */
  public ConfigPropertyWidgetFile(Composite parent, ConfigProperty prop) {
    super(parent, prop);
  }

  /**
   * @see ConfigPropertyWidgetAbstractBase#getValueWidget(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control getValueWidget(Composite parent) {

    if (mContents == null) {

      mContents = new Composite(parent, SWT.NULL);
      mContents.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      GridLayout layout = new GridLayout(2, false);
      layout.marginWidth = 0;
      layout.marginHeight = 0;
      mContents.setLayout(layout);

      mTextWidget = new Text(mContents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      mTextWidget.setLayoutData(gd);

      // integrate content assist
      ContentAssistHandler.createHandlerForText(mTextWidget, createContentAssistant());

      mBtnBrowse = new Button(mContents, SWT.PUSH);
      mBtnBrowse.setText(Messages.ConfigPropertyWidgetFile_btnBrowse0);
      mBtnBrowse.setLayoutData(new GridData());

      mBtnBrowse.addSelectionListener(new SelectionListener() {

        @Override
        public void widgetSelected(SelectionEvent e) {
          FileDialog fileDialog = new FileDialog(mTextWidget.getShell());
          fileDialog.setFileName(mTextWidget.getText());

          String file = fileDialog.open();
          if (null != file) {
            mTextWidget.setText(file);
          }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
          // NOOP
        }
      });

      String initValue = getInitValue();
      if (initValue != null) {
        mTextWidget.setText(initValue);
      }
    }

    return mContents;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getValue() {
    String result = mTextWidget.getText();
    if (result == null) {
      result = ""; //$NON-NLS-1$
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEnabled(boolean enabled) {
    mTextWidget.setEnabled(enabled);
    mBtnBrowse.setEnabled(enabled);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void restorePropertyDefault() {
    ConfigPropertyMetadata metadata = getConfigProperty().getMetaData();
    String defaultValue = metadata.getOverrideDefault() != null ? metadata.getOverrideDefault()
            : metadata.getDefaultValue();
    mTextWidget.setText(defaultValue != null ? defaultValue : ""); //$NON-NLS-1$
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
      /*
       * @see IInformationControlCreator#createInformationControl(Shell)
       */
      @Override
      public IInformationControl createInformationControl(Shell parent) {
        return new DefaultInformationControl(parent);
      }
    });

    return contentAssistant;
  }
}
