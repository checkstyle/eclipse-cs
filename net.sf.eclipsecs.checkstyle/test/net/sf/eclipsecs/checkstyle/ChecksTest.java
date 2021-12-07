package net.sf.eclipsecs.checkstyle;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    assertTrue(modules.size() > 0, "no modules");

    for (String p : packages) {
      assertTrue(new File(getEclipseCsPath(p, "")).exists(), "folder " + p + " must exist in eclipsecs");

      final Set<Class<?>> packgeModules = CheckUtil.getModulesInPackage(modules, p);

      validateEclipseCsMetaXmlFile(new File(getEclipseCsPath(p, "/checkstyle-metadata.xml")), p,
              new HashSet<>(packgeModules));

      validateEclipseCsMetaPropFile(
              new File(getEclipseCsPath(p, "/checkstyle-metadata.properties")), p,
              new HashSet<>(packgeModules));
    }
  }

  private static void validateEclipseCsMetaXmlFile(File file, String packge,
          Set<Class<?>> packgeModules) throws Exception {
    assertTrue(file.exists(), "'checkstyle-metadata.xml' must exist in eclipsecs in inside " + packge);

    final String input = new String(Files.readAllBytes(file.toPath()), UTF_8);
    final Document document = XmlUtil.getRawXml(file.getAbsolutePath(), input, input);

    final NodeList ruleGroups = document.getElementsByTagName("rule-group-metadata");

    assertEquals(1, ruleGroups.getLength(), packge + " checkstyle-metadata.xml must contain only one rule group");

    for (int position = 0; position < ruleGroups.getLength(); position++) {
      final Node ruleGroup = ruleGroups.item(position);
      final Set<Node> children = XmlUtil.getChildrenElements(ruleGroup);

      assertEquals(0, children.size(), packge + " checkstyle-metadata.xml must contain no rules");
    }
  }

  private static void validateEclipseCsMetaPropFile(File file, String packge,
          Set<Class<?>> packgeModules) throws Exception {
    assertTrue(file.exists(), "'checkstyle-metadata.properties' must exist in eclipsecs in inside " + packge);

    final Properties prop = new Properties();
    prop.load(new FileInputStream(file));

    final Set<Object> properties = new HashSet<>(Collections.list(prop.keys()));

    assertEquals(1, properties.size(),
            packge + " checkstyle-metadata.properties must contain only the rule group name");

    assertTrue(properties.iterator().next().toString().endsWith(".group"),
            packge + " checkstyle-metadata.properties must contain only the rule group name");
  }

  private static String getEclipseCsPath(String packageName, String fileName) throws IOException {
    return new File(
            "../net.sf.eclipsecs.checkstyle/metadata/" + packageName.replace(".", "/") + fileName)
                    .getCanonicalPath();
  }
}
