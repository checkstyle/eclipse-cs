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

package net.sf.eclipsecs.ui.config;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import net.sf.eclipsecs.core.config.ConfigProperty;
import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.config.Severity;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.widgets.ConfigPropertyWidgetFactory;
import net.sf.eclipsecs.ui.config.widgets.IConfigPropertyWidget;

public final class RuleConfigurationEditDialogGeneralSettings extends Composite {

  private final ComboViewer mSeverityCombo;
  private final List<IConfigPropertyWidget> mConfigPropertyWidgets;

  public RuleConfigurationEditDialogGeneralSettings(Composite parent, int style, Module rule,
          boolean readonly) {
    super(parent, style);
    GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

    // Build severity
    Label lblSeverity = new Label(this, SWT.NULL);
    lblSeverity.setText(Messages.RuleConfigurationEditDialog_lblSeverity);
    GridDataFactory.swtDefaults().applyTo(lblSeverity);

    mSeverityCombo = new ComboViewer(this);
    mSeverityCombo.setContentProvider(ArrayContentProvider.getInstance());
    mSeverityCombo.setLabelProvider(
            LabelProvider.createTextProvider(element -> ((Severity) element).toXmlValue()));
    GridDataFactory.swtDefaults().applyTo(mSeverityCombo.getControl());

    mSeverityCombo.setInput(Severity.values());
    mSeverityCombo.getCombo().setEnabled(!readonly);
    if (rule.getMetaData().hasSeverity()) {
      mSeverityCombo.setSelection(new StructuredSelection(rule.getSeverity()));
    } else {
      mSeverityCombo.getCombo().setEnabled(false);
    }

    if (rule.getProperties().size() > 0) {
      Group properties = new Group(this, SWT.NULL);
      GridLayoutFactory.swtDefaults().numColumns(3).applyTo(properties);
      properties.setText(Messages.RuleConfigurationEditDialog_lblProperties);
      GridDataFactory.create(GridData.FILL_BOTH).span(2, 1).applyTo(properties);
      mConfigPropertyWidgets = rule.getProperties().stream()
              .map(prop -> ConfigPropertyWidgetFactory.createWidget(properties, prop, getShell()))
              .peek(widget -> widget.setEnabled(!readonly)).toList();
    } else {
      mConfigPropertyWidgets = Collections.emptyList();
    }
  }

  public Severity getSeverity() {
    return (Severity) mSeverityCombo.getStructuredSelection().getFirstElement();
  }

  public void setSeverity(Severity severity) {
    mSeverityCombo.setSelection(new StructuredSelection(severity));
  }

  public void restoreProperties() {
    mConfigPropertyWidgets.forEach(IConfigPropertyWidget::restorePropertyDefault);
  }

  public Optional<String> validatePropertyWidgets() {
    for (IConfigPropertyWidget widget : mConfigPropertyWidgets) {
      ConfigProperty property = widget.getConfigProperty();
      try {
        widget.validate();
      } catch (CheckstylePluginException ex) {
        String message = NLS.bind(Messages.RuleConfigurationEditDialog_msgInvalidPropertyValue,
                property.getMetaData().getName());
        return Optional.of(message);
      }
      property.setValue(widget.getValue());
    }
    return Optional.empty();
  }

}
