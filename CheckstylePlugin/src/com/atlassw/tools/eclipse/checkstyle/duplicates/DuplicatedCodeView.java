//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
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

package com.atlassw.tools.eclipse.checkstyle.duplicates;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;

/**
 * View that shows the report of the strict duplicated code analysis. The
 * duplications are shown per file. They are displayed in a tree and it is
 * possible to open both the source and target files involved in a code
 * duplication.
 */

public class DuplicatedCodeView extends ViewPart
{

    /**
     * Id of the view. Cf. plugin.xml
     */
    public static final String VIEW_ID = CheckstylePlugin.PLUGIN_ID + ".duplicatesView";

    /**
     * Tree viewer that displays the result of the analysis.
     */
    private TreeViewer mViewer;

    /**
     * Report to display. This is a m1ap that contains :
     * <ul>
     * <li>as a key : a IFile</li>
     * <li>as a value : a Collection of DuplicatedCode objects related to that
     * file.</li>
     * </ul>
     */
    private Map mReport;

    /**
     * Adapter used for adding navigation actions.
     */
    private DrillDownAdapter mDrillDownAdapter;

    /**
     * Action that opens a source file.
     */
    private Action mOpenSourceFileAction;

    /**
     * Open that opens a target file.
     */
    private Action mOpenDuplicatedCodeFileAction;

    /**
     * Content provider for the tree viewer.
     */
    class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider
    {
        /**
         * Cf. method below.
         * 
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(
         *      org.eclipse.jface.viewers.Viewer, java.lang.Object,
         *      java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {}

        /**
         * Cf. method below.
         * 
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose()
        {}

        /**
         * Cf. method below.
         * 
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(Object parent)
        {
            if (parent instanceof Map && mReport != null)
            {
                return mReport.keySet().toArray();
            }
            return getChildren(parent);
        }

        /**
         * Cf. method below.
         * 
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
         */
        public Object getParent(Object child)
        {
            if (child instanceof DuplicatedCode)
            {
                return ((DuplicatedCode) child).getSourceFile();
            }
            return null;
        }

        /**
         * Cf. method below.
         * 
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
         */
        public Object[] getChildren(Object parent)
        {
            if (parent instanceof IFile)
            {
                Collection duplicatedCodes = (Collection) mReport.get((IFile) parent);
                return duplicatedCodes.toArray();
            }
            return new Object[0];
        }

        /**
         * Cf. method below.
         * 
         * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
         */
        public boolean hasChildren(Object parent)
        {
            if (parent instanceof IFile)
            {
                Collection duplicatedCodes = (Collection) mReport.get((IFile) parent);
                return !duplicatedCodes.isEmpty();
            }
            return false;
        }
    }

    /**
     * Label provider for the tree viewer.
     */
    class ViewLabelProvider extends LabelProvider
    {
        /**
         * Cf. method below.
         * 
         * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
         */
        public String getText(Object obj)
        {
            if (obj instanceof IFile)
            {
                return ((IFile) obj).getFullPath().toString();
            }
            return obj.toString();
        }

        /**
         * Cf. method below.
         * 
         * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
         */
        public Image getImage(Object obj)
        {
            String imageKey = ISharedImages.IMG_OBJS_ERROR_TSK;
            if (obj instanceof IFile)
            {
                imageKey = ISharedImages.IMG_OBJ_FILE;
            }
            return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
        }
    }

    /**
     * The constructor.
     */
    public DuplicatedCodeView()
    {}

    /**
     * Cf. method below.
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {
        mViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE);
        mDrillDownAdapter = new DrillDownAdapter(mViewer);
        mViewer.setContentProvider(new ViewContentProvider());
        mViewer.setLabelProvider(new ViewLabelProvider());
        mViewer.setInput(null);
        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();
    }

    /**
     * Give the report of the duplicated code analysis as a Map.
     * 
     * @param report : a map that contains IFile as keys and a collection of
     *            DuplicatedCode objects as values
     */
    public void setReport(Map report)
    {
        this.mReport = report;
        Display.getDefault().asyncExec(new Runnable()
        {
            /**
             * Cf. overriden method documentation.
             * 
             * @see java.lang.Runnable#run()
             */
            public void run()
            {
                mViewer.setInput(DuplicatedCodeView.this.mReport);
            }
        });
    }

