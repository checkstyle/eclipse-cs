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

package net.sf.eclipsecs.ui.config.widgets;

import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;

import net.sf.eclipsecs.core.config.ConfigProperty;
import net.sf.eclipsecs.core.config.meta.ConfigPropertyMetadata;
import net.sf.eclipsecs.ui.CheckstyleUiPlugin;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.PropertiesContentAssistProcessor;

/**
 * A string property configuration widget.
 */
public final class ConfigPropertyWidgetFile extends AbstractConfigPropertyWidget {

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
  private ConfigPropertyWidgetFile(Composite parent, ConfigProperty prop) {
    super(parent, prop);
  }

  public static ConfigPropertyWidgetFile create(Composite parent, ConfigProperty prop) {
    return new ConfigPropertyWidgetFile(parent, prop);
  }

  @Override
  protected Control getValueWidget(Composite parent) {
    if (mContents == null) {
      mContents = new Composite(parent, SWT.NULL);
      GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(mContents);
      GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(mContents);

      mTextWidget = new Text(mContents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
      GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(mTextWidget);

      // integrate content assist
      ContentAssistHandler.createHandlerForText(mTextWidget, createContentAssistant());

      mBtnBrowse = new Button(mContents, SWT.PUSH);
      mBtnBrowse.setText(Messages.ConfigPropertyWidgetFile_btnBrowse0);
      GridDataFactory.swtDefaults().applyTo(mBtnBrowse);

      mBtnBrowse.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
        FileDialog fileDialog = new FileDialog(mTextWidget.getShell());
        fileDialog.setFileName(mTextWidget.getText());
        String file = fileDialog.open();
        if (file != null) {
          mTextWidget.setText(file);
        }
      }));

      String initValue = getInitValue();
      if (initValue != null) {
        mTextWidget.setText(initValue);
      }
    }

    return mContents;
  }

  @Override
  public String getValue() {
    String result = mTextWidget.getText();
    if (result == null) {
      result = ""; //$NON-NLS-1$
    }
    return result;
  }

  @Override
  public void setEnabled(boolean enabled) {
    mTextWidget.setEnabled(enabled);
    mBtnBrowse.setEnabled(enabled);
  }

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
            .setRestoreCompletionProposalSize(CheckstyleUiPlugin.getDefault().getDialogSettings());

    IContentAssistProcessor processor = new PropertiesContentAssistProcessor();
    contentAssistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
    contentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
    contentAssistant.setInformationControlCreator(DefaultInformationControl::new);

    return contentAssistant;
  }
}
