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

package net.sf.eclipsecs.core.config.meta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Class that provides all known tokens from the checkstyle java grammar. This is used for modules
 * that allow all tokens as options - which is very tedious to maintain in the metadata.
 *
 */
public class AllTokensProvider implements IOptionProvider {

    /** The list of options. */
    private static List<String> sAllOptions = new ArrayList<>();

    /* Static initializer. Builds the option list. */
    static {

        final Field[] fields = TokenTypes.class.getDeclaredFields();
        for (Field field : fields) {
            // Only process the int declarations.
            if (field.getType() != Integer.TYPE) {
                continue;
            }
            sAllOptions.add(field.getName());
        }
    }

    /**
     * Returns all options.
     *
     * @return the options
     */
    @Override
    public List<String> getOptions() {
        return sAllOptions;
    }
}
