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

package com.atlassw.tools.eclipse.checkstyle.quickfixes.design;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractASTResolution;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginImages;

/**
 * Quickfix implementation which adds the final modifiers a method declaration.
 * 
 * @author Levon Saldamli
 * @author Lars Ködderitzsch
 */
public class DesignForExtensionQuickfix extends AbstractASTResolution
{

    /**
     * List containing modifier keywords in the order proposed by Java Language
     * specification, sections 8.1.1, 8.3.1 and 8.4.3.
     */
    private static final List MODIFIER_ORDER = Arrays.asList(new Object[] {
        ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.PROTECTED_KEYWORD,
        ModifierKeyword.PRIVATE_KEYWORD, ModifierKeyword.ABSTRACT_KEYWORD,
        ModifierKeyword.STATIC_KEYWORD, ModifierKeyword.FINAL_KEYWORD,
        ModifierKeyword.TRANSIENT_KEYWORD, ModifierKeyword.VOLATILE_KEYWORD,
        ModifierKeyword.SYNCHRONIZED_KEYWORD, ModifierKeyword.NATIVE_KEYWORD,
        ModifierKeyword.STRICTFP_KEYWORD, });

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

                    if (!Modifier.isFinal(node.getModifiers()))
                    {

                        MethodDeclaration copy = (MethodDeclaration) ASTNode.copySubtree(node
                                .getAST(), node);

                        Modifier finalModifier = node.getAST().newModifier(
                                ModifierKeyword.FINAL_KEYWORD);
                        copy.modifiers().add(finalModifier);

                        // create new modifier nodes
                        List newModifiers = new ArrayList();
                        Iterator it = copy.modifiers().iterator();
                        while (it.hasNext())
                        {

                            Modifier tmp = (Modifier) it.next();
                            newModifiers.add(node.getAST().newModifier(tmp.getKeyword()));
                        }

                        // oder modifiers to correct order
                        Collections.sort(newModifiers, new Comparator()
                        {
                            public int compare(Object arg0, Object arg1)
                            {
                                Modifier m1 = (Modifier) arg0;
                                Modifier m2 = (Modifier) arg1;

                                int modifierIndex1 = MODIFIER_ORDER.indexOf(m1.getKeyword());
                                int modifierIndex2 = MODIFIER_ORDER.indexOf(m2.getKeyword());

                                return new Integer(modifierIndex1).compareTo(new Integer(
                                        modifierIndex2));
                            }
                        });

                        // replace modifiers
                        copy.modifiers().clear();
                        copy.modifiers().addAll(newModifiers);

                        astRewrite.replace(node, copy, null);
                    }

                    return true;
                }
                return false;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription()
    {
        return Messages.DesignForExtensionQuickfix_description;
    }

    /**
     * {@inheritDoc}
     */
    public String getLabel()
    {
        return Messages.DesignForExtensionQuickfix_label;
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage()
    {
        return CheckstylePluginImages.getImage(CheckstylePluginImages.CORRECTION_ADD);
    }

    /**
     * {@inheritDoc}
     */
    protected boolean handleGetCreateOnlyPartialAST()
    {
        // needed because method body seems not to be in AST otherwise
        return false;
    }
}
