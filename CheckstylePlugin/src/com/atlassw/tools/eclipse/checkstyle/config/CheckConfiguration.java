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
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;
import com.puppycrawl.tools.checkstyle.api.Configuration;

//=================================================
// Imports from org namespace
//=================================================
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents an audit configuration. An audit configuration consists of a
 * collection of audit rules and their configuration parameters.
 */
public class CheckConfiguration implements Cloneable, Configuration, Comparable
{
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Static class variables.
    //=================================================

    //=================================================
    // Instance member variables.
    //=================================================

    private String     mConfigName;

    private List       mRuleConfigs = new LinkedList();

    private TreeWalker mTreeWalker  = new TreeWalker();

    //=================================================
    // Constructors & finalizer.
    //=================================================

    /**
     * Default constructor.
     */
    CheckConfiguration()
    {}

    /**
     * Construct from a config file DOM node.
     * 
     * @param node An XML document node containing the
     *            <code>CheckConfiguration</code>'s data.
     * 
     * @throws CheckstylePluginException Could not properly construct the
     *             <code>CheckConfiguration</code> from the XML node.
     */
    public CheckConfiguration(Node node) throws CheckstylePluginException
    {
        mConfigName = XMLUtil.getNodeAttributeValue(node, XMLTags.NAME_TAG);

        NodeList children = node.getChildNodes();
        int count = children.getLength();

        for (int i = 0; i < count; i++)
        {
            Node child = children.item(i);

            if (child.getNodeName().equals(XMLTags.RULE_CONFIG_TAG))
            {
                RuleConfiguration rule = null;

                try
                {
                    rule = new RuleConfiguration(child);
                }
                catch (CheckstylePluginException e)
                {
                    rule = null;
                    CheckstyleLog.warning("Failed to create rule configuration, ignoring rule");
                }

                if (rule != null)
                {
                    mRuleConfigs.add(rule);
                }
            }
        }
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * Get the name.
     * 
     * @return The name.
     */
    public String getConfigName()
    {
        return mConfigName;
    }

    /**
     * Get the description of the configuration.
     * 
     * @return the description
     */
    public String getConfigDecription()
    {

        //TODO provide description of the config
        return "TODO: provide description";
    }

    /**
     * Set the name.
     * 
     * @param name The new name.
     */
    public void setName(String name)
    {
        mConfigName = name;
    }

    /**
     * Get the rule configurations.
     * 
     * @return A <code>HashMap</code> containing
     *         <code>RuleConfiguration</code> objects.
     */
    public List getRuleConfigs()
    {
        return mRuleConfigs;
    }

    /**
     * Set the rule configurations.
     * 
     * @param rules A <code>HashMap</code> containing
     *            <code>RuleConfiguration</code> objects.
     */
    public void setRuleConfigs(List rules)
    {
        mRuleConfigs = rules;
    }

    /**
     * Create a clone of the object.
     * 
     * @return The clone.
     * 
     * @throws CloneNotSupportedException Object can't be cloned.
     */
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    /**
     * Create an XML DOM node representation of the audit configuration.
     * 
     * @param doc The document to create the node within.
     * 
     * @return An XML node containing the <code>CheckConfiguration</code>'s
     *         data.
     */
    public Node toDOMNode(Document doc)
    {
        Element configNode = null;

        try
        {
            configNode = doc.createElement(XMLTags.CHECK_CONFIG_TAG);
            configNode.setAttribute(XMLTags.NAME_TAG, mConfigName);

            Collections.sort(mRuleConfigs);
            Iterator iter = mRuleConfigs.iterator();

            while (iter.hasNext())
            {
                RuleConfiguration rule = (RuleConfiguration) iter.next();
                Node ruleNode = rule.toDOMNode(doc);

                if (ruleNode == null)
                {
                    CheckstyleLog.warning("Audit rule lost");
                }
                else
                {
                    configNode.appendChild(ruleNode);
                }
            }
        }
        catch (DOMException e)
        {
            configNode = null;
            CheckstyleLog.warning("Failed to create XML DOM node for audit configuration", e);
        }

        return configNode;
    }

    /**
     * Get an attribute by name. Required by the <code>Configuration</code>
     * interface. A <code>CheckConfiguration</code> does not have any
     * attributes so <code>null</code> is always returned.
     * 
     * @param name The attribute name.
     * 
     * @return <code>null</code> is always returned.
     */
    public String getAttribute(String name)
    {
        return null;
    }

    /**
     * Get the names of all attributes.
     * 
     * @return An array of names.
     */
    public String[] getAttributeNames()
    {
        return new String[0];
    }

    /**
     * Get the child configuration nodes.
     * 
     * @return An array of <code>Configuration</code> objects.
     */
    public Configuration[] getChildren()
    {
        Configuration[] result = new Configuration[1];

        result[0] = mTreeWalker;

        return result;
    }

    /**
     * Get the name of this <code>Configuration</code>.
     * 
     * @return The name "Checker"
     */
    public String getName()
    {
        return "Checker";
    }

    /**
     * Compare to objects to determine greater then / less then.
     * 
     * @see java.lang.Comparable
     */
    public int compareTo(Object obj)
    {
        int result = 0;

        if (obj instanceof CheckConfiguration)
        {
            String string1 = getConfigName();
            String string2 = ((CheckConfiguration) obj).getConfigName();

            result = string1.compareToIgnoreCase(string2);
        }

        return result;
    }

    /**
     * This class is used as a place holder for the checkstyle configuration's
     * TreeWalker module.
     */
    private class TreeWalker implements Configuration
    {
        public String getAttribute(String name)
        {
            return null;
        }

        public String[] getAttributeNames()
        {
            return new String[0];
        }

        public Configuration[] getChildren()
        {
            Configuration[] result = new Configuration[mRuleConfigs.size()];

            Collections.sort(mRuleConfigs);
            result = (Configuration[]) mRuleConfigs.toArray(result);

            return result;
        }

        public String getName()
        {
            return "TreeWalker";
        }
    }
}