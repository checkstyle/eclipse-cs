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

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JPanel;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.title.TextTitle;
import org.jfree.util.Rotation;

import com.atlassw.tools.eclipse.checkstyle.stats.Messages;
import com.atlassw.tools.eclipse.checkstyle.stats.StatsCheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.stats.analyser.AnalyserEvent;
import com.atlassw.tools.eclipse.checkstyle.stats.data.Stats;
import com.atlassw.tools.eclipse.checkstyle.stats.preferences.PreferencePage;
import com.atlassw.tools.eclipse.checkstyle.stats.views.internal.FiltersAction;

/**
 * Vue qui affiche les statistiques Checkstyle sous forme de graph (camember).
 * 
 * @author Fabrice BELLINGARD
 */

public class GraphStatsView extends AbstractStatsView
{

    /**
     * Identifiant unique de cette vue. Cf. plugin.xml
     */
    public static final String VIEW_ID = "com.atlassw.tools.eclipse.checkstyle.stats.views.GraphStatsView"; //$NON-NLS-1$

    /**
     * Le composite SWT qui contient les éléments Swing/AWT.
     */
    private Composite mEmbeddedComposite;

    /**
     * Le graphe sous forme de camember.
     */
    private JFreeChart mGraph;

    /**
     * Le dataset utilisé par le graphe.
     */
    private GraphPieDataset mPieDataset;

    /**
     * Les stats à afficher sous forme de graphe.
     */
    private Stats mStatsToDisplay;

    /**
     * La sélection qui correspond à l'affichage.
     */
    private IStructuredSelection mSelectionToDisplay;

    /**
     * Montre la vue de listing des erreurs.
     */
    private Action mListingAction;

    /**
     * Permet l'affichage ou pas des erreurs Javadoc.
     */
    private Action mShowJavadocErrorsAction;

    /**
     * Permet l'affichage ou pas de toutes les catégories.
     */
    private Action mShowAllCategoriesAction;

    /**
     * The constructor.
     */
    public GraphStatsView()
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

        // on crée les composants permettant d'encapsuler du AWT/Swing dans SWT
        mEmbeddedComposite = new Composite(parent, SWT.EMBEDDED);
        Frame fileTableFrame = SWT_AWT.new_Frame(mEmbeddedComposite);

        // puis on crée les éléments du graph
        mPieDataset = new GraphPieDataset();
        mGraph = createChart(mPieDataset);
        JPanel panel = new ChartPanel(mGraph);
        fileTableFrame.add(panel);

