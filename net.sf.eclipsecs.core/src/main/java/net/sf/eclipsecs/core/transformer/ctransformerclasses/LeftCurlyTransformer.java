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

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

import net.sf.eclipsecs.core.transformer.AbstractCTransformationClass;
import net.sf.eclipsecs.core.transformer.FormatterConfiguration;

/**
 * Wrapper class for converting the checkstyle-rule LeftCurly to appropriate
 * eclipse-formatter-rules.
 */
public class LeftCurlyTransformer extends AbstractCTransformationClass {

    /** Formatter settings to apply for the CLASS_DEF token. */
    private static final List<String> CLASS_DEF_SETTINGS = List.of(
        DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_ANONYMOUS_TYPE_DECLARATION,
        DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_ENUM_CONSTANT,
        DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_ENUM_DECLARATION,
        DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION,
        DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_ANNOTATION_TYPE_DECLARATION);

    /** Formatter settings to apply for the INTERFACE_DEF token. */
    private static final List<String> INTERFACE_DEF_SETTINGS = List.of(
        DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_ANNOTATION_TYPE_DECLARATION,
        DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION);

    /** Formatter setting to apply for the CTOR_DEF token. */
    private static final List<String> CTOR_DEF_SETTINGS = List
        .of(DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_CONSTRUCTOR_DECLARATION);

    /** Formatter setting to apply for the METHOD_DEF token. */
    private static final List<String> METHOD_DEF_SETTINGS = List
        .of(DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION);

    /** Formatter setting to apply for block-statement tokens. */
    private static final List<String> BLOCK_SETTINGS = List
        .of(DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_BLOCK);

    /** Formatter setting to apply for the LITERAL_SWITCH token. */
    private static final List<String> SWITCH_SETTINGS = List
        .of(DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_SWITCH);

    /**
     * Returns the formatter settings for the given token.
     *
     * @param token
     *            the token to look up
     * @return the formatter settings for the token
     */
    private static List<String> getSettingsForToken(String token) {
        return switch (token) {
            case null -> Collections.emptyList();
            case "CLASS_DEF" -> CLASS_DEF_SETTINGS;
            case "INTERFACE_DEF" -> INTERFACE_DEF_SETTINGS;
            case "CTOR_DEF" -> CTOR_DEF_SETTINGS;
            case "METHOD_DEF" -> METHOD_DEF_SETTINGS;
            case "LITERAL_DO", "LITERAL_ELSE", "LITERAL_FOR", "LITERAL_IF", "LITERAL_WHILE",
                "LITERAL_CATCH", "LITERAL_FINALLY", "LITERAL_TRY",
                "LITERAL_SYNCHRONIZED" -> BLOCK_SETTINGS;
            case "LITERAL_SWITCH" -> SWITCH_SETTINGS;
            default -> Collections.emptyList();
        };
    }

    @Override
    public FormatterConfiguration transformRule() {
        String tokens = getAttribute("tokens");
        if (tokens == null) {
            tokens = "CLASS_DEF, CTOR_DEF, INTERFACE_DEF, METHOD_DEF, LITERAL_CATCH, LITERAL_DO, "
                + "LITERAL_ELSE, LITERAL_FINALLY, LITERAL_FOR, LITERAL_IF, LITERAL_SYNCHRONIZED, "
                + "LITERAL_TRY, LITERAL_WHILE";
        }

        final String option = switch (getAttribute("option")) {
            case null -> DefaultCodeFormatterConstants.END_OF_LINE;
            case "eol" -> DefaultCodeFormatterConstants.END_OF_LINE;
            case "nl", "nlow" -> DefaultCodeFormatterConstants.NEXT_LINE;
            case String s -> s;
        };

        for (String token : tokens.split("\\s*,\\s*")) {
            getSettingsForToken(token).forEach(setting -> userFormatterSetting(setting, option));
        }
        return getFormatterSetting();
    }

}
