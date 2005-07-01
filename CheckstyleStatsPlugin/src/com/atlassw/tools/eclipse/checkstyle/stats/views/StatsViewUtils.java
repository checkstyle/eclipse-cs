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
package com.atlassw.tools.eclipse.checkstyle.stats.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;

import com.atlassw.tools.eclipse.checkstyle.stats.Messages;
import com.atlassw.tools.eclipse.checkstyle.stats.analyser.AnalyserEvent;

/**
 * Utility class for Stats views.
 * 
 * @author Fabrice BELLINGARD
 */
public final class StatsViewUtils
{

    /**
     * Private constructor for it is a utility class.
     */
    private StatsViewUtils()
    {
    }

    /**
     * Returns the main title for an analyser event.
     * 
     * @param event
     *            the analyser event
     * @return the title
     */
    public static String computeMainTitle(AnalyserEvent event)
    {
        return NLS.bind(Messages.StatsViewUtils_checkstyleErrorsCount,
            new Integer(event.getStats().getMarkerCount()));
    }

    /**
     * Returns a list of displayable names of the resources that were analysed.
     * 
     * @param event
     *            the analyser event
     * @return a String Collection of the resource names
     */
    public static Collection computeAnalysedResourceNames(AnalyserEvent event)
    {
        IStructuredSelection selection = event.getSelection();
        ArrayList resourceNames = new ArrayList();

        if (selection != null)
        {
            // on va regarder quels éléments ont été sélectionnés
            for (Iterator it = selection.iterator(); it.hasNext();)
            {
                Object element = it.next();
                if (element instanceof IAdaptable)
                {
                    String name = computeResourceName((IAdaptable) element);
                    if (name != null)
                    {
                        resourceNames.add(name);
                    }
                }
            }
        }

        return resourceNames;
    }

    /**
     * Returns a displayable name for the adaptable object.
     * 
     * @param adaptable
     *            the resource
     * @return the displayable name or null if not found
     */
    private static String computeResourceName(IAdaptable adaptable)
    {
        ICompilationUnit classe = (ICompilationUnit) adaptable
            .getAdapter(ICompilationUnit.class);
        if (classe != null)
        {
            return NLS.bind(Messages.StatsViewUtils_classElement, classe
                .getElementName().replaceAll(".java", "")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        IPackageFragment packageFragment = (IPackageFragment) adaptable
            .getAdapter(IPackageFragment.class);
        if (packageFragment != null)
        {
            return NLS.bind(Messages.StatsViewUtils_packageElement,
                packageFragment.getElementName());
        }

        IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) adaptable
            .getAdapter(IPackageFragmentRoot.class);
        if (packageFragmentRoot != null)
        {
            return NLS.bind(Messages.StatsViewUtils_fragmentRootElement,
                packageFragmentRoot.getElementName());
        }

        IJavaProject project = (IJavaProject) adaptable
            .getAdapter(IJavaProject.class);
        if (project != null)
        {
            return NLS.bind(Messages.StatsViewUtils_projectElement, project
                .getElementName());
        }

        IType type = (IType) adaptable.getAdapter(IType.class);
        if (type != null)
        {
            return NLS.bind(Messages.StatsViewUtils_classElement, type
                .getElementName().replaceAll(".java", "")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        IFile file = (IFile) adaptable.getAdapter(IFile.class);
        if (file != null)
        {
            return NLS.bind(Messages.StatsViewUtils_classElement, file
                .getName().replaceAll(".java", "")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return null;
    }

}
