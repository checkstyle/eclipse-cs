
package com.atlassw.tools.eclipse.checkstyle.config.gui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor;
import org.eclipse.jface.contentassist.SubjectControlContextInformationValidator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class PropertiesContentAssistProcessor implements IContentAssistProcessor,
        ISubjectControlContentAssistProcessor
{

    /** The context information validator. */
    private IContextInformationValidator mValidator = new SubjectControlContextInformationValidator(
            this);

    /**
     * {@inheritDoc}
     */
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset)
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset)
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public char[] getCompletionProposalAutoActivationCharacters()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public char[] getContextInformationAutoActivationCharacters()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getErrorMessage()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IContextInformationValidator getContextInformationValidator()
    {
        return mValidator;
    }

    /**
     * {@inheritDoc}
     */
    public ICompletionProposal[] computeCompletionProposals(
            IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset)
    {
        List proposals = new ArrayList();

        String basedir = "${basedir}"; //$NON-NLS-1$
        String projectLoc = "${project_loc}"; //$NON-NLS-1$
        String workspaceLoc = "${workspace_loc}"; //$NON-NLS-1$
        String configLoc = "${config_loc}"; //$NON-NLS-1$
        String samedir = "${samedir}"; //$NON-NLS-1$

        //TODO translate the descriptions
        
        proposals.add(new CompletionProposal(basedir, documentOffset, 0, basedir.length(), null,
                basedir, null, "Maps to the current project directory."));
        proposals.add(new CompletionProposal(projectLoc, documentOffset, 0, projectLoc.length(),
                null, projectLoc, null, "Same as ${basedir}."));
        proposals.add(new CompletionProposal(workspaceLoc, documentOffset, 0,
                workspaceLoc.length(), null, workspaceLoc, null,
                "Maps to the current Eclipse workspace directorys."));
        proposals.add(new CompletionProposal(configLoc, documentOffset, 0, configLoc.length(),
                null, configLoc, null, "Maps to the directory the configuration file lies in."));
        proposals.add(new CompletionProposal(samedir, documentOffset, 0, samedir.length(), null,
                samedir, null, "Same as ${config_loc}."));

        return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IContextInformation[] computeContextInformation(
            IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset)
    {
        return null;
    }

}
