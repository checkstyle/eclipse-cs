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
import java.util.function.Function;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.ui.Messages;

public final class FileSetEditDialogMatchedFilesPreview extends Composite {

  private final TableViewer matchesViewer;
  private final Group matchGroup;

  public FileSetEditDialogMatchedFilesPreview(Composite parent, int style, FileSet fileSet) {
    super(parent, style);
    setLayout(new FillLayout());

    this.matchGroup = new Group(this, SWT.NONE);
    matchGroup.setLayout(new GridLayout(1, false));
    matchGroup.setText(Messages.FileSetEditDialog_msgBuildTestResults);

    this.matchesViewer = new TableViewer(matchGroup);
    matchesViewer.setContentProvider(new ArrayContentProvider());
    matchesViewer.setLabelProvider(new LabelProvider() {

      private final WorkbenchLabelProvider mDelegate = new WorkbenchLabelProvider();

      @Override
      public String getText(Object element) {
        String text = ""; //$NON-NLS-1$
        if (element instanceof IFile) {
          text = ((IFile) element).getProjectRelativePath().toString();
        }
        return text;
      }

      @Override
      public Image getImage(Object element) {
        return mDelegate.getImage(element);
      }
    });
    matchesViewer.addFilter(new ViewerFilter() {

      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element) {
        return element instanceof IFile file && fileSet.includesFile(file);
      }
    });
    matchesViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
  }

  public void refresh() {
    matchesViewer.refresh();
  }

  public void setInput(List<IFile> projectFiles) {
    matchesViewer.setInput(projectFiles);
  }

  public void setText(Function<Integer, String> text) {
    matchGroup.setText(text.apply(matchesViewer.getTable().getItemCount()));
  }
}
