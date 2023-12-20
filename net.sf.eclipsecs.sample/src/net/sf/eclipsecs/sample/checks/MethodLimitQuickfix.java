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

package net.sf.eclipsecs.sample.checks;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.quickfixes.AbstractASTResolution;

public class MethodLimitQuickfix extends AbstractASTResolution {

  @Override
  protected ASTVisitor handleGetCorrectingASTVisitor(final IRegion lineInfo,
      final int markerStartOffset) {

    return new ASTVisitor() {

      @Override
      @SuppressWarnings("unchecked")
      public boolean visit(MethodDeclaration node) {

        Javadoc doc = node.getJavadoc();

        if (doc == null) {
          doc = node.getAST().newJavadoc();
          node.setJavadoc(doc);
        }

        TagElement newTag = node.getAST().newTagElement();
        newTag.setTagName("TODO Added by MethodLimit Sample quickfix");

        doc.tags().add(0, newTag);

        return true;
      }
    };
  }

  @Override
  public String getDescription() {
    return "Sample MethodLimit Quickfix";
  }

  @Override
  public String getLabel() {
    return "Sample MethodLimit Quickfix";
  }

  @Override
  public Image getImage() {
    return CheckstyleUIPluginImages.CORRECTION_CHANGE.getImage();
  }
}
