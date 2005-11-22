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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.atlassw.tools.eclipse.checkstyle.builder.CheckstyleMarker;
import com.atlassw.tools.eclipse.checkstyle.config.meta.MetadataFactory;
import com.atlassw.tools.eclipse.checkstyle.config.meta.RuleMetadata;
import com.atlassw.tools.eclipse.checkstyle.stats.Messages;
import com.atlassw.tools.eclipse.checkstyle.stats.StatsCheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.stats.data.MarkerStat;
import com.atlassw.tools.eclipse.checkstyle.stats.data.Stats;

/**
 * 
 * Class that scans the files to look for Checkstyle markers and that creates
 * stats about those markers.
 * 
 * @author Fabrice BELLINGARD
 */
public final class MarkerAnalyser implements ISelectionListener
{

    /**
     * Id of checkstyle markers.
     */
    private static final String CHECKSTYLE_MARKER_ID = "com.atlassw.tools.eclipse.checkstyle.CheckstyleMarker"; //$NON-NLS-1$

    /**
     * Single instance.
     */
    private static MarkerAnalyser sInstance = new MarkerAnalyser();

    /**
     * Analyser listeners. Contains IAnalyserListener classes.
     */
    private ArrayList mAnalyserListeners;

    /**
     * Singleton pattern.
     */
    private MarkerAnalyser()
    {
        mAnalyserListeners = new ArrayList();
    }

    /**
     * The unique instance.
     * 
     * @return the unique instance
     */
    public static MarkerAnalyser getInstance()
    {
        return sInstance;
    }

    /**
     * Adds a listener to the analyser.
     * 
     * @param listener :
     *            the listener
     */
    public void addAnalyserListener(final IAnalyserListener listener)
    {
        mAnalyserListeners.add(listener);
        IWorkbenchPage page = listener.getPage();
        if (page != null)
        {
            page.addSelectionListener(this);
        }
    }

    /**
     * Removes the listener from the list.
     * 
     * @param listener :
     *            the listener
     */
    public void removeAnalyserListener(IAnalyserListener listener)
    {
        mAnalyserListeners.remove(listener);
        IWorkbenchPage page = listener.getPage();
        if (page != null)
        {
            boolean pageFound = false;
            // we try to find if another listener belongs to that page
            for (Iterator iter = mAnalyserListeners.iterator(); iter.hasNext();)
            {
                IAnalyserListener analyserListener = (IAnalyserListener) iter
                    .next();
                if (page.equals(analyserListener.getPage()))
                {
                    pageFound = true;
                    break;
                }
            }
            if (!pageFound)
            {
                // no listener left on this page
                page.removeSelectionListener(this);
            }
        }
    }

