//============================================================================
//
// Copyright (C) 2003-2023 the original author or authors.
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
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//
//============================================================================

package net.sf.eclipsecs.ui.stats.views;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.MarkerUtilities;

import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.ui.stats.Messages;
import net.sf.eclipsecs.ui.stats.data.MarkerStat;
import net.sf.eclipsecs.ui.stats.data.Stats;
import net.sf.eclipsecs.ui.util.table.ITableComparableProvider;
import net.sf.eclipsecs.ui.util.table.ITableSettingsProvider;

public record MarkerStatsViewDataProviders(MarkerStatsViewMasterDataProviders master,
        MarkerStatsViewDetailDataProviders detail) {

  private static final String TAG_SECTION_MASTER = "masterView";
  private static final String TAG_SECTION_DETAIL = "detailView";

  public MarkerStatsViewDataProviders(IDialogSettings dialogSettings) {
    this(new MarkerStatsViewMasterDataProviders(dialogSettings),
            new MarkerStatsViewDetailDataProviders(dialogSettings));
  }

  public record MarkerStatsViewMasterDataProviders(MasterContentProvider contentProvider,
          MasterViewMultiProvider multiProvider) {

    public MarkerStatsViewMasterDataProviders(IDialogSettings dialogSettings) {
      this(new MasterContentProvider(), new MasterViewMultiProvider(dialogSettings));
    }
  }

  public record MarkerStatsViewDetailDataProviders(DetailContentProvider contentProvider,
          DetailViewMultiProvider multiProvider) {

    public MarkerStatsViewDetailDataProviders(IDialogSettings dialogSettings) {
      this(new DetailContentProvider(), new DetailViewMultiProvider(dialogSettings));
    }
  }

  /**
   * Content provider for the master table viewer.
   *
   * @author Lars Ködderitzsch
   */
  public static final class MasterContentProvider implements IStructuredContentProvider {
    private Object[] mCurrentMarkerStats;

    private MasterContentProvider() {

    }

    @Override
    public Object[] getElements(Object inputElement) {
      if (mCurrentMarkerStats == null) {
        // find the marker statistics for the current category
        Stats currentStats = (Stats) inputElement;
        mCurrentMarkerStats = currentStats.getMarkerStats().toArray();
      }

      return mCurrentMarkerStats;
    }

    @Override
    public void dispose() {
      mCurrentMarkerStats = null;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      mCurrentMarkerStats = null;
    }
  }

  /**
   * Content provider for the detail table viewer.
   *
   * @author Lars Ködderitzsch
   */
  public static final class DetailContentProvider implements IStructuredContentProvider {

    private Object[] mCurrentDetails;
    private String currentDetailCategory;

    private DetailContentProvider() {

    }

    @Override
    public Object[] getElements(Object inputElement) {
      if (mCurrentDetails == null) {
        // find the marker statistics for the current category
        Stats currentStats = (Stats) inputElement;
        Collection<MarkerStat> markerStats = currentStats.getMarkerStats();
        Iterator<MarkerStat> iter = markerStats.iterator();
        while (iter.hasNext()) {
          MarkerStat markerStat = iter.next();
          if (markerStat.getIdentifiant().equals(currentDetailCategory)) {
            mCurrentDetails = markerStat.getMarkers().toArray();
            break;
          }
        }
      }

      return mCurrentDetails != null ? mCurrentDetails : new Object[0];
    }

    @Override
    public void dispose() {
      mCurrentDetails = null;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      mCurrentDetails = null;
    }

    public int getMarkerCount() {
      return mCurrentDetails != null ? mCurrentDetails.length : 0;
    }

    public String getCurrentDetailCategory() {
      return currentDetailCategory;
    }

    public void setCurrentDetailCategory(String currentDetailCategory) {
      this.currentDetailCategory = currentDetailCategory;
      this.mCurrentDetails = null;
    }

  }

  /**
   * Label provider for the master table viewer.
   *
   * @author Lars Ködderitzsch
   */
  public static final class MasterViewMultiProvider extends LabelProvider
          implements ITableLabelProvider, ITableComparableProvider, ITableSettingsProvider {

    private final IDialogSettings mainSettings;

    private MasterViewMultiProvider(IDialogSettings mainSettings) {
      this.mainSettings = mainSettings;
    }

    @Override
    public String getColumnText(Object obj, int index) {
      MarkerStat stat = (MarkerStat) obj;
      return switch (index) {
        case 1 -> stat.getIdentifiant();
        case 2 -> Integer.toString(stat.getCount());
        default -> "";
      };
    }

    @Override
    public Image getColumnImage(Object obj, int index) {
      Image image = null;
      MarkerStat stat = (MarkerStat) obj;

      if (index == 0) {
        int severity = stat.getMaxSeverity();
        ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();

        if (IMarker.SEVERITY_ERROR == severity) {
          image = sharedImages.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
        } else if (IMarker.SEVERITY_WARNING == severity) {
          image = sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
        } else if (IMarker.SEVERITY_INFO == severity) {
          image = sharedImages.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
        }
      }
      return image;
    }

    @Override
    public Comparable<?> getComparableValue(Object element, int colIndex) {
      MarkerStat stat = (MarkerStat) element;
      return switch (colIndex) {
        case 0 -> Integer.valueOf(stat.getMaxSeverity() * -1);
        case 1 -> stat.getIdentifiant();
        case 2 -> Integer.valueOf(stat.getCount());
        default -> "";
      };
    }

    @Override
    public IDialogSettings getTableSettings() {
      IDialogSettings settings = mainSettings.getSection(TAG_SECTION_MASTER);
      if (settings == null) {
        settings = mainSettings.addNewSection(TAG_SECTION_MASTER);
      }
      return settings;
    }
  }

  /**
   * Label provider for the detail table viewer.
   *
   * @author Lars Ködderitzsch
   */
  public static final class DetailViewMultiProvider extends LabelProvider
          implements ITableLabelProvider, ITableComparableProvider, ITableSettingsProvider {

    private final IDialogSettings mainSettings;

    private DetailViewMultiProvider(IDialogSettings mainSettings) {
      this.mainSettings = mainSettings;
    }

    @Override
    public String getColumnText(Object obj, int index) {
      IMarker marker = (IMarker) obj;
      try {
        return switch (index) {
          case 1 -> marker.getResource().getName();
          case 2 -> marker.getResource().getParent().getFullPath().toString();
          case 3 -> Objects.toString(marker.getAttribute(IMarker.LINE_NUMBER), "");
          case 4 -> Objects.toString(marker.getAttribute(IMarker.MESSAGE), "");
          default -> "";
        };
      } catch (Exception ex) {
        // Can't do anything: let's put a default value
        CheckstyleLog.log(ex);
        return Messages.MarkerStatsView_unknownProblem;
      }
    }

    @Override
    public Image getColumnImage(Object obj, int index) {
      Image image = null;
      IMarker marker = (IMarker) obj;

      if (index == 0) {
        int severity = MarkerUtilities.getSeverity(marker);
        ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();

        if (IMarker.SEVERITY_ERROR == severity) {
          image = sharedImages.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
        } else if (IMarker.SEVERITY_WARNING == severity) {
          image = sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
        } else if (IMarker.SEVERITY_INFO == severity) {
          image = sharedImages.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
        }
      }
      return image;
    }

    @Override
    public Comparable<?> getComparableValue(Object element, int colIndex) {
      IMarker marker = (IMarker) element;
      return switch (colIndex) {
        case 0 -> Integer.valueOf(marker.getAttribute(IMarker.SEVERITY, Integer.MAX_VALUE) * -1);
        case 1 -> marker.getResource().getName();
        case 2 -> marker.getResource().getParent().getFullPath().toString();
        case 3 -> Integer.valueOf(marker.getAttribute(IMarker.LINE_NUMBER, Integer.MAX_VALUE));
        case 4 -> marker.getAttribute(IMarker.MESSAGE, "");
        default -> "";
      };
    }

    @Override
    public IDialogSettings getTableSettings() {
      IDialogSettings settings = mainSettings.getSection(TAG_SECTION_DETAIL);

      if (settings == null) {
        settings = mainSettings.addNewSection(TAG_SECTION_DETAIL);
      }

      return settings;
    }
  }

}
