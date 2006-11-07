//============================================================================
//
// Copyright (C) 2002-2006  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.quickfixes.misc;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractASTResolution;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginImages;

/**
 * Quickfix implementation which adds final modifiers to parameters in method
 * declarations.
 * 
 * @author Levon Saldamli
 * @author Lars Ködderitzsch
 */
public class FinalParametersQuickfix extends AbstractASTResolution
{

    /** The length of the javadoc comment declaration. */
    private static final int JAVADOC_COMMENT_LENGTH = 6;

    /**
     * {@inheritDoc}
     */
    protected ASTVisitor handleGetCorrectingASTVisitor(final ASTRewrite astRewrite,
            final IRegion lineInfo)
    {
        return new ASTVisitor()
        {

            public boolean visit(MethodDeclaration node)
            {
                // recalculate start position because optional javadoc is mixed
                // into the original start position
                int pos = node.getStartPosition()
                        + (node.getJavadoc() != null ? node.getJavadoc().getLength()
                                + JAVADOC_COMMENT_LENGTH : 0);
                if (pos >= lineInfo.getOffset()
                        && pos <= (lineInfo.getOffset() + lineInfo.getLength()))
                {

                    List parameters = node.parameters();
                    Iterator it = parameters.iterator();
                    while (it.hasNext())
                    {
                        // add final keyword to non-final parameters
                        SingleVariableDeclaration param = (SingleVariableDeclaration) it.next();
                        if (!Modifier.isFinal(param.getModifiers()))
                        {
                            SingleVariableDeclaration copy = (SingleVariableDeclaration) ASTNode
                                    .copySubtree(node.getAST(), param);
                            Modifier finalModifier = node.getAST().newModifier(
                                    ModifierKeyword.FINAL_KEYWORD);
                            copy.modifiers().add(finalModifier);

                            astRewrite.replace(param, copy, null);
                        }
                    }
                }
                return true;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription()
    {
        return Messages.FinalParametersQuickfix_description;
    }

    /**
     * {@inheritDoc}
     */
    public String getLabel()
    {
        return Messages.FinalParametersQuickfix_label;
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage()
    {
        return CheckstylePluginImages.getImage(CheckstylePluginImages.CORRECTION_ADD);
    }

}
