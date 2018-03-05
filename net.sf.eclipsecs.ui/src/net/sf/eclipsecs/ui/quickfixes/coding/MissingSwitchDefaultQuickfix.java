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
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

/**
 * Quickfix implementation that add a missing default statement to a switch
 * case.
 * 
 * @author Levon Saldamli
 */
public class MissingSwitchDefaultQuickfix extends AbstractASTResolution {

  /**
   * {@inheritDoc}
   */
  @Override
  protected ASTVisitor handleGetCorrectingASTVisitor(final IRegion lineInfo,
          final int markerStartOffset) {

    return new ASTVisitor() {

      @SuppressWarnings("unchecked")
      @Override
      public boolean visit(SwitchStatement node) {
        if (containsPosition(lineInfo, node.getStartPosition())) {
          SwitchCase defNode = node.getAST().newSwitchCase();
          defNode.setExpression(null);
          node.statements().add(defNode);
          node.statements().add(node.getAST().newBreakStatement());
        }
        return true; // also visit children
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDescription() {
    return Messages.MissingSwitchDefaultQuickfix_description;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getLabel() {
    return Messages.MissingSwitchDefaultQuickfix_label;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Image getImage() {
    return CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.CORRECTION_ADD);
  }
}
