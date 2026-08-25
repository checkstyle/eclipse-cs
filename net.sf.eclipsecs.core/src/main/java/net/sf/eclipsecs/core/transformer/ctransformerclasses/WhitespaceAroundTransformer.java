//============================================================================
//
// Copyright (C) 2003-2023  Lukas Frena
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

package net.sf.eclipsecs.core.transformer.ctransformerclasses;

import java.util.List;

import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

import net.sf.eclipsecs.core.transformer.AbstractCTransformationClass;
import net.sf.eclipsecs.core.transformer.FormatterConfiguration;

/**
 * Wrapper class for converting the checkstyle-rule WhitespaceAround to appropriate
 * eclipse-formatter-rules.
 *
 */
public class WhitespaceAroundTransformer extends AbstractCTransformationClass {

    /** Formatter settings to apply for assignment-operator tokens. */
    private static final List<String> ASSIGNMENT_SETTINGS = List.of(
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR);

    /** Formatter settings to apply for binary-operator tokens. */
    private static final List<String> BINARY_SETTINGS = List.of(
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_BINARY_OPERATOR,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BINARY_OPERATOR);

    /** Formatter settings to apply for the COLON token. */
    private static final List<String> COLON_SETTINGS = List.of(
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_FOR,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_FOR,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL);

    /** Formatter settings to apply for the QUESTION token. */
    private static final List<String> QUESTION_SETTINGS = List.of(
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL);

    /** Formatter settings to apply for the LCURLY token. */
    private static final List<String> LCURLY_SETTINGS = List.of(
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_TYPE_DECLARATION,
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER,
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ANNOTATION_TYPE_DECLARATION,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_BLOCK,
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_METHOD_DECLARATION,
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ENUM_DECLARATION,
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_CONSTRUCTOR_DECLARATION,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ENUM_CONSTANT,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_SWITCH,
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ANONYMOUS_TYPE_DECLARATION,
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ARRAY_INITIALIZER);

    /** Formatter settings to apply for the RCURLY token. */
    private static final List<String> RCURLY_SETTINGS = List.of(
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_BRACE_IN_BLOCK,
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER);

    /** Formatter setting to apply for the LITERAL_CATCH token. */
    private static final List<String> CATCH_SETTINGS = List
        .of(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_CATCH);

    /** Formatter setting to apply for the LITERAL_FOR token. */
    private static final List<String> FOR_SETTINGS = List
        .of(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_FOR);

    /** Formatter setting to apply for the LITERAL_IF token. */
    private static final List<String> IF_SETTINGS = List
        .of(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_IF);

    /** Formatter setting to apply for the LITERAL_RETURN token. */
    private static final List<String> RETURN_SETTINGS = List.of(
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_BEFORE_PARENTHESIZED_EXPRESSION_IN_RETURN);

    /** Formatter setting to apply for the LITERAL_SYNCHRONIZED token. */
    private static final List<String> SYNCHRONIZED_SETTINGS = List
        .of(DefaultCodeFormatterConstants
                .FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_SYNCHRONIZED);

    /** Formatter setting to apply for loop-related tokens. */
    private static final List<String> WHILE_SETTINGS = List
        .of(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_WHILE);

    /** Formatter setting to apply for the LITERAL_SWITCH token. */
    private static final List<String> SWITCH_SETTINGS = List
        .of(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_SWITCH);

    /** Formatter setting to apply for the SLIST token. */
    private static final List<String> SLIST_SETTINGS = List
        .of(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_BLOCK);

    /** Formatter settings to apply for the TYPE_EXTENSION_AND token. */
    private static final List<String> TYPE_EXTENSION_AND_SETTINGS = List.of(
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_AND_IN_TYPE_PARAMETER,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_AND_IN_TYPE_PARAMETER);

    /** Formatter settings to apply for the LAMBDA token. */
    private static final List<String> LAMBDA_SETTINGS = List.of(
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_LAMBDA_ARROW,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_LAMBDA_ARROW);

    /** Formatter settings to apply for the ELLIPSIS token. */
    private static final List<String> ELLIPSIS_SETTINGS = List.of(
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ELLIPSIS,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ELLIPSIS);

    /** Formatter settings to apply for the WILDCARD_TYPE token. */
    private static final List<String> WILDCARD_TYPE_SETTINGS = List.of(
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_QUESTION_IN_WILDCARD,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_QUESTION_IN_WILDCARD);

