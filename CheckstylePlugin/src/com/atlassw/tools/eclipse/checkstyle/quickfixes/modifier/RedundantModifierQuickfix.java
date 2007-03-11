//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.quickfixes.modifier;

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
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractASTResolution;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginImages;

/**
 * Quickfix implementation that removes redundant modifiers.
 * 
 * @author Lars Ködderitzsch
 */
public class RedundantModifierQuickfix extends AbstractASTResolution
{

    /** The length of the javadoc comment declaration. */
    private static final int JAVADOC_COMMENT_LENGTH = 6;

    /**
     * {@inheritDoc}
     */
    protected ASTVisitor handleGetCorrectingASTVisitor(final IRegion lineInfo,
            final int markerStartOffset)
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
                if (containsPosition(lineInfo, pos))
                {

                    List redundantKeyWords = Collections.EMPTY_LIST;

                    if (node.getParent() instanceof TypeDeclaration)
                    {
                        TypeDeclaration type = (TypeDeclaration) node.getParent();
                        if (type.isInterface())
                        {
                            redundantKeyWords = Arrays.asList(new Object[] {
                                ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.ABSTRACT_KEYWORD,
                                ModifierKeyword.FINAL_KEYWORD });
                        }
                        else if (Modifier.isFinal(type.getModifiers()))
                        {
                            redundantKeyWords = Arrays
                                    .asList(new Object[] { ModifierKeyword.FINAL_KEYWORD });
                        }
                    }

                    deleteRedundantModifiers(node.modifiers(), redundantKeyWords);
                }
                return true;
            }

            public boolean visit(FieldDeclaration node)
            {
                // recalculate start position because optional javadoc is mixed
                // into the original start position
                int pos = node.getStartPosition()
                        + (node.getJavadoc() != null ? node.getJavadoc().getLength()
                                + JAVADOC_COMMENT_LENGTH : 0);
                if (containsPosition(lineInfo, pos))
                {
                    List redundantKeyWords = Collections.EMPTY_LIST;

                    if (node.getParent() instanceof TypeDeclaration)
                    {
                        TypeDeclaration type = (TypeDeclaration) node.getParent();
                        if (type.isInterface())
                        {
                            redundantKeyWords = Arrays.asList(new Object[] {
                                ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.ABSTRACT_KEYWORD,
                                ModifierKeyword.FINAL_KEYWORD, ModifierKeyword.STATIC_KEYWORD });
                        }
                    }
                    else if (node.getParent() instanceof AnnotationTypeDeclaration)
                    {

                        redundantKeyWords = Arrays.asList(new Object[] {
                            ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.ABSTRACT_KEYWORD,
                            ModifierKeyword.FINAL_KEYWORD, ModifierKeyword.STATIC_KEYWORD });
                    }

                    deleteRedundantModifiers(node.modifiers(), redundantKeyWords);
                }
                return true;
            }

            public boolean visit(AnnotationTypeMemberDeclaration node)
            {

                // recalculate start position because optional javadoc is mixed
                // into the original start position
                int pos = node.getStartPosition()
                        + (node.getJavadoc() != null ? node.getJavadoc().getLength()
                                + JAVADOC_COMMENT_LENGTH : 0);
                if (containsPosition(lineInfo, pos))
                {

                    if (node.getParent() instanceof AnnotationTypeDeclaration)
                    {

                        List redundantKeyWords = Arrays.asList(new Object[] {
                            ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.ABSTRACT_KEYWORD,
                            ModifierKeyword.FINAL_KEYWORD, ModifierKeyword.STATIC_KEYWORD });

                        deleteRedundantModifiers(node.modifiers(), redundantKeyWords);
                    }

                }
                return true;
            }

            private void deleteRedundantModifiers(List modifiers, List redundantModifierKeywords)
            {
                Iterator it = modifiers.iterator();

                while (it.hasNext())
                {
                    ASTNode node = (ASTNode) it.next();

                    if (node instanceof Modifier)
                    {
                        Modifier modifier = (Modifier) node;
                        if (redundantModifierKeywords.contains(modifier.getKeyword()))
                        {
                            it.remove();
                        }
                    }
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription()
    {
        return Messages.RedundantModifierQuickfix_description;
    }

    /**
     * {@inheritDoc}
     */
    public String getLabel()
    {
        return Messages.RedundantModifierQuickfix_label;
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage()
    {
        return CheckstylePluginImages.getImage(CheckstylePluginImages.CORRECTION_REMOVE);
    }

}
