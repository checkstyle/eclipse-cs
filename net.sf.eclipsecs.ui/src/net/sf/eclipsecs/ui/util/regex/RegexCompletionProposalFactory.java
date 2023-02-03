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
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package net.sf.eclipsecs.ui.util.regex;

import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.text.FindReplaceDocumentAdapterContentProposalProvider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * Factory for providing regular expression completion proposals.
 *
 */
public final class RegexCompletionProposalFactory {

  private RegexCompletionProposalFactory() {
    // factory
  }

  /**
   * Create content assistant for regex pattern completion.
   * @param widget text widget to complete
   */
  public static void createForText(Text widget) {
    TextContentAdapter contentAdapter = new TextContentAdapter();
    FindReplaceDocumentAdapterContentProposalProvider proposer =
            new FindReplaceDocumentAdapterContentProposalProvider(true);
    ContentAssistCommandAdapter contentAssist = new ContentAssistCommandAdapter(
        widget,
        contentAdapter,
        proposer,
        ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS,
        new char[0],
        true);
    contentAssist.setEnabled(true);
  }
}
