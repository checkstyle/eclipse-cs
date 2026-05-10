//============================================================================
//
// Copyright (C) 2003-2023 the original author or authors.
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
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//
//============================================================================

package net.sf.eclipsecs.ui.quickfixes;

import java.util.Iterator;
import java.util.function.BiFunction;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.ui.Messages;

public class AstQuickfixExecutor {

  private AstQuickfixExecutor() {

  }

  public static final void run(IMarker marker,
          BiFunction<IRegion, Integer, ASTVisitor> handleGetCorrectingASTVisitor) {
    if (!(marker.getResource() instanceof IFile)) {
      return;
    }

    ICompilationUnit compilationUnit = getCompilationUnit(marker);

    if (compilationUnit == null) {
      return;
    }

    ITextFileBufferManager bufferManager = null;

    IPath path = compilationUnit.getPath();

    try {
      // open the file the editor
      JavaUI.openInEditor(compilationUnit);

      // reimplemented according to this article
      // http://www.eclipse.org/articles/Article-JavaCodeManipulation_AST/index.html
      bufferManager = FileBuffers.getTextFileBufferManager();
      bufferManager.connect(path, LocationKind.IFILE, null);

      ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.IFILE);

      IDocument document = textFileBuffer.getDocument();
      IAnnotationModel annotationModel = textFileBuffer.getAnnotationModel();

      MarkerAnnotation annotation = getMarkerAnnotation(annotationModel, marker);

      // if the annotation is null it means that is was probably deleted
      // by a previous quickfix
      if (annotation == null) {
        return;
      }

      Position pos = annotationModel.getPosition(annotation);

      final IRegion lineInfo = document.getLineInformationOfOffset(pos.getOffset());
      final int markerStart = pos.getOffset();

      ASTParser astParser = ASTParser.newParser(AST.getJLSLatest());
      astParser.setKind(ASTParser.K_COMPILATION_UNIT);
      astParser.setSource(compilationUnit);

      final IProgressMonitor monitor = new NullProgressMonitor();
      CompilationUnit ast = (CompilationUnit) astParser.createAST(monitor);
      ast.recordModifications();

      ast.accept(handleGetCorrectingASTVisitor.apply(lineInfo, markerStart));

      // rewrite all recorded changes to the document
      var wasDirtyBefore = textFileBuffer.isDirty();
      ast.rewrite(document, compilationUnit.getJavaProject().getOptions(true)).apply(document);

      // commit changes to underlying file
      if (!wasDirtyBefore) {
        textFileBuffer.commit(monitor, false);
      }
    } catch (CoreException | MalformedTreeException | BadLocationException ex) {
      CheckstyleLog.log(ex, Messages.AbstractASTResolution_msgErrorQuickfix);
    } finally {
      if (bufferManager != null) {
        try {
          bufferManager.disconnect(path, LocationKind.IFILE, null);
        } catch (CoreException ex) {
          CheckstyleLog.log(ex, "Error processing quickfix"); //$NON-NLS-1$
        }
      }
    }
  }

  private static ICompilationUnit getCompilationUnit(IMarker marker) {
    if (marker.getResource() instanceof IFile file && file.isAccessible()
            && JavaCore.create(file) instanceof ICompilationUnit element) {
      return element;

    }
    return null;
  }

  private static MarkerAnnotation getMarkerAnnotation(IAnnotationModel annotationModel,
          IMarker marker) {
    Iterator<Annotation> iter = annotationModel.getAnnotationIterator();
    while (iter.hasNext()) {
      if (iter.next() instanceof MarkerAnnotation markerAnnotation) {
        if (markerAnnotation.getMarker().equals(marker)) {
          return markerAnnotation;
        }
      }
    }
    return null;
  }

}
