//
// Created      : 22-Nov-2003, 16:20:47
// Description  : 
// Author       : NIKOLAY
// Copyright    : (c) Teamphone.com Ltd. 2003 - All Rights Reserved
//
package com.atlassw.tools.eclipse.checkstyle.config;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import junit.framework.TestCase;

/**
 * @author NIKOLAY
 */
public class TestMetadataFactory extends TestCase
{
   private static final String CHECKSTYLE_CHECKS_PACKAGE = "com.puppycrawl.tools.checkstyle.checks";
   private static final List KNOWN_PROBLEMATIC_CHECKS = Arrays.asList(new String[] {
                CHECKSTYLE_CHECKS_PACKAGE + ".duplicates.StrictDuplicateCodeCheck",
                CHECKSTYLE_CHECKS_PACKAGE + ".HeaderCheck",
                CHECKSTYLE_CHECKS_PACKAGE + ".RegexpHeaderCheck",
                CHECKSTYLE_CHECKS_PACKAGE + ".javadoc.PackageHtmlCheck",
                CHECKSTYLE_CHECKS_PACKAGE + ".NewlineAtEndOfFileCheck",
                CHECKSTYLE_CHECKS_PACKAGE + ".TranslationCheck",
   });
   public void testAllChecksAreIncluded() throws ClassNotFoundException, IOException
   {
      final List includedClasses = new ArrayList();
      final Iterator it = MetadataFactory.getRuleGroupMetadata().iterator();
      while (it.hasNext())
      {
         RuleGroupMetadata ruleGroup = (RuleGroupMetadata) it.next();
         final Iterator it2 = ruleGroup.getRuleMetadata().iterator();
         while (it2.hasNext())
         {
            final RuleMetadata rule = (RuleMetadata) it2.next();
            final String checkImplClassname = rule.getCheckImplClassname();
            includedClasses.add(checkImplClassname);
            try
            {
               Class.forName(checkImplClassname);
            }
            catch (ClassNotFoundException e)
            {
               fail("class " + checkImplClassname + " doesn't exist");
            }
            
         }
      }
      JarFile jar = new JarFile("checkstyle-all-3.2.jar");
      final Enumeration entries = jar.entries();
      while (entries.hasMoreElements())
      {
         JarEntry entry = (JarEntry) entries.nextElement();
         
         final String name = entry.getName();
         if(name.startsWith(CHECKSTYLE_CHECKS_PACKAGE.replace('.', '/')) && name.endsWith("Check.class") && -1 == name.indexOf('$'))
         {
            final String className = name.substring(0, name.lastIndexOf('.')).replace('/', '.');
            final Class checkClass = Class.forName(className);
            if(!Modifier.isAbstract(checkClass.getModifiers()))
            {
               final String existingClassName = checkClass.getName();
               if(!KNOWN_PROBLEMATIC_CHECKS.contains(existingClassName))
               {   
                  assertTrue("<" + existingClassName + "> Is not included in your xml config file", includedClasses.contains(existingClassName));
               }
            }
         }
         
      }
      
   }
}
