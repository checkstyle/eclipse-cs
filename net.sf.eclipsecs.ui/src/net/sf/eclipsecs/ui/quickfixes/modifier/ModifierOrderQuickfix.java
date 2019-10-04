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

package net.sf.eclipsecs.ui.quickfixes.modifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.quickfixes.AbstractASTResolution;
import net.sf.eclipsecs.ui.quickfixes.Messages;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

/**
 * Quickfix implementation that orders modifiers into the suggested order by the JLS.
 *
 * @author Lars Ködderitzsch
 */
public class ModifierOrderQuickfix extends AbstractASTResolution {

  /**
   * List containing modifier keywords in the order proposed by Java Language specification,
   * sections 8.1.1, 8.3.1 and 8.4.3.
   */
  private static final List<Object> MODIFIER_ORDER = Arrays
          .asList(new Object[] { ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.PROTECTED_KEYWORD,
              ModifierKeyword.PRIVATE_KEYWORD, ModifierKeyword.ABSTRACT_KEYWORD,
              ModifierKeyword.STATIC_KEYWORD, ModifierKeyword.FINAL_KEYWORD,
              ModifierKeyword.TRANSIENT_KEYWORD, ModifierKeyword.VOLATILE_KEYWORD,
              ModifierKeyword.SYNCHRONIZED_KEYWORD, ModifierKeyword.NATIVE_KEYWORD,
              ModifierKeyword.STRICTFP_KEYWORD,
              "default", /*
                          * issue 371: can't use this yet since were still supporting
                          * non-Java8-enabled eclipse versions ModifierKeyword.DEFAULT_KEYWORD,
                          */ });

  /**
   * Reorders the given list of <code>Modifier</code> nodes into their suggested order by the JLS.
   *
   * @param modifiers
   *          the list of modifiers to reorder
   * @return the reordered list of modifiers
   */
  public static List<ASTNode> reOrderModifiers(List<ASTNode> modifiers) {

    List<ASTNode> copies = new ArrayList<>();
    Iterator<ASTNode> it = modifiers.iterator();
    while (it.hasNext()) {
      ASTNode mod = it.next();
      copies.add(ASTNode.copySubtree(mod.getAST(), mod));
    }

    // oder modifiers to correct order
    Collections.sort(copies, new Comparator<ASTNode>() {
      @Override
      public int compare(ASTNode arg0, ASTNode arg1) {
        if (!(arg0 instanceof Modifier) || !(arg1 instanceof Modifier)) {
          return 0;
        }

        Modifier m1 = (Modifier) arg0;
        Modifier m2 = (Modifier) arg1;

        int modifierIndex1 = MODIFIER_ORDER.indexOf(m1.getKeyword());
        int modifierIndex2 = MODIFIER_ORDER.indexOf(m2.getKeyword());

        return Integer.valueOf(modifierIndex1).compareTo(Integer.valueOf(modifierIndex2));
      }
    });

    return copies;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ASTVisitor handleGetCorrectingASTVisitor(final IRegion lineInfo,
          final int markerStartOffset) {

    return new ASTVisitor() {

      @Override
      public boolean visit(TypeDeclaration node) {
        return visitBodyDecl(node);
      }

      @Override
      public boolean visit(MethodDeclaration node) {
        return visitBodyDecl(node);
      }

      @Override
      public boolean visit(FieldDeclaration node) {
        return visitBodyDecl(node);
      }

      @Override
      public boolean visit(AnnotationTypeMemberDeclaration node) {
        return visitBodyDecl(node);
      }

      @SuppressWarnings("unchecked")
      private boolean visitBodyDecl(BodyDeclaration node) {
        List<Modifier> modifiers = (List<Modifier>) node.modifiers().stream()
                .filter(Modifier.class::isInstance).map(Modifier.class::cast)
                .collect(Collectors.toList());
        if (modifiers == null || modifiers.isEmpty()) {
          return true;
        }
        // find the range from first to last modifier. marker must be in between
        int minPos = modifiers.stream().mapToInt(Modifier::getStartPosition).min().getAsInt();
        int maxPos = modifiers.stream().mapToInt(Modifier::getStartPosition).max().getAsInt();

        if (minPos <= markerStartOffset && markerStartOffset <= maxPos) {
          List<?> reorderedModifiers = reOrderModifiers(node.modifiers());
          node.modifiers().clear();
          node.modifiers().addAll(reorderedModifiers);
        }
        return true;
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDescription() {
    return Messages.ModifierOrderQuickfix_description;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getLabel() {
    return Messages.ModifierOrderQuickfix_label;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Image getImage() {
    return CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.CORRECTION_CHANGE);
  }

}
