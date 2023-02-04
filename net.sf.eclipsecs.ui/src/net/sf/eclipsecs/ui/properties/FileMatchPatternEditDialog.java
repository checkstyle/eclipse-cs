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
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package net.sf.eclipsecs.ui.properties;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sf.eclipsecs.core.projectconfig.FileMatchPattern;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.util.regex.RegexCompletionProposalFactory;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog to edit file match patterns.
 *
 * @author David Schneider
 * @author Lars Ködderitzsch
 */
public class FileMatchPatternEditDialog extends TitleAreaDialog {

  private Button mIncludeButton;

  private Text mFileMatchPatternText;

  private FileMatchPattern mPattern;

  /**
   * Creates a file matching pattern editor dialog.
   *
   * @param parentShell
   *          the parent shell
   * @param pattern
   *          the pattern
   */
  public FileMatchPatternEditDialog(Shell parentShell, FileMatchPattern pattern) {
    super(parentShell);
    setHelpAvailable(false);
    mPattern = pattern;
  }

  /**
   * Returns the pattern edited by this dialog.
   *
   * @return the pattern
   */
  public FileMatchPattern getPattern() {
    return mPattern;
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite composite = (Composite) super.createDialogArea(parent);

    Composite dialog = new Composite(composite, SWT.NONE);
    dialog.setLayoutData(new GridData(GridData.FILL_BOTH));
    dialog.setLayout(new GridLayout(1, false));

    Label nameLabel = new Label(dialog, SWT.NULL);
    nameLabel.setText(Messages.FileMatchPatternEditDialog_lblRegex);

    mFileMatchPatternText = new Text(dialog, SWT.SINGLE | SWT.BORDER);
    mFileMatchPatternText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    mIncludeButton = new Button(dialog, SWT.CHECK);
    mIncludeButton.setText(Messages.FileMatchPatternEditDialog_chkIncludesFiles);
    mIncludeButton.setLayoutData(new GridData());

    // integrate content assist
    RegexCompletionProposalFactory.createForText(mFileMatchPatternText);

    // init the controls
    if (mPattern != null) {
      mFileMatchPatternText.setText(mPattern.getMatchPattern());
      mIncludeButton.setSelection(mPattern.isIncludePattern());
    } else {
      mIncludeButton.setSelection(true);
    }

    this.setTitleImage(CheckstyleUIPluginImages.PLUGIN_LOGO.getImage());
    this.setTitle(Messages.FileMatchPatternEditDialog_title);
    this.setMessage(Messages.FileMatchPatternEditDialog_message);

    return dialog;
  }

  @Override
  protected void okPressed() {

    String pattern = mFileMatchPatternText.getText();

    try {
      //
      // Try compiling the pattern using the regular expression compiler.
      //
      Pattern.compile(pattern);

      if (mPattern == null) {
        mPattern = new FileMatchPattern(pattern);
      } else {
        mPattern.setMatchPattern(pattern);
      }

      mPattern.setIsIncludePattern(mIncludeButton.getSelection());
    } catch (PatternSyntaxException | CheckstylePluginException ex) {
      this.setErrorMessage(ex.getLocalizedMessage());
      return;
    }

    super.okPressed();
  }

  /**
   * Over-rides method from Window to configure the shell (e.g. the enclosing
   * window).
   */
  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText(Messages.FileMatchPatternEditDialog_titleRegexEditor);
  }

}
