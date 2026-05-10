//============================================================================
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch
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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;

import net.sf.eclipsecs.core.builder.CheckstyleMarker;

/**
 * Abstract base class for marker resolutions using AST rewrite techniques.
 *
 */
public abstract class AbstractASTResolution extends WorkbenchMarkerResolution
        implements ICheckstyleMarkerResolution {

  private String module;

  /**
   * Template method to be implemented by concrete quickfix implementations. These must provide
   * their fixing modification through an AST visitor, more specifically by doing the necessary
   * modifications directly on the visited AST nodes. The AST itself will record modification.
   *
   * @param lineInfo
   *          the IRegion for the line containing the marker to fix
   * @param markerStartOffset
   *          the actual offset where the problem marker starts
   * @return the modifying AST visitor
   */
  protected abstract ASTVisitor handleGetCorrectingASTVisitor(IRegion lineInfo,
          int markerStartOffset);

  @Override
  public boolean canFix(IMarker marker) {
    try {
      if (!CheckstyleMarker.MARKER_ID.equals(marker.getType())) {
        return false;
      }
      String markerModule = marker.getAttribute(CheckstyleMarker.MODULE_NAME, "");
      if (module.equals(markerModule)) {
        return true;
      }
      var shortName = StringUtils.substringAfterLast(markerModule, '.');
      return module.equals(shortName);
    } catch (CoreException ex) {
      // ignore
    }
    return false;
  }

  @Override
  public Image getImage() {
    // default implementation returns no image
    return null;
  }

  @Override
  public IMarker[] findOtherMarkers(IMarker[] markers) {
    return Arrays.stream(markers)
            .filter(this::canFix)
            .toArray(IMarker[]::new);
  }

  @Override
  public void run(IMarker marker) {
    AstQuickfixExecutor.run(marker, this::handleGetCorrectingASTVisitor);
  }

  @Override
  public void setModule(String module) {
    this.module = module;
  }

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
  protected static boolean replace(final ASTNode node, final ASTNode replacement) {
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

}
