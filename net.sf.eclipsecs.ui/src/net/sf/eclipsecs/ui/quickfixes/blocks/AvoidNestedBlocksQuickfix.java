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

package net.sf.eclipsecs.ui.quickfixes.blocks;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.quickfixes.AbstractASTResolution;
import net.sf.eclipsecs.ui.quickfixes.Messages;

/**
 * Quickfix implementation that removes nested blocks.
 *
 */
public class AvoidNestedBlocksQuickfix extends AbstractASTResolution {

  @Override
  protected ASTVisitor handleGetCorrectingASTVisitor(final IRegion lineInfo,
          final int markerStartOffset) {

    return new ASTVisitor() {

      @SuppressWarnings("unchecked")
      @Override
      public boolean visit(Block node) {

        if (containsPosition(lineInfo, node.getStartPosition())) {

          if (node.getParent() instanceof Block) {

            List<?> statements = ((Block) node.getParent()).statements();
            int index = statements.indexOf(node);

            statements.remove(node);
            statements.addAll(index, ASTNode.copySubtrees(node.getAST(), node.statements()));

          } else if (node.getParent() instanceof SwitchStatement) {

            List<?> statements = ((SwitchStatement) node.getParent()).statements();
            int index = statements.indexOf(node);

            statements.remove(node);
            statements.addAll(index, ASTNode.copySubtrees(node.getAST(), node.statements()));
          }
        }
        return true;
      }
    };
  }

  @Override
  public String getDescription() {
    return Messages.AvoidNestedBlocksQuickfix_description;
  }

  @Override
  public String getLabel() {
    return Messages.AvoidNestedBlocksQuickfix_label;
  }

  @Override
  public Image getImage() {
    return CheckstyleUIPluginImages.CORRECTION_REMOVE.getImage();
  }
}
