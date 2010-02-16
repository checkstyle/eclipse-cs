package org.maven.ide.eclipse.checkstyle;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.maven.ide.eclipse.project.configurator.AbstractProjectConfigurator;

/**
 * Specialization of AbstractProjectConfigurator for mavenPlugin related tasks.
 * 
 * @author <a href="mailto:nicolas@apache.org">Nicolas De loof</a>
 */
public abstract class AbstractMavenPluginProjectConfigurator
    extends AbstractProjectConfigurator
{

    /**
     * Retrieve a plugin parameter resource as URL by trying various load strategies
     * 
     * @param location the resource path
     * @param pluginClassLoader the plugin classloader ("realm")
     * @return the resource as URL, or null if not found
     */
    protected URL locatePluginResource( String location, ClassLoader pluginClassLoader )
    {
        // Try filesystem
        File file = new File( location );
        if ( file.exists() )
        {
            try
            {
                return file.toURL();
            }
            catch ( MalformedURLException e )
            {
                // ??
            }
        }

        // try a url
        try
        {
            URL url = new URL( location );
            url.openStream();
        }
        catch ( MalformedURLException e )
        {
            // Not a valid URL
        }
        catch ( Exception e )
        {
            // Valid URL but does not exist
        }

        // as classloader resource
        URL url = pluginClassLoader.getResource( location );
        if ( url == null )
        {
            console.logError( "Failed to locate Checkstyle configuration " + location );
        }
        else
        {
            String str = url.toString();
            if ( str.startsWith( "jar:file:/" ) && str.charAt( 10 ) != '/' )
            {
                try
                {
                    url = new URL( str.substring( 0, 9 ) + "//localhost" + str.substring( 9 ) );
                }
                catch ( MalformedURLException e )
                {
                    // ??
                }
            }
        }

        return url;
    }

    /**
     * Create a classloader based on the plugin artifact and dependencies, if any
     * 
     * @param mavenProject the maven project that declares the plugin
     * @param maven maven maven to resolve maven artifacts and dependencies
     * @param plugin the maven plugin
     */
    protected ClassLoader getPluginClassLoader( Plugin mavenPlugin, MavenProject mavenProject,
        IProgressMonitor monitor )
        throws CoreException
    {
        // Let's default to the current context classloader
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // list of jars that will make up classpath
        List<URL> jars = new LinkedList<URL>();

        // add the plugin artifact
        Artifact pluginArtifact = resolvePluginArtifact( mavenPlugin, mavenProject, monitor );

        if ( !pluginArtifact.isResolved() )
        {
            console.logMessage( "Failed to resolve maven plugin " + mavenPlugin.getKey() );
            // May not be a blocker : in many case, the classloader is used to retrieve
            // additional resources, not the plugin artifact itself
        }
        else
        {
            try
            {
                jars.add( pluginArtifact.getFile().toURI().toURL() );
            }
            catch ( MalformedURLException e )
            {
                console.logError( "Could not create URL for artifact: " + pluginArtifact.getFile() );
            }
        }

        List<Dependency> dependencies = mavenPlugin.getDependencies();
        if ( dependencies != null && dependencies.size() > 0 )
        {
            for ( Dependency dependency : dependencies )
            {
                // create artifact based on dependency
                Artifact artifact =
                    maven.resolve( dependency.getGroupId(), dependency.getArtifactId(),
                                      dependency.getVersion(), dependency.getScope(), 
                                      dependency.getType(), 
                                      mavenProject.getRemoteArtifactRepositories(),
                                      monitor );

                // add artifact and its dependencies to list of jars
                if ( artifact.isResolved() )
                {
                    try
                    {
                        // Use classpath ordering to mimic dependency overrides
                        jars.add( 0, artifact.getFile().toURI().toURL() );
                    }
                    catch ( MalformedURLException e )
                    {
                        console.logError( "Could not create URL for artifact: " + artifact.getFile() );
                    }
                }
            }
        }

        classLoader = new URLClassLoader( jars.toArray( new URL[0] ), classLoader );

        return classLoader;
    }

    private Artifact resolvePluginArtifact( Plugin mavenPlugin, MavenProject mavenProject,
        IProgressMonitor monitor )
        throws CoreException
    {
        String groupId = mavenPlugin.getGroupId();
        String artifactId = mavenPlugin.getArtifactId();
        String version = mavenPlugin.getVersion();
        if ( version == null )
        {
            version = Artifact.LATEST_VERSION;
        }
        return maven.resolve( groupId, artifactId, version, "compile", "maven-plugin",
            mavenProject.getRemoteArtifactRepositories(), monitor );

    }

    /**
     * Retrieve a configuration paramter from Maven plugin configuration
     * 
     * @param String the considered maven plugin
     * @param parameter the plugin configuration parameter name
     * @return the configured value, or null if not found
     */
    protected String extractMavenConfiguration( Plugin plugin, String parameter )
    {
        Object configuration = plugin.getConfiguration();
        if ( configuration instanceof Xpp3Dom )
        {
            Xpp3Dom configDom = (Xpp3Dom) configuration;
            Xpp3Dom parameterValue = configDom.getChild( parameter );
            if ( parameterValue != null )
            {
                return parameterValue.getValue();
            }
        }
        return null;
    }

}