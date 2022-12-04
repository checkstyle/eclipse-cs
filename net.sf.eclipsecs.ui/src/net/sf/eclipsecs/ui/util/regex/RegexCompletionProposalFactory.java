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
