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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.quickfixes.AbstractASTResolution;
import net.sf.eclipsecs.ui.quickfixes.Messages;

/**
 * Quickfix implementation that moves the default case of a switch statement to
 * the last position.
 *
 */
public class DefaultComesLastQuickfix extends AbstractASTResolution {

  @Override
  protected ASTVisitor handleGetCorrectingASTVisitor(final IRegion lineInfo,
          final int markerStartOffset) {

    return new ASTVisitor() {

      @SuppressWarnings("unchecked")
      @Override
      public boolean visit(SwitchCase node) {

        if (containsPosition(lineInfo, node.getStartPosition())) {

          if (node.isDefault() && !isLastSwitchCase(node)) {
            SwitchStatement switchStatement = (SwitchStatement) node.getParent();

            List<ASTNode> defaultCaseStatements = new ArrayList<>();
            defaultCaseStatements.add(node);

            // collect all statements belonging to the default case
            int defaultStatementIndex = switchStatement.statements().indexOf(node);
            for (int i = defaultStatementIndex + 1; i < switchStatement.statements().size(); i++) {
              ASTNode tmpNode = (ASTNode) switchStatement.statements().get(i);

              if (tmpNode instanceof SwitchCase) {
                break;
              } else {
                defaultCaseStatements.add(tmpNode);
              }
            }

            // move the statements to the end of the statement list
            switchStatement.statements().removeAll(defaultCaseStatements);
            switchStatement.statements().addAll(defaultCaseStatements);
          }
        }
        return true;
      }

      private boolean isLastSwitchCase(SwitchCase switchCase) {

        SwitchStatement switchStatement = (SwitchStatement) switchCase.getParent();

        // collect all statements belonging to the default case
        int defaultStatementIndex = switchStatement.statements().indexOf(switchCase);
        for (int i = defaultStatementIndex + 1; i < switchStatement.statements().size(); i++) {
          ASTNode tmpNode = (ASTNode) switchStatement.statements().get(i);

          if (tmpNode instanceof SwitchCase) {
            return false;
          }
        }
        return true;
      }
    };
  }

  @Override
  public String getDescription() {
    return Messages.DefaultComesLastQuickfix_description;
  }

  @Override
  public String getLabel() {
    return Messages.DefaultComesLastQuickfix_label;
  }

  @Override
  public Image getImage() {
    return CheckstyleUIPluginImages.CORRECTION_CHANGE.getImage();
  }
}
