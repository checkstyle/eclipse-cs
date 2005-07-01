//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars Ködderitzsch, Fabrice Bellingard
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
package com.atlassw.tools.eclipse.checkstyle.stats.analyser;

import org.eclipse.ui.IWorkbenchPage;

/**
 * Interface that classes which want to be notified of stats computing should
 * implement.
 * 
 * @author Fabrice BELLINGARD
 */
public interface IAnalyserListener
{

    /**
     * Notify the listener that the stats have been updated.
     * 
     * @param analyserEvent :
     *            the event that carries the stats
     */
    void statsUpdated(AnalyserEvent analyserEvent);

    /**
     * If the listener is a UI listener, then this method return the workbench
     * page it belongs to.
     * 
     * @return the page
     */
    IWorkbenchPage getPage();
}
