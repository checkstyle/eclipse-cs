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

package net.sf.eclipsecs.ui.quickfixes.coding;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.quickfixes.AbstractASTResolution;
import net.sf.eclipsecs.ui.quickfixes.Messages;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression.Operator;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

/**
 * Quickfix implementation which simplifies a boolean if/return statement. It transforms an if
 * statement like
 * <pre>
 * if (condition) {
 *   return true;
 * } else {
 *   return false;
 * }
 * </pre>
 * into a return statement like
 * <pre>
 * return condition;
 * </pre>
 *
 * @author Philip Graf
 */
public class SimplifyBooleanReturnQuickfix extends AbstractASTResolution {

  /**
   * If the condition is of one of these expression types, the parentheses are not necessary when
   * negated. I.e the replacement can be written as <code>!condition</code> instead of
   * <code>!(condition)</code>.
   */
  private static final Collection<Class<? extends Expression>> OMIT_PARANETHESES_CLASSES = Arrays
          .asList(BooleanLiteral.class, FieldAccess.class, MethodInvocation.class,
                  QualifiedName.class, SimpleName.class, ParenthesizedExpression.class,
                  SuperFieldAccess.class, SuperMethodInvocation.class, ThisExpression.class);

  /**
   * {@inheritDoc}
   */
  @Override
  protected ASTVisitor handleGetCorrectingASTVisitor(final IRegion lineInfo,
          final int markerStartOffset) {
    return new ASTVisitor() {

      @Override
      public boolean visit(final IfStatement node) {
        if (containsPosition(node, markerStartOffset)) {

          final Boolean isThenStatementTrue = isReturnStatementTrue(node.getThenStatement());

          if (isThenStatementTrue == null) {
            // the AST structure of the if statement is not as expected
            return true;
          }

          final Expression condition = removeNotFromCondition(node.getExpression());
          final boolean isNotCondition = condition != node.getExpression();

          final ReturnStatement replacement;
          if (isThenStatementTrue ^ isNotCondition) {
            // create replacement: return condition;
            replacement = node.getAST().newReturnStatement();
            replacement.setExpression(copy(condition));

          } else {
            // create replacement: return !(condition);
            final AST ast = node.getAST();
            replacement = ast.newReturnStatement();
            final PrefixExpression not = ast.newPrefixExpression();
            not.setOperator(Operator.NOT);
            if (omitParantheses(condition)) {
              not.setOperand(copy(condition));
            } else {
              final ParenthesizedExpression parentheses = ast.newParenthesizedExpression();
              parentheses.setExpression(copy(condition));
              not.setOperand(parentheses);
            }
            replacement.setExpression(not);
          }
          replace(node, replacement);

        }
        return true;
      }

      private Boolean isReturnStatementTrue(final Statement node) {
        if (node instanceof ReturnStatement) {
          final Expression expression = ((ReturnStatement) node).getExpression();
          if (expression instanceof BooleanLiteral) {
            return ((BooleanLiteral) expression).booleanValue();
          }
        } else if (node instanceof Block) {
          // the return statement might be wrapped in a block statement
          @SuppressWarnings("unchecked")
          final List<Statement> statements = ((Block) node).statements();
          if (statements.size() > 0) {
            return isReturnStatementTrue(statements.get(0));
          }
        }
        return null;
      }

      private Expression removeNotFromCondition(final Expression condition) {
        if (condition instanceof PrefixExpression) {
          final PrefixExpression prefix = (PrefixExpression) condition;
          if (PrefixExpression.Operator.NOT.equals(prefix.getOperator())) {
            return prefix.getOperand();
          }
        }
        return condition;
      }

      private boolean omitParantheses(final Expression condition) {
        return OMIT_PARANETHESES_CLASSES.contains(condition.getClass());
      }

    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDescription() {
    return Messages.SimplifyBooleanReturnQuickfix_description;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getLabel() {
    return Messages.SimplifyBooleanReturnQuickfix_label;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Image getImage() {
    return CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.CORRECTION_CHANGE);
  }

}
