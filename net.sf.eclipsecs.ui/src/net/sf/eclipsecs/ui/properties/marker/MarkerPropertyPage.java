//============================================================================
//
// Copyright (C) 2003-2023 the original author or authors.
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

package net.sf.eclipsecs.ui.properties.marker;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import net.sf.eclipsecs.core.builder.CheckstyleMarker;
import net.sf.eclipsecs.core.config.meta.MetadataFactory;
import net.sf.eclipsecs.core.config.meta.RuleMetadata;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationConfigureDialog;

/**
 * Property page for checkstyle markers.
 */
public class MarkerPropertyPage extends PropertyPage {

  private IMarker getIssue() {
    return (IMarker) getElement();
  }

  @Override
  protected Control createContents(Composite parent) {
    noDefaultAndApplyButton();

    final Composite composite = new Composite(parent, SWT.NULL);

    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    final GridLayout layout = new GridLayout();
    layout.numColumns = 3;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    composite.setLayout(layout);

    try {
      createSeverityText(composite);
      RuleMetadata metaData = createGroupText(composite);
      createRuleText(composite, metaData);
      createIdText(composite);
      createDescriptionText(composite, metaData);
    } catch (CoreException ex) {
      CheckstyleLog.log(ex);
    }
    return composite;
  }

  private void createSeverityText(final Composite composite) throws CoreException {
    new Label(composite, SWT.NONE).setImage(
            getSeverityImage(getIssue().getAttribute(IMarker.SEVERITY, -1)));
    new Label(composite, SWT.NONE).setText(Messages.MarkerPropertyPage_Issue);
    String message = (String) getIssue().getAttribute(IMarker.MESSAGE);
    Text labelMessage = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
    labelMessage.setText(message);
  }

  private RuleMetadata createGroupText(final Composite composite) throws CoreException {
    new Label(composite, SWT.NONE).setImage(
            CheckstyleUIPluginImages.MODULEGROUP_ICON.getImage());
    new Label(composite, SWT.NONE).setText(Messages.MarkerPropertyPage_Group);

    String moduleName = (String) getIssue().getAttribute(CheckstyleMarker.MODULE_NAME);
    RuleMetadata metaData = MetadataFactory.getRuleMetadata(moduleName);
    Text labelGroupName = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
    labelGroupName.setText(metaData.getGroup().getGroupName());
    return metaData;
  }

  private void createRuleText(final Composite composite, RuleMetadata metaData) {
    new Label(composite, SWT.NONE).setImage(
            CheckstyleUIPluginImages.MODULE_ICON.getImage());
    new Label(composite, SWT.NONE).setText(Messages.MarkerPropertyPage_Module);

    Text labelRuleName = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
    labelRuleName.setText(metaData.getRuleName());
  }

  private void createIdText(final Composite composite) {
    var id = getIssue().getAttribute(CheckstyleMarker.MODULE_ID, null);
    if (StringUtils.isEmpty(id)) {
      return;
    }
    new Label(composite, SWT.NONE).setImage(
            PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK));
    new Label(composite, SWT.NONE).setText(Messages.MarkerPropertyPage_Id);

    Text labelId = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
    labelId.setText(id);
  }

  private void createDescriptionText(final Composite composite, RuleMetadata metaData) {
    Label descriptionLabel = new Label(composite, SWT.NONE);
    descriptionLabel.setText(Messages.MarkerPropertyPage_Description);
    GridData gridData = new GridData();
    gridData.horizontalSpan = 3;
    gridData.verticalIndent = 20;
    descriptionLabel.setLayoutData(gridData);

    gridData = new GridData(GridData.FILL_BOTH);
    gridData.heightHint = 100;
    gridData.horizontalSpan = 3;
    Browser browserDescription = new Browser(composite, SWT.BORDER);
    browserDescription.setLayoutData(gridData);
    browserDescription.setText(
            CheckConfigurationConfigureDialog.getDescriptionHtml(metaData.getDescription()));
  }

  /**
   * Get the image for the severity if it can be identified.
   *
   * @param severity issue severity
   * @return Image or <code>null</code>
   */
  public static Image getSeverityImage(int severity) {
    ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
    switch (severity) {
      case IMarker.SEVERITY_ERROR:
        return sharedImages.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
      case IMarker.SEVERITY_WARNING:
        return sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
      case IMarker.SEVERITY_INFO:
        return sharedImages.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
      default:
        return null;
    }
  }

}
