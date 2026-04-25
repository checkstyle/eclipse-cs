//============================================================================
//
// Copyright (C) 2003-2023  Lukas Frena
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

package net.sf.eclipsecs.core.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

/**
 * The Class for transforming the formatter-settings to Checkstyle-rules. A new checkstyle-xml file
 * gets generated.
 *
 */
public class FormatterTransformer {

  private final Map<String, Class<? extends FTransformationClass>> allTransformers;

  /**
   * Creates a new instance of class CheckstyleTransformer.
   *
   * @throws CheckstylePluginException
   *           if an unexpected internal exception occurred
   */
  public FormatterTransformer() {
    this.allTransformers = discoverTransformers();
  }

  /**
   * Method for starting transforming. Converts all formatter-settings to checkstyle-rules.
   *
   * @param path
   *          The path where the checkstyle-xml file gets generated.
   * @param formatterSettings
   * @throws CheckstylePluginException
   */
  // CheckstyleFileWriter acts on its own parameters
  @SuppressWarnings("unused")
  public void transformRules(final String path, Map<String, String> formatterSettings)
          throws CheckstylePluginException {
    CheckstyleSetting checkstyleSetting = new CheckstyleSetting();
    loadTransformationClasses(formatterSettings).stream()
        .map(FTransformationClass::transformRule)
        .forEach(checkstyleSetting::addSetting);
    new CheckstyleFileWriter(checkstyleSetting, path);
  }

  /**
   * Loads all transformationclasses that are needed to recognize the formatter-settings. A instance
   * of every loaded class is stored in the field transformationClasses. Gets called by the
   * constructor.
   * @return
   */
  private List<FTransformationClass> loadTransformationClasses(Map<String, String> formatterSettings)
          throws CheckstylePluginException {
    List<FTransformationClass> targetTransformers = new ArrayList<>();
    for (Map.Entry<String, String> entry : formatterSettings.entrySet()) {
      String rule = entry.getKey();
      String value = entry.getValue();

      Class<? extends FTransformationClass> transformationClass = allTransformers.get(rule);

      if (transformationClass != null) {
        try {
          final FTransformationClass transObj = transformationClass.getDeclaredConstructor()
                  .newInstance();

          transObj.setValue(value);

          targetTransformers.add(transObj);

        } catch (final ReflectiveOperationException ex) {
          CheckstylePluginException.rethrow(ex);
        }
      }
    }
    return targetTransformers;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Class<? extends FTransformationClass>> discoverTransformers() {
    String formatterKeyAnnotation = FormatterKey.class.getName();
    ClassLoader loader = CheckstylePlugin.getDefault().getAddonExtensionClassLoader();
    try (ScanResult scanResult = new ClassGraph()
            .addClassLoader(loader)
            .enableAnnotationInfo()
            .scan()) {
      return scanResult.getClassesWithAnnotation(formatterKeyAnnotation).stream()
              .collect(Collectors.toUnmodifiableMap(
                      classInfo -> "org.eclipse.jdt.core.formatter."
                              + classInfo.getAnnotationInfo(formatterKeyAnnotation)
                                      .getParameterValues().getValue("value"),
                      classInfo -> (Class<? extends FTransformationClass>) classInfo.loadClass()));
    }
  }
}
