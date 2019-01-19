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

import com.puppycrawl.tools.checkstyle.api.Configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

import org.eclipse.core.resources.IProject;

/**
 * The Class for transforming the checkstyle-rules into eclipse-formatter-settings. A new
 * formatter-profile gets created.
 *
 * @author Lukas Frena
 */
public class CheckstyleTransformer {
  /** An object containing all settings for the eclipse-formatter. */
  private final FormatterConfiguration mFormatterSetting = new FormatterConfiguration();

  /** The list of checkstyle-rules delivered in the constructor. */
  private final List<Configuration> mRules;

  private IProject mProject;

  /**
   * The list with all TransformationClass-instances loaded in method loadTransformationClasses().
   */
  private final List<CTransformationClass> mTransformationClasses = new ArrayList<>();

  /**
   * Creates a new instance of class CheckstyleTransformer.
   *
   * @param project
   *          the project the transformer is operating on
   * @param ruleList
   *          A list of checkstyle-rules.
   * @throws CheckstylePluginException
   *           if an unexpected internal exception occurred
   */
  public CheckstyleTransformer(IProject project, final List<Configuration> ruleList)
          throws CheckstylePluginException {
    mProject = project;
    mRules = ruleList;

    final List<String> classnames = new ArrayList<>();
    final Iterator<Configuration> it = mRules.iterator();

    while (it.hasNext()) {
      Configuration item = it.next();
      classnames.add("net.sf.eclipsecs.core.transformer.ctransformerclasses." + item.getName()
              + "Transformer");
    }

    loadTransformationClasses(classnames);
  }

  /**
   * Loads all transformationclasses that are needed to recognize the checkstyle-rules. A instance
   * of every loaded class is stored in the field transformationClasses. Gets called by the
   * constructor.
   *
   * @param classnames
   *          A list of names of which classes get loaded.
   */
  private void loadTransformationClasses(final List<String> classnames)
          throws CheckstylePluginException {
    final Iterator<String> nameit = classnames.iterator();
    final Iterator<Configuration> ruleit = mRules.iterator();
    String name;
    Configuration rule;
    Class<?> transformationClass;
    while (nameit.hasNext() && ruleit.hasNext()) {
      name = nameit.next();
      rule = ruleit.next();
      try {
        transformationClass = CheckstylePlugin.getDefault().getAddonExtensionClassLoader()
                .loadClass(name);
        final CTransformationClass transObj = (CTransformationClass) transformationClass
                .newInstance();
        transObj.setRule(rule);
        mTransformationClasses.add(transObj);
        // Logger.writeln("using " + name + " to transform rule \""
        // + rule.getName() + "\"");
      } catch (final ClassNotFoundException e) {
        // NOOP there is just no appropriate transformer class
      } catch (final InstantiationException e) {
        CheckstylePluginException.rethrow(e);
      } catch (final IllegalAccessException e) {
        CheckstylePluginException.rethrow(e);
      }
    }
  }

  /**
   * Method for starting transforming. Converts all checkstyle-rules to a new
   * eclipse-formatter-profile.
   */
  public void transformRules() {
    loadRuleConfigurations();
    new FormatterConfigWriter(mProject, mFormatterSetting);
  }

  /**
   * Method which handles every single checkstyle-rule. For every rule it calls the appropriate
   * transformerclass. Gets called by transformRules().
   */
  private void loadRuleConfigurations() {
    FormatterConfiguration settings;
    final Iterator<CTransformationClass> it = mTransformationClasses.iterator();
    while (it.hasNext()) {
      settings = it.next().transformRule();
      mFormatterSetting.addConfiguration(settings);
    }
  }
}
