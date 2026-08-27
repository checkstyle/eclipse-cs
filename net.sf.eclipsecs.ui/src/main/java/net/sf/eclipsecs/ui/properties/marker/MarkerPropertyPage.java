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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import net.sf.eclipsecs.core.builder.CheckstyleMarker;
import net.sf.eclipsecs.core.config.meta.MetadataFactory;
import net.sf.eclipsecs.core.config.meta.RuleIdentity;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.util.HtmlUtil;

/**
 * Property page for checkstyle markers.
 */
public class MarkerPropertyPage extends PropertyPage {

    /** Number of columns. */
    private static final int NUM_COLUMNS = 3;
    /** Message and description width in pixels. */
    private static final int WIDTH = 300;

    /**
     * Returns the marker this property page is showing.
     *
     * @return the issue marker
     */
    private IMarker getIssue() {
        return (IMarker) getElement();
    }

    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();

        final Composite composite = new Composite(parent, SWT.NULL);
        GridLayoutFactory.fillDefaults().numColumns(NUM_COLUMNS).applyTo(composite);

        try {
            createSeverityText(composite);
            final RuleIdentity ruleIdentity = createGroupText(composite);
            createRuleText(composite, ruleIdentity);
            createIdText(composite);
            createDescriptionText(composite, ruleIdentity);
        }
        catch (CoreException ex) {
            CheckstyleLog.log(ex);
        }
        return composite;
    }

    /**
     * Creates the row showing the marker severity.
     *
     * @param composite
     *            the parent composite
     * @throws CoreException
     *             if the marker attribute cannot be read
     */
    private void createSeverityText(final Composite composite) throws CoreException {
        new Label(composite, SWT.NONE)
            .setImage(getSeverityImage(getIssue().getAttribute(IMarker.SEVERITY, -1)));
        new Label(composite, SWT.NONE).setText(Messages.MarkerPropertyPage_Issue);
        final String message = (String) getIssue().getAttribute(IMarker.MESSAGE);
        final Text labelMessage = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
        labelMessage.setText(message);
        GridDataFactory.fillDefaults().hint(WIDTH, SWT.DEFAULT).applyTo(labelMessage);
    }

    /**
     * Creates the row showing the marker group.
     *
     * @param composite
     *            the parent composite
     * @return the rule identity of the marker's module
     * @throws CoreException
     *             if the marker attribute cannot be read
     */
    private RuleIdentity createGroupText(final Composite composite) throws CoreException {
        new Label(composite, SWT.NONE)
            .setImage(CheckstyleUIPluginImages.MODULEGROUP_ICON.getImage());
        new Label(composite, SWT.NONE).setText(Messages.MarkerPropertyPage_Group);

        final String moduleName = (String) getIssue().getAttribute(CheckstyleMarker.MODULE_NAME);
        final RuleIdentity ruleIdentity = MetadataFactory.getRuleMetadata(moduleName).identity();
        final Text labelGroupName = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
        labelGroupName.setText(ruleIdentity.group().getGroupName());
        return ruleIdentity;
    }

    /**
     * Creates the row showing the marker rule name.
     *
     * @param composite
     *            the parent composite
     * @param ruleIdentity
     *            the rule identity to display
     */
    private void createRuleText(final Composite composite, RuleIdentity ruleIdentity) {
        new Label(composite, SWT.NONE).setImage(CheckstyleUIPluginImages.MODULE_ICON.getImage());
        new Label(composite, SWT.NONE).setText(Messages.MarkerPropertyPage_Module);

        final Text labelRuleName = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
        labelRuleName.setText(ruleIdentity.ruleName());
    }

    /**
     * Creates the row showing the marker id if present.
     *
     * @param composite
     *            the parent composite
     */
    private void createIdText(final Composite composite) {
        final var id = getIssue().getAttribute(CheckstyleMarker.MODULE_ID, null);
        if (!StringUtils.isEmpty(id)) {
            new Label(composite, SWT.NONE).setImage(PlatformUI.getWorkbench().getSharedImages()
                .getImage(ISharedImages.IMG_OBJS_INFO_TSK));
            new Label(composite, SWT.NONE).setText(Messages.MarkerPropertyPage_Id);

            final Text labelId = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
            labelId.setText(id);
        }
    }

    /**
     * Creates the row showing the marker description.
     *
     * @param composite
     *            the parent composite
     * @param ruleIdentity
     *            the rule identity to display the description of
     */
    private void createDescriptionText(final Composite composite, RuleIdentity ruleIdentity) {
        final Label descriptionLabel = new Label(composite, SWT.NONE);
        descriptionLabel.setText(Messages.MarkerPropertyPage_Description);
        GridDataFactory.fillDefaults().span(NUM_COLUMNS, 1).applyTo(descriptionLabel);

        final Browser browserDescription = new Browser(composite, SWT.BORDER);
        browserDescription.setText(HtmlUtil.getDescriptionHtml(ruleIdentity.description()));
        GridDataFactory.fillDefaults().span(NUM_COLUMNS, 1).hint(WIDTH, SWT.DEFAULT)
            .grab(true, true).applyTo(browserDescription);
    }

    /**
     * Get the image for the severity if it can be identified.
     *
     * @param severity
     *            issue severity
     * @return Image or <code>null</code>
     */
    public static Image getSeverityImage(int severity) {
        final ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
        return switch (severity) {
            case IMarker.SEVERITY_ERROR -> sharedImages.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
            case IMarker.SEVERITY_WARNING -> sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
            case IMarker.SEVERITY_INFO -> sharedImages.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
            default -> null;
        };
    }

}
