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
import java.util.Arrays;
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
  /** Checkstyle metadata(old) file. */
  private static final String METADATA_FILE_NAME = " checkstyle-metadata.xml: ";

  /** Check suffix string. */
  private static final String CHECK_SUFFIX = "Check";

  /**
   * From Checkstyle 8.36 onwards metadata would be fetched directly from checkstyle and won't
   * be manually edited any more in org/sonar/plugins/checkstyle/rules.xml.
   * So, these are modules which were not updated in 8.36, and hence data is consistent with
   * the XML file.
   */
  private static final Set<String> PRE_CHECKSTYLE_8_36_MODULES =
            java.util.Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "com.puppycrawl.tools.checkstyle.checks.header.HeaderCheck",
        "com.puppycrawl.tools.checkstyle.checks.header.RegexpHeaderCheck",
        "com.puppycrawl.tools.checkstyle.checks.annotation.AnnotationUseStyleCheck",
        "com.puppycrawl.tools.checkstyle.checks.annotation.MissingDeprecatedCheck",
        "com.puppycrawl.tools.checkstyle.checks.annotation.MissingOverrideCheck",
        "com.puppycrawl.tools.checkstyle.checks.annotation.PackageAnnotationCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.EqualsAvoidNullCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.NoCloneCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.NoEnumTrailingCommaCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.NoFinalizerCheck",
        "com.puppycrawl.tools.checkstyle.checks.imports.AvoidStaticImportCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocPackageCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.MissingJavadocPackageCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.InvalidJavadocPositionCheck",
        "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpMultilineCheck",
        "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpOnFilenameCheck",
        "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck",
        "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineJavaCheck",
        "com.puppycrawl.tools.checkstyle.checks.sizes.OuterTypeNumberCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.FileTabCharacterCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.GenericWhitespaceCheck",
        "com.puppycrawl.tools.checkstyle.checks.imports.RedundantImportCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.AbstractClassNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.sizes.AnonInnerLengthCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.ArrayTrailingCommaCheck",
        "com.puppycrawl.tools.checkstyle.checks.ArrayTypeStyleCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.AvoidInlineConditionalsCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.AvoidNoArgumentSuperConstructorCallCheck",
        "com.puppycrawl.tools.checkstyle.checks.blocks.AvoidNestedBlocksCheck",
        "com.puppycrawl.tools.checkstyle.checks.imports.AvoidStarImportCheck",
        "com.puppycrawl.tools.checkstyle.checks.metrics.BooleanExpressionComplexityCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.CatchParameterNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.metrics.ClassDataAbstractionCouplingCheck",
        "com.puppycrawl.tools.checkstyle.checks.metrics.ClassFanOutComplexityCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.ConstantNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.CovariantEqualsCheck",
        "com.puppycrawl.tools.checkstyle.checks.metrics.CyclomaticComplexityCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.DeclarationOrderCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.DefaultComesLastCheck",
        "com.puppycrawl.tools.checkstyle.checks.blocks.EmptyBlockCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.EmptyForInitializerPadCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.EmptyForIteratorPadCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.EmptyStatementCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.EqualsHashCodeCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.ExplicitInitializationCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.FallThroughCheck",
        "com.puppycrawl.tools.checkstyle.checks.sizes.FileLengthCheck",
        "com.puppycrawl.tools.checkstyle.checks.design.FinalClassCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.FinalLocalVariableCheck",
        "com.puppycrawl.tools.checkstyle.checks.FinalParametersCheck",
        "com.puppycrawl.tools.checkstyle.checks.design.HideUtilityClassConstructorCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.IllegalCatchCheck",
        "com.puppycrawl.tools.checkstyle.checks.imports.IllegalImportCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.IllegalInstantiationCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.IllegalThrowsCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.IllegalTokenCheck",
        "com.puppycrawl.tools.checkstyle.checks.imports.ImportOrderCheck",
        "com.puppycrawl.tools.checkstyle.checks.indentation.IndentationCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.InnerAssignmentCheck",
        "com.puppycrawl.tools.checkstyle.checks.design.InterfaceIsTypeCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocMissingWhitespaceAfterAsteriskCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocBlockTagLocationCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocContentLocationCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocVariableCheck",
        "com.puppycrawl.tools.checkstyle.checks.sizes.LineLengthCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.LocalFinalVariableNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.LocalVariableNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.MagicNumberCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.MemberNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.MethodNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.MissingCtorCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.MissingSwitchDefaultCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.ModifiedControlVariableCheck",
        "com.puppycrawl.tools.checkstyle.checks.modifier.ModifierOrderCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.MultipleStringLiteralsCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.MultipleVariableDeclarationsCheck",
        "com.puppycrawl.tools.checkstyle.checks.design.MutableExceptionCheck",
        "com.puppycrawl.tools.checkstyle.checks.metrics.NPathComplexityCheck",
        "com.puppycrawl.tools.checkstyle.checks.blocks.NeedBracesCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.NestedIfDepthCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.NestedTryDepthCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.NoArrayTrailingCommaCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.AvoidDoubleBraceInitializationCheck",
        "com.puppycrawl.tools.checkstyle.checks.NewlineAtEndOfFileCheck",
        "com.puppycrawl.tools.checkstyle.checks.NoCodeInFileCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.NoWhitespaceAfterCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.NoWhitespaceBeforeCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.OperatorWrapCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.PackageDeclarationCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.PackageNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.ParameterAssignmentCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.ParameterNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.LambdaParameterNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.sizes.ParameterNumberCheck",
        "com.puppycrawl.tools.checkstyle.checks.modifier.RedundantModifierCheck",
        "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.RequireThisCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.ReturnCountCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanReturnCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.StaticVariableNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.StringLiteralEqualityCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.SuperCloneCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.SuperFinalizeCheck",
        "com.puppycrawl.tools.checkstyle.checks.design.ThrowsCountCheck",
        "com.puppycrawl.tools.checkstyle.checks.TodoCommentCheck",
        "com.puppycrawl.tools.checkstyle.checks.TrailingCommentCheck",
        "com.puppycrawl.tools.checkstyle.checks.TranslationCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.SingleSpaceSeparatorCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.TypecastParenPadCheck",
        "com.puppycrawl.tools.checkstyle.checks.UncommentedMainCheck",
        "com.puppycrawl.tools.checkstyle.checks.imports.UnusedImportsCheck",
        "com.puppycrawl.tools.checkstyle.checks.UpperEllCheck",
        "com.puppycrawl.tools.checkstyle.checks.design.VisibilityModifierCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.WhitespaceAfterCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.WhitespaceAroundCheck",
        "com.puppycrawl.tools.checkstyle.checks.design.InnerTypeLastCheck",
        "com.puppycrawl.tools.checkstyle.checks.OuterTypeFilenameCheck",
        "com.puppycrawl.tools.checkstyle.checks.OrderedPropertiesCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.NestedForDepthCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.OneStatementPerLineCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.ClassTypeParameterNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.MethodTypeParameterNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.UniquePropertiesCheck",
        "com.puppycrawl.tools.checkstyle.checks.AvoidEscapedUnicodeCharactersCheck",
        "com.puppycrawl.tools.checkstyle.checks.imports.CustomImportOrderCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.InterfaceTypeParameterNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.design.OneTopLevelClassCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.OverloadMethodsDeclarationOrderCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.VariableDeclarationUsageDistanceCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.UnnecessarySemicolonInTryWithResourcesCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.UnnecessarySemicolonInEnumerationCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.AtclauseOrderCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.NonEmptyAtclauseDescriptionCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocParagraphCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocTagContinuationIndentationCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.SingleLineJavadocCheck",
        "com.puppycrawl.tools.checkstyle.checks.blocks.EmptyCatchBlockCheck",
        "com.puppycrawl.tools.checkstyle.checks.imports.ImportControlCheck",
        "com.puppycrawl.tools.checkstyle.checks.indentation.CommentsIndentationCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.SeparatorWrapCheck",
        "com.puppycrawl.tools.checkstyle.filefilters.BeforeExecutionExclusionFileFilter"
        )));
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
      // Check only those modules which have not been updated in checkstyle 8.36,
      // since they are not manually updated in checkstyle-metadata.xml files.
      if (PRE_CHECKSTYLE_8_36_MODULES.contains(module.getCanonicalName())) {
        fail("Module not found in " + packge + METADATA_FILE_NAME
                + module.getCanonicalName());
      }
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
      String moduleName = internalName;
      if (!internalName.endsWith("Filter")) {
        moduleName += CHECK_SUFFIX;
      }

      // Check only those modules which have not been updated in checkstyle 8.36,
      // since they are not manually updated in checkstyle-metadata.xml files.
      if (!PRE_CHECKSTYLE_8_36_MODULES.contains(packge + "." + moduleName)) {
        continue;
      }
      final Class<?> module = findModule(packgeModules, classpath);
      packgeModules.remove(module);

      assertNotNull(module, 
              "Unknown class found in " + packge + METADATA_FILE_NAME + internalName);

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
      // Check only those modules which have not been updated in checkstyle 8.36,
      // since they are not manually updated in checkstyle-metadata.xml files.
      if (!PRE_CHECKSTYLE_8_36_MODULES.contains(module.getCanonicalName())) {
        continue;
      }
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
      final String moduleName = property.toString()
              .substring(0, property.toString().lastIndexOf('.'));
      // ignore group names
      if (!property.toString().endsWith(".group")
              // Check only those modules which have not been updated in Checkstyle 8.36,
              // since they are not manually  updated in checkstyle-metadata.xml files.
              && PRE_CHECKSTYLE_8_36_MODULES.contains(packge + "." + moduleName)) {
        fail("Unknown property found in eclipsecs properties " + packge + ": " + property);
      }
    }
  }

  private static Class<?> findModule(Set<Class<?>> modules, String classPath) {
    Class<?> result = null;

    for (Class<?> module : modules) {
      final String moduleName = module.getCanonicalName();

      if (moduleName.equals(classPath) || (classPath + CHECK_SUFFIX).equals(moduleName)) {
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