    /**
     * Creates the context popup menu.
     */
    private void hookContextMenu()
    {
        MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener()
        {
            public void menuAboutToShow(IMenuManager manager)
            {
                DuplicatedCodeView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(mViewer.getControl());
        mViewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, mViewer);
    }

    /**
     * Adds action to the contect menu.
     * 
     * @param manager : the menu manager
     */
    private void fillContextMenu(IMenuManager manager)
    {
        manager.add(mOpenSourceFileAction);
        IStructuredSelection selection = (IStructuredSelection) mViewer.getSelection();
        if (selection.getFirstElement() instanceof DuplicatedCode)
        {
            manager.add(mOpenDuplicatedCodeFileAction);
        }
        manager.add(new Separator());
        mDrillDownAdapter.addNavigationActions(manager);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    /**
     * Fills the tool bar with the navigation actions.
     */
    private void contributeToActionBars()
    {
        IActionBars bars = getViewSite().getActionBars();
        mDrillDownAdapter.addNavigationActions(bars.getToolBarManager());
    }

    /**
     * Creates the actions of this view.
     */
    private void makeActions()
    {
        createOpenSourceFileAction();
        createOpenDuplicatedCodeFileAction();
    }

    /**
     * Creates the action that opens a source file.
     */
    private void createOpenSourceFileAction()
    {
        mOpenSourceFileAction = new Action()
        {
            public void run()
            {
                try
                {
                    IStructuredSelection selection = (IStructuredSelection) mViewer.getSelection();
                    if (selection.getFirstElement() instanceof IFile)
                    {
                        IDE.openEditor(getSite().getPage(), (IFile) selection.getFirstElement());
                    }
                    else if (selection.getFirstElement() instanceof DuplicatedCode)
                    {
                        DuplicatedCode duplicatedCode = (DuplicatedCode) selection
                                .getFirstElement();
                        IEditorPart editorPart = IDE.openEditor(getSite().getPage(), duplicatedCode
                                .getSourceFile());
                        if (editorPart instanceof ITextEditor)
                        {
                            // instanceof just to be sure, but the JavaEditor is
                            // an ITextEditor
                            selectAndRevealDuplicatedLines(((ITextEditor) editorPart),
                                    duplicatedCode.getSourceFileFirstLineNumber(), duplicatedCode
                                            .getSourceFileFirstLineNumber()
                                            + duplicatedCode.getNumberOfDuplicatedLines());
                        }
                    }
                }
                catch (PartInitException e)
                {
                    CheckstyleLog.errorDialog(mViewer.getControl().getShell(),
                            "Error while opening the file editor.", e, true);
                }
            }
        };
        mOpenSourceFileAction.setText("Open source file");
        mOpenSourceFileAction
                .setToolTipText("Opens the file where duplications have been detected.");
        mOpenSourceFileAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
    }

    /**
     * Creates the action that opens a target file.
     */
    private void createOpenDuplicatedCodeFileAction()
    {
        mOpenDuplicatedCodeFileAction = new Action()
        {
            public void run()
            {
                try
                {
                    IStructuredSelection selection = (IStructuredSelection) mViewer.getSelection();
                    if (selection.getFirstElement() instanceof DuplicatedCode)
                    {
                        DuplicatedCode duplicatedCode = (DuplicatedCode) selection
                                .getFirstElement();
                        IFile destinationFile = duplicatedCode.getTargetFile();
                        if (destinationFile == null)
                        {
                            // nothing to do
                            return;
                        }
                        IEditorPart editorPart = IDE.openEditor(getSite().getPage(),
                                destinationFile);
                        if (editorPart instanceof ITextEditor)
                        {
                            // instanceof just to be sure, but the JavaEditor is
                            // an ITextEditor
                            selectAndRevealDuplicatedLines(((ITextEditor) editorPart),
                                    duplicatedCode.getTargetFileFirstLineNumber(), duplicatedCode
                                            .getTargetFileFirstLineNumber()
                                            + duplicatedCode.getNumberOfDuplicatedLines());
                        }
                    }
                }
                catch (PartInitException e)
                {
                    CheckstyleLog.errorDialog(mViewer.getControl().getShell(),
                            "Error while opening the file editor.", e, true);
                }
            }
        };
        mOpenDuplicatedCodeFileAction.setText("Open target file");
        mOpenDuplicatedCodeFileAction
                .setToolTipText("Opens the file where duplicate code has been found.");
        mOpenDuplicatedCodeFileAction.setImageDescriptor(PlatformUI.getWorkbench()
                .getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
    }

    /**
     * Opens the editor and selects the area defined by the two lines.
     * 
     * @param editor the editor to open
     * @param firstLine the line to jump to and select from
     * @param lastLine the last line to select
     */
    private void selectAndRevealDuplicatedLines(ITextEditor editor, int firstLine, int lastLine)
    {
        IDocumentProvider provider = editor.getDocumentProvider();
        IDocument document = provider.getDocument(editor.getEditorInput());
        try
        {
            int start = document.getLineOffset(firstLine);
            int end = document.getLineOffset(lastLine - 1);
            editor.selectAndReveal(start, end - start);
        }
        catch (BadLocationException e)
        {

            CheckstyleLog.errorDialog(mViewer.getControl().getShell(),
                    "Error while trying to reveal and display the duplicated lines.", e, true);
        }
    }

    /**
     * Adds the double click capability.
     */
    private void hookDoubleClickAction()
    {
        mViewer.addDoubleClickListener(new IDoubleClickListener()
        {
            public void doubleClick(DoubleClickEvent event)
            {
                IStructuredSelection selection = (IStructuredSelection) mViewer.getSelection();
                mOpenSourceFileAction.run();
                if (selection.getFirstElement() instanceof DuplicatedCode)
                {
                    mOpenDuplicatedCodeFileAction.run();
                }

            }
        });
    }

    /**
     * Cf. method below.
     * 
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus()
    {
        mViewer.getControl().setFocus();
    }
}