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
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//
//============================================================================

package net.sf.eclipsecs.core.transformer.ctransformerclasses;

/**
 * Constants for the tokens recognized by checkstyle rules.
 *
 */
public final class CheckstyleTokens {

    /** Array initialization. */
    public static final String ARRAY_INIT = "ARRAY_INIT";

    /** Assignment operator ({@code "="}). */
    public static final String ASSIGN = "ASSIGN";

    /** Bitwise AND operator ({@code "&"}). */
    public static final String BAND = "BAND";

    /** Bitwise AND assignment operator ({@code "&="}). */
    public static final String BAND_ASSIGN = "BAND_ASSIGN";

    /** Bitwise OR operator ({@code "|"}). */
    public static final String BOR = "BOR";

    /** Bitwise OR assignment operator ({@code "|="}). */
    public static final String BOR_ASSIGN = "BOR_ASSIGN";

    /** Unsigned shift right operator ({@code ">>>"}). */
    public static final String BSR = "BSR";

    /** Unsigned right shift assignment operator ({@code ">>>="}). */
    public static final String BSR_ASSIGN = "BSR_ASSIGN";

    /** Bitwise exclusive OR operator ({@code "^"}). */
    public static final String BXOR = "BXOR";

    /** Bitwise exclusive OR assignment operator ({@code "^="}). */
    public static final String BXOR_ASSIGN = "BXOR_ASSIGN";

    /** Colon operator ({@code ":"}). */
    public static final String COLON = "COLON";

    /** Division operator ({@code "/"}). */
    public static final String DIV = "DIV";

    /** Division assignment operator ({@code "/="}). */
    public static final String DIV_ASSIGN = "DIV_ASSIGN";

    /** Literal {@code while} in do-while loop. */
    public static final String DO_WHILE = "DO_WHILE";

    /** Triple dot for variable-length parameters ({@code "..."}). */
    public static final String ELLIPSIS = "ELLIPSIS";

    /** Equal operator ({@code "=="}). */
    public static final String EQUAL = "EQUAL";

    /** Greater than or equal operator ({@code ">="}). */
    public static final String GE = "GE";

    /** End of generic type arguments ({@code ">"}). */
    public static final String GENERIC_END = "GENERIC_END";

    /** Start of generic type arguments ({@code "<"}). */
    public static final String GENERIC_START = "GENERIC_START";

    /** Greater than operator ({@code ">"}). */
    public static final String GT = "GT";

    /** Java 8 Lambda symbol ({@code "->"}). */
    public static final String LAMBDA = "LAMBDA";

    /** Logical AND operator ({@code "&&"}). */
    public static final String LAND = "LAND";

    /** Left curly brace ({@code "{}"}). */
    public static final String LCURLY = "LCURLY";

    /** Less than or equals operator ({@code "<="}). */
    public static final String LE = "LE";

    /** {@code assert} keyword. */
    public static final String LITERAL_ASSERT = "LITERAL_ASSERT";

    /** {@code catch} keyword. */
    public static final String LITERAL_CATCH = "LITERAL_CATCH";

    /** {@code do} keyword. */
    public static final String LITERAL_DO = "LITERAL_DO";

    /** {@code else} keyword. */
    public static final String LITERAL_ELSE = "LITERAL_ELSE";

    /** {@code finally} keyword. */
    public static final String LITERAL_FINALLY = "LITERAL_FINALLY";

    /** {@code for} keyword. */
    public static final String LITERAL_FOR = "LITERAL_FOR";

    /** {@code if} keyword. */
    public static final String LITERAL_IF = "LITERAL_IF";

    /** {@code return} keyword. */
    public static final String LITERAL_RETURN = "LITERAL_RETURN";

    /** {@code switch} keyword. */
    public static final String LITERAL_SWITCH = "LITERAL_SWITCH";

    /** {@code synchronized} keyword. */
    public static final String LITERAL_SYNCHRONIZED = "LITERAL_SYNCHRONIZED";

    /** {@code try} keyword. */
    public static final String LITERAL_TRY = "LITERAL_TRY";

    /** {@code when} keyword. */
    public static final String LITERAL_WHEN = "LITERAL_WHEN";

    /** {@code while} keyword. */
    public static final String LITERAL_WHILE = "LITERAL_WHILE";

    /** Logical OR operator ({@code "||"}). */
    public static final String LOR = "LOR";

    /** Less than operator ({@code "<"}). */
    public static final String LT = "LT";

    /** Subtraction operator ({@code "-"}). */
    public static final String MINUS = "MINUS";

    /** Subtraction assignment operator ({@code "-="}). */
    public static final String MINUS_ASSIGN = "MINUS_ASSIGN";

    /** Remainder operator ({@code "%"}). */
    public static final String MOD = "MOD";

    /** Remainder assignment operator ({@code "%="}). */
    public static final String MOD_ASSIGN = "MOD_ASSIGN";

    /** Not equal operator ({@code "!="}). */
    public static final String NOT_EQUAL = "NOT_EQUAL";

    /** Parameter declaration. */
    public static final String PARAMETER_DEF = "PARAMETER_DEF";

    /** Addition operator ({@code "+"}). */
    public static final String PLUS = "PLUS";

    /** Addition assignment operator ({@code "+="}). */
    public static final String PLUS_ASSIGN = "PLUS_ASSIGN";

    /** Inline conditional operator ({@code "?"}). */
    public static final String QUESTION = "QUESTION";

    /** Right curly brace ({@code "}"}). */
    public static final String RCURLY = "RCURLY";

    /** Shift left operator ({@code "<<"}). */
    public static final String SL = "SL";

    /** Statement list. */
    public static final String SLIST = "SLIST";

    /** Left shift assignment operator ({@code "<<="}). */
    public static final String SL_ASSIGN = "SL_ASSIGN";

    /** Signed shift right operator ({@code ">>"}). */
    public static final String SR = "SR";

    /** Signed right shift assignment operator ({@code ">>="}). */
    public static final String SR_ASSIGN = "SR_ASSIGN";

    /** Multiplication operator ({@code "*"}). */
    public static final String STAR = "STAR";

    /** Multiplication assignment operator ({@code "*="}). */
    public static final String STAR_ASSIGN = "STAR_ASSIGN";

    /** Variable declaration. */
    public static final String VARIABLE_DEF = "VARIABLE_DEF";

    /** AND symbol in generic constraint ({@code "&"}). */
    public static final String TYPE_EXTENSION_AND = "TYPE_EXTENSION_AND";

    /** Type that refers to all types ({@code "?"}). */
    public static final String WILDCARD_TYPE = "WILDCARD_TYPE";

    private CheckstyleTokens() {
    }
}