    @Override
    public FormatterConfiguration transformRule() {
        String tokens = getAttribute("tokens");
        if (tokens == null) {
            tokens = String.join(", ", List.of(CheckstyleTokens.ARRAY_INIT, CheckstyleTokens.ASSIGN,
                CheckstyleTokens.BAND, CheckstyleTokens.BAND_ASSIGN, CheckstyleTokens.BOR,
                CheckstyleTokens.BOR_ASSIGN, CheckstyleTokens.BSR, CheckstyleTokens.BSR_ASSIGN,
                CheckstyleTokens.BXOR, CheckstyleTokens.BXOR_ASSIGN, CheckstyleTokens.COLON,
                CheckstyleTokens.DIV, CheckstyleTokens.DIV_ASSIGN, CheckstyleTokens.DO_WHILE,
                CheckstyleTokens.ELLIPSIS, CheckstyleTokens.EQUAL, CheckstyleTokens.GE,
                CheckstyleTokens.GENERIC_END, CheckstyleTokens.GENERIC_START, CheckstyleTokens.GT,
                CheckstyleTokens.LAMBDA, CheckstyleTokens.LAND, CheckstyleTokens.LCURLY,
                CheckstyleTokens.LE, CheckstyleTokens.LITERAL_ASSERT,
                CheckstyleTokens.LITERAL_CATCH, CheckstyleTokens.LITERAL_DO,
                CheckstyleTokens.LITERAL_ELSE, CheckstyleTokens.LITERAL_FINALLY,
                CheckstyleTokens.LITERAL_FOR, CheckstyleTokens.LITERAL_IF,
                CheckstyleTokens.LITERAL_RETURN, CheckstyleTokens.LITERAL_SWITCH,
                CheckstyleTokens.LITERAL_SYNCHRONIZED, CheckstyleTokens.LITERAL_TRY,
                CheckstyleTokens.LITERAL_WHEN, CheckstyleTokens.LITERAL_WHILE, CheckstyleTokens.LOR,
                CheckstyleTokens.LT, CheckstyleTokens.MINUS, CheckstyleTokens.MINUS_ASSIGN,
                CheckstyleTokens.MOD, CheckstyleTokens.MOD_ASSIGN, CheckstyleTokens.NOT_EQUAL,
                CheckstyleTokens.PLUS, CheckstyleTokens.PLUS_ASSIGN, CheckstyleTokens.QUESTION,
                CheckstyleTokens.RCURLY, CheckstyleTokens.SL, CheckstyleTokens.SLIST,
                CheckstyleTokens.SL_ASSIGN, CheckstyleTokens.SR, CheckstyleTokens.SR_ASSIGN,
                CheckstyleTokens.STAR, CheckstyleTokens.STAR_ASSIGN,
                CheckstyleTokens.TYPE_EXTENSION_AND, CheckstyleTokens.WILDCARD_TYPE));
        }

        for (String token : tokens.split("\\s*,\\s*")) {
            final List<String> settings = switch (token) {
                case null -> List.of();
                case CheckstyleTokens.ASSIGN, CheckstyleTokens.BAND_ASSIGN,
                    CheckstyleTokens.BOR_ASSIGN, CheckstyleTokens.BSR_ASSIGN,
                    CheckstyleTokens.BXOR_ASSIGN, CheckstyleTokens.DIV_ASSIGN,
                    CheckstyleTokens.MINUS_ASSIGN, CheckstyleTokens.MOD_ASSIGN,
                    CheckstyleTokens.PLUS_ASSIGN, CheckstyleTokens.SL_ASSIGN,
                    CheckstyleTokens.SR_ASSIGN, CheckstyleTokens.STAR_ASSIGN ->
                    ASSIGNMENT_SETTINGS;
                case CheckstyleTokens.BAND, CheckstyleTokens.BOR, CheckstyleTokens.BSR,
                    CheckstyleTokens.BXOR, CheckstyleTokens.DIV, CheckstyleTokens.EQUAL,
                    CheckstyleTokens.GE, CheckstyleTokens.GT, CheckstyleTokens.LAND,
                    CheckstyleTokens.LE, CheckstyleTokens.LOR, CheckstyleTokens.LT,
                    CheckstyleTokens.MINUS, CheckstyleTokens.MOD, CheckstyleTokens.NOT_EQUAL,
                    CheckstyleTokens.PLUS, CheckstyleTokens.SL, CheckstyleTokens.SR,
                    CheckstyleTokens.STAR -> BINARY_SETTINGS;
                case CheckstyleTokens.COLON -> COLON_SETTINGS;
                case CheckstyleTokens.QUESTION -> QUESTION_SETTINGS;
                case CheckstyleTokens.LCURLY -> LCURLY_SETTINGS;
                case CheckstyleTokens.RCURLY -> RCURLY_SETTINGS;
                case CheckstyleTokens.LITERAL_CATCH -> CATCH_SETTINGS;
                case CheckstyleTokens.LITERAL_FOR -> FOR_SETTINGS;
                case CheckstyleTokens.LITERAL_IF -> IF_SETTINGS;
                case CheckstyleTokens.LITERAL_RETURN -> RETURN_SETTINGS;
                case CheckstyleTokens.LITERAL_SYNCHRONIZED -> SYNCHRONIZED_SETTINGS;
                case CheckstyleTokens.LITERAL_WHILE, CheckstyleTokens.DO_WHILE -> WHILE_SETTINGS;
                case CheckstyleTokens.LITERAL_SWITCH -> SWITCH_SETTINGS;
                case CheckstyleTokens.SLIST -> SLIST_SETTINGS;
                case CheckstyleTokens.TYPE_EXTENSION_AND -> TYPE_EXTENSION_AND_SETTINGS;
                case CheckstyleTokens.LAMBDA -> LAMBDA_SETTINGS;
                case CheckstyleTokens.ELLIPSIS -> ELLIPSIS_SETTINGS;
                case CheckstyleTokens.WILDCARD_TYPE -> WILDCARD_TYPE_SETTINGS;
                default -> List.of();
            };
            settings.forEach(setting -> userFullFormatterSetting(setting, "insert"));
        }
        return getFormatterSetting();
    }
}
