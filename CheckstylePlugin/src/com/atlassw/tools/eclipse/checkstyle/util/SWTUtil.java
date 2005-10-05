//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Some widely used helper funktionality regarding SWT shortcomings.
 * 
 * @author Lars Ködderitzsch
 */
public final class SWTUtil
{

    /**
     * Hidden default constructor.
     */
    private SWTUtil()
    {
    // NOOP
    }

    /**
     * Adds support to a control which shows the tooltip of the control when the
     * mouse button is pressed on it.
     * 
     * @param control the control
     */
    public static void addTooltipOnPressSupport(Control control)
    {
        control.addMouseListener(new TooltipOnPressListener());
    }

    /**
     * Adds a verifier to the given text control which allows only digits to be
     * entered.
     * 
     * @param text the text control
     */
    public static void addOnlyDigitInputSupport(Text text)
    {
        text.addVerifyListener(new OnlyDigitsVerifyListener());
    }

    /**
     * Listener that adds tooltip-on-press support.
     * 
     * @author Lars Ködderitzsch
     */
    private static final class TooltipOnPressListener extends MouseAdapter implements
            MouseTrackListener
    {

        /**
         * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseDown(MouseEvent e)
        {
            Control theControl = (Control) e.widget;

            Display display = theControl.getDisplay();
            Shell tip = new Shell(theControl.getShell(), SWT.ON_TOP | SWT.TOOL);
            tip.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
            FillLayout layout = new FillLayout();
            layout.marginHeight = 1;
            layout.marginWidth = 2;
            tip.setLayout(layout);
            Label label = new Label(tip, SWT.NONE);
            label.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
            label.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

            label.setText(theControl.getToolTipText());
            label.addMouseTrackListener(this);
            Point size = tip.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            Rectangle rect = theControl.getBounds();
            Point pt = theControl.getParent().toDisplay(rect.x, rect.y);
            tip.setBounds(pt.x, pt.y, size.x, size.y);
            tip.setVisible(true);
        }

        /**
         * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseExit(MouseEvent e)
        {
            // dispose the tooltip shell
            Label label = (Label) e.widget;
            Shell shell = label.getShell();
            shell.dispose();
        }

        /**
         * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseEnter(MouseEvent e)
        {
        // NOOP
        }

        /**
         * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseHover(MouseEvent e)
        {
        // NOOP
        }
    }

    /**
     * Verifier that allows only digits to be input.
     * 
     * @author Lars Ködderitzsch
     */
    public static final class OnlyDigitsVerifyListener implements VerifyListener
    {

        /**
         * @see org.eclipse.swt.events.VerifyListener#verifyText(org.eclipse.swt.events.VerifyEvent)
         */
        public void verifyText(VerifyEvent e)
        {

            boolean doit = true;

            // only let digits pass (and del, backspace)
            if (!(Character.isDigit(e.character) || e.character == SWT.DEL || e.character == SWT.BS))
            {
                doit = false;
            }

            // check if inserted text is an integer
            if (!doit)
            {
                try
                {
                    Integer.parseInt(e.text);
                    doit = true;
                }
                catch (NumberFormatException ex)
                {
                    doit = false;
                }
            }

            e.doit = doit;
            if (!e.doit)
            {
                Display.getCurrent().beep();
            }
        }
    }
}
