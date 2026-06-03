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

package net.sf.eclipsecs.ui.properties;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import net.sf.eclipsecs.core.projectconfig.FileMatchPattern;
import net.sf.eclipsecs.ui.Messages;

public final class FileMatchPatternControl extends Composite {

  private final FileMatchPatternTable table;

  public FileMatchPatternControl(Composite parent, int style, FileMatchPatternControlCallbacks callbacks) {
    super(parent, style);
    GridLayoutFactory.fillDefaults().applyTo(this);

    Group group = new Group(this, SWT.NONE);
    GridDataFactory.fillDefaults().grab(true, true).applyTo(group);
    group.setText(Messages.FileSetEditDialog_titlePatternsTable);
    GridLayoutFactory.fillDefaults().numColumns(2).applyTo(group);

    table = new FileMatchPatternTable(group, SWT.NONE, callbacks);
    GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

    final Composite buttons = new Composite(group, SWT.NULL);
    GridDataFactory.fillDefaults().applyTo(buttons);
    GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(buttons);

    createPushButton(buttons, Messages.FileSetEditDialog_btnAdd,
            callbacks.addFileMatchPattern);
    createPushButton(buttons, Messages.FileSetEditDialog_btnEdit,
            toRunnable(callbacks.editFileMatchPattern));
    createPushButton(buttons, Messages.FileSetEditDialog_btnRemove,
            toRunnable(callbacks.removeFileMatchPattern));
    createPushButton(buttons, Messages.FileSetEditDialog_btnUp,
            toRunnable(callbacks.upFileMatchPattern));
    createPushButton(buttons, Messages.FileSetEditDialog_btnDown,
            toRunnable(callbacks.downFileMatchPattern));
  }

  private Runnable toRunnable(Consumer<FileMatchPattern> callback) {
    return () -> {
      callback.accept(table.getSelectedPattern());
    };
  }

  public void refresh() {
    table.refresh();
  }

  public void setInput(List<FileMatchPattern> fileMatchPatterns) {
    table.setInput(fileMatchPatterns);
  }

  /**
   * Utility method that creates a push button instance and sets the default layout data.
   *
   * @param parent
   *          the parent for the new button
   * @param label
   *          the label for the new button
   * @return the newly-created button
   */
  private static Button createPushButton(Composite parent, String label, Runnable selectionListener) {
    Button button = new Button(parent, SWT.PUSH);
    button.setText(label);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(button);
    button.addSelectionListener(
            SelectionListener.widgetSelectedAdapter(event -> selectionListener.run()));
    return button;
  }

  public record FileMatchPatternControlCallbacks(Consumer<FileMatchPattern> editFileMatchPattern,
          Runnable updateMatchView, Runnable addFileMatchPattern,
          Consumer<FileMatchPattern> removeFileMatchPattern,
          Consumer<FileMatchPattern> upFileMatchPattern,
          Consumer<FileMatchPattern> downFileMatchPattern) {

  }

}
