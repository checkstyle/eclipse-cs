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

package net.sf.eclipsecs.ui.quickfixes.misc;

import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.quickfixes.AbstractASTResolution;
import net.sf.eclipsecs.ui.quickfixes.Messages;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

/**
 * Quickfix implementation which removes uncommented main methods (debugging leftovers) from the
 * code.
 *
 * @author Lars Ködderitzsch
 */
public class UncommentedMainQuickfix extends AbstractASTResolution {

  /** The length of the javadoc comment declaration. */
  private static final int JAVADOC_COMMENT_LENGTH = 6;

  @Override
  protected ASTVisitor handleGetCorrectingASTVisitor(final IRegion lineInfo,
          final int markerStartOffset) {
    return new ASTVisitor() {

      @Override
      public boolean visit(MethodDeclaration node) {
        // recalculate start position because optional javadoc is mixed
        // into the original start position
        int pos = node.getStartPosition() + (node.getJavadoc() != null
                ? node.getJavadoc().getLength() + JAVADOC_COMMENT_LENGTH
                : 0);
        if (containsPosition(lineInfo, pos)
                && node.getName().getFullyQualifiedName().equals("main")) {
          node.delete();
        }
        return true;
      }
    };
  }

  @Override
  public String getDescription() {
    return Messages.UncommentedMainQuickfix_description;
  }

  @Override
  public String getLabel() {
    return Messages.UncommentedMainQuickfix_label;
  }

  @Override
  public Image getImage() {
    return CheckstyleUIPluginImages.CORRECTION_REMOVE.getImage();
  }
}