    /**
     * Cf. method below.
     * 
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(final IWorkbenchPart part,
        final ISelection selection)
    {
        if (selection == null || selection.isEmpty())
        {
            return;
        }
        Job job = new Job(Messages.MarkerAnalyser_computingstats)
        {
            /**
             * Cf. method below.
             * 
             * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
             */
            protected IStatus run(IProgressMonitor monitor)
            {
                IStructuredSelection cleanSelection = null;
                try
                {
                    cleanSelection = filterSelection(selection);

                    if (cleanSelection != null && !cleanSelection.isEmpty())
                    {
                        // compute the stats
                        Stats stats = scanSelection(part, cleanSelection);

                        if (stats != null)
                        {
                            // notify listeners
                            fireAnalyserEvent(part, cleanSelection, stats);
                        }
                    }
                }
                catch (CoreException e)
                {
                    // Log
                    StatsCheckstylePlugin.log(IStatus.ERROR,
                        Messages.MarkerAnalyser_errorWhileComputingStats, e);
                    // tell the listeners
                    fireAnalyserEvent(part, cleanSelection, null);
                    return Status.CANCEL_STATUS;
                }

                return Status.OK_STATUS;
            }
        };
        job.setPriority(Job.SHORT);
        job.schedule();

    }

    /**
     * Filters the selection to replace any TextSelection by a
     * StructuredSelection containing the IFile object corresponding to the file
     * being edited, and to prevent having non Java related files.
     * 
     * @param selection
     *            the selection to consolidate
     * @return the consolidated selection or null if a problem occured
     */
    private IStructuredSelection filterSelection(ISelection selection)
    {
        IStructuredSelection consolidatedSelection = null;

        if (selection instanceof ITextSelection)
        {
            final Object[] selectedFiles = new Object[1];
            // an editor may have the focus
            Display.getDefault().syncExec(new Runnable()
            {
                public void run()
                {
                    IWorkbenchWindow window = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow();
                    if (window == null)
                    {
                        return;
                    }
                    IWorkbenchPage page = window.getActivePage();
                    if (page == null)
                    {
                        // on retourne la liste vide, y'a rien à afficher
                        return;
                    }
                    IWorkbenchPart activePart = page.getActivePart();
                    if (activePart instanceof IEditorPart)
                    {
                        IEditorPart editorPart = (IEditorPart) activePart;
                        IFile editedFile = (IFile) editorPart.getEditorInput()
                            .getAdapter(IFile.class);
                        if (editedFile != null)
                        {
                            selectedFiles[0] = editedFile;
                        }
                    }
                }
            });
            if (selectedFiles[0] != null)
            {
                consolidatedSelection = new StructuredSelection(selectedFiles);
            }
        }
        else if (selection instanceof IStructuredSelection)
        {
            // we need to filter
            ArrayList adaptableObjects = new ArrayList();
            for (Iterator it = ((IStructuredSelection) selection).iterator(); it
                .hasNext();)
            {
                Object currentObject = it.next();
                if (currentObject instanceof IAdaptable)
                {
                    IAdaptable adaptable = (IAdaptable) currentObject;
                    if (adaptable.getAdapter(IJavaProject.class) != null
                        || adaptable.getAdapter(IPackageFragmentRoot.class) != null
                        || adaptable.getAdapter(IPackageFragment.class) != null
                        || adaptable.getAdapter(ICompilationUnit.class) != null
                        || adaptable.getAdapter(IType.class) != null)
                    {
                        adaptableObjects.add(currentObject);
                    }
                }
            }
            consolidatedSelection = new StructuredSelection(adaptableObjects
                .toArray());
        }
        else
        {
            consolidatedSelection = null;
        }

        return consolidatedSelection;
    }

    /**
     * Notify listeners that the class has finished to analyse the selection.
     * 
     * @param part :
     *            the part where the selection was made
     * @param selection :
     *            the selection
     * @param stats :
     *            the computed stats
     */
    private void fireAnalyserEvent(IWorkbenchPart part,
        IStructuredSelection selection, Stats stats)
    {
        for (Iterator iter = mAnalyserListeners.iterator(); iter.hasNext();)
        {
            IAnalyserListener analyserListener = (IAnalyserListener) iter
                .next();
            IWorkbenchPage page = analyserListener.getPage();
            if (page != null && page.getActivePart() != part)
            {
                continue;
            }
            // notify the other listeners: those which belong to the part
            // and those who are not UI
            analyserListener.statsUpdated(new AnalyserEvent(stats, selection));
        }
    }

    /**
     * Scanne la sélection passée en paramètre pour en tirer les statistiques
     * Checkstyle.
     * 
     * @param part :
     *            the part where the selection was made
     * @param selection :
     *            les éléments sélectionnés pour le scan
     * @return les statistiques générées par le scan ou NULL si la sélection
     *         n'est pas analysable
     * @throws CoreException
     *             si pbm lors du scan
     */
    private Stats scanSelection(IWorkbenchPart part,
        IStructuredSelection selection) throws CoreException
    {
        ArrayList markerStatsCollection = new ArrayList();
        int markerCount = 0;

        // on va regarder quels éléments ont été sélectionnés
        IStructuredSelection structuredSelection = (IStructuredSelection) selection;
        for (Iterator it = structuredSelection.iterator(); it.hasNext();)
        {
            Object element = it.next();
            if (element instanceof IFile)
            {
                markerCount += scanResource((IFile) element,
                    IResource.DEPTH_ZERO, markerStatsCollection);
            }
            else
            {
                markerCount += scanAdaptable((IAdaptable) element,
                    markerStatsCollection);
            }
        }

        // on fait le tri par défaut
        Collections.sort(markerStatsCollection);

        return new Stats(markerStatsCollection, markerCount, 0);
    }

    /**
     * Scanne l'objet adaptable passée en paramètre et trie les marqueurs
     * Checkstyle pour en faire des stats.
     * 
     * @param adaptable :
     *            l'élement à scanner
     * @param markerStatsCollection :
     *            la collection d'objet MarkerStat à compléter
     * @return le nombre de marqueurs trouvés
     * @throws CoreException
     *             si pbm lors du scan
     * @throws StatsCheckstylePluginException
     *             si l'adaptable que l'on scanne n'est pas analysable
     */
    private int scanAdaptable(IAdaptable adaptable,
        Collection markerStatsCollection) throws CoreException
    {
        // par défaut, on va scanner la ressource sélectionnée et tous ses
        // descendants
        int depthToScan = IResource.DEPTH_INFINITE;

        if (adaptable.getAdapter(IPackageFragment.class) != null)
        {
            // comme il s'agit d'un package, on ne scanne que le répertoire
            // correspondant à ce package
            depthToScan = IResource.DEPTH_ONE;
        }

        // maintenant, on scan la ressource
        int markerCount = scanResource((IResource) adaptable
            .getAdapter(IResource.class), depthToScan, markerStatsCollection);

        return markerCount;
    }

    /**
     * Scanne la ressource passée en paramètre et trie les marqueurs Checkstyle
     * pour en faire des stats.
     * 
     * @param resource :
     *            la ressource à scanner
     * @param depthToScan :
     *            the depth to look for markers of the given resource
     * @param markerStatsCollection :
     *            la collection d'objet MarkerStat à compléter
     * @return le nombre de marqueurs trouvés
     * @throws CoreException
     *             si pbm lors du scan
     * @throws StatsCheckstylePluginException
     *             si l'adaptable que l'on scanne n'est pas analysable
     */
    private int scanResource(IResource resource, int depthToScan,
        Collection markerStatsCollection) throws CoreException
    {
        if (!resource.isAccessible())
        {
            // ressource non dispo
            return 0;
        }

        // et on scanne
        IMarker[] markers = resource.findMarkers(CHECKSTYLE_MARKER_ID, true,
            depthToScan);

        for (int i = 0; i < markers.length; i++)
        {
            updateStats(markers[i], markerStatsCollection);
        }

        return markers.length;
    }

    /**
     * Returns.
     * 
     * @param aClassName
     * @param key
     * @return
     */
    private String getMessageBundle(String aClassName, String key)
    {
        int endIndex = aClassName.lastIndexOf('.');
        String messages = "messages"; //$NON-NLS-1$
        if (endIndex >= 0)
        {
            String packageName = aClassName.substring(0, endIndex);
            messages = packageName + "." + messages; //$NON-NLS-1$
        }
        ResourceBundle resourceBundle = ResourceBundle.getBundle(messages);
        return resourceBundle.getString(key);
    }

    private String getUnlocalizedMessage(IMarker marker) throws CoreException
    {
        String key = (String) marker.getAttribute(CheckstyleMarker.MESSAGE_KEY);
        RuleMetadata ruleMetadata = MetadataFactory
            .getRuleMetadata((String) marker
                .getAttribute(CheckstyleMarker.MODULE_NAME));

        for (Iterator iter = ruleMetadata.getAlternativeNames().iterator(); iter
            .hasNext();)
        {
            String checker = (String) iter.next();
            try
            {
                String message = getMessageBundle(checker, key);
                return message;
            }
            catch (MissingResourceException e)
            {
                // let's continue to check the other alternative names
            }
        }

        // none was found: return the key name
        return key;
    }

    /**
     * Met à jour les statistiques concernant les marqueurs Checkstyle.
     * 
     * @param marker :
     *            le marqueur à prendre en compte
     * @param markerStatsCollection :
     *            la collection à metre à jour
     * @throws CoreException
     *             si pbm lors du scan
     */
    private void updateStats(IMarker marker, Collection markerStatsCollection)
        throws CoreException
    {
        String message = getUnlocalizedMessage(marker);
        message = cleanMessage(message);

        // on vérifie que le message n'est pas null ou vide
        if (message == null || message.equals("")) //$NON-NLS-1$
        {
            // cela ne devrait pas arriver, mais bon, on laisse faire
            StatsCheckstylePlugin.log(IStatus.WARNING,
                Messages.MarkerAnalyser_markerMessageShouldntBeEmpty, null);
            return;
        }

        // puis on recherche
        MarkerStat stat = findMarkerStat(message, markerStatsCollection);
        if (stat == null)
        {
            // 1ere fois qu'on rencontre un marqueur de ce type
            MarkerStat newMarkerStat = new MarkerStat(message);
            newMarkerStat.addMarker(marker);
            markerStatsCollection.add(newMarkerStat);
        }
        else
        {
            // on augmente juste le nombre d'occurence
            stat.addMarker(marker);
        }
    }

    /**
     * Cleans the unlocalized message so that it is more readable.
     * 
     * @param message :
     *            the message to clean
     * @return the cleaned message
     */
    private String cleanMessage(String message)
    {
        // replacements
        String finalMessage = REGEXP_HOLES.matcher(message).replaceAll("X"); //$NON-NLS-1$
        finalMessage = REGEXP_QUOTE.matcher(finalMessage).replaceAll("'"); //$NON-NLS-1$

        return finalMessage;
    }

    /**
     * Retourne l'objet MarkerStat correspondant au message passé en paramètre.
     * 
     * @param message :
     *            message du marqueur, non NULL
     * @param markerStatsCollection :
     *            la collection de MarkerStat dans laquelle chercher
     * @return : un objet MarkerStat si trouvé, ou NULL
     */
    private MarkerStat findMarkerStat(String message,
        Collection markerStatsCollection)
    {
        for (Iterator iter = markerStatsCollection.iterator(); iter.hasNext();)
        {
            MarkerStat markerStat = (MarkerStat) iter.next();
            if (markerStat.getIdentifiant().equals(message))
            {
                return markerStat;
            }
        }
        return null;
    }

    /**
     * Regexp to find {0}-like strings.
     */
    private static final Pattern REGEXP_HOLES = Pattern
        .compile("\\{[0-9]+(\\S)*\\}"); //$NON-NLS-1$

    /**
     * Regexp to find suites of ' character.
     */
    private static final Pattern REGEXP_QUOTE = Pattern.compile("'+"); //$NON-NLS-1$

}
