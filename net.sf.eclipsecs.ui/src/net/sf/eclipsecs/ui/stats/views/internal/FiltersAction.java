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

package net.sf.eclipsecs.ui.stats.views.internal;

import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.stats.Messages;
import net.sf.eclipsecs.ui.stats.views.AbstractStatsView;

import org.eclipse.jface.action.Action;

/**
 * Action implementation for the filters action.
 * 
 * @author Lars Ködderitzsch
 */
public class FiltersAction extends Action {

  //
  // attributes
  //

  /** the view that uses this action. */
  private AbstractStatsView mStatsView;

  //
  // constructor
  //

  /**
   * Creates the action.
   * 
   * @param view
   *          the stats view
   */
  public FiltersAction(AbstractStatsView view) {
    super(Messages.FiltersAction_text);
    setImageDescriptor(CheckstyleUIPluginImages.FILTER_ICON);
    setToolTipText(Messages.FiltersAction_tooltip);
    this.mStatsView = view;
    setEnabled(true);
  }

  //
  // methods
  //

  /**
   * Opens the dialog. Notifies the view if the filter has been modified.
   */
  @Override
  public void run() {
    mStatsView.openFiltersDialog();
  }
}
