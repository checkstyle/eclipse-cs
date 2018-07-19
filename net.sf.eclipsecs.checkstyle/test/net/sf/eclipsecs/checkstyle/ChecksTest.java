package net.sf.eclipsecs.checkstyle;

import static java.nio.charset.StandardCharsets.UTF_8;

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

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ChecksTest {
  @Test
  public void testMetadataFiles() throws Exception {
    final Set<Class<?>> modules = CheckUtil.getCheckstyleModules();
    final Set<String> packages = CheckUtil.getPackages(modules);

    Assert.assertTrue("no modules", modules.size() > 0);

    for (String p : packages) {
      Assert.assertTrue("folder " + p + " must exist in eclipsecs",
              new File(getEclipseCsPath(p, "")).exists());

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
    Assert.assertTrue("'checkstyle-metadata.xml' must exist in eclipsecs in inside " + packge,
            file.exists());

    final String input = new String(Files.readAllBytes(file.toPath()), UTF_8);
    final Document document = XmlUtil.getRawXml(file.getAbsolutePath(), input, input);

    final NodeList ruleGroups = document.getElementsByTagName("rule-group-metadata");

    Assert.assertEquals(packge + " checkstyle-metadata.xml must contain only one rule group", 1,
            ruleGroups.getLength());

    for (int position = 0; position < ruleGroups.getLength(); position++) {
      final Node ruleGroup = ruleGroups.item(position);
      final Set<Node> children = XmlUtil.getChildrenElements(ruleGroup);

      validateEclipseCsMetaXmlFileRules(packge, packgeModules, children);
    }

    for (Class<?> module : packgeModules) {
      Assert.fail("Module not found in " + packge + " checkstyle-metadata.xml: "
              + module.getCanonicalName());
    }
  }

  private static void validateEclipseCsMetaXmlFileRules(String packge, Set<Class<?>> packgeModules,
          Set<Node> rules) throws Exception {
    for (Node rule : rules) {
      final NamedNodeMap attributes = rule.getAttributes();
      final Node internalNameNode = attributes.getNamedItem("internal-name");

      Assert.assertNotNull(packge + " checkstyle-metadata.xml must contain an internal name",
              internalNameNode);

      final String internalName = internalNameNode.getTextContent();
      final String classpath = packge + "." + internalName;

      final Class<?> module = findModule(packgeModules, classpath);
      packgeModules.remove(module);

      Assert.assertNotNull(
              "Unknown class found in " + packge + " checkstyle-metadata.xml: " + internalName,
              module);

      final Node nameAttribute = attributes.getNamedItem("name");

      Assert.assertNotNull(packge + " checkstyle-metadata.xml requires a name for " + internalName,
              nameAttribute);
      Assert.assertEquals(
              packge + " checkstyle-metadata.xml requires a valid name for " + internalName,
              "%" + internalName + ".name", nameAttribute.getTextContent());

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

      Assert.assertNotNull(
              packge + " checkstyle-metadata.xml requires a parent for " + internalName,
              parentAttribute);
      Assert.assertEquals(
              packge + " checkstyle-metadata.xml requires a valid parent for " + internalName,
              parentValue, parentAttribute.getTextContent());

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

          Assert.assertNotNull(packge
                  + " checkstyle-metadata.xml must contain an internal name for " + moduleName,
                  internalNameNode);

          final String internalName = internalNameNode.getTextContent();

          Assert.assertEquals(packge
                  + " checkstyle-metadata.xml requires a valid internal-name for " + moduleName,
                  module.getName(), internalName);
          break;
        case "description":
          Assert.assertEquals(packge + " checkstyle-metadata.xml requires a valid description for "
                  + moduleName, "%" + moduleName + ".desc", child.getTextContent());
          break;
        case "property-metadata":
          final String propertyName = attributes.getNamedItem("name").getTextContent();

          Assert.assertTrue(packge + " checkstyle-metadata.xml has an unknown parameter for "
                  + moduleName + ": " + propertyName, properties.remove(propertyName));

          validateEclipseCsMetaXmlFileRuleProperty(packge, module, moduleName, propertyName, child);
          break;
        case "message-key":
          final String key = attributes.getNamedItem("key").getTextContent();

          Assert.assertTrue(packge + " checkstyle-metadata.xml has an unknown message for "
                  + moduleName + ": " + key, messages.remove(key));
          break;
        case "quickfix":
          final String className = attributes.getNamedItem("classname").getTextContent();

          Assert.assertEquals(packge
                  + " checkstyle-metadata.xml should have a valid quickfix class for " + moduleName,
                  quickfixClass, className);

          quickfixClass = null;
          break;
        default:
          Assert.fail(packge + " checkstyle-metadata.xml unknown node for " + moduleName + ": "
                  + child.getNodeName());
          break;
      }
    }

    Assert.assertNull(packge + " checkstyle-metadata.xml missing quickfix for " + moduleName + ": "
            + quickfixClass, quickfixClass);

    for (String property : properties) {
      Assert.fail(packge + " checkstyle-metadata.xml missing parameter for " + moduleName + ": "
              + property);
    }

    for (String message : messages) {
      Assert.fail(packge + " checkstyle-metadata.xml missing message for " + moduleName + ": "
              + message);
    }
  }

  private static void validateEclipseCsMetaXmlFileRuleProperty(String packge, Class<?> module,
          String moduleName, String propertyName, Node propertyNode) throws Exception {
    final Node firstChild = propertyNode.getFirstChild().getNextSibling();

    Assert.assertNotNull(packge + " checkstyle-metadata.xml requires atleast one child for "
            + moduleName + ", " + propertyName, firstChild);
    Assert.assertEquals(
            packge + " checkstyle-metadata.xml should have a description for the "
                    + "first child of " + moduleName + ", " + propertyName,
            "description", firstChild.getNodeName());
    Assert.assertEquals(
            packge + " checkstyle-metadata.xml requires a valid description for " + moduleName
                    + ", " + propertyName,
            "%" + moduleName + "." + propertyName, firstChild.getTextContent());

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
      Assert.assertNull(packge + " checkstyle-metadata.xml should not have a default value for "
              + moduleName + ", " + propertyName, defaultValueNode);
    } else {
      Assert.assertNotNull(packge + " checkstyle-metadata.xml requires a default value for "
              + moduleName + ", " + propertyName, defaultValueNode);

      Assert.assertEquals(packge + " checkstyle-metadata.xml requires a valid default value for "
              + moduleName + ", " + propertyName, defaultText, defaultValueNode.getTextContent());
    }

    final Node enumerationChild = propertyNode.getFirstChild().getNextSibling().getNextSibling()
            .getNextSibling();

    if (acceptableText == null) {
      Assert.assertNull(
              packge + " checkstyle-metadata.xml should not have an enumeration child for "
                      + moduleName + ", " + propertyName,
              enumerationChild);
    } else {
      Assert.assertNotNull(packge + " checkstyle-metadata.xml requires an enumeration child for "
              + moduleName + ", " + propertyName, enumerationChild);
      Assert.assertEquals(
              packge + " checkstyle-metadata.xml should have a enumeration for the " + " child of "
                      + moduleName + ", " + propertyName,
              "enumeration", enumerationChild.getNodeName());

      if ("TokenTypes".equals(acceptableText)) {
        // TODO
      } else {
        final Set<String> options = new HashSet<>();
        Collections.addAll(options, acceptableText.split(","));

        for (Node child : XmlUtil.getChildrenElements(enumerationChild)) {
          switch (child.getNodeName()) {
            case "property-value-option":
              final String value = child.getAttributes().getNamedItem("value").getTextContent();

              Assert.assertTrue(
                      packge + " checkstyle-metadata.xml has an unknown acceptable token for "
                              + moduleName + ", " + propertyName + ": " + value,
                      options.remove(value));
              break;
            default:
              Assert.fail(packge + " checkstyle-metadata.xml unknown node for " + moduleName + ", "
                      + propertyName + ": " + child.getNodeName());
              break;
          }
        }

        for (String option : options) {
          Assert.fail(packge + " checkstyle-metadata.xml missing acceptable token for " + moduleName
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
    Assert.assertTrue(
            "'checkstyle-metadata.properties' must exist in eclipsecs in inside " + packge,
            file.exists());

    final Properties prop = new Properties();
    prop.load(new FileInputStream(file));

    final Set<Object> properties = new HashSet<>(Collections.list(prop.keys()));

    for (Class<?> module : packgeModules) {
      final String moduleName = CheckUtil.getSimpleCheckstyleModuleName(module);

      Assert.assertTrue(moduleName + " requires a name in eclipsecs properties " + packge,
              properties.remove(moduleName + ".name"));
      Assert.assertTrue(moduleName + " requires a desc in eclipsecs properties " + packge,
              properties.remove(moduleName + ".desc"));

      final Set<String> moduleProperties = CheckUtil.getCheckProperties(module);

      for (String moduleProperty : moduleProperties) {
        Assert.assertTrue(
                moduleName + " requires the property " + moduleProperty
                        + " in eclipsecs properties " + packge,
                properties.remove(moduleName + "." + moduleProperty));
      }
    }

    for (Object property : properties) {
      // ignore group names
      if (!property.toString().endsWith(".group")) {
        Assert.fail("Unknown property found in eclipsecs properties " + packge + ": " + property);
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
