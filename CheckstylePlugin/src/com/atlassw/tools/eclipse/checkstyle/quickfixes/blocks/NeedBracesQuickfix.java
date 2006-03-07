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

package com.atlassw.tools.eclipse.checkstyle.quickfixes.blocks;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractASTResolution;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginImages;

/**
 * Quickfix implementation that adds braces to if/for/while/do statements.
 * 
 * @author Lars Ködderitzsch
 */
public class NeedBracesQuickfix extends AbstractASTResolution
{

    /**
     * {@inheritDoc}
     */
    protected ASTVisitor handleGetCorrectingASTVisitor(final ASTRewrite astRewrite,
            final IRegion lineInfo)
    {
        return new ASTVisitor()
        {
            public boolean visit(IfStatement node)
            {
                int pos = node.getStartPosition();
                int len = pos + node.getLength();
                if ((pos >= lineInfo.getOffset() && pos <= (lineInfo.getOffset() + lineInfo
                        .getLength()))
                        || (pos <= lineInfo.getOffset() && len >= lineInfo.getOffset()
                                + lineInfo.getLength()))
                {
                    IfStatement copy = (IfStatement) ASTNode.copySubtree(node.getAST(), node);

                    bracifyIfStatement(copy);

                    astRewrite.replace(node, copy, null);

                    return true;
                }
                return false;
            }

            /**
             * Helper method to recursivly bracify a if-statement.
             * 
             * @param ifStatement the if statement
             */
            private void bracifyIfStatement(IfStatement ifStatement)
            {

                // change the then statement to a block if necessary
                if (!(ifStatement.getThenStatement() instanceof Block))
                {
                    Block block = ifStatement.getAST().newBlock();
                    block.statements().add(
                            ASTNode.copySubtree(block.getAST(), ifStatement.getThenStatement()));
                    ifStatement.setThenStatement(block);
                }

                // check the else statement if it is a block
                Statement elseStatement = ifStatement.getElseStatement();
                if (elseStatement != null && !(elseStatement instanceof Block))
                {

                    // in case the else statement is an further if statement
                    // (else if)
                    // do the recursion
                    if (elseStatement instanceof IfStatement)
                    {
                        bracifyIfStatement((IfStatement) elseStatement);
                    }
                    else
                    {
                        // change the else statement to a block
                        Block block = ifStatement.getAST().newBlock();
                        block.statements().add(ASTNode.copySubtree(block.getAST(), elseStatement));
                        ifStatement.setElseStatement(block);
                    }
                }
            }

            public boolean visit(ForStatement node)
            {
                int pos = node.getStartPosition();
                if (pos >= lineInfo.getOffset()
                        && pos <= (lineInfo.getOffset() + lineInfo.getLength()))
                {

                    ForStatement copy = (ForStatement) ASTNode.copySubtree(node.getAST(), node);
                    Block block = copy.getAST().newBlock();
                    block.statements().add(ASTNode.copySubtree(block.getAST(), copy.getBody()));
                    copy.setBody(block);

                    astRewrite.replace(node, copy, null);
                }

                return false;
            }

            public boolean visit(DoStatement node)
            {
                int pos = node.getStartPosition();
                if (pos >= lineInfo.getOffset()
                        && pos <= (lineInfo.getOffset() + lineInfo.getLength()))
                {

                    DoStatement copy = (DoStatement) ASTNode.copySubtree(node.getAST(), node);
                    Block block = copy.getAST().newBlock();
                    block.statements().add(ASTNode.copySubtree(block.getAST(), copy.getBody()));
                    copy.setBody(block);

                    astRewrite.replace(node, copy, null);
                }

                return false;
            }

            public boolean visit(WhileStatement node)
            {
                int pos = node.getStartPosition();
                if (pos >= lineInfo.getOffset()
                        && pos <= (lineInfo.getOffset() + lineInfo.getLength()))
                {

                    WhileStatement copy = (WhileStatement) ASTNode.copySubtree(node.getAST(), node);
                    Block block = copy.getAST().newBlock();
                    block.statements().add(ASTNode.copySubtree(block.getAST(), copy.getBody()));
                    copy.setBody(block);

                    astRewrite.replace(node, copy, null);
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
        return Messages.NeedBracesQuickfix_description;
    }

    /**
     * {@inheritDoc}
     */
    public String getLabel()
    {
        return Messages.NeedBracesQuickfix_label;
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage()
    {
        return CheckstylePluginImages.getImage(CheckstylePluginImages.CORRECTION_ADD);
    }
}
