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

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

import net.sf.eclipsecs.core.transformer.AbstractCTransformationClass;
import net.sf.eclipsecs.core.transformer.FormatterConfiguration;

/**
 * Wrapperclass for converting the checkstyle-rule ParenPad to appropriate eclipse-formatter-rules.
 *
 */
public class ParenPadTransformer extends AbstractCTransformationClass {

    /** Formatter settings to apply for the LPAREN token. */
    private static final List<String> LPAREN_SETTINGS = List.of(
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_PARENTHESIZED_EXPRESSION,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_WHILE,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_FOR,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_IF,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_SWITCH,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_SYNCHRONIZED,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CATCH,
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_INVOCATION,
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_ANNOTATION,
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CONSTRUCTOR_DECLARATION,
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_ENUM_CONSTANT,
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_DECLARATION);

    /** Formatter settings to apply for the RPAREN token. */
    private static final List<String> RPAREN_SETTINGS = List.of(
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHESIZED_EXPRESSION,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_WHILE,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_FOR,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_IF,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_SWITCH,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_SYNCHRONIZED,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CATCH,
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_INVOCATION,
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_DECLARATION,
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CONSTRUCTOR_DECLARATION,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_ENUM_CONSTANT,
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_ANNOTATION);

    /** Formatter settings to apply for method-invocation tokens. */
    private static final List<String> METHOD_INVOCATION_SETTINGS = List.of(
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_INVOCATION,
        DefaultCodeFormatterConstants
            .FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_INVOCATION);

    @Override
    public FormatterConfiguration transformRule() {
        String tokens = getAttribute("tokens");
        if (tokens == null) {
            tokens = "CTOR_CALL, LPAREN, METHOD_CALL, RPAREN, SUPER_CTOR_CALL";
        }
        final String option = getAttribute("option");
        final String value = switch (option) {
            case null -> JavaCore.DO_NOT_INSERT;
            case "nospace" -> JavaCore.DO_NOT_INSERT;
            default -> "insert";
        };

        for (String token : tokens.split("\\s*,\\s*")) {
            final List<String> settings = switch (token) {
                case null -> List.of();
                case "LPAREN" -> LPAREN_SETTINGS;
                case "RPAREN" -> RPAREN_SETTINGS;
                case "CTOR_CALL", "METHOD_CALL", "SUPER_CTOR_CALL" -> METHOD_INVOCATION_SETTINGS;
                default -> List.of();
            };
            settings.forEach(setting -> userFullFormatterSetting(setting, value));
        }
        return getFormatterSetting();
    }

}
