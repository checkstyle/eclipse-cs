//============================================================================
//
// Copyright (C) 2002-2003  David Schneider
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

package com.atlassw.tools.eclipse.checkstyle.preferences;

//=================================================
// Imports from java namespace
//=================================================
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================

//=================================================
// Imports from org namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.config.ConfigPropertyMetadata;
import com.atlassw.tools.eclipse.checkstyle.config.RuleConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.RuleMetadata;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigProperty;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

/**
 *  This class is the working of a rule configuration while the rule is
 *  being configured.
 */
public class RuleConfigWorkingCopy implements Cloneable
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
    
    private RuleMetadata        mMetadata;
    
    private RuleConfiguration   mRuleConfig;

	//=================================================
	// Constructors & finalizer.
	//=================================================
    
    RuleConfigWorkingCopy(RuleMetadata metadata, RuleConfiguration ruleConfig)
    {
        mMetadata = metadata;
        
        if (ruleConfig == null)
        {
        	//
        	//  No existing rule configuration was given so create a default one.
        	//
            mRuleConfig = buildDefaultRuleConfig(metadata);
        }
        else
        {
        	mRuleConfig = ruleConfig;
        }
    }

	//=================================================
	// Methods.
	//=================================================
    
    public RuleConfiguration getRuleConfig()
    {
        return mRuleConfig;
    }
    
    public String getImplClassname()
    {
        return mMetadata.getCheckImplClassname();
    }
    
    public String getRuleName()
    {
        return mMetadata.getRuleName();
    }
    
    public String getRuleComment()
    {
        return mRuleConfig.getComment();
    }
    
    public void setRuleComment(String comment)
    {
        mRuleConfig.setComment(comment);
    }
    
    public SeverityLevel getSeverityLevel()
    {
        return mRuleConfig.getSeverityLevel();
    }
    
    public void setSeverityLevel(SeverityLevel severity)
    {
        mRuleConfig.setSeverityLevel(severity);
    }
    
    public String getRuleDescription()
    {
        return mMetadata.getDescription();
    }

    /**
     * Returns the configuration item metadata.
     * 
     * @return A list of <code>ConfigItemMetadata</code> objects.
     */
    public List getConfigItemMetadata()
    {
        return mMetadata.getConfigItemMetadata();
    }
    
    public ConfigProperty getConfigProperty(String name)
    {
        return mRuleConfig.getConfigProperty(name);
    }
    
    public void setConfigItems(HashMap items)
    {
        mRuleConfig.setConfigProperties(items);
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
    
    private RuleConfiguration buildDefaultRuleConfig(RuleMetadata metadata)
    {
        RuleConfiguration ruleConfig = 
            new RuleConfiguration(metadata.getCheckImplClassname());
        ruleConfig.setSeverityLevel(mMetadata.getDefaultSeverityLevel());
        
        //
        //  Set the default values of the configuration properties.
        //
        HashMap configProperties = new HashMap();
        Iterator iter = metadata.getConfigItemMetadata().iterator();
        while (iter.hasNext())
        {
            ConfigPropertyMetadata propMeta = (ConfigPropertyMetadata)iter.next();

            ConfigProperty prop = new ConfigProperty();            
            prop.setName(propMeta.getName());
            prop.setValue(propMeta.getDefaultValue());
            
            configProperties.put(prop.getName(), prop);
        }
        ruleConfig.setConfigProperties(configProperties);
        
        return ruleConfig;
    }
}
