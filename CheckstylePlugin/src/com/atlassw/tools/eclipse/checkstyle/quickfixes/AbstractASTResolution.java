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

package com.atlassw.tools.eclipse.checkstyle.quickfixes;

import java.util.Iterator;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import com.atlassw.tools.eclipse.checkstyle.ErrorMessages;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;

/**
 * Abstract base class for marker resolutions using AST rewrite techniques.
 * 
 * @author Lars Ködderitzsch
 */
public abstract class AbstractASTResolution implements ICheckstyleMarkerResolution
{

    private boolean mAutoCommit;

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
    public void setAutoCommitChanges(boolean autoCommit)
    {
        mAutoCommit = autoCommit;
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

        ICompilationUnit compilationUnit = getCompilationUnit(marker);

        if (compilationUnit == null)
        {
            return;
        }

        ITextFileBufferManager bufferManager = null;

        IPath path = compilationUnit.getPath();

        try
        {
            IProgressMonitor monitor = new NullProgressMonitor();

            // reimplemented according to this article
            // http://www.eclipse.org/articles/Article-JavaCodeManipulation_AST/index.html
            bufferManager = FileBuffers.getTextFileBufferManager();
            bufferManager.connect(path, null);

            ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path);

            IDocument document = textFileBuffer.getDocument();
            IAnnotationModel annotationModel = textFileBuffer.getAnnotationModel();

            MarkerAnnotation annotation = getMarkerAnnotation(annotationModel, marker);

            // if the annotation is null it means that is was probably deleted
            // by a previous quickfix
            if (annotation == null)
            {
                return;
            }

            Position pos = annotationModel.getPosition(annotation);

            IRegion lineInfo = document.getLineInformationOfOffset(pos.getOffset());
            int markerStart = pos.getOffset();

            ASTParser astParser = ASTParser.newParser(AST.JLS3);
            astParser.setKind(ASTParser.K_COMPILATION_UNIT);
            astParser.setSource(compilationUnit);

            CompilationUnit ast = (CompilationUnit) astParser.createAST(monitor);
            ast.recordModifications();

            ast.accept(handleGetCorrectingASTVisitor(lineInfo, markerStart));

            // rewrite all recorded changes to the document
            TextEdit edit = ast
                    .rewrite(document, compilationUnit.getJavaProject().getOptions(true));
            edit.apply(document);

            // commit changes to underlying file
            if (mAutoCommit)
            {
                textFileBuffer.commit(monitor, false);
            }
        }
        catch (CoreException e)
        {
            CheckstyleLog.log(e, ErrorMessages.AbstractASTResolution_msgErrorQuickfix);
        }
        catch (MalformedTreeException e)
        {
            CheckstyleLog.log(e, "Error processing quickfix"); //$NON-NLS-1$
        }
        catch (BadLocationException e)
        {
            CheckstyleLog.log(e, "Error processing quickfix"); //$NON-NLS-1$
        }
        finally
        {

            if (bufferManager != null)
            {
                try
                {
                    bufferManager.disconnect(path, null);
                }
                catch (CoreException e)
                {
                    CheckstyleLog.log(e, "Error processing quickfix"); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * Template method to be implemented by concrete quickfix implementations.
     * These must provide their fixing modification through an AST visitor, more
     * specifically by doing the neccessary modifications directly on the
     * visited AST nodes. The AST itself will recored modification.
     * 
     * @param lineInfo the IRegion for the line containing the marker to fix
     * @param markerStartOffset the actual offset where the problem marker
     *            starts
     * @return the modifying AST visitor
     */
    protected abstract ASTVisitor handleGetCorrectingASTVisitor(IRegion lineInfo,
            int markerStartOffset);

    /**
     * Determines if the given position lies within the boundaries of the
     * ASTNode.
     * 
     * @param node the ASTNode
     * @param position the position to check for
     * @return <code>true</code> if the position is within the ASTNode
     */
    protected boolean containsPosition(ASTNode node, int position)
    {
        return node.getStartPosition() <= position
                && position <= node.getStartPosition() + node.getLength();
    }

    /**
     * Determines if the given position lies within the boundaries of the
     * region.
     * 
     * @param region the region
     * @param position the position to check for
     * @return <code>true</code> if the position is within the region
     */
    protected boolean containsPosition(IRegion region, int position)
    {
        return region.getOffset() <= position
                && position <= region.getOffset() + region.getLength();
    }

    private ICompilationUnit getCompilationUnit(IMarker marker)
    {
        IResource res = marker.getResource();
        if (res instanceof IFile && res.isAccessible())
        {
            IJavaElement element = JavaCore.create((IFile) res);
            if (element instanceof ICompilationUnit)
            {
                return (ICompilationUnit) element;
            }
        }
        return null;
    }

    private MarkerAnnotation getMarkerAnnotation(IAnnotationModel annotationModel, IMarker marker)
    {

        Iterator it = annotationModel.getAnnotationIterator();
        while (it.hasNext())
        {
            Annotation tmp = (Annotation) it.next();

            if (tmp instanceof MarkerAnnotation)
            {

                IMarker theMarker = ((MarkerAnnotation) tmp).getMarker();

                if (theMarker.equals(marker))
                {
                    return (MarkerAnnotation) tmp;
                }
            }
        }
        return null;
    }
}
