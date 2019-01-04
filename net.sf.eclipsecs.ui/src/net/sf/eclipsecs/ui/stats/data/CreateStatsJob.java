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

package net.sf.eclipsecs.ui.stats.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import net.sf.eclipsecs.core.builder.CheckstyleMarker;
import net.sf.eclipsecs.core.config.meta.MetadataFactory;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.stats.Messages;
import net.sf.eclipsecs.ui.stats.views.internal.CheckstyleMarkerFilter;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Job implementation that builds the data objects for the statistic views.
 *
 * @author Lars Ködderitzsch
 */
public class CreateStatsJob extends Job {

  /** Regexp to find {0}-like strings. */
  private static final Pattern REGEXP_HOLES = Pattern.compile("\\{[0-9]+(\\S)*\\}"); //$NON-NLS-1$

  /** Regexp to find suites of ' character. */
  private static final Pattern REGEXP_QUOTE = Pattern.compile("'+"); //$NON-NLS-1$

  /** The filter to analyze. */
  private final CheckstyleMarkerFilter mFilter;

  /** The statistics data object. */
  private Stats mStats;

  /** The job family this job belongs to. */
  private final String mFamily;

  /**
   * Creates the job.
   *
   * @param filter
   *          the marker filter to analyze
   * @param family
   *          the job family
   */
  public CreateStatsJob(CheckstyleMarkerFilter filter, String family) {
    super(Messages.CreateStatsJob_msgAnalyzeMarkers);
    mFilter = (CheckstyleMarkerFilter) filter.clone();
    mFamily = family;
  }

  @Override
  public boolean shouldSchedule() {

    Job[] similarJobs = getJobManager().find(mFamily);
    return similarJobs.length == 0;
  }

  @Override
  public boolean belongsTo(Object family) {
    return Objects.equals(mFamily, family);
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    try {

      int wholeAmountOfMarkers = ResourcesPlugin.getWorkspace().getRoot()
              .findMarkers(CheckstyleMarker.MARKER_ID, true, IResource.DEPTH_INFINITE).length;

      IMarker[] markers = mFilter.findMarkers(monitor);

      Map<String, MarkerStat> markerStats = new HashMap<>();

      for (int i = 0, size = markers.length; i < size; i++) {

        String message = null;
        try {
          message = getUnlocalizedMessage(markers[i]);
          message = cleanMessage(message);
        } catch (CoreException e) {
          CheckstyleLog.log(e, Messages.CreateStatsJob_errorAnalyzingMarkers);
        }

        // check that the message is not empty
        if (message == null || message.trim().length() == 0) {
          // cela ne devrait pas arriver, mais bon, on laisse faire
          CheckstyleLog.log(null, Messages.CreateStatsJob_markerMessageShouldntBeEmpty);
          continue;
        }

        // puis on recherche
        MarkerStat stat = markerStats.get(message);
        if (stat == null) {
          // 1ere fois qu'on rencontre un marqueur de ce type
          MarkerStat newMarkerStat = new MarkerStat(message);
          newMarkerStat.addMarker(markers[i]);
          markerStats.put(newMarkerStat.getIdentifiant(), newMarkerStat);
        } else {
          // on augmente juste le nombre d'occurence
          stat.addMarker(markers[i]);
        }
      }

      mStats = new Stats(markerStats.values(), markers.length, wholeAmountOfMarkers);
    } catch (CoreException e) {
      return new Status(IStatus.ERROR, CheckstyleUIPlugin.PLUGIN_ID, IStatus.OK,
              Messages.CreateStatsJob_errorAnalyzingMarkers, e);
    }

    return Status.OK_STATUS;
  }

  /**
   * Returns the statistics data compiled by the job.
   *
   * @return the statistics data
   */
  public Stats getStats() {
    return mStats;
  }

  /**
   * Returns the standard, untranslated message for a Checkstyle violation
   * marker.
   *
   * @param marker
   *          the marker
   * @return the untranslated message
   * @throws CoreException
   *           error accessing marker attributes
   */
  public static String getUnlocalizedMessage(IMarker marker) throws CoreException {
    String key = (String) marker.getAttribute(CheckstyleMarker.MESSAGE_KEY);
    String moduleInternalName = (String) marker.getAttribute(CheckstyleMarker.MODULE_NAME);

    String standardMessage = MetadataFactory.getStandardMessage(key, moduleInternalName);

    if (standardMessage == null) {
      standardMessage = (String) marker.getAttribute(IMarker.MESSAGE);
    }
    return standardMessage;
  }

  /**
   * Cleans the unlocalized message so that it is more readable.
   *
   * @param message
   *          : the message to clean
   * @return the cleaned message
   */
  public static String cleanMessage(String message) {
    // replacements
    String finalMessage = REGEXP_HOLES.matcher(message).replaceAll("X"); //$NON-NLS-1$
    finalMessage = REGEXP_QUOTE.matcher(finalMessage).replaceAll("'"); //$NON-NLS-1$

    return finalMessage;
  }
}
