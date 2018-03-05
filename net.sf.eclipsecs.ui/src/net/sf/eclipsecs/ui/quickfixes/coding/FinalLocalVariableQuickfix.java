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

import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.quickfixes.AbstractASTResolution;
import net.sf.eclipsecs.ui.quickfixes.Messages;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

/**
 * Quickfix implementation which adds final modifiers to parameters in method
 * declarations.
 * 
 * @author Levon Saldamli
 * @author Lars Ködderitzsch
 */
public class FinalLocalVariableQuickfix extends AbstractASTResolution {

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDescription() {
    return Messages.FinalLocalVariableQuickfix_description;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getLabel() {
    return Messages.FinalLocalVariableQuickfix_label;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Image getImage() {
    return CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.CORRECTION_ADD);
  }

}
