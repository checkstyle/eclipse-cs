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

package net.sf.eclipsecs.ui.quickfixes.modifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.quickfixes.AbstractASTResolution;
import net.sf.eclipsecs.ui.quickfixes.Messages;

/**
 * Quickfix implementation that removes redundant modifiers.
 *
 */
public class RedundantModifierQuickfix extends AbstractASTResolution {

  /** The length of the javadoc comment declaration. */
  private static final int JAVADOC_COMMENT_LENGTH = 6;

  @Override
  protected ASTVisitor handleGetCorrectingASTVisitor(final IRegion lineInfo,
          final int markerStartOffset) {

    return new ASTVisitor() {

      @SuppressWarnings("unchecked")
      @Override
      public boolean visit(MethodDeclaration node) {

        if (containsPosition(node, markerStartOffset)) {
          List<ModifierKeyword> redundantKeyWords = Collections.emptyList();

          if (node.getParent() instanceof TypeDeclaration) {
            TypeDeclaration type = (TypeDeclaration) node.getParent();
            if (type.isInterface()) {
              redundantKeyWords = Arrays
                      .asList(new ModifierKeyword[] { ModifierKeyword.PUBLIC_KEYWORD,
                          ModifierKeyword.ABSTRACT_KEYWORD, ModifierKeyword.FINAL_KEYWORD });
            } else if (Modifier.isFinal(type.getModifiers())) {
              redundantKeyWords = Arrays
                      .asList(new ModifierKeyword[] { ModifierKeyword.FINAL_KEYWORD });
            }
          }

          deleteRedundantModifiers(node.modifiers(), redundantKeyWords);
        }
        return true;
      }

      @SuppressWarnings("unchecked")
      @Override
      public boolean visit(FieldDeclaration node) {
        // recalculate start position because optional javadoc is mixed
        // into the original start position
        int pos = node.getStartPosition() + (node.getJavadoc() != null
                ? node.getJavadoc().getLength() + JAVADOC_COMMENT_LENGTH
                : 0);
        if (containsPosition(lineInfo, pos)) {
          List<ModifierKeyword> redundantKeyWords = Collections.emptyList();

          if (node.getParent() instanceof TypeDeclaration) {
            TypeDeclaration type = (TypeDeclaration) node.getParent();
            if (type.isInterface()) {
              redundantKeyWords = Arrays.asList(new ModifierKeyword[] {
                  ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.ABSTRACT_KEYWORD,
                  ModifierKeyword.FINAL_KEYWORD, ModifierKeyword.STATIC_KEYWORD });
            }
          } else if (node.getParent() instanceof AnnotationTypeDeclaration) {

            redundantKeyWords = Arrays.asList(new ModifierKeyword[] {
                ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.ABSTRACT_KEYWORD,
                ModifierKeyword.FINAL_KEYWORD, ModifierKeyword.STATIC_KEYWORD });
          }

          deleteRedundantModifiers(node.modifiers(), redundantKeyWords);
        }
        return true;
      }

      @SuppressWarnings("unchecked")
      @Override
      public boolean visit(AnnotationTypeMemberDeclaration node) {

        // recalculate start position because optional javadoc is mixed
        // into the original start position
        int pos = node.getStartPosition() + (node.getJavadoc() != null
                ? node.getJavadoc().getLength() + JAVADOC_COMMENT_LENGTH
                : 0);
        if (containsPosition(lineInfo, pos)) {

          if (node.getParent() instanceof AnnotationTypeDeclaration) {

            List<ModifierKeyword> redundantKeyWords = Arrays.asList(new ModifierKeyword[] {
                ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.ABSTRACT_KEYWORD,
                ModifierKeyword.FINAL_KEYWORD, ModifierKeyword.STATIC_KEYWORD });

            deleteRedundantModifiers(node.modifiers(), redundantKeyWords);
          }

        }
        return true;
      }

      private void deleteRedundantModifiers(List<ASTNode> modifiers,
              List<ModifierKeyword> redundantModifierKeywords) {

        Iterator<ASTNode> iter = modifiers.iterator();

        while (iter.hasNext()) {
          ASTNode node = iter.next();

          if (node instanceof Modifier) {
            Modifier modifier = (Modifier) node;
            if (redundantModifierKeywords.contains(modifier.getKeyword())) {
              iter.remove();
            }
          }
        }
      }
    };
  }

  @Override
  public String getDescription() {
    return Messages.RedundantModifierQuickfix_description;
  }

  @Override
  public String getLabel() {
    return Messages.RedundantModifierQuickfix_label;
  }

  @Override
  public Image getImage() {
    return CheckstyleUIPluginImages.CORRECTION_REMOVE.getImage();
  }

}
