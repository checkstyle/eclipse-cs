//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
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

package com.atlassw.tools.eclipse.checkstyle.config;

//=================================================
// Imports from java namespace
//=================================================
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.ModuleFactory;
import com.puppycrawl.tools.checkstyle.PackageNamesLoader;
import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Used to convert from a standard Checkstyle config file to
 *  the plug-in's internal CheckConfiguration object.
 */
public class CheckConfigConverter
{
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Static class variables.
    //=================================================

    private static final String CHECKER_TAG = "Checker";

    private static final String TREE_WALKER_TAG = "TreeWalker";

    private static final String CORR_TAG_ROOT = "com.atlassw.property.to.resolve.";

    private static final SeverityLevel DEFAULT_SEVERITY = SeverityLevel.WARNING;

    //=================================================
    // Instance member variables.
    //=================================================

    private String mFilePath;

    private Configuration mChecker = null;

    private Configuration mTreeWalker = null;

    private HashMap mCheckerProps = new HashMap();

    private HashMap mTreeWalkerProps = new HashMap();

    /**  Used to hold property values that need to be resolved. */
    private List mPropsToResolve = new LinkedList();

    /**  Sequence number for resolved properties. */
    private int mPropSequence = 0;

    /**  Hash map of resolved property values, keyed by tempory value. */
    private HashMap mResolvedProps = new HashMap();

    //=================================================
    // Constructors & finalizer.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    /**
     *  Load a standard Checkstyle configuration file.
     * 
     *  @param  filePath  Path to the Checkstyle config file.
     * 
     *  @throws CheckstylePluginException  Error loading the file.
     */
    public void loadConfig(String filePath) throws CheckstylePluginException
    {
        mFilePath = filePath;

        //
        //  Use the Checkstyle configuration loader to load the configuration.
        //
        try
        {
            mChecker = ConfigurationLoader.loadConfiguration(filePath, new CSPropertyResolver());
            if (!mChecker.getName().equals(CHECKER_TAG))
            {
                String message = "Root configuration node is not \"Checker\"";
                CheckstyleLog.warning(message);
                throw new CheckstylePluginException(message);
            }
        }
        catch (CheckstyleException e)
        {
            String message = "Error loading check configuration";
            CheckstyleLog.warning(message, e);
            throw new CheckstylePluginException(message + e.getMessage());
        }

        //
        //  Now move down the heirarchy from Checker to TreeWalker.
        //
        Configuration[] children = mChecker.getChildren();
        for (int i = 0; i < children.length; i++)
        {
            if (children[i].getName().equals(TREE_WALKER_TAG))
            {
                mTreeWalker = children[i];
                break;
            }
        }
        if (mTreeWalker == null)
        {
            String message = "Failed to find config node \"TreeWalker\"";
            CheckstyleLog.warning(message);
            throw new CheckstylePluginException(message);
        }

        //
        //  Get the properties from Checker and TreeWalker.
        //
        try
        {
            String[] names = mChecker.getAttributeNames();
            for (int i = 0; i < names.length; i++)
            {
                mCheckerProps.put(names[i], mChecker.getAttribute(names[i]));
            }

            names = mTreeWalker.getAttributeNames();
            for (int i = 0; i < names.length; i++)
            {
                mTreeWalkerProps.put(names[i], mTreeWalker.getAttribute(names[i]));
            }
        }
        catch (CheckstyleException e)
        {
            String message = "Error loading check configuration";
            CheckstyleLog.warning(message, e);
            throw new CheckstylePluginException(message + e.getMessage());
        }
    }

    /**
     * Get the list of properties that need to be resolved.
     * 
     * @return list of properties.
     */
    public List getPropsToResolve()
    {
        return mPropsToResolve;
    }

