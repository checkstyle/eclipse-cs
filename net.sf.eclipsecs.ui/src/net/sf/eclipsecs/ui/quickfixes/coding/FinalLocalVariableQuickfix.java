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
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.quickfixes.AbstractASTResolution;
import net.sf.eclipsecs.ui.quickfixes.Messages;

/**
 * Quickfix implementation which adds final modifiers to parameters in method
 * declarations.
 *
 */
public class FinalLocalVariableQuickfix extends AbstractASTResolution {

  @Override
  protected ASTVisitor handleGetCorrectingASTVisitor(final IRegion lineInfo,
          final int markerStartOffset) {
    return new ASTVisitor() {

      @SuppressWarnings("unchecked")
      @Override
      public boolean visit(SingleVariableDeclaration node) {
        if (containsPosition(node, markerStartOffset) && !Modifier.isFinal(node.getModifiers())) {
          if (!Modifier.isFinal(node.getModifiers())) {
            Modifier finalModifier = node.getAST().newModifier(ModifierKeyword.FINAL_KEYWORD);
            node.modifiers().add(finalModifier);
          }
        }
        return true;
      }

      @SuppressWarnings("unchecked")
      @Override
      public boolean visit(VariableDeclarationStatement node) {
        if (containsPosition(node, markerStartOffset) && !Modifier.isFinal(node.getModifiers())) {
          if (!Modifier.isFinal(node.getModifiers())) {
            Modifier finalModifier = node.getAST().newModifier(ModifierKeyword.FINAL_KEYWORD);
            node.modifiers().add(finalModifier);
          }
        }
        return true;
      }
    };
  }

  @Override
  public String getDescription() {
    return Messages.FinalLocalVariableQuickfix_description;
  }

  @Override
  public String getLabel() {
    return Messages.FinalLocalVariableQuickfix_label;
  }

  @Override
  public Image getImage() {
    return CheckstyleUIPluginImages.CORRECTION_ADD.getImage();
  }

}
