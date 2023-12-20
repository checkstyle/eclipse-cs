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

package net.sf.eclipsecs.ui.quickfixes.coding;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.quickfixes.AbstractASTResolution;
import net.sf.eclipsecs.ui.quickfixes.Messages;

/**
 * Quickfix implementation that removes an empty statement (unneccessary
 * semicolon).
 *
 * @author Lars Ködderitzsch
 */
public class EmptyStatementQuickfix extends AbstractASTResolution {

  @Override
  protected ASTVisitor handleGetCorrectingASTVisitor(final IRegion lineInfo,
          final int markerStartPosition) {

    return new ASTVisitor() {
      @Override
      public boolean visit(EmptyStatement node) {
        if (containsPosition(lineInfo, node.getStartPosition())) {

          // early exit if the statement is mandatory, e.g. only
          // statement in a for-statement without block
          StructuralPropertyDescriptor desc = node.getLocationInParent();
          if (desc.isChildProperty() && ((ChildPropertyDescriptor) desc).isMandatory()) {
            return false;
          }

          node.delete();
        }
        return false;
      }
    };
  }

  @Override
  public String getDescription() {
    return Messages.EmptyStatementQuickfix_description;
  }

  @Override
  public String getLabel() {
    return Messages.EmptyStatementQuickfix_label;
  }

  @Override
  public Image getImage() {
    return CheckstyleUIPluginImages.CORRECTION_REMOVE.getImage();
  }

}
