//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
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

package com.atlassw.tools.eclipse.checkstyle.builder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.HTMLPrinter;
import org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;

/**
 * Hover implementation for checkstyle messages.
 * 
 * @author Lars Ködderitzsch
 */
public class CheckstyleHover implements IJavaEditorTextHover
{

    //
    // constants
    //

    /** Type of the annotation presented by this hover */
    private static final String CHECKSTYLE_ANNOTATION = CheckstylePlugin.PLUGIN_ID
                                                              + ".CheckstyleAnnotation";

    //
    // attributes
    //

    /** the actual active editor */
    private IEditorPart         ivEditor;

    //
    // methods
    //

    /**
     * @see org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover#setEditor(org.eclipse.ui.IEditorPart)
     */
    public void setEditor(IEditorPart editor)
    {

        this.ivEditor = editor;
    }

    /**
     * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer,
     *      org.eclipse.jface.text.IRegion)
     */
    public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
    {

        List messages = new ArrayList();

        if (this.ivEditor == null)
        {
            return null;
        }

        IDocumentProvider provider = JavaPlugin.getDefault().getCompilationUnitDocumentProvider();
        IAnnotationModel model = provider.getAnnotationModel(this.ivEditor.getEditorInput());

        if (model != null)
        {

            Iterator it = model.getAnnotationIterator();
            while (it.hasNext())
            {

                Annotation annotation = (Annotation) it.next();

                if (CHECKSTYLE_ANNOTATION.equals(annotation.getType()))
                {

                    Position position = model.getPosition(annotation);

                    if (position != null
                            && position.overlapsWith(hoverRegion.getOffset(), hoverRegion
                                    .getLength()))
                    {
                        String msg = annotation.getText();
                        if (msg != null && msg.trim().length() > 0)
                        {
                            messages.add(msg);
                        }
                    }
                }
            }
        }

        return buildHover(messages);
    }

    /**
     * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer,
     *      int)
     */
    public IRegion getHoverRegion(ITextViewer textViewer, int offset)
    {

        IDocument document = textViewer.getDocument();
        IRegion region = null;
        try
        {
            region = document.getLineInformationOfOffset(offset);
        }
        catch (BadLocationException ble)
        {
            //ignore
            region = null;
        }
        return region;
    }

    /**
     * Builds and formats the hover for the given messages
     * 
     * @param messages the messages
     * @return the hover string
     */
    private String buildHover(List messages)
    {

        StringBuffer hover = new StringBuffer();
        if (messages.size() > 0)
        {

            HTMLPrinter.addPageProlog(hover);
            HTMLPrinter.addParagraph(hover, CheckstylePlugin
                    .getResourceString("CheckstyleHover.header"));
            HTMLPrinter.startBulletList(hover);
            for (int i = 0; i < messages.size(); i++)
            {
                HTMLPrinter.addBullet(hover, (String) messages.get(i));
            }
            HTMLPrinter.endBulletList(hover);
            HTMLPrinter.addPageEpilog(hover);
        }

        return hover.toString();
    }

}