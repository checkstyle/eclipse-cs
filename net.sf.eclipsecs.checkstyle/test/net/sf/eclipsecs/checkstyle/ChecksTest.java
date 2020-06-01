package net.sf.eclipsecs.checkstyle;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.utils.ModuleReflectionUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import net.sf.eclipsecs.checkstyle.utils.CheckUtil;
import net.sf.eclipsecs.checkstyle.utils.XmlUtil;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ChecksTest {
  @Test
  public void testMetadataFiles() throws Exception {
    final Set<Class<?>> modules = CheckUtil.getCheckstyleModules();
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

      validateEclipseCsMetaXmlFileRules(packge, packgeModules, children);
    }

    for (Class<?> module : packgeModules) {
      fail("Module not found in " + packge + " checkstyle-metadata.xml: "
              + module.getCanonicalName());
    }
  }

  private static void validateEclipseCsMetaXmlFileRules(String packge, Set<Class<?>> packgeModules,
          Set<Node> rules) throws Exception {
    for (Node rule : rules) {
      final NamedNodeMap attributes = rule.getAttributes();
      final Node internalNameNode = attributes.getNamedItem("internal-name");

      assertNotNull(internalNameNode, packge + " checkstyle-metadata.xml must contain an internal name");

      final String internalName = internalNameNode.getTextContent();
      final String classpath = packge + "." + internalName;

      final Class<?> module = findModule(packgeModules, classpath);
      packgeModules.remove(module);

      assertNotNull(module, 
              "Unknown class found in " + packge + " checkstyle-metadata.xml: " + internalName);

      final Node nameAttribute = attributes.getNamedItem("name");

      assertNotNull(nameAttribute, packge + " checkstyle-metadata.xml requires a name for " + internalName);
      assertEquals("%" + internalName + ".name", nameAttribute.getTextContent(), 
              packge + " checkstyle-metadata.xml requires a valid name for " + internalName);

      final Node parentAttribute = attributes.getNamedItem("parent");
      final String parentValue;

      if (ModuleReflectionUtil.isCheckstyleTreeWalkerCheck(module)
              || ModuleReflectionUtil.isTreeWalkerFilterModule(module)) {
        parentValue = "TreeWalker";
      } else if (ModuleReflectionUtil.isRootModule(module)) {
        parentValue = "Root";
      } else {
        parentValue = "Checker";
      }

      assertNotNull(parentAttribute, 
              packge + " checkstyle-metadata.xml requires a parent for " + internalName);
      assertEquals(parentValue, parentAttribute.getTextContent(), 
              packge + " checkstyle-metadata.xml requires a valid parent for " + internalName);

      final Set<Node> children = XmlUtil.getChildrenElements(rule);

      validateEclipseCsMetaXmlFileRule(packge, module, children);
    }
  }

  private static void validateEclipseCsMetaXmlFileRule(String packge, Class<?> module,
          Set<Node> children) throws Exception {
    final String moduleName = CheckUtil.getSimpleCheckstyleModuleName(module);
    String quickfixClass = getQuickfixClass(module);
    final Set<String> properties = CheckUtil.getCheckProperties(module);
    final Set<Field> fieldMessages = CheckUtil.getCheckMessages(module);
    final Set<String> messages = new TreeSet<>();

    for (Field fieldMessage : fieldMessages) {
      // below is required for package/private classes
      if (!fieldMessage.isAccessible()) {
        fieldMessage.setAccessible(true);
      }

      messages.add(fieldMessage.get(null).toString());
    }

    for (Node child : children) {
      final NamedNodeMap attributes = child.getAttributes();

      switch (child.getNodeName()) {
        case "alternative-name":
          final Node internalNameNode = attributes.getNamedItem("internal-name");

          assertNotNull(internalNameNode, packge
                  + " checkstyle-metadata.xml must contain an internal name for " + moduleName);

          final String internalName = internalNameNode.getTextContent();

          assertEquals(module.getName(), internalName, packge
                  + " checkstyle-metadata.xml requires a valid internal-name for " + moduleName);
          break;
        case "description":
          assertEquals("%" + moduleName + ".desc", child.getTextContent(), packge + " checkstyle-metadata.xml requires a valid description for "
                  + moduleName);
          break;
        case "property-metadata":
          final String propertyName = attributes.getNamedItem("name").getTextContent();

          assertTrue(properties.remove(propertyName), packge + " checkstyle-metadata.xml has an unknown parameter for "
                  + moduleName + ": " + propertyName);

          validateEclipseCsMetaXmlFileRuleProperty(packge, module, moduleName, propertyName, child);
          break;
        case "message-key":
          final String key = attributes.getNamedItem("key").getTextContent();

          assertTrue(messages.remove(key), packge + " checkstyle-metadata.xml has an unknown message for "
                  + moduleName + ": " + key);
          break;
        case "quickfix":
          final String className = attributes.getNamedItem("classname").getTextContent();

          assertEquals(quickfixClass, className, packge
                  + " checkstyle-metadata.xml should have a valid quickfix class for " + moduleName);

          quickfixClass = null;
          break;
        default:
          fail(packge + " checkstyle-metadata.xml unknown node for " + moduleName + ": "
                  + child.getNodeName());
          break;
      }
    }

    assertNull(quickfixClass, packge + " checkstyle-metadata.xml missing quickfix for " + moduleName + ": "
            + quickfixClass);

    for (String property : properties) {
      fail(packge + " checkstyle-metadata.xml missing parameter for " + moduleName + ": "
              + property);
    }

    for (String message : messages) {
      fail(packge + " checkstyle-metadata.xml missing message for " + moduleName + ": "
              + message);
    }
  }

  private static void validateEclipseCsMetaXmlFileRuleProperty(String packge, Class<?> module,
          String moduleName, String propertyName, Node propertyNode) throws Exception {
    final Node firstChild = propertyNode.getFirstChild().getNextSibling();

    assertNotNull(firstChild, packge + " checkstyle-metadata.xml requires atleast one child for "
            + moduleName + ", " + propertyName);
    assertEquals("description", firstChild.getNodeName(), 
            packge + " checkstyle-metadata.xml should have a description for the "
                    + "first child of " + moduleName + ", " + propertyName);
    assertEquals("%" + moduleName + "." + propertyName, firstChild.getTextContent(), 
            packge + " checkstyle-metadata.xml requires a valid description for " + moduleName
            + ", " + propertyName);

    if ("tokens".equals(propertyName)) {
      validateEclipseCsMetaXmlFileRuleTokens(packge, module, moduleName, propertyName,
              propertyNode);
    } else if ("javadocTokens".equals(propertyName)) {
      validateEclipseCsMetaXmlFileRuleJavadocTokens();
    }
    // TODO: other default values
  }

  private static void validateEclipseCsMetaXmlFileRuleTokens(String packge, Class<?> module,
          String moduleName, String propertyName, Node propertyNode) throws Exception {
    final AbstractCheck check = (AbstractCheck) module.newInstance();
    final String defaultText = CheckUtil.getTokenText(check.getDefaultTokens(),
            check.getRequiredTokens());
    final String acceptableText = CheckUtil.getTokenText(check.getAcceptableTokens(),
            check.getRequiredTokens());

    final Node defaultValueNode = propertyNode.getAttributes().getNamedItem("default-value");

    if (defaultText == null) {
      assertNull(defaultValueNode, packge + " checkstyle-metadata.xml should not have a default value for "
              + moduleName + ", " + propertyName);
    } else {
      assertNotNull(defaultValueNode, packge + " checkstyle-metadata.xml requires a default value for "
              + moduleName + ", " + propertyName);

      assertEquals(defaultText, defaultValueNode.getTextContent(), packge + " checkstyle-metadata.xml requires a valid default value for "
              + moduleName + ", " + propertyName);
    }

    final Node enumerationChild = propertyNode.getFirstChild().getNextSibling().getNextSibling()
            .getNextSibling();

    if (acceptableText == null) {
      assertNull(enumerationChild, 
              packge + " checkstyle-metadata.xml should not have an enumeration child for "
                      + moduleName + ", " + propertyName);
    } else {
      assertNotNull(enumerationChild, packge + " checkstyle-metadata.xml requires an enumeration child for "
              + moduleName + ", " + propertyName);
      assertEquals("enumeration", enumerationChild.getNodeName(), 
              packge + " checkstyle-metadata.xml should have a enumeration for the " + " child of "
                      + moduleName + ", " + propertyName);

      if ("TokenTypes".equals(acceptableText)) {
        // TODO
      } else {
        final Set<String> options = new HashSet<>();
        Collections.addAll(options, acceptableText.split(","));

        for (Node child : XmlUtil.getChildrenElements(enumerationChild)) {
          switch (child.getNodeName()) {
            case "property-value-option":
              final String value = child.getAttributes().getNamedItem("value").getTextContent();

              assertTrue(options.remove(value), 
                      packge + " checkstyle-metadata.xml has an unknown acceptable token for "
                              + moduleName + ", " + propertyName + ": " + value);
              break;
            default:
              fail(packge + " checkstyle-metadata.xml unknown node for " + moduleName + ", "
                      + propertyName + ": " + child.getNodeName());
              break;
          }
        }

        for (String option : options) {
          fail(packge + " checkstyle-metadata.xml missing acceptable token for " + moduleName
                  + ", " + propertyName + ": " + option);
        }
      }
    }
  }

  private static void validateEclipseCsMetaXmlFileRuleJavadocTokens() {
    // TODO Auto-generated method stub

  }

  private static String getQuickfixClass(Class<?> module) throws IOException {
    String clss = "net.sf.eclipsecs.ui.quickfixes"
            + module.getPackage().getName().replace("com.puppycrawl.tools.checkstyle.checks", "")
            + "." + CheckUtil.getSimpleCheckstyleModuleName(module) + "Quickfix";
    String location = getEclipseQuickfixPath(clss);

    if (new File(location).exists()) {
      return clss;
    }

    // eclipse-cs puts misc in different package than normal

    clss = "net.sf.eclipsecs.ui.quickfixes.misc." + CheckUtil.getSimpleCheckstyleModuleName(module)
            + "Quickfix";
    location = getEclipseQuickfixPath(clss);

    if (new File(location).exists()) {
      return clss;
    }

    return null;
  }

  private static void validateEclipseCsMetaPropFile(File file, String packge,
          Set<Class<?>> packgeModules) throws Exception {
    assertTrue(file.exists(), "'checkstyle-metadata.properties' must exist in eclipsecs in inside " + packge);

    final Properties prop = new Properties();
    prop.load(new FileInputStream(file));

    final Set<Object> properties = new HashSet<>(Collections.list(prop.keys()));

    for (Class<?> module : packgeModules) {
      final String moduleName = CheckUtil.getSimpleCheckstyleModuleName(module);

      assertTrue(properties.remove(moduleName + ".name"), moduleName + " requires a name in eclipsecs properties " + packge);
      assertTrue(properties.remove(moduleName + ".desc"), moduleName + " requires a desc in eclipsecs properties " + packge);

      final Set<String> moduleProperties = CheckUtil.getCheckProperties(module);

      for (String moduleProperty : moduleProperties) {
        assertTrue(properties.remove(moduleName + "." + moduleProperty), 
                moduleName + " requires the property " + moduleProperty
                + " in eclipsecs properties " + packge);
      }
    }

    for (Object property : properties) {
      // ignore group names
      if (!property.toString().endsWith(".group")) {
        fail("Unknown property found in eclipsecs properties " + packge + ": " + property);
      }
    }
  }

  private static Class<?> findModule(Set<Class<?>> modules, String classPath) {
    Class<?> result = null;

    for (Class<?> module : modules) {
      final String moduleName = module.getCanonicalName();

      if (moduleName.equals(classPath) || moduleName.equals(classPath + "Check")) {
        result = module;
        break;
      }
    }

    return result;
  }

  private static String getEclipseCsPath(String packageName, String fileName) throws IOException {
    return new File(
            "../net.sf.eclipsecs.checkstyle/metadata/" + packageName.replace(".", "/") + fileName)
                    .getCanonicalPath();
  }

  private static String getEclipseQuickfixPath(String classpath) throws IOException {
    return new File("../net.sf.eclipsecs.ui/src/" + classpath.replace(".", "/") + ".java")
            .getCanonicalPath();
  }
}
