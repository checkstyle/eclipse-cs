//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.voting;

import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;

/**
 * Page to cast votes for the plugin.
 * 
 * @author Lars Koedderitzsch
 */
public class VotingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    // =================================================
    // Public static final variables.
    // =================================================

    // =================================================
    // Static class variables.
    // =================================================

    private static final Integer[] RATINGS = new Integer[] { new Integer(10), new Integer(9),
        new Integer(8), new Integer(7), new Integer(6), new Integer(5), new Integer(4),
        new Integer(3), new Integer(2), new Integer(1) };

    // =================================================
    // Instance member variables.
    // =================================================

    private ComboViewer mComboRating;

    private Text mTxtComment;

    private Button mBtnVote;

    private PageController mPageController = new PageController();

    // =================================================
    // Constructors & finalizer.
    // =================================================

    /**
     * Constructor.
     */
    public VotingPreferencePage()
    {
        super();
    }

    // =================================================
    // Methods.
    // =================================================

    /**
     * {@inheritDoc}
     */
    public void init(IWorkbench workbench)
    {
    // NOOP
    }

    /**
     * {@inheritDoc}
     */
    public Control createContents(Composite ancestor)
    {

        noDefaultAndApplyButton();

        //
        // Build the top level composite with one colume.
        //
        Composite parentComposite = new Composite(ancestor, SWT.NULL);
        GridLayout layout = new GridLayout(2, false);
        parentComposite.setLayout(layout);

        Label lblVotingDesc = new Label(parentComposite, SWT.WRAP);
        lblVotingDesc
                .setText(Messages.VotingPreferencePage_lblDescription);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        lblVotingDesc.setLayoutData(gd);

        Label lblRating = new Label(parentComposite, SWT.NULL);
        lblRating.setText(Messages.VotingPreferencePage_lblRating);
        gd = new GridData();
        lblRating.setLayoutData(gd);

        mComboRating = new ComboViewer(parentComposite);
        mComboRating.setContentProvider(new ArrayContentProvider());
        mComboRating.setInput(RATINGS);
        mComboRating.setSelection(new StructuredSelection(RATINGS[0]));
        mComboRating.setLabelProvider(new RatingLabelProvider());
        gd = new GridData();
        mComboRating.getCombo().setLayoutData(gd);

        Label lblComment = new Label(parentComposite, SWT.NULL);
        lblComment.setText(Messages.VotingPreferencePage_lblComment);
        gd = new GridData();
        gd.horizontalSpan = 2;
        lblComment.setLayoutData(gd);

        mTxtComment = new Text(parentComposite, SWT.MULTI | SWT.WRAP | SWT.BORDER);
        gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        mTxtComment.setLayoutData(gd);

        mBtnVote = new Button(parentComposite, SWT.PUSH);
        mBtnVote.setText(Messages.VotingPreferencePage_btnVote);
        gd = new GridData();
        gd.widthHint = 80;
        gd.horizontalAlignment = GridData.END;
        gd.horizontalSpan = 2;
        mBtnVote.setLayoutData(gd);
        mBtnVote.addSelectionListener(mPageController);

        return parentComposite;
    }

    /**
     * The controller.
     * 
     * @author Lars Ködderitzsch
     */
    private class PageController implements SelectionListener
    {

        public void widgetSelected(SelectionEvent e)
        {
            if (e.widget == mBtnVote)
            {
                IStructuredSelection sel = (IStructuredSelection) mComboRating.getSelection();

                Vote vote = new Vote(((Integer) sel.getFirstElement()).intValue(), mTxtComment
                        .getText());

                try
                {
                    vote.cast();
                    MessageDialog
                            .openInformation(getShell(), Messages.VotingPreferencePage_titleVoteRegistered,
                                    Messages.VotingPreferencePage_msgVoteRegistered);
                }
                catch (IOException e1)
                {
                    CheckstyleLog.errorDialog(getShell(), e1, false);
                }
            }
        }

        public void widgetDefaultSelected(SelectionEvent e)
        {
        // NOOP
        }
    }

    /**
     * Label provider to illustrate the ratings a bit more.
     * 
     * @author Lars Koedderitzsch
     */
    private class RatingLabelProvider extends LabelProvider
    {
        public String getText(Object element)
        {
            String text = null;
            if (element.equals(RATINGS[0]))
            {
                text = Messages.VotingPreferencePage_ratingBest;
            }
            else if (element.equals(RATINGS[RATINGS.length - 1]))
            {
                text = Messages.VotingPreferencePage_ratingWorst;
            }
            else
            {
                text = super.getText(element);
            }
            return text;
        }
    }
}