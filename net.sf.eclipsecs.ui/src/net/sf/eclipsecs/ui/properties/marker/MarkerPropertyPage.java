package net.sf.eclipsecs.ui.properties.marker;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;

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
      new Label(composite, SWT.NONE).setImage(
              getSeverityImage(getIssue().getAttribute(IMarker.SEVERITY, -1)));
      new Label(composite, SWT.NONE).setText(Messages.MarkerPropertyPage_Issue);
      String message = (String) getIssue().getAttribute(IMarker.MESSAGE);
      Label labelMessage = new Label(composite, SWT.NONE);
      labelMessage.setText(message);

      new Label(composite, SWT.NONE).setImage(
              CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.MODULEGROUP_ICON));
      new Label(composite, SWT.NONE).setText(Messages.MarkerPropertyPage_Group);

      String moduleName = (String) getIssue().getAttribute(CheckstyleMarker.MODULE_NAME);
      RuleMetadata metaData = MetadataFactory.getRuleMetadata(moduleName);
      new Label(composite, SWT.NONE).setText(metaData.getGroup().getGroupName());

      new Label(composite, SWT.NONE).setImage(
              CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.MODULE_ICON));
      new Label(composite, SWT.NONE).setText(Messages.MarkerPropertyPage_Module);

      RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
      rowLayout.marginLeft = 0;
      rowLayout.marginTop = 0;
      rowLayout.marginBottom = 0;
      rowLayout.marginRight = 0;
      Composite nameComposite = new Composite(composite, SWT.NONE);
      nameComposite.setLayout(rowLayout);
      
      new Label(nameComposite, SWT.NONE).setText(metaData.getRuleName());

      Label helpIcon = new Label(nameComposite, SWT.NONE);
      helpIcon.setImage(
              CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.HELP_ICON));
      helpIcon.setToolTipText(NLS.bind(Messages.MarkerPropertyPage_SuppressionHint, 
              metaData.getInternalName()));

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
    } catch (CoreException e) {
      CheckstyleLog.log(e);
    }
    return composite;
  }

  /**
   * Get the image for the severity if it can be identified.
   *
   * @param severity issue severity
   * @return Image or <code>null</code>
   */
  public static Image getSeverityImage(int severity) {

    if (severity == IMarker.SEVERITY_ERROR) {
      return getIdeImage(IDEInternalWorkbenchImages.IMG_OBJS_ERROR_PATH);
    }
    if (severity == IMarker.SEVERITY_WARNING) {
      return getIdeImage(IDEInternalWorkbenchImages.IMG_OBJS_WARNING_PATH);
    }
    if (severity == IMarker.SEVERITY_INFO) {
      return getIdeImage(IDEInternalWorkbenchImages.IMG_OBJS_INFO_PATH);
    }

    return null;
  }

  /**
   * Get the IDE image at path.
   *
   * @param constantName image descriptor name
   * @return Image
   */
  private static Image getIdeImage(String constantName) {
    return JFaceResources.getResources().createImageWithDefault(
        IDEInternalWorkbenchImages.getImageDescriptor(constantName));
  }


}
