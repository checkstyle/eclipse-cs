//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch
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
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package net.sf.eclipsecs.ui.quickfixes.blocks;

import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.quickfixes.AbstractASTResolution;
import net.sf.eclipsecs.ui.quickfixes.Messages;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

/**
 * Quickfix implementation that adds braces to if/for/while/do statements.
 *
 * @author Lars Ködderitzsch
 */
public class NeedBracesQuickfix extends AbstractASTResolution {

  /**
   * {@inheritDoc}
   */
  @Override
  protected ASTVisitor handleGetCorrectingASTVisitor(final IRegion lineInfo,
          final int markerStartOffset) {

    return new ASTVisitor() {
      @Override
      public boolean visit(IfStatement node) {

        int nodePos = node.getStartPosition();
        int nodeEnd = nodePos + node.getLength();
        if ((nodePos >= lineInfo.getOffset()
                && nodePos <= (lineInfo.getOffset() + lineInfo.getLength()))
                || (nodePos <= lineInfo.getOffset()
                        && nodeEnd >= lineInfo.getOffset() + lineInfo.getLength())) {
          bracifyIfStatement(node);
        }

        return true;
      }

      @Override
      public boolean visit(ForStatement node) {
        if (containsPosition(lineInfo, node.getStartPosition())) {
          Block block = createBracifiedCopy(node.getAST(), node.getBody());
          node.setBody(block);
        }

        return true;
      }

      @Override
      public boolean visit(DoStatement node) {
        if (containsPosition(lineInfo, node.getStartPosition())) {
          Block block = createBracifiedCopy(node.getAST(), node.getBody());
          node.setBody(block);
        }

        return true;
      }

      @Override
      public boolean visit(WhileStatement node) {
        if (containsPosition(lineInfo, node.getStartPosition())) {
          Block block = createBracifiedCopy(node.getAST(), node.getBody());
          node.setBody(block);
        }

        return true;
      }

      /**
       * Helper method to recursively bracify a if-statement.
       *
       * @param ifStatement
       *          the if statement
       */
      private void bracifyIfStatement(IfStatement ifStatement) {

        // change the then statement to a block if necessary
        if (!(ifStatement.getThenStatement() instanceof Block)) {
          if (ifStatement.getThenStatement() instanceof IfStatement) {
            bracifyIfStatement((IfStatement) ifStatement.getThenStatement());
          }
          Block block = createBracifiedCopy(ifStatement.getAST(), ifStatement.getThenStatement());
          ifStatement.setThenStatement(block);
        }

        // check the else statement if it is a block
        Statement elseStatement = ifStatement.getElseStatement();
        if (elseStatement != null && !(elseStatement instanceof Block)) {

          // in case the else statement is an further if statement
          // (else if)
          // do the recursion
          if (elseStatement instanceof IfStatement) {
            bracifyIfStatement((IfStatement) elseStatement);
          } else {
            // change the else statement to a block
            // Block block = ifStatement.getAST().newBlock();
            // block.statements().add(ASTNode.copySubtree(block.getAST(),
            // elseStatement));
            Block block = createBracifiedCopy(ifStatement.getAST(), ifStatement.getElseStatement());
            ifStatement.setElseStatement(block);
          }
        }
      }

      @SuppressWarnings("unchecked")
      private Block createBracifiedCopy(AST ast, Statement body) {
        Block block = ast.newBlock();
        block.statements().add(ASTNode.copySubtree(block.getAST(), body));
        return block;
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDescription() {
    return Messages.NeedBracesQuickfix_description;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getLabel() {
    return Messages.NeedBracesQuickfix_label;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Image getImage() {
    return CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.CORRECTION_ADD);
  }
}
