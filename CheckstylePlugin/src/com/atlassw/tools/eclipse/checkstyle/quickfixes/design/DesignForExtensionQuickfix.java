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

package com.atlassw.tools.eclipse.checkstyle.quickfixes.design;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.internal.corext.callhierarchy.JavaImplementorFinder;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.ICheckstyleMarkerResolution;

public class DesignForExtensionQuickfix implements ICheckstyleMarkerResolution
{

    /*
     * @Override protected ASTVisitor handleGetCorrectingASTVisitor() { return
     * new ASTVisitor() { public boolean visit(VariableDeclarationExpression
     * node) { System.out.println("vd expr"); return false; } public boolean
     * visit(VariableDeclarationStatement node) { System.out.println("vd stmt");
     * return false; } public boolean visit(VariableDeclarationFragment node) {
     * System.out.println("vd frag"); return false; } }; }
     */

    public String getDescription()
    {
        return "Add final modifier to method";
    }

    public String getLabel()
    {
        return "Change to final method";
    }

    public boolean canFix(IMarker marker)
    {
        if (marker.getAttribute("MessageKey", "-").equals("design.forExtension"))
        {
            return true;
        }
        return false;
    }

    public Image getImage()
    {
        // TODO Remove dependency to jdt internal class
        return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    }

    public void run(IMarker marker)
    {
        IResource resource = marker.getResource();

        int charStart = marker.getAttribute("charStart", -1);

        if (resource instanceof IFile && charStart >= 0)
        {
            IFile file = (IFile) resource;
            JavaPlugin plugin = JavaPlugin.getDefault();

            IEditorInput input = new FileEditorInput(file);
            IDocument doc = plugin.getCompilationUnitDocumentProvider().getDocument(input);
            if (doc != null)
            {
                final InsertEdit edit = new InsertEdit(charStart, "final ");
                try
                {
                    edit.apply(doc);
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
    }
}
