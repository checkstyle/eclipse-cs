//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.ui.quickfixes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.eclipsecs.core.builder.CheckstyleMarker;
import net.sf.eclipsecs.core.config.meta.RuleMetadata;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.ui.Messages;

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
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.ui.JavaUI;
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
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;

/**
 * Abstract base class for marker resolutions using AST rewrite techniques.
 *
 * @author Lars Ködderitzsch
 * @author Philip Graf
 */
public abstract class AbstractASTResolution extends WorkbenchMarkerResolution
        implements ICheckstyleMarkerResolution {

  private boolean mAutoCommit;

  private RuleMetadata mMetaData;

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRuleMetaData(RuleMetadata metadata) {
    this.mMetaData = metadata;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canFix(IMarker marker) {

    String moduleName = marker.getAttribute(CheckstyleMarker.MODULE_NAME, null);
    try {
      return CheckstyleMarker.MARKER_ID.equals(marker.getType())
              && (mMetaData.getInternalName().equals(moduleName)
                      || mMetaData.getAlternativeNames().contains(moduleName));
    } catch (CoreException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAutoCommitChanges(boolean autoCommit) {
    mAutoCommit = autoCommit;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Image getImage() {
    // default implementation returns no image
    return null;
  }

  @Override
  public IMarker[] findOtherMarkers(IMarker[] markers) {

    Set<IMarker> candidates = new HashSet<>();

    for (IMarker m : markers) {

      if (canFix(m)) {
        candidates.add(m);
      }
    }

    return candidates.toArray(new IMarker[candidates.size()]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void run(IMarker marker) {

    IResource resource = marker.getResource();

    if (!(resource instanceof IFile)) {
      return;
    }

    ICompilationUnit compilationUnit = getCompilationUnit(marker);

    if (compilationUnit == null) {
      return;
    }

    ITextFileBufferManager bufferManager = null;

    IPath path = compilationUnit.getPath();

    try {

      final IProgressMonitor monitor = new NullProgressMonitor();

      // open the file the editor
      JavaUI.openInEditor(compilationUnit);

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
      if (annotation == null) {
        return;
      }

      Position pos = annotationModel.getPosition(annotation);

      final IRegion lineInfo = document.getLineInformationOfOffset(pos.getOffset());
      final int markerStart = pos.getOffset();

      ASTParser astParser = ASTParser.newParser(AST.JLS3);
      astParser.setKind(ASTParser.K_COMPILATION_UNIT);
      astParser.setSource(compilationUnit);

      CompilationUnit ast = (CompilationUnit) astParser.createAST(monitor);
      ast.recordModifications();

      ast.accept(handleGetCorrectingASTVisitor(lineInfo, markerStart));

      // rewrite all recorded changes to the document
      TextEdit edit = ast.rewrite(document, compilationUnit.getJavaProject().getOptions(true));
      edit.apply(document);

      // commit changes to underlying file
      if (mAutoCommit) {
        textFileBuffer.commit(monitor, false);
      }
    } catch (CoreException e) {
      CheckstyleLog.log(e, Messages.AbstractASTResolution_msgErrorQuickfix);
    } catch (MalformedTreeException e) {
      CheckstyleLog.log(e, Messages.AbstractASTResolution_msgErrorQuickfix);
    } catch (BadLocationException e) {
      CheckstyleLog.log(e, Messages.AbstractASTResolution_msgErrorQuickfix);
    } finally {

      if (bufferManager != null) {
        try {
          bufferManager.disconnect(path, null);
        } catch (CoreException e) {
          CheckstyleLog.log(e, "Error processing quickfix"); //$NON-NLS-1$
        }
      }
    }
  }

  /**
   * Template method to be implemented by concrete quickfix implementations. These must provide
   * their fixing modification through an AST visitor, more specifically by doing the neccessary
   * modifications directly on the visited AST nodes. The AST itself will recored modification.
   *
   * @param lineInfo
   *          the IRegion for the line containing the marker to fix
   * @param markerStartOffset
   *          the actual offset where the problem marker starts
   * @return the modifying AST visitor
   */
  protected abstract ASTVisitor handleGetCorrectingASTVisitor(IRegion lineInfo,
          int markerStartOffset);

  /**
   * Determines if the given position lies within the boundaries of the ASTNode.
   *
   * @param node
   *          the ASTNode
   * @param position
   *          the position to check for
   * @return <code>true</code> if the position is within the ASTNode
   */
  protected boolean containsPosition(ASTNode node, int position) {
    return node.getStartPosition() <= position
            && position <= node.getStartPosition() + node.getLength();
  }

  /**
   * Determines if the given position lies within the boundaries of the region.
   *
   * @param region
   *          the region
   * @param position
   *          the position to check for
   * @return <code>true</code> if the position is within the region
   */
  protected boolean containsPosition(IRegion region, int position) {
    return region.getOffset() <= position && position <= region.getOffset() + region.getLength();
  }

  /**
   * Returns a deep copy of the subtree of AST nodes rooted at the given node. The resulting nodes
   * are owned by the same AST as the given node. Even if the given node has a parent, the result
   * node will be unparented.
   * <p>
   * Source range information on the original nodes is automatically copied to the new nodes. Client
   * properties ( <code>properties</code>) are not carried over.
   * </p>
   * <p>
   * The node's <code>AST</code> and the target <code>AST</code> must support the same API level.
   * </p>
   *
   * @param node
   *          the node to copy, or <code>null</code> if none
   *
   * @return the copied node, or <code>null</code> if <code>node</code> is <code>null</code>
   */
  @SuppressWarnings("unchecked")
  protected <T extends ASTNode> T copy(final T node) {
    return (T) ASTNode.copySubtree(node.getAST(), node);
  }

  /**
   * Replaces a node in an AST with another node. If the replacement is successful the original node
   * is deleted.
   *
   * @param node
   *          The node to replace.
   * @param replacement
   *          The replacement node.
   * @return <code>true</code> if the node was successfully replaced.
   */
  protected boolean replace(final ASTNode node, final ASTNode replacement) {
    final ASTNode parent = node.getParent();
    final StructuralPropertyDescriptor descriptor = node.getLocationInParent();
    if (descriptor != null) {
      if (descriptor.isChildProperty()) {
        parent.setStructuralProperty(descriptor, replacement);
        node.delete();
        return true;
      } else if (descriptor.isChildListProperty()) {
        @SuppressWarnings("unchecked")
        final List<ASTNode> children = (List<ASTNode>) parent.getStructuralProperty(descriptor);
        children.set(children.indexOf(node), replacement);
        node.delete();
        return true;
      }
    }
    return false;
  }

  private ICompilationUnit getCompilationUnit(IMarker marker) {
    IResource res = marker.getResource();
    if (res instanceof IFile && res.isAccessible()) {
      IJavaElement element = JavaCore.create((IFile) res);
      if (element instanceof ICompilationUnit) {
        return (ICompilationUnit) element;
      }
    }
    return null;
  }

  private MarkerAnnotation getMarkerAnnotation(IAnnotationModel annotationModel, IMarker marker) {

    Iterator<Annotation> it = annotationModel.getAnnotationIterator();
    while (it.hasNext()) {
      Annotation tmp = it.next();

      if (tmp instanceof MarkerAnnotation) {

        IMarker theMarker = ((MarkerAnnotation) tmp).getMarker();

        if (theMarker.equals(marker)) {
          return (MarkerAnnotation) tmp;
        }
      }
    }
    return null;
  }
}
