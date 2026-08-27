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

package net.sf.eclipsecs.ui.config.widgets;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.swt.widgets.Composite;

import net.sf.eclipsecs.core.config.ConfigProperty;

/**
 * Property configuration widget for string arrays.
 */
public final class ConfigPropertyWidgetStringArray extends ConfigPropertyWidgetString {

    /**
     * Constructor creating the string array property widget.
     *
     * @param parent
     *            the parent composite
     * @param prop
     *            the configuration property
     */
    private ConfigPropertyWidgetStringArray(Composite parent, ConfigProperty prop) {
        super(parent, prop);
    }

    /**
     * Creates a new string array property widget for the given property.
     *
     * @param parent
     *            the parent composite
     * @param prop
     *            the configuration property
     * @return the newly-created widget
     */
    public static ConfigPropertyWidgetStringArray create(Composite parent, ConfigProperty prop) {
        return new ConfigPropertyWidgetStringArray(parent, prop);
    }

    @Override
    protected String getInitValue() {
        return normalizeSeparator(super.getInitValue());
    }

    @Override
    public String getValue() {
        return normalizeSeparator(super.getValue());
    }

    /**
     * Normalize array properties to be separated by a comma and a blank for better readability of
     * the plain config file.
     *
     * @param text
     *            the text to normalize
     * @return text with normalized separators
     */
    private String normalizeSeparator(String text) {
        return Arrays.stream(text.split(",")).map(String::strip).collect(Collectors.joining(", "));
    }
}