        makeActions();
    }

    /**
     * {@inheritDoc}
     */
    protected void initMenu(IMenuManager menu)
    {
        menu.add(new FiltersAction(this));
        menu.add(new Separator());
        menu.add(mListingAction);
        menu.add(new Separator());
        menu.add(mShowJavadocErrorsAction);
        menu.add(mShowAllCategoriesAction);
    }

    /**
     * {@inheritDoc}
     */
    protected void initToolBar(IToolBarManager tbm)
    {
        tbm.add(new FiltersAction(this));
    }

    /**
     * @see com.atlassw.tools.eclipse.checkstyle.stats.views.AbstractStatsView#getViewId()
     */
    protected String getViewId()
    {
        return VIEW_ID;
    }

    protected void handleStatsRebuilt()
    {
        if (!mEmbeddedComposite.isDisposed() && mEmbeddedComposite.isVisible())
        {
            // on met à jour le dataset, qui notifie le graph, qui se
            // met à jour
            if (mStatsToDisplay == null)
            {
                mPieDataset.removeValues();
            }
            else
            {
                mPieDataset.setMarkerStatCollection(
                    getStats().getMarkerStats(), getStats().getMarkerCount());
            }
            // on met à jour le titre
            // ArrayList titlesList = computeTitleList(analyserEvent);
            // mGraph.setSubtitles(titlesList);
        }
    }


    protected void makeActions()
    {
        mListingAction = new Action()
        {
            public void run()
            {
                try
                {
                    MarkerStatsView view = (MarkerStatsView) getSite()
                        .getWorkbenchWindow().getActivePage().showView(
                            MarkerStatsView.VIEW_ID);
                    // if (view != null)
                    // {
                    // view.statsUpdated(new AnalyserEvent(mStatsToDisplay,
                    // mSelectionToDisplay));
                    // }
                }
                catch (PartInitException e)
                {
                    StatsCheckstylePlugin.log(IStatus.ERROR, NLS.bind(
                        Messages.GraphStatsView_unableToOpenListingView,
                        MarkerStatsView.VIEW_ID), e);
                    // TODO : mettre message d'erreur à l'utilisateur
                }
            }
        };
        mListingAction.setText(Messages.GraphStatsView_displayListing);
        mListingAction.setImageDescriptor(PlatformUI.getWorkbench()
            .getSharedImages().getImageDescriptor(
                ISharedImages.IMG_TOOL_FORWARD));

        mShowJavadocErrorsAction = new Action(
            Messages.GraphStatsView_displayJavadocErrors, Action.AS_CHECK_BOX)
        {
            public void run()
            {
                Display.getDefault().asyncExec(new Runnable()
                {
                    /**
                     * Cf. méthode surchargée.
                     * 
                     * @see java.lang.Runnable#run()
                     */
                    public void run()
                    {
                        if (!mEmbeddedComposite.isDisposed()
                            && mEmbeddedComposite.isVisible())
                        {
                            // on averti le dataset
                            mPieDataset.setShowJavadoc(mShowJavadocErrorsAction
                                .isChecked());
                            statsUpdated(new AnalyserEvent(mStatsToDisplay,
                                mSelectionToDisplay));
                        }
                    }
                });
            }
        };
        mShowJavadocErrorsAction.setImageDescriptor(PlatformUI.getWorkbench()
            .getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
        mShowJavadocErrorsAction.setChecked(StatsCheckstylePlugin.getDefault()
            .getPreferenceStore().getBoolean(
                PreferencePage.PROPS_SHOW_JAVADOC_ERRORS));
        mPieDataset.setShowJavadoc(mShowJavadocErrorsAction.isChecked());

        mShowAllCategoriesAction = new Action(
            Messages.GraphStatsView_displayAllCategories, Action.AS_CHECK_BOX)
        {
            public void run()
            {
                Display.getDefault().asyncExec(new Runnable()
                {
                    /**
                     * Cf. méthode surchargée.
                     * 
                     * @see java.lang.Runnable#run()
                     */
                    public void run()
                    {
                        if (!mEmbeddedComposite.isDisposed()
                            && mEmbeddedComposite.isVisible())
                        {
                            // on averti le dataset
                            mPieDataset
                                .setShowAllCategories(mShowAllCategoriesAction
                                    .isChecked());
                            statsUpdated(new AnalyserEvent(mStatsToDisplay,
                                mSelectionToDisplay));
                        }
                    }
                });
            }
        };
        mShowAllCategoriesAction.setImageDescriptor(PlatformUI.getWorkbench()
            .getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));
        mShowAllCategoriesAction.setChecked(StatsCheckstylePlugin.getDefault()
            .getPreferenceStore().getBoolean(
                PreferencePage.PROPS_SHOW_ALL_CATEGORIES));
        mPieDataset.setShowAllCategories(mShowAllCategoriesAction.isChecked());
    }

    /**
     * Crée le graphe JFreeChart.
     * 
     * @param piedataset :
     *            la source de données à afficher
     * @return le diagramme
     */
    private JFreeChart createChart(GraphPieDataset piedataset)
    {
        JFreeChart jfreechart = ChartFactory.createPieChart3D(
            Messages.GraphStatsView_errorsRepartition, piedataset, true, true,
            false);
        jfreechart.setLegend(null);
        PiePlot3D pieplot3d = (PiePlot3D) jfreechart.getPlot();
        final double angle = 290D;
        pieplot3d.setStartAngle(angle);
        pieplot3d.setDirection(Rotation.CLOCKWISE);
        final float foreground = 0.5F;
        pieplot3d.setForegroundAlpha(foreground);
        pieplot3d.setNoDataMessage(Messages.GraphStatsView_noDataToDisplay);
        return jfreechart;
    }

    public void statsUpdated(final AnalyserEvent analyserEvent)
    {
        mStatsToDisplay = analyserEvent.getStats();
        mSelectionToDisplay = analyserEvent.getSelection();
        // nécessaire car il va y avoir un travail de fait sur l'UI
        Display.getDefault().asyncExec(new Runnable()
        {
            /**
             * Cf. méthode surchargée.
             * 
             * @see java.lang.Runnable#run()
             */
            public void run()
            {
                if (!mEmbeddedComposite.isDisposed()
                    && mEmbeddedComposite.isVisible())
                {
                    // on met à jour le dataset, qui notifie le graph, qui se
                    // met à jour
                    if (mStatsToDisplay == null)
                    {
                        mPieDataset.removeValues();
                    }
                    else
                    {
                        mPieDataset
                            .setMarkerStatCollection(mStatsToDisplay
                                .getMarkerStats(), mStatsToDisplay
                                .getMarkerCount());
                    }
                    // on met à jour le titre
                    ArrayList titlesList = computeTitleList(analyserEvent);
                    mGraph.setSubtitles(titlesList);
                }
            }
        });
    }

    /**
     * Rend la liste des sous-titres à afficher.
     * 
     * @param event
     *            l'évènement de changement de stats
     * @return la liste de String
     */
    private ArrayList computeTitleList(AnalyserEvent event)
    {
        // liste des sous-titres, dont le 1er est le nombre d'erreurs
        ArrayList titles = new ArrayList();
        StringBuffer title = new StringBuffer(StatsViewUtils
            .computeMainTitle(event));
        if (!mShowJavadocErrorsAction.isChecked())
        {
            title.append(" "); //$NON-NLS-1$
            title.append(Messages.GraphStatsView_javadocNotDisplayed);
        }
        titles.add(new TextTitle(title.toString()));

        // vérification pour la sélection
        Collection namesList = StatsViewUtils
            .computeAnalysedResourceNames(event);
        if (!namesList.isEmpty())
        {
            StringBuffer namesBuffer = new StringBuffer();
            for (Iterator iter = namesList.iterator(); iter.hasNext();)
            {

                namesBuffer.append(iter.next());
                if (iter.hasNext())
                {
                    namesBuffer.append(", "); //$NON-NLS-1$
                }
            }
            titles.add(new TextTitle(namesBuffer.toString()));
        }

        return titles;
    }

    /**
     * Cf. méthode surchargée.
     * 
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus()
    {

    }

    /**
     * Ajoute les actions à la toolbar.
     */
    private void contributeToActionBars()
    {
        IActionBars bars = getViewSite().getActionBars();
        bars.getMenuManager().add(mListingAction);
        bars.getMenuManager().add(new Separator());
        bars.getMenuManager().add(mShowJavadocErrorsAction);
        bars.getMenuManager().add(mShowAllCategoriesAction);
    }

}