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

package net.sf.eclipsecs.ui.quickfixes.coding;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.IRegion;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.quickfixes.AbstractASTResolution;
import net.sf.eclipsecs.ui.quickfixes.Messages;

/**
 * Quickfix implementation which removes the explicit default initialization of
 * a class or object member.
 *
 */
public class ExplicitInitializationQuickfix extends AbstractASTResolution {

  private String mFieldName = Messages.ExplicitInitializationQuickfix_unknownFieldName;

  @Override
  public boolean canFix(final IMarker marker) {
    if (!super.canFix(marker)) {
      return false;
    }
    retrieveFieldName(marker);
    return true;
  }

  private void retrieveFieldName(final IMarker marker) {
    try {
      final Map<String, Object> attributes = marker.getAttributes();
      final int start = (Integer) attributes.get("charStart"); //$NON-NLS-1$
      final int end = (Integer) attributes.get("charEnd"); //$NON-NLS-1$
      final IFile resource = (IFile) marker.getResource();
      final InputStream in = resource.getContents();
      final byte[] buffer = new byte[end - start];
      in.skip(start);
      in.read(buffer, 0, buffer.length);
      in.close();
      final String snippet = new String(buffer, resource.getCharset());
      mFieldName = snippet.substring(0, snippet.indexOf('=')).trim();
    } catch (final CoreException | IOException | IndexOutOfBoundsException | ClassCastException | NullPointerException ex) {
      handleRetrieveFieldNameException(ex);
    }
  }

  private void handleRetrieveFieldNameException(final Exception error) {
    CheckstyleLog.log(error, Messages.ExplicitInitializationQuickfix_errorMessageFieldName);
  }

  @Override
  protected ASTVisitor handleGetCorrectingASTVisitor(final IRegion lineInfo,
          final int markerStartOffset) {
    return new ASTVisitor() {

      @Override
      public boolean visit(final VariableDeclarationFragment node) {
        if (containsPosition(node, markerStartOffset)) {
          node.getInitializer().delete();
        }
        return false;
      }

    };
  }

  @Override
  public String getDescription() {
    return NLS.bind(Messages.ExplicitInitializationQuickfix_description, mFieldName);
  }

  @Override
  public String getLabel() {
    return NLS.bind(Messages.ExplicitInitializationQuickfix_label, mFieldName);
  }

  @Override
  public Image getImage() {
    return CheckstyleUIPluginImages.CORRECTION_REMOVE.getImage();
  }

}
