//============================================================================
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.ui.config;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor;
import org.eclipse.jface.contentassist.SubjectControlContextInformationValidator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import net.sf.eclipsecs.ui.Messages;

/**
 * Provides content assist for builtin properties.
 *
 */
public class PropertiesContentAssistProcessor
        implements ISubjectControlContentAssistProcessor {

  /** The context information validator. */
  private IContextInformationValidator mValidator = new SubjectControlContextInformationValidator(
          this);

  @Override
  public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
    return null;
  }

  @Override
  public IContextInformation[] computeContextInformation(
          IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
    return null;
  }

  @Override
  public char[] getCompletionProposalAutoActivationCharacters() {
    return null;
  }

  @Override
  public char[] getContextInformationAutoActivationCharacters() {
    return null;
  }

  @Override
  public String getErrorMessage() {
    return null;
  }

  @Override
  public IContextInformationValidator getContextInformationValidator() {
    return mValidator;
  }

  @Override
  public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
    return null;
  }

  @Override
  public ICompletionProposal[] computeCompletionProposals(
          IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
    List<CompletionProposal> proposals = new ArrayList<>();

    String basedir = "${basedir}"; //$NON-NLS-1$
    String projectLoc = "${project_loc}"; //$NON-NLS-1$
    String workspaceLoc = "${workspace_loc}"; //$NON-NLS-1$
    String configLoc = "${config_loc}"; //$NON-NLS-1$
    String samedir = "${samedir}"; //$NON-NLS-1$

    // TODO translate the descriptions

    proposals.add(new CompletionProposal(basedir, documentOffset, 0, basedir.length(), null,
            basedir, null, Messages.PropertiesContentAssistProcessor_basedir));
    proposals.add(new CompletionProposal(projectLoc, documentOffset, 0, projectLoc.length(), null,
            projectLoc, null, Messages.PropertiesContentAssistProcessor_projectLoc));
    proposals.add(new CompletionProposal(workspaceLoc, documentOffset, 0, workspaceLoc.length(),
            null, workspaceLoc, null, Messages.PropertiesContentAssistProcessor_workspaceLoc));
    proposals.add(new CompletionProposal(configLoc, documentOffset, 0, configLoc.length(), null,
            configLoc, null, Messages.PropertiesContentAssistProcessor_configLoc));
    proposals.add(new CompletionProposal(samedir, documentOffset, 0, samedir.length(), null,
            samedir, null, Messages.PropertiesContentAssistProcessor_samedir));

    return proposals.toArray(new ICompletionProposal[proposals.size()]);
  }

}
