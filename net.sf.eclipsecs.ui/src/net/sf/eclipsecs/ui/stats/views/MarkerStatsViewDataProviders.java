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

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import net.sf.eclipsecs.ui.stats.data.MarkerStat;
import net.sf.eclipsecs.ui.stats.data.Stats;

public record MarkerStatsViewDataProviders(MarkerStatsViewMasterDataProviders master,
    MarkerStatsViewDetailDataProviders detail) {

    /** Section tag for master view settings. */
    private static final String TAG_SECTION_MASTER = "masterView";
    /** Section tag for detail view settings. */
    private static final String TAG_SECTION_DETAIL = "detailView";

    public MarkerStatsViewDataProviders(IDialogSettings dialogSettings) {
        this(new MarkerStatsViewMasterDataProviders(dialogSettings),
            new MarkerStatsViewDetailDataProviders(dialogSettings));
    }

    public record MarkerStatsViewMasterDataProviders(MasterContentProvider contentProvider,
        IDialogSettings dialogSettings) {

        public MarkerStatsViewMasterDataProviders(IDialogSettings dialogSettings) {
            this(new MasterContentProvider(), dialogSettings);
        }

        public IDialogSettings getTableSettings() {
            IDialogSettings settings = dialogSettings.getSection(TAG_SECTION_MASTER);
            if (settings == null) {
                settings = dialogSettings.addNewSection(TAG_SECTION_MASTER);
            }
            return settings;
        }
    }

    public record MarkerStatsViewDetailDataProviders(DetailContentProvider contentProvider,
        IDialogSettings dialogSettings) {

        public MarkerStatsViewDetailDataProviders(IDialogSettings dialogSettings) {
            this(new DetailContentProvider(), dialogSettings);
        }

        public IDialogSettings getTableSettings() {
            IDialogSettings settings = dialogSettings.getSection(TAG_SECTION_DETAIL);
            if (settings == null) {
                settings = dialogSettings.addNewSection(TAG_SECTION_DETAIL);
            }
            return settings;
        }
    }

    /**
     * Content provider for the master table viewer.
     *
     * @author Lars Ködderitzsch
     */
    public static final class MasterContentProvider implements IStructuredContentProvider {
        /** The current marker stats. */
        private Object[] mCurrentMarkerStats;

        private MasterContentProvider() {

        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (mCurrentMarkerStats == null) {
                // find the marker statistics for the current category
                final Stats currentStats = (Stats) inputElement;
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

        /** The current detail markers. */
        private Object[] mCurrentDetails;
        /** The current detail category. */
        private String currentDetailCategory;

        private DetailContentProvider() {

        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (mCurrentDetails == null) {
                // find the marker statistics for the current category
                final Stats currentStats = (Stats) inputElement;
                final Collection<MarkerStat> markerStats = currentStats.getMarkerStats();
                final Iterator<MarkerStat> iter = markerStats.iterator();
                while (iter.hasNext()) {
                    final MarkerStat markerStat = iter.next();
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

}
