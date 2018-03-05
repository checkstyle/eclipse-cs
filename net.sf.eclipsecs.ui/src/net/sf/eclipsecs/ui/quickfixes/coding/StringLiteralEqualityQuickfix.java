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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.quickfixes.AbstractASTResolution;
import net.sf.eclipsecs.ui.quickfixes.Messages;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

/**
 * Quickfix implementation that replaces a string literal comparison using == or
 * != with a proper equals() comparison.
 *
 * @author Lars Ködderitzsch
 */
public class StringLiteralEqualityQuickfix extends AbstractASTResolution {

  /**
   * {@inheritDoc}
   */
  @Override
  protected ASTVisitor handleGetCorrectingASTVisitor(final IRegion lineInfo,
          final int markerStartPosition) {

    return new ASTVisitor() {

      @SuppressWarnings("unchecked")
      @Override
      public boolean visit(InfixExpression node) {

        if (containsPosition(lineInfo, node.getStartPosition())) {

          StringLiteral literal = null;
          Expression otherOperand = null;

          if (node.getLeftOperand() instanceof StringLiteral) {
            literal = (StringLiteral) node.getLeftOperand();
            otherOperand = node.getRightOperand();
          } else if (node.getRightOperand() instanceof StringLiteral) {
            literal = (StringLiteral) node.getRightOperand();
            otherOperand = node.getLeftOperand();
          } else {
            return true;
          }

          Expression replacementNode = null;

          MethodInvocation equalsInvocation = node.getAST().newMethodInvocation();
          equalsInvocation.setName(node.getAST().newSimpleName("equals")); //$NON-NLS-1$
          equalsInvocation.setExpression((Expression) ASTNode.copySubtree(node.getAST(), literal));
          equalsInvocation.arguments().add(ASTNode.copySubtree(node.getAST(), otherOperand));

          // if the string was compared with != create a not
          // expression
          if (node.getOperator().equals(InfixExpression.Operator.NOT_EQUALS)) {
            PrefixExpression prefixExpression = node.getAST().newPrefixExpression();
            prefixExpression.setOperator(PrefixExpression.Operator.NOT);
            prefixExpression.setOperand(equalsInvocation);
            replacementNode = prefixExpression;
          } else {
            replacementNode = equalsInvocation;
          }

          replaceNode(node, replacementNode);
        }
        return true;
      }

      /**
       * Replaces the given node with the replacement node (using reflection
       * since I am not aware of a proper API to do this).
       *
       * @param node
       *          the node to replace
       * @param replacementNode
       *          the replacement
       */
      private void replaceNode(ASTNode node, ASTNode replacementNode) {

        try {
          if (node.getLocationInParent().isChildProperty()) {

            String property = node.getLocationInParent().getId();

            String capitalizedProperty = property.substring(0, 1).toUpperCase()
                    + property.substring(1);
            String setterMethodName = "set" + capitalizedProperty;

            Class<?> testClass = node.getClass();

            while (testClass != null) {

              try {
                Method setterMethod = node.getParent().getClass().getMethod(setterMethodName,
                        testClass);
                setterMethod.invoke(node.getParent(), replacementNode);
                break;
              } catch (NoSuchMethodException e) {
                testClass = testClass.getSuperclass();
              }
            }

          } else if (node.getLocationInParent().isChildListProperty()) {
            Method listMethod = node.getParent().getClass()
                    .getMethod(node.getLocationInParent().getId(), (Class<?>[]) null);
            @SuppressWarnings("unchecked")
            List<ASTNode> list = (List<ASTNode>) listMethod.invoke(node.getParent(), (Object[]) null);
            list.set(list.indexOf(node), replacementNode);
          }
        } catch (InvocationTargetException e) {
          CheckstyleLog.log(e);
        } catch (IllegalAccessException e) {
          CheckstyleLog.log(e);
        } catch (NoSuchMethodException e) {
          CheckstyleLog.log(e);
        }
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDescription() {
    return Messages.StringLiteralEqualityQuickfix_description;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getLabel() {
    return Messages.StringLiteralEqualityQuickfix_label;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Image getImage() {
    return CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.CORRECTION_CHANGE);
  }
}