    /**
     *  Build a check configuration from the loaded information and
     *  the resolved properties.
     * 
     *  @return  A <code>CheckConfiguration</code>
     * 
     *  @throws CheckstylePluginException  Error during processing.
     */
    public CheckConfiguration getCheckConfiguration() throws CheckstylePluginException
    {
        //
        //  First, build the map of resolved property values.
        //
        buildResolvedHashMap();

        //
        //  Update the Checker and TreeWalker properties with any resolved values.
        //
        resolveProps(mCheckerProps);
        resolveProps(mTreeWalkerProps);

        //
        //  Build the list of rules.
        //
        List rules = buildRules();

        //
        //  Build an initial configuration name from the file name.
        //
        File file = new File(mFilePath);
        String name = file.getName();

        //
        //  Build the CheckConfiguration.
        //
        CheckConfiguration config = new CheckConfiguration();
        config.setRuleConfigs(rules);
        config.setName(name);

        return config;
    }

    /**
     *  Build a hash map of resolved property values.
     */
    private void buildResolvedHashMap()
    {
        Iterator iter = mPropsToResolve.iterator();
        while (iter.hasNext())
        {
            ResolvableProperty prop = (ResolvableProperty)iter.next();
            mResolvedProps.put(prop.getCorrelationTag(), prop.getValue());
        }
    }

    /**
     *  Update a hash map of property values with resolved values.
     */
    private void resolveProps(HashMap map)
    {
        Iterator iter = map.keySet().iterator();
        while (iter.hasNext())
        {
            String key = (String)iter.next();
            String value = (String)map.get(key);

            //
            //  See if the value contains one or more
            //  correlation tags in the resolved values HashMap.
            //
            Iterator tagIter = mResolvedProps.entrySet().iterator();
            while (tagIter.hasNext())
            {
                Map.Entry tagEntry = (Map.Entry)tagIter.next();
                String tagName = (String)tagEntry.getKey();
                String tagValue = (String)tagEntry.getValue();
                if (value.indexOf(tagName) != -1)
                {
                    value = value.replaceAll(tagName, tagValue);
                }
            }
            
            //
            //  Update the target map with the new value.
            //
            map.put(key, value);
        }
    }

    private List buildRules() throws CheckstylePluginException
    {
        List rules = new LinkedList();

        //
        //  Each module below TreeWalker should be a check rule.
        //
        ModuleFactory moduleFactory;
        try
        {
            moduleFactory =
                PackageNamesLoader.loadModuleFactory(mTreeWalker.getClass().getClassLoader());
        }
        catch (CheckstyleException e)
        {
            String message = "Failed to load module factory";
            CheckstyleLog.warning(message);
            throw new CheckstylePluginException(message);
        }

        Configuration[] children = mTreeWalker.getChildren();
        for (int i = 0; i < children.length; i++)
        {
            RuleConfiguration rule = buildRuleConfig(children[i], moduleFactory);
            if (rule != null)
            {
                rules.add(rule);
            }
        }

        return rules;
    }

    private RuleConfiguration buildRuleConfig(Configuration module, ModuleFactory moduleFactory)
        throws CheckstylePluginException
    {
        String ruleClassname = null;
        try
        {
            Object obj = moduleFactory.createModule(module.getName());
            ruleClassname = obj.getClass().getName();
        }
        catch (CheckstyleException e)
        {
            String message = "Unable to load class for rule " + module.getName();
            CheckstyleLog.warning(message, e);
            throw new CheckstylePluginException(message + e.getMessage());
        }

        //
        //  Build a hash map of all the properties.
        //
        HashMap ruleProps = new HashMap();
        String[] propNames = module.getAttributeNames();
        for (int i = 0; i < propNames.length; i++)
        {
            try
            {
                ruleProps.put(propNames[i], module.getAttribute(propNames[i]));
            }
            catch (CheckstyleException e1)
            {
                CheckstyleLog.warning("Failed to find module attribute " + propNames[i], e1);
            }
        }

        //
        //  Resolve any variable values in the rule's properties.
        //
        resolveProps(ruleProps);

        //
        //  Get the rule's severity level.
        //
        SeverityLevel severity = getSeverityLevel(ruleProps);

        //
        //  Check the rule's metadata to see if any of the TreeWalker or Checker
        //  properties need to be propogated to the rule itself.
        //
        checkRuleMetadata(ruleClassname, ruleProps);

        RuleConfiguration result = new RuleConfiguration(ruleClassname);
        result.setSeverityLevel(severity);
        result.setConfigProperties(buildConfigProperties(ruleProps));
        return result;
    }

