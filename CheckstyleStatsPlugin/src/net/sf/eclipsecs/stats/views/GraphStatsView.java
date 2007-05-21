//============================================================================
//
// Copyright (C) 2002-2006  David Schneider, Lars Ködderitzsch, Fabrice Bellingard
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

package net.sf.eclipsecs.stats.views;

import java.awt.Frame;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.eclipsecs.stats.Messages;
import net.sf.eclipsecs.stats.PrefsInitializer;
import net.sf.eclipsecs.stats.data.Stats;
import net.sf.eclipsecs.stats.util.CheckstyleStatsPluginImages;
import net.sf.eclipsecs.stats.views.internal.FiltersAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.ImageEncoder;
import org.jfree.chart.encoders.ImageEncoderFactory;
import org.jfree.chart.encoders.ImageFormat;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.Rotation;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;

/**
 * View that shows a graph for the Checkstyle marker distribution.
 * 
 * @author Fabrice BELLINGARD
 * @author Lars Ködderitzsch
 */

public class GraphStatsView extends AbstractStatsView
{

    //
    // constants
    //

    /** The unique view id. */
    public static final String VIEW_ID = GraphStatsView.class.getName();

    //
    // attributes
    //

    /** The label containing the view description. */
    private Label mLabelDesc;

    /** The composite to harbor the JFreeChart control. */
    private Composite mEmbeddedComposite;

    /** The graph component. */
    private JFreeChart mGraph;

    /** The dataset for the graph. */
    private GraphPieDataset mPieDataset;

    /** Action to go back to the marker overview view. */
    private Action mListingAction;

    /** Allows to show all the categories or not. */
    private Action mShowAllCategoriesAction;

    /** Exports the error listing as a report. */
    private Action mExportGraphAsImageAction;

    /** The last folder used to store the generated reports. */
    private String mLastExportFolderName;

    /** The last file name used to store the generated reports. */
    private String mLastExportFileName = "CheckstyleStatsGraph";

    //
    // methods
    //

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {

        super.createPartControl(parent);

        // set up the main layout
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        parent.setLayout(layout);

        // the label
        mLabelDesc = new Label(parent, SWT.NONE);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        mLabelDesc.setLayoutData(gridData);

        // create the date set for the chart
        mPieDataset = new GraphPieDataset();
        mPieDataset.setShowAllCategories(mShowAllCategoriesAction.isChecked());
        mGraph = createChart(mPieDataset);

        // creates the chart component
        mEmbeddedComposite = new Composite(parent, SWT.EMBEDDED);
        mEmbeddedComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        Frame fileTableFrame = SWT_AWT.new_Frame(mEmbeddedComposite);
        ChartPanel panel = new ChartPanel(mGraph);
        fileTableFrame.add(panel);

        panel.addChartMouseListener(new ChartMouseListener()
        {

            public void chartMouseClicked(ChartMouseEvent event)
            {
            // TODO Auto-generated method stub

            }

            public void chartMouseMoved(ChartMouseEvent event)
            {
            // TODO Auto-generated method stub

            }
        });

// experimental usage of JFreeChart SWT
// // the composite to harbor the Swing chart control
// mEmbeddedComposite = new ChartComposite(parent, SWT.NONE, mGraph, true);
// mEmbeddedComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        // initialize the view data
        refresh();
    }

    /**
     * {@inheritDoc}
     */
    protected void initMenu(IMenuManager menu)
    {
        menu.add(new FiltersAction(this));
        menu.add(new Separator());
        menu.add(mShowAllCategoriesAction);
    }

    /**
     * {@inheritDoc}
     */
    protected void initToolBar(IToolBarManager tbm)
    {
        tbm.add(mListingAction);
        tbm.add(mExportGraphAsImageAction);
        tbm.add(new Separator());
        tbm.add(mShowAllCategoriesAction);
        tbm.add(new FiltersAction(this));
    }

    /**
     * {@inheritDoc}
     */
    protected String getViewId()
    {
        return VIEW_ID;
    }

