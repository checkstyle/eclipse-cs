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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.atlassw.tools.eclipse.checkstyle.stats.Messages;
import com.atlassw.tools.eclipse.checkstyle.stats.StatsCheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.stats.data.MarkerStat;

/**
 * View that displays the list of the markers for a specific error category.
 * 
 * @author Fabrice BELLINGARD
 */

public class DetailStatsView extends AbstractStatsView
{

    /**
     * Identifiant unique de cette vue. Cf. plugin.xml
     */
    public static final String VIEW_ID = "com.atlassw.tools.eclipse.checkstyle.stats.views.DetailStatsView"; //$NON-NLS-1$

    /**
     * Largeur par défaut de la colonne Fichier.
     */
    private static final int FILE_COL_WIDTH = 500;

    /**
     * Opens the editor and shows the error in the code.
     */
    private Action mShowErrorAction;

    /**
     * The MarkerStat that is currently detailed.
     */
    private MarkerStat mCurrentMarkerStat;

    /**
     * See method below.
     * 
     * @see com.atlassw.tools.eclipse.checkstyle.stats.views.AbstractStatsView#createLabelProvider()
     */
    protected IBaseLabelProvider createLabelProvider()
    {
        return new DetailStatsViewLabelProvider();
    }

    protected String getViewId()
    {
        return VIEW_ID;
    }

    /**
     * See method below.
     * 
     * @see com.atlassw.tools.eclipse.checkstyle.stats.views.AbstractStatsView#createColumns(org.eclipse.swt.widgets.Table)
     */
    protected void createColumns(Table table)
    {
        TableColumn idCol = new TableColumn(table, SWT.LEFT, 0);
        idCol.setText(Messages.DetailStatsView_fileColumn);
        idCol.setWidth(FILE_COL_WIDTH);
        idCol
            .addSelectionListener(new SorterSelectionListener(new NameSorter()));

        TableColumn countCol = new TableColumn(table, SWT.CENTER, 1);
        countCol.setText(Messages.DetailStatsView_lineColumn);
        countCol.setWidth(100);

        TableColumn messageCol = new TableColumn(table, SWT.LEFT, 2);
        messageCol.setText(Messages.DetailStatsView_messageColumn);
        messageCol.setWidth(500);
    }

    /**
     * Specifies the marker stats to detail.
     * 
     * @param markerStat :
     *            the marker stats
     */
    public void setMarkerCategoryToDisplay(MarkerStat markerStat)
    {
        mCurrentMarkerStat = markerStat;
        Display.getDefault().asyncExec(new Runnable()
        {
            /**
             * Cf. method below.
             * 
             * @see java.lang.Runnable#run()
             */
            public void run()
            {
                // update the UI
                if (getViewer().getContentProvider() != null)
                {
                    getViewer().setInput(mCurrentMarkerStat.getMarkers());
                    getDescLabel().setText(
                        NLS.bind(Messages.DetailStatsView_descriptionLabel,
                            new Integer(mCurrentMarkerStat.getCount()),
                            mCurrentMarkerStat.getIdentifiant()));
                }
            }
        });
    }

    /**
     * Crée les actions du viewer.
     */
    protected void makeActions()
    {
        mShowErrorAction = new Action()
        {
            public void run()
            {
                IStructuredSelection selection = (IStructuredSelection) getViewer()
                    .getSelection();
                if (selection.getFirstElement() instanceof IMarker)
                {
                    IMarker marker = (IMarker) selection.getFirstElement();
                    try
                    {
                        IDE.openEditor(getSite().getPage(), marker);
                    }
                    catch (PartInitException e)
                    {
                        StatsCheckstylePlugin.log(IStatus.ERROR,
                            Messages.DetailStatsView_unableToShowMarker, e);
                        // TODO : mettre message d'erreur à l'utilisateur
                    }
                }
            }
        };
        mShowErrorAction.setText(Messages.DetailStatsView_displayError);
        mShowErrorAction
            .setToolTipText(Messages.DetailStatsView_displayErrorTooltip);
        mShowErrorAction.setImageDescriptor(PlatformUI.getWorkbench()
            .getSharedImages().getImageDescriptor(
                ISharedImages.IMG_TOOL_FORWARD));

        // hooks the action to double click
        hookDoubleClickAction(mShowErrorAction);

        // and to the context menu too
        ArrayList actionList = new ArrayList(1);
        actionList.add(mShowErrorAction);
        hookContextMenu(actionList);
    }

}