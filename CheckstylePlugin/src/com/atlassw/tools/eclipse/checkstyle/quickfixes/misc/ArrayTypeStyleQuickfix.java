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
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractASTResolution;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginImages;

/**
 * Quickfix implementation moves the array declaration.
 * 
 * @author Lars Ködderitzsch
 */
public class ArrayTypeStyleQuickfix extends AbstractASTResolution
{

    /**
     * {@inheritDoc}
     */
    protected ASTVisitor handleGetCorrectingASTVisitor(final ASTRewrite rewrite,
            final IRegion lineInfo, final int markerStartOffset)
    {

        return new ASTVisitor()
        {

            public boolean visit(VariableDeclarationStatement node)
            {

                if (containsPosition(node, markerStartOffset))
                {

                    if (isCStyle(node.fragments()))
                    {

                        VariableDeclarationStatement copy = (VariableDeclarationStatement) ASTNode
                                .copySubtree(node.getAST(), node);

                        int dimensions = 0;

                        List fragments = copy.fragments();
                        for (int i = 0, size = fragments.size(); i < size; i++)
                        {
                            VariableDeclaration decl = (VariableDeclaration) fragments.get(i);
                            if (decl.getExtraDimensions() > dimensions)
                            {
                                dimensions = decl.getExtraDimensions();

                            }
                            decl.setExtraDimensions(0);
                        }

                        // wrap current type into ArrayType
                        ArrayType arrayType = createArrayType(copy.getType(), dimensions);
                        copy.setType(arrayType);

                        rewrite.replace(node, copy, null);
                    }
                    else if (isJavaStyle(node.getType()))
                    {

                        VariableDeclarationStatement copy = (VariableDeclarationStatement) ASTNode
                                .copySubtree(node.getAST(), node);

                        int dimensions = ((ArrayType) copy.getType()).getDimensions();

                        List fragments = copy.fragments();
                        for (int i = 0, size = fragments.size(); i < size; i++)
                        {
                            VariableDeclaration decl = (VariableDeclaration) fragments.get(i);
                            decl.setExtraDimensions(dimensions);
                        }

                        Type elementType = (Type) ASTNode.copySubtree(copy.getAST(),
                                ((ArrayType) copy.getType()).getElementType());
                        copy.setType(elementType);

                        rewrite.replace(node, copy, null);
                    }
                }
                return false;
            }

            public boolean visit(SingleVariableDeclaration node)
            {

                if (containsPosition(node, markerStartOffset))
                {
                    if (isCStyle(node))
                    {

                        SingleVariableDeclaration copy = (SingleVariableDeclaration) ASTNode
                                .copySubtree(node.getAST(), node);

                        // wrap the existing type into an array type
                        copy.setType(createArrayType(copy.getType(), copy.getExtraDimensions()));
                        copy.setExtraDimensions(0);

                        rewrite.replace(node, copy, null);
                    }
                    else if (isJavaStyle(node.getType()))
                    {

                        SingleVariableDeclaration copy = (SingleVariableDeclaration) ASTNode
                                .copySubtree(node.getAST(), node);

                        ArrayType arrayType = (ArrayType) copy.getType();
                        Type elementType = (Type) ASTNode.copySubtree(copy.getAST(), arrayType
                                .getElementType());

                        copy.setType(elementType);
                        copy.setExtraDimensions(arrayType.getDimensions());

                        rewrite.replace(node, copy, null);
                    }
                }

                return false;
            }

            public boolean visit(FieldDeclaration node)
            {

                if (containsPosition(node, markerStartOffset))
                {

                    if (isCStyle(node.fragments()))
                    {

                        FieldDeclaration copy = (FieldDeclaration) ASTNode.copySubtree(node
                                .getAST(), node);

                        int dimensions = 0;

                        List fragments = copy.fragments();
                        for (int i = 0, size = fragments.size(); i < size; i++)
                        {
                            VariableDeclaration decl = (VariableDeclaration) fragments.get(i);
                            if (decl.getExtraDimensions() > dimensions)
                            {
                                dimensions = decl.getExtraDimensions();

                            }
                            decl.setExtraDimensions(0);
                        }

                        // wrap current type into ArrayType
                        ArrayType arrayType = createArrayType(copy.getType(), dimensions);
                        copy.setType(arrayType);

                        rewrite.replace(node, copy, null);
                    }
                    else if (isJavaStyle(node.getType()))
                    {

                        FieldDeclaration copy = (FieldDeclaration) ASTNode.copySubtree(node
                                .getAST(), node);

                        int dimensions = ((ArrayType) copy.getType()).getDimensions();

                        List fragments = copy.fragments();
                        for (int i = 0, size = fragments.size(); i < size; i++)
                        {
                            VariableDeclaration decl = (VariableDeclaration) fragments.get(i);
                            decl.setExtraDimensions(dimensions);
                        }

                        Type elementType = (Type) ASTNode.copySubtree(copy.getAST(),
                                ((ArrayType) copy.getType()).getElementType());
                        copy.setType(elementType);

                        rewrite.replace(node, copy, null);
                    }
                }
                return false;
            }

            private boolean isJavaStyle(Type type)
            {
                return type instanceof ArrayType;
            }

            private boolean isCStyle(VariableDeclaration decl)
            {
                return decl.getExtraDimensions() > 0;
            }

            private boolean isCStyle(List fragments)
            {

                Iterator it = fragments.iterator();
                while (it.hasNext())
                {
                    VariableDeclaration decl = (VariableDeclaration) it.next();
                    if (isCStyle(decl))
                    {
                        return true;
                    }
                }
                return false;
            }

            private ArrayType createArrayType(Type componentType, int dimensions)
            {
                Type type = (Type) ASTNode.copySubtree(componentType.getAST(), componentType);
                ArrayType arrayType = componentType.getAST().newArrayType(type, dimensions);

                return arrayType;
            }
        };
    }

    protected boolean containsPosition(ASTNode node, int position)
    {
        return node.getStartPosition() <= position
                && position <= node.getStartPosition() + node.getLength();
    }

    protected boolean containsPosition(IRegion region, int position)
    {
        return region.getOffset() <= position
                && position <= region.getOffset() + region.getLength();
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription()
    {
        return Messages.ArrayTypeStyleQuickfix_description;
    }

    /**
     * {@inheritDoc}
     */
    public String getLabel()
    {
        return Messages.ArrayTypeStyleQuickfix_label;
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage()
    {
        return CheckstylePluginImages.getImage(CheckstylePluginImages.CORRECTION_CHANGE);
    }

}
