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

package com.atlassw.tools.eclipse.checkstyle.quickfixes;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;

/**
 * Abstract base class for marker resolutions using AST rewrite techniques.
 * 
 * @author Lars Ködderitzsch
 */
public abstract class AbstractASTResolution implements ICheckstyleMarkerResolution
{

    /**
     * {@inheritDoc}
     */
    public boolean canFix(IMarker marker)
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage()
    {
        // default implementation returns no image
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void run(IMarker marker)
    {

        IResource resource = marker.getResource();

        if (!(resource instanceof IFile))
        {
            return;
        }

        ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom((IFile) resource);

        if (compilationUnit == null)
        {
            return;
        }

        ICompilationUnit workingCopy = null;

        try
        {
            IProgressMonitor monitor = new NullProgressMonitor();

            workingCopy = compilationUnit.getWorkingCopy(monitor);
            String source = workingCopy.getBuffer().getContents();
            Document doc = new Document(source);

            // determine the source region if the line where the marker lies
            int lineNumber = ((Integer) marker.getAttribute(IMarker.LINE_NUMBER)).intValue();
            IRegion lineInfo = doc.getLineInformation(lineNumber == 0 ? 0 : lineNumber - 1);

            ASTParser astParser = ASTParser.newParser(AST.JLS3);

            // only create a partial AST
            astParser.setFocalPosition(lineInfo.getOffset());
            astParser.setSource(workingCopy);

            ASTNode ast = astParser.createAST(monitor);

            // collect the changes from the concrete quickfix
            ASTRewrite rewrite = ASTRewrite.create(ast.getAST());
            ast.accept(handleGetCorrectingASTVisitor(rewrite, lineInfo));

            // rewrite all recorded changes to the document
            TextEdit edit = rewrite.rewriteAST(doc, null);
            edit.apply(doc);

            // update of the compilation unit
            workingCopy.getBuffer().setContents(doc.get());
            workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

            // Commit changes
            workingCopy.commitWorkingCopy(false, null);
        }
        catch (CoreException e)
        {
            CheckstyleLog.log(e, "Error processing quickfix");
        }
        catch (MalformedTreeException e)
        {
            CheckstyleLog.log(e, "Error processing quickfix");
        }
        catch (BadLocationException e)
        {
            CheckstyleLog.log(e, "Error processing quickfix");
        }
        finally
        {
            if (workingCopy != null)
            {
                // Destroy working copy
                try
                {
                    workingCopy.discardWorkingCopy();
                }
                catch (JavaModelException e)
                {
                    CheckstyleLog.log(e);
                }
            }
        }
    }

    /**
     * Template method to be implemented by concrete quickfix implementations.
     * These must provide their fixing modification through an AST visitor, more
     * specifically by doing the neccessary modifications through the given
     * ASTRewrite object.
     * 
     * @param astRewrite the ASTRewrite object where modifications should be
     *            added
     * @param lineInfo the IRegion for the line containing the marker to fix
     * @return the modifying AST visitor
     */
    protected abstract ASTVisitor handleGetCorrectingASTVisitor(ASTRewrite astRewrite,
            IRegion lineInfo);

}
