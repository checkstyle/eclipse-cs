//============================================================================
//
// Copyright (C) 2009 Lukas Frena
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
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package net.sf.eclipsecs.core.transformer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

/**
 * The Class for transforming the formatter-settings to Checkstyle-rules. A new checkstyle-xml file
 * gets generated.
 *
 * @author Lukas Frena
 */
public class FormatterTransformer {
  /** An object containing all settings for the eclipse-formatter. */
  private FormatterConfiguration mFormatterSetting = new FormatterConfiguration();

  /** An object containing all settings for the checkstyle-file. */
  private final CheckstyleSetting mCheckstyleSetting = new CheckstyleSetting();

  /**
   * The list with all TransformationClass-instances loaded in method loadTransformationClasses().
   */
  private final List<FTransformationClass> mTransformationClasses = new ArrayList<>();

  /**
   * Creates a new instance of class CheckstyleTransformer.
   *
   * @param rules
   *          A configuration of formatter-rules.
   * @throws CheckstylePluginException
   *           if an unexpected internal exception ocurred
   */
  public FormatterTransformer(final FormatterConfiguration rules) throws CheckstylePluginException {
    mFormatterSetting = rules;

    final List<String> classnames = new ArrayList<>();
    final Iterator<String> it = mFormatterSetting.getFormatterSettings().keySet().iterator();
    String help;
    String[] tokens;
    String className;
    while (it.hasNext()) {
      help = it.next();
      tokens = help.split("\\.");
      className = "net.sf.eclipsecs.core.transformer.ftransformerclasses.T";

      for (int i = 5; i < tokens.length; i++) {
        className = className + "_" + tokens[i];
      }
      classnames.add(className);
    }

    loadTransformationClasses(classnames);
  }

  /**
   * Loads all transformationclasses that are needed to recognize the formatter-settings. A instance
   * of every loaded class is stored in the field transformationClasses. Gets called by the
   * constructor.
   *
   * @param classnames
   *          A list of names of which classes get loaded.
   */
  private void loadTransformationClasses(final List<String> classnames)
          throws CheckstylePluginException {
    final Iterator<String> nameit = classnames.iterator();
    final Iterator<String> ruleit = mFormatterSetting.getFormatterSettings().keySet().iterator();
    String name;
    String rule;
    Class<?> transformationClass;
    while (nameit.hasNext() && ruleit.hasNext()) {
      name = nameit.next();
      rule = ruleit.next();
      try {
        transformationClass = CheckstylePlugin.getDefault().getAddonExtensionClassLoader()
                .loadClass(name);

        final FTransformationClass transObj = (FTransformationClass) transformationClass
                .newInstance();

        transObj.setValue(mFormatterSetting.getFormatterSettings().get(rule));

        mTransformationClasses.add(transObj);

      } catch (final ClassNotFoundException e) {
        // NOOP no appropriate transformer class present
      } catch (final InstantiationException e) {
        CheckstylePluginException.rethrow(e);
      } catch (final IllegalAccessException e) {
        CheckstylePluginException.rethrow(e);
      }
    }
  }

  /**
   * Method for starting transforming. Converts all formatter-settings to checkstyle-rules.
   *
   * @param path
   *          The path where the checkstyle-xml file gets generated.
   */
  public void transformRules(final String path) {
    loadRuleConfigurations();
    new CheckstyleFileWriter(mCheckstyleSetting, path);
  }

  /**
   * Method which handles every single formatter-setting. For every rule it calls the appropriate
   * transformerclass. Gets called by transformRules().
   */
  private void loadRuleConfigurations() {
    CheckstyleSetting settings;
    final Iterator<FTransformationClass> it = mTransformationClasses.iterator();
    while (it.hasNext()) {
      settings = it.next().transformRule();
      mCheckstyleSetting.addSetting(settings);
    }
  }
}
