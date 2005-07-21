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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.atlassw.tools.eclipse.checkstyle.stats.Messages;
import com.atlassw.tools.eclipse.checkstyle.stats.StatsCheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.stats.analyser.AnalyserEvent;
import com.atlassw.tools.eclipse.checkstyle.stats.analyser.IAnalyserListener;
import com.atlassw.tools.eclipse.checkstyle.stats.analyser.MarkerAnalyser;
import com.atlassw.tools.eclipse.checkstyle.stats.data.MarkerStat;
import com.atlassw.tools.eclipse.checkstyle.stats.data.Stats;
import com.atlassw.tools.eclipse.checkstyle.stats.views.internal.CheckstyleMarkerFilter;
import com.atlassw.tools.eclipse.checkstyle.stats.views.internal.FiltersAction;

/**
 * View that displays statistics about checkstyle markers.
 * 
 * @author Fabrice BELLINGARD
 */

public class MarkerStatsView extends AbstractStatsView implements
    IAnalyserListener
{

    /**
     * Les stats à afficher.
     */
    private Stats mStatsToDisplay;

    /**
     * La sélection qui correspond à l'affichage.
     */
    private IStructuredSelection mSelectionToDisplay;

    /**
     * Identifiant unique de cette vue. Cf. plugin.xml
     */
    public static final String VIEW_ID = "com.atlassw.tools.eclipse.checkstyle.stats.views.MarkerStatsView"; //$NON-NLS-1$

    /**
     * Largeur par défaut de la colonne Description.
     */
    private static final int DESC_COL_WIDTH = 400;

    /**
     * Lance le diagramme.
     */
    private Action mChartAction;

    /**
     * Shows the detail of the selected error category.
     */
    private Action mDetailErrorCategoryAction;

    /**
     * The constructor.
     */
    public MarkerStatsView()
    {
    }

    /**
     * Cf. méthode surchargée.
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);

        contributeToActionBars();

        // on veut écouter les statistiques
        MarkerAnalyser.getInstance().addAnalyserListener(this);
        MarkerAnalyser.getInstance().selectionChanged(this,
            getSite().getPage().getSelection());
    }

    /**
     * Cf. method below.
     * 
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose()
    {
        super.dispose();

        // remove the listener
        MarkerAnalyser.getInstance().removeAnalyserListener(this);
    }

    /**
     * See method below.
     * 
     * @see com.atlassw.tools.eclipse.checkstyle.stats.analyser.IAnalyserListener#getPage()
     */
    public IWorkbenchPage getPage()
    {
        return getViewSite().getPage();
    }

    /**
     * @see com.atlassw.tools.eclipse.checkstyle.stats.views.AbstractStatsView#getViewId()
     */
    protected String getViewId()
    {
        return VIEW_ID;
    }

    /**
     * See method below.
     * 
     * @see com.atlassw.tools.eclipse.checkstyle.stats.views.AbstractStatsView#createLabelProvider()
     */
    protected IBaseLabelProvider createLabelProvider()
    {
        return new MarkerStatsViewLabelProvider();
    }

    /**
     * See method below.
     * 
     * @see com.atlassw.tools.eclipse.checkstyle.stats.views.AbstractStatsView#createColumns(org.eclipse.swt.widgets.Table)
     */
    protected void createColumns(Table table)
    {
        TableColumn idCol = new TableColumn(table, SWT.LEFT, 0);
        idCol.setText(Messages.MarkerStatsView_kindOfErrorColumn);
        idCol.setWidth(DESC_COL_WIDTH);
        idCol
            .addSelectionListener(new SorterSelectionListener(new NameSorter()));

        TableColumn countCol = new TableColumn(table, SWT.CENTER, 1);
        countCol.setText(Messages.MarkerStatsView_numberOfErrorsColumn);
        countCol.pack();
        countCol.addSelectionListener(new SorterSelectionListener(
            new CountSorter()));
    }

    /**
     * See method below.
     * 
     * @see com.atlassw.tools.eclipse.checkstyle.stats.analyser.IAnalyserListener#statsUpdated(com.atlassw.tools.eclipse.checkstyle.stats.analyser.AnalyserEvent)
     */
    public void statsUpdated(final AnalyserEvent analyserEvent)
    {
        mStatsToDisplay = analyserEvent.getStats();
        mSelectionToDisplay = analyserEvent.getSelection();
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
                    // sets the viewer input
                    getViewer().setInput(mStatsToDisplay.getMarkerStats());
                    // and updates the view description label
                    StringBuffer labelBuffer = new StringBuffer(" "); //$NON-NLS-1$
                    labelBuffer.append(StatsViewUtils
                        .computeMainTitle(analyserEvent));
                    labelBuffer.append(" - "); //$NON-NLS-1$
                    Collection namesList = StatsViewUtils
                        .computeAnalysedResourceNames(analyserEvent);
                    for (Iterator iter = namesList.iterator(); iter.hasNext();)
                    {
                        labelBuffer.append(iter.next());
                        if (iter.hasNext())
                        {
                            labelBuffer.append(", "); //$NON-NLS-1$
                        }
                    }
                    getDescLabel().setText(labelBuffer.toString());
                }
            }
        });
    }

    /**
     * See method below.
     * 
     * @see com.atlassw.tools.eclipse.checkstyle.stats.views.AbstractStatsView#makeActions()
     */
    protected void makeActions()
    {
        // Action used to display the pie chart
        mChartAction = new Action()
        {
            public void run()
            {
                try
                {
                    GraphStatsView view = (GraphStatsView) getSite()
                        .getWorkbenchWindow().getActivePage().showView(
                            GraphStatsView.VIEW_ID);
                    if (view != null)
                    {
                        view.statsUpdated(new AnalyserEvent(mStatsToDisplay,
                            mSelectionToDisplay));
                    }
                }
                catch (PartInitException e)
                {
                    StatsCheckstylePlugin.log(IStatus.ERROR, NLS.bind(
                        Messages.MarkerStatsView_unableToOpenGraph,
                        GraphStatsView.VIEW_ID), e);
                    // TODO : mettre message d'erreur à l'utilisateur
                }
            }
        };
        mChartAction.setText(Messages.MarkerStatsView_displayChart);
        mChartAction
            .setToolTipText(Messages.MarkerStatsView_displayChartTooltip);
        mChartAction.setImageDescriptor(PlatformUI.getWorkbench()
            .getSharedImages().getImageDescriptor(
                ISharedImages.IMG_TOOL_FORWARD));

        // action used to display the detail of a specific error type
        mDetailErrorCategoryAction = new Action()
        {
            public void run()
            {
                IStructuredSelection selection = (IStructuredSelection) getViewer()
                    .getSelection();
                if (selection.getFirstElement() instanceof MarkerStat)
                {
                    MarkerStat markerStat = (MarkerStat) selection
                        .getFirstElement();
                    try
                    {
                        DetailStatsView view = (DetailStatsView) getSite()
                            .getWorkbenchWindow().getActivePage().showView(
                                DetailStatsView.VIEW_ID);
                        if (view != null)
                        {
                            view.setMarkerCategoryToDisplay(markerStat);
                        }
                    }
                    catch (PartInitException e)
                    {
                        StatsCheckstylePlugin.log(IStatus.ERROR, NLS.bind(
                            Messages.MarkerStatsView_unableToOpenGraph,
                            GraphStatsView.VIEW_ID), e);
                        // TODO : mettre message d'erreur à l'utilisateur
                    }
                }
            }
        };
        mDetailErrorCategoryAction
            .setText(Messages.MarkerStatsView_showDetails);
        mDetailErrorCategoryAction
            .setToolTipText(Messages.MarkerStatsView_showDetailsTooltip);
        mDetailErrorCategoryAction.setImageDescriptor(PlatformUI.getWorkbench()
            .getSharedImages().getImageDescriptor(
                ISharedImages.IMG_OBJS_INFO_TSK));

        // hooks the action to double click
        hookDoubleClickAction(mDetailErrorCategoryAction);

        // and to the context menu too
        ArrayList actionList = new ArrayList(1);
        actionList.add(mDetailErrorCategoryAction);
        actionList.add(mChartAction);
        hookContextMenu(actionList);
    }

    /**
     * Ajoute les actions à la toolbar.
     */
    private void contributeToActionBars()
    {
        IActionBars bars = getViewSite().getActionBars();
        bars.getMenuManager().add(mChartAction);
        bars.getMenuManager().add(new FiltersAction(this));
    }

}