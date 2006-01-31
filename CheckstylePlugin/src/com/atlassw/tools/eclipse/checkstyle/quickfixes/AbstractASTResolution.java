
package com.atlassw.tools.eclipse.checkstyle.quickfixes;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public abstract class AbstractASTResolution implements ICheckstyleMarkerResolution
{

    public boolean canFix(IMarker marker)
    {
        // TODO Auto-generated method stub
        return true;
    }

    public Image getImage()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void run(IMarker marker)
    {

        IResource resource = marker.getResource();

        if (resource instanceof IFile)
        {
            ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom((IFile) resource);

            if (compilationUnit != null)
            {
                ICompilationUnit workingCopy;
                try
                {
                    IProgressMonitor monitor = new NullProgressMonitor();

                    workingCopy = compilationUnit.getWorkingCopy(monitor);

                    ASTParser astParser = ASTParser.newParser(AST.JLS3);
                    astParser.setSource(workingCopy);
                    

                    ASTNode ast = astParser.createAST(monitor);

                    CompilationUnit cuNode = (CompilationUnit) ast;

                    cuNode.accept(handleGetCorrectingASTVisitor());

                    final IBuffer buf = workingCopy.getBuffer();
                    final String contents = buf.getContents();
                    final Document doc = new Document(contents);
                    final TextEdit edit = cuNode.rewrite(doc, null);
                    edit.apply(doc);
                    buf.setContents(doc.get());
                    workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

                    // Commit changes
                    workingCopy.commitWorkingCopy(false, null);

                    // Destroy working copy
                    workingCopy.discardWorkingCopy();
                }
                catch (JavaModelException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (MalformedTreeException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (BadLocationException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
        // TODO Auto-generated method stub

    }

    protected abstract ASTVisitor handleGetCorrectingASTVisitor();

}