    private SeverityLevel getSeverityLevel(HashMap ruleProps)
    {
        //
        //  See if a severity was specified in the rule's properties.  If not, check
        //  the TreeWalker and Checker properties and then set to a default value if
        //  not found.
        //
        String severity = (String)ruleProps.get(XMLTags.SEVERITY_TAG);
        ruleProps.remove(XMLTags.SEVERITY_TAG);
        if (severity == null)
        {
            severity = (String)mTreeWalkerProps.get(XMLTags.SEVERITY_TAG);
            if (severity == null)
            {
                severity = (String)mCheckerProps.get(XMLTags.SEVERITY_TAG);
            }
        }

        SeverityLevel result = DEFAULT_SEVERITY;
        if (severity != null)
        {
            try
            {
                result = SeverityLevel.getInstance(severity);
            }
            catch (Exception e)
            {
                //
                //  Use a default value.
                //
                result = DEFAULT_SEVERITY;
            }
        }
        return result;
    }

    /**
     * Check to see if any of the rule's property values need to be propagated from
     * collection of global properties.
     * 
     * @param ruleClassname  The rule's classname
     * @param ruleProps      The rule's properties
     */
    private void checkRuleMetadata(String ruleClassname, HashMap ruleProps)
    {
        RuleMetadata metadata = MetadataFactory.getRuleMetadata(ruleClassname);
        if (metadata == null)
        {
            //
            //  No metadata is available for this rule.
            //
            return;
        }

        //
        //  Iterate through the config property metadata.
        //
        Iterator iter = metadata.getConfigItemMetadata().iterator();
        while (iter.hasNext())
        {
            ConfigPropertyMetadata propMetadata = (ConfigPropertyMetadata)iter.next();
            String name = propMetadata.getName();
            String value = (String)ruleProps.get(name);
            if (value == null)
            {
                //
                //  No value was specified at the rule level, see if a value was
                //  specified at the TreeWalker level.
                //
                value = (String)mTreeWalkerProps.get(name);
                if (value != null)
                {
                    ruleProps.put(name, value);
                }
                else
                {
                    //
                    //  Try the Checker.
                    //
                    value = (String)mCheckerProps.get(name);
                    if (value != null)
                    {
                        ruleProps.put(name, value);
                    }
                }
            }
        }
    }

    /**
     *  Build a hash map of ConfigProperty objects.
     */
    private HashMap buildConfigProperties(HashMap inMap)
    {
        HashMap outMap = new HashMap();
        Iterator iter = inMap.keySet().iterator();
        while (iter.hasNext())
        {
            String name = (String)iter.next();
            String value = (String)inMap.get(name);

            outMap.put(name, new ConfigProperty(name, value));
        }

        return outMap;
    }

    private class CSPropertyResolver implements PropertyResolver
    {
        public String resolve(String varName)
        {
            //
            // Check to see if this variable has already been seen.
            //
            Iterator iter = mPropsToResolve.iterator();
            while (iter.hasNext())
            {
                ResolvableProperty prop = (ResolvableProperty)iter.next();
                if (varName.equals(prop.getPropertyName()))
                {
                    return prop.getCorrelationTag();
                }
            }
            
            //
            //  Record the variable that needs to be resolved and assign
            //  a tempory value that will be later replaced.
            //
            String corrTag = CORR_TAG_ROOT + Integer.toString(mPropSequence++);
            ResolvableProperty prop = new ResolvableProperty();
            prop.setPropertyName(varName);
            prop.setCorrelationTag(corrTag);
            mPropsToResolve.add(prop);
            return corrTag;
        }
    }
}
