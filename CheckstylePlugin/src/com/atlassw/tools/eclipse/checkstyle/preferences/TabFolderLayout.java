//============================================================================
//
// Copyright (C) 2002-2003  David Schneider
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
package com.atlassw.tools.eclipse.checkstyle.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public class TabFolderLayout extends Layout
{

	protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache)
	{
		if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
			return new Point(wHint, hHint);

		Control[] children = composite.getChildren();
		int count = children.length;
		int maxWidth = 0, maxHeight = 0;
		for (int i = 0; i < count; i++)
		{
			Control child = children[i];
			Point pt = child.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
			maxWidth = Math.max(maxWidth, pt.x);
			maxHeight = Math.max(maxHeight, pt.y);
		}

		if (wHint != SWT.DEFAULT)
			maxWidth = wHint;
		if (hHint != SWT.DEFAULT)
			maxHeight = hHint;

		return new Point(maxWidth, maxHeight);

	}

	protected void layout(Composite composite, boolean flushCache)
	{
		Rectangle rect = composite.getClientArea();

		Control[] children = composite.getChildren();
		for (int i = 0; i < children.length; i++)
		{
			children[i].setBounds(rect);
		}
	}
}
