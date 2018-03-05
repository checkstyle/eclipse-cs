/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
//CHECKSTYLE:OFF

package net.sf.eclipsecs.ui.util.regex;

import java.util.ArrayList;

import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor;
import org.eclipse.jface.contentassist.SubjectControlContextInformationValidator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * Content assist processor for regular expressions.
 *
 * @since 3.0
 */
public final class RegExContentAssistProcessor
        implements IContentAssistProcessor, ISubjectControlContentAssistProcessor {

  /**
   * Proposal computer.
   */
  private static class ProposalComputer {

    /**
     * The whole regular expression.
     */
    private final String fExpression;

    /**
     * The document offset.
     */
    private final int fDocumentOffset;

    /**
     * The high-priority proposals.
     */
    private final ArrayList<CompletionProposal> fPriorityProposals;

    /**
     * The low-priority proposals.
     */
    private final ArrayList<CompletionProposal> fProposals;

    /**
     * <code>true</code> iff <code>fExpression</code> ends with an open escape.
     */
    private final boolean fIsEscape;

    /**
     * Creates a new Proposal Computer.
     *
     * @param contentAssistSubjectControl
     *          the subject control
     * @param documentOffset
     *          the offset
     */
    public ProposalComputer(IContentAssistSubjectControl contentAssistSubjectControl,
            int documentOffset) {
      fExpression = contentAssistSubjectControl.getDocument().get();
      fDocumentOffset = documentOffset;
      fPriorityProposals = new ArrayList<>();
      fProposals = new ArrayList<>();

      boolean isEscape = false;
      esc: for (int i = documentOffset - 1; i >= 0; i--) {
        if (fExpression.charAt(i) == '\\') {
          isEscape = !isEscape;
        } else {
          break esc;
        }
      }
      fIsEscape = isEscape;
    }

    /**
     * Computes applicable proposals for the find field.
     *
     * @return the proposals
     */
    public ICompletionProposal[] computeFindProposals() {
      // characters
      addBsProposal("\\\\", RegExMessages.displayString_bs_bs, RegExMessages.additionalInfo_bs_bs); //$NON-NLS-1$
      addBracketProposal("\\0", 2, RegExMessages.displayString_bs_0, //$NON-NLS-1$
              RegExMessages.additionalInfo_bs_0);
      addBracketProposal("\\x", 2, RegExMessages.displayString_bs_x, //$NON-NLS-1$
              RegExMessages.additionalInfo_bs_x);
      addBracketProposal("\\u", 2, RegExMessages.displayString_bs_u, //$NON-NLS-1$
              RegExMessages.additionalInfo_bs_u);
      addBsProposal("\\t", RegExMessages.displayString_bs_t, RegExMessages.additionalInfo_bs_t); //$NON-NLS-1$
      addBsProposal("\\n", RegExMessages.displayString_bs_n, RegExMessages.additionalInfo_bs_n); //$NON-NLS-1$
      addBsProposal("\\r", RegExMessages.displayString_bs_r, RegExMessages.additionalInfo_bs_r); //$NON-NLS-1$
      addBsProposal("\\f", RegExMessages.displayString_bs_f, RegExMessages.additionalInfo_bs_f); //$NON-NLS-1$
      addBsProposal("\\a", RegExMessages.displayString_bs_a, RegExMessages.additionalInfo_bs_a); //$NON-NLS-1$
      addBsProposal("\\e", RegExMessages.displayString_bs_e, RegExMessages.additionalInfo_bs_e); //$NON-NLS-1$
      addBsProposal("\\c", RegExMessages.displayString_bs_c, RegExMessages.additionalInfo_bs_c); //$NON-NLS-1$

      if (!fIsEscape) {
        addBracketProposal(".", 1, RegExMessages.displayString_dot, //$NON-NLS-1$
                RegExMessages.additionalInfo_dot);
      }
      addBsProposal("\\d", RegExMessages.displayString_bs_d, RegExMessages.additionalInfo_bs_d); //$NON-NLS-1$
      addBsProposal("\\D", RegExMessages.displayString_bs_D, RegExMessages.additionalInfo_bs_D); //$NON-NLS-1$
      addBsProposal("\\s", RegExMessages.displayString_bs_s, RegExMessages.additionalInfo_bs_s); //$NON-NLS-1$
      addBsProposal("\\S", RegExMessages.displayString_bs_S, RegExMessages.additionalInfo_bs_S); //$NON-NLS-1$
      addBsProposal("\\w", RegExMessages.displayString_bs_w, RegExMessages.additionalInfo_bs_w); //$NON-NLS-1$
      addBsProposal("\\W", RegExMessages.displayString_bs_W, RegExMessages.additionalInfo_bs_W); //$NON-NLS-1$

      // backreference
      addBsProposal("\\", RegExMessages.displayString_bs_i, RegExMessages.additionalInfo_bs_i); //$NON-NLS-1$

      // quoting
      addBsProposal("\\", RegExMessages.displayString_bs, RegExMessages.additionalInfo_bs); //$NON-NLS-1$
      addBsProposal("\\Q", RegExMessages.displayString_bs_Q, RegExMessages.additionalInfo_bs_Q); //$NON-NLS-1$
      addBsProposal("\\E", RegExMessages.displayString_bs_E, RegExMessages.additionalInfo_bs_E); //$NON-NLS-1$

      // character sets
      if (!fIsEscape) {
        addBracketProposal("[]", 1, RegExMessages.displayString_set, //$NON-NLS-1$
                RegExMessages.additionalInfo_set);
        addBracketProposal("[^]", 2, RegExMessages.displayString_setExcl, //$NON-NLS-1$
                RegExMessages.additionalInfo_setExcl);
        addBracketProposal("[-]", 1, RegExMessages.displayString_setRange, //$NON-NLS-1$
                RegExMessages.additionalInfo_setRange);
        addProposal("&&", RegExMessages.displayString_setInter, //$NON-NLS-1$
                RegExMessages.additionalInfo_setInter);
      }
      if (!fIsEscape && fDocumentOffset > 0 && fExpression.charAt(fDocumentOffset - 1) == '\\') {
        addProposal("\\p{}", 3, RegExMessages.displayString_posix, //$NON-NLS-1$
                RegExMessages.additionalInfo_posix);
        addProposal("\\P{}", 3, RegExMessages.displayString_posixNot, //$NON-NLS-1$
                RegExMessages.additionalInfo_posixNot);
      } else {
        addBracketProposal("\\p{}", 3, RegExMessages.displayString_posix, //$NON-NLS-1$
                RegExMessages.additionalInfo_posix);
        addBracketProposal("\\P{}", 3, RegExMessages.displayString_posixNot, //$NON-NLS-1$
                RegExMessages.additionalInfo_posixNot);
      }

      // addBsProposal("\\p{Lower}",
      // RegExMessages.displayString_bs_p{Lower},
      // RegExMessages.additionalInfo_bs_p{Lower}); //$NON-NLS-1$
      // addBsProposal("\\p{Upper}",
      // RegExMessages.displayString_bs_p{Upper},
      // RegExMessages.additionalInfo_bs_p{Upper}); //$NON-NLS-1$
      // addBsProposal("\\p{ASCII}",
      // RegExMessages.displayString_bs_p{ASCII},
      // RegExMessages.additionalInfo_bs_p{ASCII}); //$NON-NLS-1$
      // addBsProposal("\\p{Alpha}",
      // RegExMessages.displayString_bs_p{Alpha},
      // RegExMessages.additionalInfo_bs_p{Alpha}); //$NON-NLS-1$
      // addBsProposal("\\p{Digit}",
      // RegExMessages.displayString_bs_p{Digit},
      // RegExMessages.additionalInfo_bs_p{Digit}); //$NON-NLS-1$
      // addBsProposal("\\p{Alnum}",
      // RegExMessages.displayString_bs_p{Alnum},
      // RegExMessages.additionalInfo_bs_p{Alnum}); //$NON-NLS-1$
      // addBsProposal("\\p{Punct}",
      // RegExMessages.displayString_bs_p{Punct},
      // RegExMessages.additionalInfo_bs_p{Punct}); //$NON-NLS-1$
      // addBsProposal("\\p{Graph}",
      // RegExMessages.displayString_bs_p{Graph},
      // RegExMessages.additionalInfo_bs_p{Graph}); //$NON-NLS-1$
      // addBsProposal("\\p{Print}",
      // RegExMessages.displayString_bs_p{Print},
      // RegExMessages.additionalInfo_bs_p{Print}); //$NON-NLS-1$
      // addBsProposal("\\p{Blank}",
      // RegExMessages.displayString_bs_p{Blank},
      // RegExMessages.additionalInfo_bs_p{Blank}); //$NON-NLS-1$
      // addBsProposal("\\p{Cntrl}",
      // RegExMessages.displayString_bs_p{Cntrl},
      // RegExMessages.additionalInfo_bs_p{Cntrl}); //$NON-NLS-1$
      // addBsProposal("\\p{XDigit}",
      // RegExMessages.displayString_bs_p{XDigit},
      // RegExMessages.additionalInfo_bs_p{XDigit}); //$NON-NLS-1$
      // addBsProposal("\\p{Space}",
      // RegExMessages.displayString_bs_p{Space},
      // RegExMessages.additionalInfo_bs_p{Space}); //$NON-NLS-1$
      //
      // addBsProposal("\\p{InGreek}",
      // RegExMessages.displayString_bs_p{InGreek},
      // RegExMessages.additionalInfo_bs_p{InGreek}); //$NON-NLS-1$
      // addBsProposal("\\p{Lu}", RegExMessages.displayString_bs_p{Lu},
      // RegExMessages.additionalInfo_bs_p{Lu}); //$NON-NLS-1$
      // addBsProposal("\\p{Sc}", RegExMessages.displayString_bs_p{Sc},
      // RegExMessages.additionalInfo_bs_p{Sc}); //$NON-NLS-1$
      // addBsProposal("\\P{InGreek}",
      // RegExMessages.displayString_bs_P{InGreek},
      // RegExMessages.additionalInfo_bs_P{InGreek}); //$NON-NLS-1$

      // boundary matchers
      if (fDocumentOffset == 0) {
        addPriorityProposal("^", RegExMessages.displayString_start, //$NON-NLS-1$
                RegExMessages.additionalInfo_start);
      } else if (fDocumentOffset == 1 && fExpression.charAt(0) == '^') {
        addBracketProposal("^", 1, RegExMessages.displayString_start, //$NON-NLS-1$
                RegExMessages.additionalInfo_start);
      }
      if (fDocumentOffset == fExpression.length()) {
        addProposal("$", RegExMessages.displayString_end, RegExMessages.additionalInfo_end); //$NON-NLS-1$
      }
      addBsProposal("\\b", RegExMessages.displayString_bs_b, RegExMessages.additionalInfo_bs_b); //$NON-NLS-1$
      addBsProposal("\\B", RegExMessages.displayString_bs_B, RegExMessages.additionalInfo_bs_B); //$NON-NLS-1$
      addBsProposal("\\A", RegExMessages.displayString_bs_A, RegExMessages.additionalInfo_bs_A); //$NON-NLS-1$
      addBsProposal("\\G", RegExMessages.displayString_bs_G, RegExMessages.additionalInfo_bs_G); //$NON-NLS-1$
      addBsProposal("\\Z", RegExMessages.displayString_bs_Z, RegExMessages.additionalInfo_bs_Z); //$NON-NLS-1$
      addBsProposal("\\z", RegExMessages.displayString_bs_z, RegExMessages.additionalInfo_bs_z); //$NON-NLS-1$

      if (!fIsEscape) {
        // capturing groups
        addBracketProposal("()", 1, RegExMessages.displayString_group, //$NON-NLS-1$
                RegExMessages.additionalInfo_group);

        // flags
        addBracketProposal("(?)", 2, RegExMessages.displayString_flag, //$NON-NLS-1$
                RegExMessages.additionalInfo_flag);
        addBracketProposal("(?:)", 3, RegExMessages.displayString_flagExpr, //$NON-NLS-1$
                RegExMessages.additionalInfo_flagExpr);

        // noncapturing group
        addBracketProposal("(?:)", 3, RegExMessages.displayString_nonCap, //$NON-NLS-1$
                RegExMessages.additionalInfo_nonCap);
        addBracketProposal("(?>)", 3, RegExMessages.displayString_atomicCap, //$NON-NLS-1$
                RegExMessages.additionalInfo_atomicCap);

        // lookaraound
        addBracketProposal("(?=)", 3, RegExMessages.displayString_posLookahead, //$NON-NLS-1$
                RegExMessages.additionalInfo_posLookahead);
        addBracketProposal("(?!)", 3, RegExMessages.displayString_negLookahead, //$NON-NLS-1$
                RegExMessages.additionalInfo_negLookahead);
        addBracketProposal("(?<=)", 4, RegExMessages.displayString_posLookbehind, //$NON-NLS-1$
                RegExMessages.additionalInfo_posLookbehind);
        addBracketProposal("(?<!)", 4, RegExMessages.displayString_negLookbehind, //$NON-NLS-1$
                RegExMessages.additionalInfo_negLookbehind);

        // greedy quantifiers
        addBracketProposal("?", 1, RegExMessages.displayString_quest, //$NON-NLS-1$
                RegExMessages.additionalInfo_quest);
        addBracketProposal("*", 1, RegExMessages.displayString_star, //$NON-NLS-1$
                RegExMessages.additionalInfo_star);
        addBracketProposal("+", 1, RegExMessages.displayString_plus, //$NON-NLS-1$
                RegExMessages.additionalInfo_plus);
        addBracketProposal("{}", 1, RegExMessages.displayString_exact, //$NON-NLS-1$
                RegExMessages.additionalInfo_exact);
        addBracketProposal("{,}", 1, RegExMessages.displayString_least, //$NON-NLS-1$
                RegExMessages.additionalInfo_least);
        addBracketProposal("{,}", 1, RegExMessages.displayString_count, //$NON-NLS-1$
                RegExMessages.additionalInfo_count);

        // lazy quantifiers
        addBracketProposal("??", 1, RegExMessages.displayString_questLazy, //$NON-NLS-1$
                RegExMessages.additionalInfo_questLazy);
        addBracketProposal("*?", 1, RegExMessages.displayString_starLazy, //$NON-NLS-1$
                RegExMessages.additionalInfo_starLazy);
        addBracketProposal("+?", 1, RegExMessages.displayString_plusLazy, //$NON-NLS-1$
                RegExMessages.additionalInfo_plusLazy);
        addBracketProposal("{}?", 1, RegExMessages.displayString_exactLazy, //$NON-NLS-1$
                RegExMessages.additionalInfo_exactLazy);
        addBracketProposal("{,}?", 1, RegExMessages.displayString_leastLazy, //$NON-NLS-1$
                RegExMessages.additionalInfo_leastLazy);
        addBracketProposal("{,}?", 1, RegExMessages.displayString_countLazy, //$NON-NLS-1$
                RegExMessages.additionalInfo_countLazy);

        // possessive quantifiers
        addBracketProposal("?+", 1, RegExMessages.displayString_questPoss, //$NON-NLS-1$
                RegExMessages.additionalInfo_questPoss);
        addBracketProposal("*+", 1, RegExMessages.displayString_starPoss, //$NON-NLS-1$
                RegExMessages.additionalInfo_starPoss);
        addBracketProposal("++", 1, RegExMessages.displayString_plusPoss, //$NON-NLS-1$
                RegExMessages.additionalInfo_plusPoss);
        addBracketProposal("{}+", 1, RegExMessages.displayString_exactPoss, //$NON-NLS-1$
                RegExMessages.additionalInfo_exactPoss);
        addBracketProposal("{,}+", 1, RegExMessages.displayString_leastPoss, //$NON-NLS-1$
                RegExMessages.additionalInfo_leastPoss);
        addBracketProposal("{,}+", 1, RegExMessages.displayString_countPoss, //$NON-NLS-1$
                RegExMessages.additionalInfo_countPoss);

        // alternative
        addBracketProposal("|", 1, RegExMessages.displayString_alt, //$NON-NLS-1$
                RegExMessages.additionalInfo_alt);
      }

      fPriorityProposals.addAll(fProposals);
      return fPriorityProposals
              .toArray(new ICompletionProposal[fProposals.size()]);
    }

    /**
     * Computes applicable proposals for the replace field.
     *
     * @return the proposals
     */
    public ICompletionProposal[] computeReplaceProposals() {
      if (fDocumentOffset > 0 && '$' == fExpression.charAt(fDocumentOffset - 1)) {
        addProposal("", RegExMessages.displayString_dollar, RegExMessages.additionalInfo_dollar); //$NON-NLS-1$
      } else {
        addProposal("$", RegExMessages.displayString_dollar, RegExMessages.additionalInfo_dollar); //$NON-NLS-1$
        addBsProposal("\\", RegExMessages.displayString_replace_bs, //$NON-NLS-1$
                RegExMessages.additionalInfo_replace_bs);
        addProposal("\t", RegExMessages.displayString_tab, RegExMessages.additionalInfo_tab); //$NON-NLS-1$
        addProposal("\n", RegExMessages.displayString_nl, RegExMessages.additionalInfo_nl); //$NON-NLS-1$
        addProposal("\r", RegExMessages.displayString_cr, RegExMessages.additionalInfo_cr); //$NON-NLS-1$
      }
      return fProposals.toArray(new ICompletionProposal[fProposals.size()]);
    }

    /**
     * Adds a proposal.
     *
     * @param proposal
     *          the string to be inserted
     * @param displayString
     *          the proposal's label
     * @param additionalInfo
     *          the additional information
     */
    private void addProposal(String proposal, String displayString, String additionalInfo) {
      fProposals.add(new CompletionProposal(proposal, fDocumentOffset, 0, proposal.length(), null,
              displayString, null, additionalInfo));
    }

    /**
     * Adds a proposal.
     *
     * @param proposal
     *          the string to be inserted
     * @param cursorPosition
     *          the cursor position after insertion, relative to the start of the proposal
     * @param displayString
     *          the proposal's label
     * @param additionalInfo
     *          the additional information
     */
    private void addProposal(String proposal, int cursorPosition, String displayString,
            String additionalInfo) {
      fProposals.add(new CompletionProposal(proposal, fDocumentOffset, 0, cursorPosition, null,
              displayString, null, additionalInfo));
    }

    /**
     * Adds a proposal to the priority proposals list.
     *
     * @param proposal
     *          the string to be inserted
     * @param displayString
     *          the proposal's label
     * @param additionalInfo
     *          the additional information
     */
    private void addPriorityProposal(String proposal, String displayString, String additionalInfo) {
      fPriorityProposals.add(new CompletionProposal(proposal, fDocumentOffset, 0, proposal.length(),
              null, displayString, null, additionalInfo));
    }

    /**
     * Adds a proposal. Ensures that existing pre- and postfixes are not duplicated.
     *
     * @param proposal
     *          the string to be inserted
     * @param cursorPosition
     *          the cursor position after insertion, relative to the start of the proposal
     * @param displayString
     *          the proposal's label
     * @param additionalInfo
     *          the additional information
     */
    private void addBracketProposal(String proposal, int cursorPosition, String displayString,
            String additionalInfo) {
      String prolog = fExpression.substring(0, fDocumentOffset);
      if (!fIsEscape && prolog.endsWith("\\") && proposal.startsWith("\\")) { //$NON-NLS-1$//$NON-NLS-2$
        fProposals.add(new CompletionProposal(proposal, fDocumentOffset, 0, cursorPosition, null,
                displayString, null, additionalInfo));
        return;
      }
      for (int i = 1; i <= cursorPosition; i++) {
        String prefix = proposal.substring(0, i);
        if (prolog.endsWith(prefix)) {
          String postfix = proposal.substring(cursorPosition);
          String epilog = fExpression.substring(fDocumentOffset);
          if (epilog.startsWith(postfix)) {
            fPriorityProposals.add(
                    new CompletionProposal(proposal.substring(i, cursorPosition), fDocumentOffset,
                            0, cursorPosition - i, null, displayString, null, additionalInfo));
          } else {
            fPriorityProposals.add(new CompletionProposal(proposal.substring(i), fDocumentOffset, 0,
                    cursorPosition - i, null, displayString, null, additionalInfo));
          }
          return;
        }
      }
      fProposals.add(new CompletionProposal(proposal, fDocumentOffset, 0, cursorPosition, null,
              displayString, null, additionalInfo));
    }

    /**
     * Adds a proposal that starts with a backslash.
     *
     * @param proposal
     *          the string to be inserted
     * @param displayString
     *          the proposal's label
     * @param additionalInfo
     *          the additional information
     */
    private void addBsProposal(String proposal, String displayString, String additionalInfo) {
      if (fIsEscape) {
        fPriorityProposals.add(new CompletionProposal(proposal.substring(1), fDocumentOffset, 0,
                proposal.length() - 1, null, displayString, null, additionalInfo));
      } else {
        addProposal(proposal, displayString, additionalInfo);
      }
    }
  }

  /**
   * The context information validator.
   */
  private IContextInformationValidator fValidator = new SubjectControlContextInformationValidator(
          this);

  /**
   * <code>true</code> iff the processor is for the find field. <code>false</code> iff the processor
   * is for the replace field.
   */
  private final boolean fIsFind;

  public RegExContentAssistProcessor(boolean isFind) {
    fIsFind = isFind;
  }

  @Override
  public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
    return null;
  }

  @Override
  public ICompletionProposal[] computeCompletionProposals(
          IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
    if (fIsFind) {
      return new ProposalComputer(contentAssistSubjectControl, documentOffset)
              .computeFindProposals();
    }

    return new ProposalComputer(contentAssistSubjectControl, documentOffset)
            .computeReplaceProposals();
  }

  @Override
  public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
    return null;
  }

  @Override
  public IContextInformation[] computeContextInformation(
          IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
    return null;
  }

  @Override
  public char[] getCompletionProposalAutoActivationCharacters() {
    if (fIsFind) {
      return new char[] { '\\', '[', '(' };
    }

    return new char[] { '$' };
  }

  @Override
  public char[] getContextInformationAutoActivationCharacters() {
    return new char[] {};
  }

  @Override
  public IContextInformationValidator getContextInformationValidator() {
    return fValidator;
  }

  @Override
  public String getErrorMessage() {
    return null;
  }

}
