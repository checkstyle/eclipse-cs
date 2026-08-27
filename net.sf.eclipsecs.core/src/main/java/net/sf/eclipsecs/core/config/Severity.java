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

package net.sf.eclipsecs.core.config;

import java.util.Arrays;

/**
 * Enumeration for Checkstyle's severity levels. The intent is to decouple highler level funtions
 * (UI) from dealing with Checkstyle code API.
 *
 */
public enum Severity {

    /** Unspecified severity level, inherited from parent module. */
    INHERIT("inherit"),

    /** Severity level 'ignore'. */
    IGNORE("ignore"),

    /** Severity level 'info'. */
    INFO("info"),

    /** Severity level 'warning'. */
    WARNING("warning"),

    /** Severity level 'error'. */
    ERROR("error");

    /** String used to serialize the Severity in XML files. */
    private final String xmlValue;

    /**
     * Creates a severity with the given XML value.
     *
     * @param xmlValue
     *            the XML value
     */
    Severity(String xmlValue) {
        this.xmlValue = xmlValue;
    }

    /**
     * Get xmlValue.
     *
     * @return the xmlValue
     */
    public String getXmlValue() {
        return xmlValue;
    }

    /**
     * Converts an XML value to the corresponding severity.
     *
     * @param xmlValue
     *            the XML value
     * @return the matching severity
     */
    public static Severity fromXmlValue(String xmlValue) {
        return Arrays.stream(Severity.values())
            .filter(severity -> severity.getXmlValue().equals(xmlValue)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unexpected value: " + xmlValue));
    }

}
