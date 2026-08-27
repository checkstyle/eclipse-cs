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

/**
 * This record holds the data providers for the marker statistics view.
 *
 * @param master
 *            the providers for the master table showing the marker categories
 * @param detail
 *            the providers for the detail table showing the markers of a category
 */
public record MarkerStatsViewDataProviders(MarkerStatsViewMasterDataProviders master,
    MarkerStatsViewDetailDataProviders detail) {

    /** Section tag for master view settings. */
    private static final String TAG_SECTION_MASTER = "masterView";
    /** Section tag for detail view settings. */
    private static final String TAG_SECTION_DETAIL = "detailView";

    /**
     * Constructs a data providers record based on the given dialog settings.
     *
     * @param dialogSettings
     *            the dialog settings used to initialize the master and detail providers
     */
    public MarkerStatsViewDataProviders(IDialogSettings dialogSettings) {
        this(new MarkerStatsViewMasterDataProviders(dialogSettings),
            new MarkerStatsViewDetailDataProviders(dialogSettings));
    }

    /**
     * This record contains the data providers for the master table of the marker statistics view.
     *
     * @param contentProvider
     *            the content provider that supplies the marker category statistics
     * @param dialogSettings
     *            the dialog settings used to persist the master table layout
     */
    public record MarkerStatsViewMasterDataProviders(MasterContentProvider contentProvider,
        IDialogSettings dialogSettings) {

        /**
         * Constructs the master data provider record based on the given dialog settings.
         *
         * @param dialogSettings
         *            the dialog settings used to persist the master table layout
         */
        public MarkerStatsViewMasterDataProviders(IDialogSettings dialogSettings) {
            this(new MasterContentProvider(), dialogSettings);
        }

        /**
         * Returns the dialog settings section used to persist the master table layout, creating it
         * if it does not yet exist.
         *
         * @return the dialog settings section for the master view
         */
        public IDialogSettings getTableSettings() {
            IDialogSettings settings = dialogSettings.getSection(TAG_SECTION_MASTER);
            if (settings == null) {
                settings = dialogSettings.addNewSection(TAG_SECTION_MASTER);
            }
            return settings;
        }
    }

    /**
     * This record contains the data providers for the detail table of the marker statistics view.
     *
     * @param contentProvider
     *            the content provider that supplies the markers of the selected category
     * @param dialogSettings
     *            the dialog settings used to persist the detail table layout
     */
    public record MarkerStatsViewDetailDataProviders(DetailContentProvider contentProvider,
        IDialogSettings dialogSettings) {

        /**
         * Constructs the detail data provider record based on the given dialog settings.
         *
         * @param dialogSettings
         *            the dialog settings used to persist the detail table layout
         */
        public MarkerStatsViewDetailDataProviders(IDialogSettings dialogSettings) {
            this(new DetailContentProvider(), dialogSettings);
        }

        /**
         * Returns the dialog settings section used to persist the detail table layout, creating it
         * if it does not yet exist.
         *
         * @return the dialog settings section for the detail view
         */
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

        /** Creates the master content provider. */
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

        /** Creates the detail content provider. */
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

            Object[] details = mCurrentDetails;
            if (details == null) {
                details = new Object[0];
            }
            return details;
        }

        @Override
        public void dispose() {
            mCurrentDetails = null;
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            mCurrentDetails = null;
        }

        /**
         * Returns the number of markers currently loaded for the selected detail category.
         *
         * @return the number of markers, or zero if none are loaded
         */
        public int getMarkerCount() {
            int count = 0;
            if (mCurrentDetails != null) {
                count = mCurrentDetails.length;
            }
            return count;
        }

        /**
         * Returns the currently selected detail category.
         *
         * @return the current detail category
         */
        public String getCurrentDetailCategory() {
            return currentDetailCategory;
        }

        /**
         * Sets the current detail category, invalidating the cached details so they are reloaded.
         *
         * @param currentDetailCategory
         *            the detail category to select
         */
        public void setCurrentDetailCategory(String currentDetailCategory) {
            this.currentDetailCategory = currentDetailCategory;
            this.mCurrentDetails = null;
        }

    }

}
