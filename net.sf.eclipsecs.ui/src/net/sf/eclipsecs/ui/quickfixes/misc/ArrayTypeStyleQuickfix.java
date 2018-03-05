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

package net.sf.eclipsecs.ui.quickfixes.misc;

import java.util.Iterator;
import java.util.List;

import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.quickfixes.AbstractASTResolution;
import net.sf.eclipsecs.ui.quickfixes.Messages;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

/**
 * Quickfix implementation which moves the array declaration (C-style to
 * Java-style and reverse).
 * 
 * @author Lars Ködderitzsch
 */
public class ArrayTypeStyleQuickfix extends AbstractASTResolution {

  /**
   * {@inheritDoc}
   */
  @Override
  protected ASTVisitor handleGetCorrectingASTVisitor(final IRegion lineInfo,
          final int markerStartOffset) {

    return new ASTVisitor() {

      @Override
      public boolean visit(VariableDeclarationStatement node) {

        if (containsPosition(node, markerStartOffset)) {

          if (isCStyle(node.fragments())) {

            int dimensions = 0;

            List<?> fragments = node.fragments();
            for (int i = 0, size = fragments.size(); i < size; i++) {
              VariableDeclaration decl = (VariableDeclaration) fragments.get(i);
              if (decl.getExtraDimensions() > dimensions) {
                dimensions = decl.getExtraDimensions();

              }
              decl.setExtraDimensions(0);
            }

            // wrap current type into ArrayType
            ArrayType arrayType = createArrayType(node.getType(), dimensions);
            node.setType(arrayType);

          } else if (isJavaStyle(node.getType())) {

            int dimensions = ((ArrayType) node.getType()).getDimensions();

            List<?> fragments = node.fragments();
            for (int i = 0, size = fragments.size(); i < size; i++) {
              VariableDeclaration decl = (VariableDeclaration) fragments.get(i);
              decl.setExtraDimensions(dimensions);
            }

            Type elementType = (Type) ASTNode.copySubtree(node.getAST(),
                    ((ArrayType) node.getType()).getElementType());
            node.setType(elementType);
          }
        }
        return true;
      }

      @Override
      public boolean visit(SingleVariableDeclaration node) {

        if (containsPosition(node, markerStartOffset)) {
          if (isCStyle(node)) {
            // wrap the existing type into an array type
            node.setType(createArrayType(node.getType(), node.getExtraDimensions()));
            node.setExtraDimensions(0);
          } else if (isJavaStyle(node.getType())) {

            ArrayType arrayType = (ArrayType) node.getType();
            Type elementType = (Type) ASTNode.copySubtree(node.getAST(),
                    arrayType.getElementType());

            node.setType(elementType);
            node.setExtraDimensions(arrayType.getDimensions());
          }
        }

        return true;
      }

      @Override
      public boolean visit(FieldDeclaration node) {

        if (containsPosition(node, markerStartOffset)) {

          if (isCStyle(node.fragments())) {

            int dimensions = 0;

            List<?> fragments = node.fragments();
            for (int i = 0, size = fragments.size(); i < size; i++) {
              VariableDeclaration decl = (VariableDeclaration) fragments.get(i);
              if (decl.getExtraDimensions() > dimensions) {
                dimensions = decl.getExtraDimensions();

              }
              decl.setExtraDimensions(0);
            }

            // wrap current type into ArrayType
            ArrayType arrayType = createArrayType(node.getType(), dimensions);
            node.setType(arrayType);
          } else if (isJavaStyle(node.getType())) {

            int dimensions = ((ArrayType) node.getType()).getDimensions();

            List<?> fragments = node.fragments();
            for (int i = 0, size = fragments.size(); i < size; i++) {
              VariableDeclaration decl = (VariableDeclaration) fragments.get(i);
              decl.setExtraDimensions(dimensions);
            }

            Type elementType = (Type) ASTNode.copySubtree(node.getAST(),
                    ((ArrayType) node.getType()).getElementType());
            node.setType(elementType);
          }
        }
        return true;
      }

      private boolean isJavaStyle(Type type) {
        return type instanceof ArrayType;
      }

      private boolean isCStyle(VariableDeclaration decl) {
        return decl.getExtraDimensions() > 0;
      }

      private boolean isCStyle(List<?> fragments) {

        Iterator<?> it = fragments.iterator();
        while (it.hasNext()) {
          VariableDeclaration decl = (VariableDeclaration) it.next();
          if (isCStyle(decl)) {
            return true;
          }
        }
        return false;
      }

      private ArrayType createArrayType(Type componentType, int dimensions) {
        Type type = (Type) ASTNode.copySubtree(componentType.getAST(), componentType);
        ArrayType arrayType = componentType.getAST().newArrayType(type, dimensions);

        return arrayType;
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDescription() {
    return Messages.ArrayTypeStyleQuickfix_description;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getLabel() {
    return Messages.ArrayTypeStyleQuickfix_label;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Image getImage() {
    return CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.CORRECTION_CHANGE);
  }

}
