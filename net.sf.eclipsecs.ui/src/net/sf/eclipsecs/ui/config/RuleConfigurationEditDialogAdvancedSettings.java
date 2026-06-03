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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.config.meta.MetadataFactory;
import net.sf.eclipsecs.ui.Messages;

public final class RuleConfigurationEditDialogAdvancedSettings extends Composite {

  private final Text mCommentText;
  private final Text mIdText;
  private final Map<String, Text> mCustomMessages;

  public RuleConfigurationEditDialogAdvancedSettings(Composite parent, int style, Module rule, boolean readonly) {
    super(parent, style);

    GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

    mCommentText = createLabeledText(this, Messages.RuleConfigurationEditDialog_lblComment);
    mIdText = createLabeledText(this, Messages.RuleConfigurationEditDialog_lblId);

    Group messagesGroup = new Group(this, SWT.NULL);
    messagesGroup.setText(Messages.RuleConfigurationEditDialog_titleCustMsg);
    GridLayoutFactory.swtDefaults().numColumns(2).applyTo(messagesGroup);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).span(2, 1).applyTo(messagesGroup);

    mCustomMessages = new HashMap<>();

    // take keys from metadata as well as predefined from the
    // configuration. This way we don't lose keys not defined in metadata.
    Set<String> msgKeys = new TreeSet<>();
    msgKeys.addAll(rule.getMetaData().messageKeys());
    msgKeys.addAll(rule.getCustomMessages().keySet());

    for (String msgKey : msgKeys) {
      final Text msgText = createLabeledText(messagesGroup, msgKey);

      final String standardMessage = MetadataFactory.getStandardMessage(msgKey,
              rule.getMetaData().identity().internalName());

      if (standardMessage != null) {
        msgText.setMessage(standardMessage);
      }

      String message = rule.getCustomMessages().get(msgKey);
      if (StringUtils.isNotBlank(message)) {
        msgText.setText(message);
      }
      msgText.setEnabled(!readonly);

      mCustomMessages.put(msgKey, msgText);
    }

    String comment = rule.getComment();
    if (comment != null) {
      mCommentText.setText(comment);
    }

    String id = rule.getId();
    if (id != null) {
      mIdText.setText(id);
    }

    mIdText.setEnabled(!readonly);
    // mCustomMessageText.setEditable(!mReadonly);
    mCommentText.setEnabled(!readonly);
  }

  public String getComment() {
    return mCommentText.getText();
  }

  public String getId() {
    return mIdText.getText();
  }

  public Map<String, String> getCustomMessages() {
    return mCustomMessages.entrySet().stream()
            .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getText()));
  }

  public void resetComment() {
    mCommentText.setText("");
  }

  private static Text createLabeledText(Composite parent, String label) {
    Label commentLabel = new Label(parent, SWT.NULL);
    commentLabel.setText(label);
    GridDataFactory.swtDefaults().applyTo(commentLabel);
    Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(text);
    return text;
  }

}