    /**
     * {@inheritDoc}
     */
    protected void handleStatsRebuilt()
    {
        if (!mEmbeddedComposite.isDisposed() && mEmbeddedComposite.isVisible())
        {
            // change the marker stats on the data set
            mPieDataset.setStats(getStats());

            updateLabel();
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void makeActions()
    {
        mListingAction = new Action()
        {
            public void run()
            {
                try
                {
                    getSite().getWorkbenchWindow().getActivePage()
                            .showView(MarkerStatsView.VIEW_ID);
                }
                catch (PartInitException e)
                {
                    CheckstyleLog.log(e, NLS.bind(Messages.GraphStatsView_unableToOpenListingView,
                            MarkerStatsView.VIEW_ID));
                    // TODO : mettre message d'erreur à l'utilisateur
                }
            }
        };
        mListingAction.setText(Messages.GraphStatsView_displayListing);
        mListingAction.setToolTipText(Messages.GraphStatsView_displayListing);
        mListingAction.setImageDescriptor(CheckstyleStatsPluginImages.LIST_VIEW_ICON);

        mShowAllCategoriesAction = new Action(Messages.GraphStatsView_displayAllCategories,
                Action.AS_CHECK_BOX)
        {
            public void run()
            {
                Display.getDefault().asyncExec(new Runnable()
                {
                    public void run()
                    {
                        if (!mEmbeddedComposite.isDisposed() && mEmbeddedComposite.isVisible())
                        {
                            // on averti le dataset
                            mPieDataset.setShowAllCategories(mShowAllCategoriesAction.isChecked());

                            // update the preference
                            CheckstylePlugin.getDefault().getPreferenceStore().setValue(
                                    PrefsInitializer.PROPS_SHOW_ALL_CATEGORIES,
                                    mShowAllCategoriesAction.isChecked());

                            refresh();
                        }
                    }
                });
            }
        };
        mShowAllCategoriesAction.setToolTipText(Messages.GraphStatsView_displayAllCategories);
        mShowAllCategoriesAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));
        mShowAllCategoriesAction.setChecked(CheckstylePlugin.getDefault().getPreferenceStore()
                .getBoolean(PrefsInitializer.PROPS_SHOW_ALL_CATEGORIES));

        // Action used export the error listing as a report
        mExportGraphAsImageAction = new Action()
        {
            public void run()
            {
                FileDialog dialog = new FileDialog(getSite().getShell());
                dialog.setText(Messages.MarkerStatsView_chooseFileToExport);
                dialog.setFileName(mLastExportFileName);
                if (mLastExportFolderName != null)
                {
                    dialog.setFilterPath(mLastExportFolderName);
                }
                String selectedFilePath = dialog.open();
                if (selectedFilePath != null)
                {
                    // TODO : the user should be able to choose the type of
                    // image...
                    if (!selectedFilePath.endsWith(".png"))
                    {
                        selectedFilePath += ".png";
                    }
                    File selectedFile = new File(selectedFilePath);
                    mLastExportFileName = selectedFile.getName();
                    mLastExportFolderName = selectedFile.getParentFile().getAbsolutePath();

                    try
                    {
                        // TODO : the user should be able to choose the type of
                        // image, the width
                        // and the height of the buffered image
                        ImageEncoder imageEncoder = ImageEncoderFactory
                                .newInstance(ImageFormat.PNG);
                        imageEncoder.encode(mGraph.createBufferedImage(800, 600),
                                new FileOutputStream(selectedFile));
                    }
                    catch (IOException e)
                    {
                        CheckstyleLog.log(e, Messages.MarkerStatsView_graphExportFailed);
                        MessageDialog.openError(getSite().getShell(),
                                Messages.MarkerStatsView_graphExportFailed,
                                Messages.MarkerStatsView_graphExportFailed + "\n" + e.getMessage());
                    }
                }
            }
        };
        mExportGraphAsImageAction.setText(Messages.MarkerStatsView_exportGraphAsImage);
        mExportGraphAsImageAction
                .setToolTipText(Messages.MarkerStatsView_exportGraphAsImageTooltip);
        mExportGraphAsImageAction
                .setImageDescriptor(CheckstyleStatsPluginImages.EXPORT_REPORT_ICON);

    }

    /**
     * Crée le graphe JFreeChart.
     * 
     * @param piedataset : la source de données à afficher
     * @return le diagramme
     */
    private JFreeChart createChart(GraphPieDataset piedataset)
    {
        JFreeChart jfreechart = ChartFactory.createPieChart3D(null, piedataset, false, true, false);

        PiePlot3D pieplot3d = (PiePlot3D) jfreechart.getPlot();
        pieplot3d.setInsets(new RectangleInsets(0, 0, 0, 0));
        final double angle = 290D;
        pieplot3d.setStartAngle(angle);
        pieplot3d.setDirection(Rotation.CLOCKWISE);
        final float foreground = 0.5F;
        pieplot3d.setForegroundAlpha(foreground);
        pieplot3d.setNoDataMessage(Messages.GraphStatsView_noDataToDisplay);

        return jfreechart;
    }

    /**
     * Updates the title label.
     */
    private void updateLabel()
    {

        Stats stats = getStats();

        String text = NLS.bind(Messages.GraphStatsView_lblViewMessage, new Object[] {
            new Integer(stats.getMarkerCount()), new Integer(stats.getMarkerStats().size()),
            new Integer(stats.getMarkerCountAll()) });
        mLabelDesc.setText(text);
    }

}