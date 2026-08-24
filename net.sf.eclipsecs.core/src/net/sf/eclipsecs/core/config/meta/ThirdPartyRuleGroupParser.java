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

package net.sf.eclipsecs.core.config.meta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * Parser for the rule group data of third party extension modules declared in an
 * eclipse-metadata.yml file.
 */
final class ThirdPartyRuleGroupParser {

    private ThirdPartyRuleGroupParser() {
    }

    /**
     * Parses the rule groups from the given eclipse-metadata.yml content.
     *
     * @param metadataContent
     *            the YML content
     * @return the rule group info keyed by package name
     */
    static Map<String, ThirdPartyRuleGroupInfo> parse(String metadataContent) {
        final Map<String, List<Map<String, Object>>> objects = new Yaml().load(metadataContent);
        final Map<String, ThirdPartyRuleGroupInfo> ruleGroups = new HashMap<>();
        for (Map<String, Object> obj : objects.get("ruleGroups")) {
            ruleGroups.put((String) obj.get("package"),
                new ThirdPartyRuleGroupInfo((String) obj.get("name"),
                    (String) obj.get("description"), (Integer) obj.get("priority")));
        }
        return ruleGroups;
    }
}
