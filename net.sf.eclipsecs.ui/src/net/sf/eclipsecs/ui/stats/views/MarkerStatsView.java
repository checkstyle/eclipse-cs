//============================================================================
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch, Fabrice Bellingard
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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.ui.stats.Messages;
import net.sf.eclipsecs.ui.stats.data.Stats;
import net.sf.eclipsecs.ui.stats.views.MarkerStatsViewDataProviders.DetailContentProvider;
import net.sf.eclipsecs.ui.stats.views.internal.FiltersAction;

/**
 * View that displays statistics about checkstyle markers.
 *
 * @author Fabrice BELLINGARD
 * @author Lars Ködderitzsch
 */
public class MarkerStatsView extends AbstractStatsView {

  /** The unique view id. */
  public static final String VIEW_ID = MarkerStatsView.class.getName();

  //
  // attributes
  //

  /** The description label. */
  private Label mDescLabel;

  /** The main composite. */
  private MarkerStatsMainView mMainSection;

  private MarkerStatsViewActions actions;

  private DetailContentProvider detailContentProvider;

  /** The state if the view is currently drilled down to details. */
  private boolean mIsDrilledDown;

  //
  // methods
  //

  @Override
  public void createPartControl(Composite parent) {
    super.createPartControl(parent);

    MarkerStatsViewDataProviders providers = new MarkerStatsViewDataProviders(getDialogSettings());
    this.detailContentProvider = providers.detail().contentProvider();

    // set up the main layout
    GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(parent);

    // the label
    mDescLabel = new Label(parent, SWT.NONE);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(mDescLabel);

    // the main section
    mMainSection = new MarkerStatsMainView(parent, SWT.NONE, providers, getSite(),
            this::updateActions, actions);
    GridDataFactory.create(GridData.FILL_BOTH).applyTo(mMainSection);

    updateActions();

    // initialize the view data
    refresh();

    // initFromSettings();
  }

  @Override
  public void setFocus() {
    super.setFocus();
    mMainSection.focusTopControl();
  }

  @Override
  protected void initMenu(IMenuManager menu) {
    menu.add(new FiltersAction(this));
  }

  @Override
  protected void initToolBar(IToolBarManager tbm) {
    tbm.add(actions.mDrillBackAction());
    tbm.add(actions.mDrillDownAction());
    tbm.add(new FiltersAction(this));
  }

  @Override
  protected String getViewId() {
    return VIEW_ID;
  }

  @Override
  protected void handleStatsRebuilt() {
    if (mMainSection != null) {
      mMainSection.setStats(getStats());
      if (mIsDrilledDown && detailContentProvider.getMarkerCount() == 0) {
        drillBack();
      } else {
        // update the actions and the label
        updateActions();
        updateLabel();
      }
    }
  }

  @Override
  protected void makeActions() {
    // action used to display the detail of a specific error type
    Action mDrillDownAction = new Action() {
      @Override
      public void run() {
        mMainSection.getSelectedMarkerCategory().ifPresent(markerStat -> {
          mIsDrilledDown = true;
          detailContentProvider.setCurrentDetailCategory(markerStat.getIdentifiant());
          mMainSection.setDetailAsTopControl();

          updateActions();
          updateLabel();
        });
      }
    };
    mDrillDownAction.setText(Messages.MarkerStatsView_showDetails);
    mDrillDownAction.setToolTipText(Messages.MarkerStatsView_showDetailsTooltip);
    mDrillDownAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
    mDrillDownAction.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD_DISABLED));

    // action used to go back to the master view
    Action mDrillBackAction = new Action() {
      @Override
      public void run() {
        drillBack();
      }

    };
    mDrillBackAction.setText(Messages.MarkerStatsView_actionBack);
    mDrillBackAction.setToolTipText(Messages.MarkerStatsView_actionBackTooltip);
    mDrillBackAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
    mDrillBackAction.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_TOOL_BACK_DISABLED));

    // action used to show a specific error in the editor
    Action mShowErrorAction = new Action() {
      @Override
      public void run() {
        mMainSection.getSelectedMarker().ifPresent(marker -> {
          try {
            IDE.openEditor(getSite().getPage(), marker);
          } catch (PartInitException ex) {
            CheckstyleLog.log(ex, Messages.MarkerStatsView_unableToShowMarker);
          }
        });
      }
    };
    mShowErrorAction.setText(Messages.MarkerStatsView_displayError);
    mShowErrorAction.setToolTipText(Messages.MarkerStatsView_displayErrorTooltip);
    mShowErrorAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(IDE.SharedImages.IMG_OPEN_MARKER));

    this.actions = new MarkerStatsViewActions(mDrillDownAction, mShowErrorAction, mDrillBackAction);
  }

  /**
   * Helper method to manage the state of the view's actions.
   */
  private void updateActions() {
    actions.mDrillBackAction().setEnabled(mIsDrilledDown);
    actions.mDrillDownAction()
            .setEnabled(!mIsDrilledDown && !mMainSection.getSelectedMarkerCategory().isEmpty());
    actions.mShowErrorAction()
            .setEnabled(mIsDrilledDown && !mMainSection.getSelectedMarker().isEmpty());
  }

  private void drillBack() {
    mIsDrilledDown = false;
    detailContentProvider.setCurrentDetailCategory(null);
    mMainSection.setMasterAsTopControl();

    updateActions();
    updateLabel();
  }

  /**
   * Helper method to update the label of the view.
   */
  private void updateLabel() {
    if (!mIsDrilledDown) {

      Stats stats = getStats();
      if (stats != null) {
        String text = NLS.bind(Messages.MarkerStatsView_lblOverviewMessage,
                new Object[] {
                    Integer.valueOf(stats.getMarkerCount()),
                    Integer.valueOf(stats.getMarkerStats().size()),
                    Integer.valueOf(stats.getMarkerCountAll()),
                });
        mDescLabel.setText(text);
      } else {
        mDescLabel.setText("");
      }
    } else {

      String text = NLS.bind(Messages.MarkerStatsView_lblDetailMessage,
              new Object[] {
                  detailContentProvider.getCurrentDetailCategory(),
                  Integer.valueOf(detailContentProvider.getMarkerCount()),
              });
      mDescLabel.setText(text);
    }
  }

  public record MarkerStatsViewActions(IAction mDrillDownAction, IAction mShowErrorAction,
          IAction mDrillBackAction) {

  }

}
