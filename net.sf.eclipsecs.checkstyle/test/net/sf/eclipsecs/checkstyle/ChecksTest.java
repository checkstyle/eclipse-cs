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

package net.sf.eclipsecs.checkstyle;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import net.sf.eclipsecs.checkstyle.utils.CheckUtil;
import net.sf.eclipsecs.checkstyle.utils.XmlUtil;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.TreeWalker;

public class ChecksTest {

  @Test
  public void testMetadataFiles() throws Exception {
    final Set<Class<?>> modules = CheckUtil.getCheckstyleModules();

    // don't test root modules
    modules.remove(Checker.class);
    modules.remove(TreeWalker.class);

    final Set<String> packages = CheckUtil.getPackages(modules);

    assertThat(modules).as("modules").isNotEmpty();

    for (String p : packages) {
      assertThat(new File(getEclipseCsPath(p, ""))).exists();

      final Set<Class<?>> packgeModules = CheckUtil.getModulesInPackage(modules, p);
      assertThat(packgeModules).as("package modules").isNotEmpty();

      validateEclipseCsMetaXmlFile(new File(getEclipseCsPath(p, "/checkstyle-metadata.xml")), p);

      validateEclipseCsMetaPropFile(
              new File(getEclipseCsPath(p, "/checkstyle-metadata.properties")), p);
    }
  }

  private static void validateEclipseCsMetaXmlFile(File file, String packge) throws Exception {
    assertThat(file)
            .withFailMessage(
                    () -> "'checkstyle-metadata.xml' must exist in eclipsecs inside " + packge)
            .exists();

    final String input = new String(Files.readAllBytes(file.toPath()), UTF_8);
    final Document document = XmlUtil.getRawXml(file.getAbsolutePath(), input, input);

    final NodeList ruleGroups = document.getElementsByTagName("rule-group-metadata");

    assertThat(ruleGroups.getLength())
            .withFailMessage(
                    () -> packge + " checkstyle-metadata.xml must contain only one rule group")
            .isEqualTo(1);

    for (int position = 0; position < ruleGroups.getLength(); position++) {
      final Node ruleGroup = ruleGroups.item(position);
      final Set<Node> children = XmlUtil.getChildrenElements(ruleGroup);

      assertThat(children)
              .withFailMessage(() -> packge + " checkstyle-metadata.xml must contain no rules")
              .isEmpty();
    }
  }

  private static void validateEclipseCsMetaPropFile(File file, String packge) throws Exception {
    assertThat(file).withFailMessage(
            () -> "'checkstyle-metadata.properties' must exist in eclipsecs inside " + packge)
            .exists();

    final Properties prop = new Properties();
    prop.load(new FileInputStream(file));

    final Set<Object> properties = new HashSet<>(Collections.list(prop.keys()));

    assertThat(properties).withFailMessage(
            () -> packge + " checkstyle-metadata.properties must contain only the rule group name")
            .hasSize(1);

    assertThat(properties.iterator().next().toString()).withFailMessage(
            () -> packge + " checkstyle-metadata.properties must contain only the rule group name")
            .endsWith(".group");
  }

  private static String getEclipseCsPath(String packageName, String fileName) throws IOException {
    return new File(
            "../net.sf.eclipsecs.checkstyle/metadata/" + packageName.replace(".", "/") + fileName)
                    .getCanonicalPath();
  }
}
