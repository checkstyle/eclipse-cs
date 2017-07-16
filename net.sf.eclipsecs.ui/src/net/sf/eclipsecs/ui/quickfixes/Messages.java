//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.ui.quickfixes;

//CHECKSTYLE:OFF
import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "net.sf.eclipsecs.ui.quickfixes.messages"; //$NON-NLS-1$

  private Messages() {
  }

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  public static String ModifierOrderQuickfix_description;

  public static String ModifierOrderQuickfix_label;

  public static String RedundantModifierQuickfix_description;

  public static String RedundantModifierQuickfix_label;

  public static String ArrayTypeStyleQuickfix_description;

  public static String ArrayTypeStyleQuickfix_label;

  public static String FinalParametersQuickfix_description;

  public static String FinalParametersQuickfix_label;

  public static String UncommentedMainQuickfix_description;

  public static String UncommentedMainQuickfix_label;

  public static String UpperEllQuickfix_description;

  public static String UpperEllQuickfix_label;

  public static String DesignForExtensionQuickfix_description;

  public static String DesignForExtensionQuickfix_label;

  public static String FinalClassQuickfix_description;

  public static String FinalClassQuickfix_label;

  public static String EmptyStatementQuickfix_description;

  public static String EmptyStatementQuickfix_label;

  public static String FinalLocalVariableQuickfix_description;

  public static String FinalLocalVariableQuickfix_label;

  public static String RequireThisQuickfix_description;

  public static String RequireThisQuickfix_label;

  public static String ExplicitInitializationQuickfix_description;

  public static String ExplicitInitializationQuickfix_errorMessageFieldName;

  public static String ExplicitInitializationQuickfix_label;

  public static String ExplicitInitializationQuickfix_unknownFieldName;

  public static String SimplifyBooleanReturnQuickfix_description;

  public static String SimplifyBooleanReturnQuickfix_label;

  public static String MissingSwitchDefaultQuickfix_defaultCaseComment;

  public static String MissingSwitchDefaultQuickfix_description;

  public static String MissingSwitchDefaultQuickfix_label;

  public static String DefaultComesLastQuickfix_description;

  public static String DefaultComesLastQuickfix_label;

  public static String StringLiteralEqualityQuickfix_description;

  public static String StringLiteralEqualityQuickfix_label;

  public static String AvoidNextedBlocksQuickfix_description;

  public static String AvoidNextedBlocksQuickfix_label;

  public static String NeedBracesQuickfix_description;

  public static String NeedBracesQuickfix_label;
}
// CHECKSTYLE:ON
