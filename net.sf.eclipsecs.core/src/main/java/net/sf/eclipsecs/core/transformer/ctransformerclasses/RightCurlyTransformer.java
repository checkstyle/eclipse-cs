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
 * Wrapper class for converting the checkstyle-rule RightCurly to appropriate
 * eclipse-formatter-rules.
 *
 */
public class RightCurlyTransformer extends AbstractCTransformationClass {

    @Override
    public FormatterConfiguration transformRule() {
        String tokens = getAttribute("tokens");
        if (tokens == null) {
            tokens = "LITERAL_TRY, LITERAL_CATCH, LITERAL_FINALLY, LITERAL_IF, LITERAL_ELSE";
        }

        final String option = getAttribute("option");
        final String value = switch (option) {
            case null -> JavaCore.DO_NOT_INSERT;
            case "same" -> JavaCore.DO_NOT_INSERT;
            default -> "insert";
        };

        for (String token : tokens.split("\\s*,\\s*")) {
            final List<String> settings = switch (token) {
                case null -> List.of();
                case "LITERAL_TRY" -> List.of(
                    DefaultCodeFormatterConstants
                        .FORMATTER_INSERT_NEW_LINE_BEFORE_CATCH_IN_TRY_STATEMENT,
                    DefaultCodeFormatterConstants
                        .FORMATTER_INSERT_NEW_LINE_BEFORE_FINALLY_IN_TRY_STATEMENT);
                case "LITERAL_CATCH" -> List.of(
                    DefaultCodeFormatterConstants
                        .FORMATTER_INSERT_NEW_LINE_BEFORE_FINALLY_IN_TRY_STATEMENT);
                case "LITERAL_IF" -> List.of(
                    DefaultCodeFormatterConstants
                        .FORMATTER_INSERT_NEW_LINE_BEFORE_ELSE_IN_IF_STATEMENT);
                case "LITERAL_DO" -> List.of(
                    DefaultCodeFormatterConstants
                        .FORMATTER_INSERT_NEW_LINE_BEFORE_WHILE_IN_DO_STATEMENT);
                default -> List.of();
            };
            settings.forEach(setting -> userFullFormatterSetting(setting, value));
        }
        return getFormatterSetting();
    }

}
