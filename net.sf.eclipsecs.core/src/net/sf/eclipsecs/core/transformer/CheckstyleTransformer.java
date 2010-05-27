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

import com.puppycrawl.tools.checkstyle.api.Configuration;

/**
 * The Class for transforming the checkstyle-rules into
 * eclipse-formatter-settings. A new formatter-profile gets created.
 * 
 * @author Lukas Frena
 */
public class CheckstyleTransformer {
    /** An object containing all settings for the eclipse-formatter. */
    private final FormatterConfiguration mFormatterSetting = new FormatterConfiguration();

    /** The list of checkstyle-rules delivered in the constructor. */
    private final List<Configuration> mRules;

    /**
     * The list with all TransformationClass-instances loaded in method
     * loadTransformationClasses().
     */
    private final List<CTransformationClass> mTransformationClasses = new ArrayList<CTransformationClass>();

    /**
     * Creates a new instance of class CheckstyleTransformer.
     * 
     * @param ruleList
     *            A list of checkstyle-rules.
     */
    public CheckstyleTransformer(final List<Configuration> ruleList) {
        mRules = ruleList;

        final List<String> classnames = new ArrayList<String>();
        final Iterator<Configuration> it = mRules.iterator();
        Configuration help;
        while (it.hasNext()) {
            help = it.next();
            classnames
                .add("net.sf.eclipsecs.core.transformer.ctransformerclasses."
                    + help.getName() + "Transformer");
        }

        loadTransformationClasses(classnames);
    }

    /**
     * Loads all transformationclasses that are needed to recognize the
     * checkstyle-rules. A instance of every loaded class is stored in the field
     * transformationClasses. Gets called by the constructor.
     * 
     * @param classnames
     *            A list of names of which classes get loaded.
     */
    private void loadTransformationClasses(final List<String> classnames) {
        final Iterator<String> nameit = classnames.iterator();
        final Iterator<Configuration> ruleit = mRules.iterator();
        String name;
        Configuration rule;
        Class<?> transformationClass;
        while (nameit.hasNext() && ruleit.hasNext()) {
            name = nameit.next();
            rule = ruleit.next();
            try {
                transformationClass = Class.forName(name);
                final CTransformationClass transObj = (CTransformationClass) transformationClass
                    .newInstance();
                transObj.setRule(rule);
                mTransformationClasses.add(transObj);
                Logger.writeln("using " + name + " to transform rule \""
                    + rule.getName() + "\"");
            }
            catch (final ClassNotFoundException e) {
                Logger.writeln("no class for rule \"" + rule.getName() + "\"");
            }
            catch (final InstantiationException e) {
                Logger.writeln("unable to instantiate transformationclass: "
                    + e);
            }
            catch (final IllegalAccessException e) {
                Logger.writeln("illegal acces to transformationclass: " + e);
            }
        }
    }

    /**
     * Method for starting transforming. Converts all checkstyle-rules to a new
     * eclipse-formatter-profile.
     * 
     * @param path
     *            The path to the .settings folder with
     *            eclipse-configuration-files
     */
    public void transformRules() {
        loadRuleConfigurations();
        new FormatterConfigWriter(mFormatterSetting);
    }

    /**
     * Method which handles every single checkstyle-rule. For every rule it
     * calls the appropriate transformerclass. Gets called by transformRules().
     */
    private void loadRuleConfigurations() {
        FormatterConfiguration settings;
        final Iterator<CTransformationClass> it = mTransformationClasses
            .iterator();
        while (it.hasNext()) {
            settings = it.next().transformRule();
            mFormatterSetting.addConfiguration(settings);
        }
    }
}
