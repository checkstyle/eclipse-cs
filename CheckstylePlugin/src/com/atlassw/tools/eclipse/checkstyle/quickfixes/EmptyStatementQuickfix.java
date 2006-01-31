
package com.atlassw.tools.eclipse.checkstyle.quickfixes;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.EmptyStatement;

public class EmptyStatementQuickfix extends AbstractASTResolution
{

    protected ASTVisitor handleGetCorrectingASTVisitor()
    {
        // TODO Auto-generated method stub
        return new ASTVisitor()
        {
            public boolean visit(EmptyStatement node)
            {
                node.delete();
                return true;
            }
        };
    }

    public String getDescription()
    {
        return "Removes the superfluous semicolon";
    }

    public String getLabel()
    {
        return "Remove Semicolon";
    }

}
