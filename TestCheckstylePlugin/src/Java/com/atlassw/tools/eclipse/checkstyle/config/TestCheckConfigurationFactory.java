//
// Created      : 22-Nov-2003, 16:05:09
// Description  : 
// Author       : NIKOLAY
// Copyright    : (c) Teamphone.com Ltd. 2003 - All Rights Reserved
//
package com.atlassw.tools.eclipse.checkstyle.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * @author NIKOLAY
 */
public class TestCheckConfigurationFactory extends TestCase
{
   public void testUpdateFile() throws IOException
   {
      ClassLoader loader = CheckConfigurationFactory.class.getClassLoader();
      Properties classnameMap = new Properties();
      InputStream in = loader.getResourceAsStream("com/atlassw/tools/eclipse/checkstyle/config/classnames_v3.2.0_update.properties");
      classnameMap.load(in);
      final Iterator it = classnameMap.values().iterator();
      while (it.hasNext())
      {
         String newClassName = (String) it.next();
         try
         {
            Class.forName(newClassName); //this shouldn't throw
         }
         catch (ClassNotFoundException e)
         {
            fail(newClassName + " check doesn't exist in checkstyle");
         }
      }
   }
}
