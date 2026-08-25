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

import java.util.List;

import net.sf.eclipsecs.core.config.Severity;

/**
 * This class contains the metadata that describes a check rule.
 *
 * @param identity
 *          the identity of the rule (name, internal name, etc.)
 * @param defaultSeverity
 *          the default severity level
 * @param hidden
 *          <code>true</code> if the rule should be hidden from the user
 * @param hasSeverity
 *          <code>true</code> if the rule has a severity level
 * @param deletable
 *          <code>true</code> if the rule is deletable
 * @param isSingleton
 *          <code>true</code> if the rule is a singleton
 * @param messageKeys
 *          the message keys supported by the rule
 * @param configPropMetadata
 *          the property metadata for the rule
 */
public record RuleMetadata(RuleIdentity identity, Severity defaultSeverity, boolean hidden,
        boolean hasSeverity, boolean deletable, boolean isSingleton, List<String> messageKeys,
        List<ConfigPropertyMetadata> configPropMetadata) {

}
