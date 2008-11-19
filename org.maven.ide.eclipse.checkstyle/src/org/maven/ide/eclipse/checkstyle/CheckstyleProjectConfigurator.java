//============================================================================
//
// Copyright (C) 2008  Nicolas De loof
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

package org.maven.ide.eclipse.checkstyle;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringInputStream;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.maven.ide.eclipse.project.configurator.AbstractProjectConfigurator;
import org.maven.ide.eclipse.project.configurator.ProjectConfigurationRequest;

import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationWorkingCopy;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfigurationWorkingSet;
import com.atlassw.tools.eclipse.checkstyle.config.ResolvableProperty;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.ConfigurationTypes;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.IConfigurationType;
import com.atlassw.tools.eclipse.checkstyle.nature.CheckstyleNature;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.FileMatchPattern;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.FileSet;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.IProjectConfiguration;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.ProjectConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.ProjectConfigurationWorkingCopy;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

/**
 * Configure eclipse-cs on maven project import, if the maven project has maven-checkstyle-project configured.
 * <p>
 * The configurator create (or update) a remote configuration that uses the configLocation set in maven as an URL. If
 * the configLocation points to a dependency resource, it will be resolved using the special <code>jar:!</code> URL
 * syntax.
 */
public class CheckstyleProjectConfigurator
    extends AbstractProjectConfigurator
{

    /** Checkstyle ruleset name to match maven's one */
    private static final String CONFIGURATION_NAME = "maven-chekstyle-plugin";

    /** checkstyle maven plugin groupId */
    private static final String CHECKSTYLE_PLUGIN_GROUPID = "org.apache.maven.plugins";

    /** checkstyle maven plugin artifactId */
    private static final String CHECKSTYLE_PLUGIN_ARTIFACTID = "maven-checkstyle-plugin";

    /** IConfigurationType for remote configuration */
    private IConfigurationType remoteConfigurationType = ConfigurationTypes.getByInternalName( "remote" );

    /**
     * {@inheritDoc}
     * 
     * @see org.maven.ide.eclipse.project.configurator.AbstractProjectConfigurator#configure(org.apache.maven.embedder.MavenEmbedder,
     *      org.maven.ide.eclipse.project.configurator.ProjectConfigurationRequest,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void configure( MavenEmbedder embedder, ProjectConfigurationRequest request, IProgressMonitor monitor )
        throws CoreException
    {
        monitor.beginTask( "Checkstyle configuration update", 2 );
        Plugin plugin = getCheckstylePlugin( request.getMavenProject() );
        if ( plugin != null )
        {
            createOrUpdateEclispeCS( embedder, request.getMavenProject(), plugin, request.getProject(), monitor );
            addNature( request.getProject(), CheckstyleNature.NATURE_ID, monitor );
        }
        monitor.done();
    }

    /**
     * Configure the eclipse Checkstyle plugin based on maven plugin configuration and resources
     */
    private void createOrUpdateEclispeCS( MavenEmbedder embedder, MavenProject mavenProject, Plugin mavenPlugin,
                                          IProject project, IProgressMonitor monitor )
        throws CoreException
    {
        try
        {
            IProjectConfiguration pc = ProjectConfigurationFactory.getConfiguration( project );
            ProjectConfigurationWorkingCopy workingCopy = new ProjectConfigurationWorkingCopy( pc );
            workingCopy.setUseSimpleConfig( false );

            URL ruleSet = getMavenCheckstyleRuleSet( embedder, mavenProject, mavenPlugin, project, monitor );
            if ( ruleSet == null )
            {
                return;
            }

            monitor.worked( 1 );
            console.logMessage( "Configure checkstyle from ruleSet " + ruleSet );
            ICheckConfiguration checkConfig = createOrUpdateLocalCheckConfiguration( project, workingCopy, ruleSet );
            if ( checkConfig == null )
            {
                return;
            }

            createOrUpdateFileSet( project, mavenProject, workingCopy, checkConfig );

            monitor.worked( 1 );
            if ( workingCopy.isDirty() )
            {
                workingCopy.store();
            }
        }
        catch ( CheckstylePluginException cpe )
        {
            embedder.getLogger().error( "Failed to configure Checkstyle plugin", cpe );
        }
    }

    /**
     * Retrieve the CheckStyle ruleset from mavenPlugin configuration
     */
    private URL getMavenCheckstyleRuleSet( MavenEmbedder embedder, MavenProject mavenProject, Plugin mavenPlugin,
                                           IProject project, IProgressMonitor monitor )
        throws CoreException
    {
        // get a classloader that takes into account plugin dependencies
        ClassLoader classLoader = configureClassLoader( embedder, mavenProject, mavenPlugin );

        String configLocation = extractConfiguredCSLocation( mavenPlugin );
        if ( configLocation == null )
        {
            configLocation = "config/sun_checks.xml";
        }
        URL url = locateRuleSet( classLoader, configLocation );
        return url;
    }

    /**
     * Retrieve a pre-existing LocalCheckConfiguration for maven to eclipse-cs integration, or create a new one
     */
    private ICheckConfiguration createOrUpdateLocalCheckConfiguration( IProject project,
                                                                       ProjectConfigurationWorkingCopy projectConfig,
                                                                       URL ruleSet )
        throws CheckstylePluginException
    {
        ICheckConfigurationWorkingSet workingSet = projectConfig.getLocalCheckConfigWorkingSet();

        CheckConfigurationWorkingCopy workingCopy = null;

        // Try to retrieve an existing checkstyle configuration to be updated
        CheckConfigurationWorkingCopy[] workingCopies = workingSet.getWorkingCopies();
        if ( workingCopies != null )
        {
            for ( CheckConfigurationWorkingCopy copy : workingCopies )
            {
                if ( CONFIGURATION_NAME.equals( copy.getName() ) )
                {
                    if ( remoteConfigurationType.equals( copy.getType() ) )
                    {
                        console.logMessage( "A local Checkstyle configuration allready exists with name "
                            + CONFIGURATION_NAME + ". It will be updated to maven plugin configuration" );
                        workingCopy = copy;
                        break;
                    }
                    else
                    {
                        console.logError( "A local Checkstyle configuration allready exists with name "
                            + CONFIGURATION_NAME + " with incompatible type" );
                        return null;
                    }
                }
            }
        }

        if ( workingCopy == null )
        {
            // Create a fresh check config
            workingCopy = workingSet.newWorkingCopy( remoteConfigurationType );
            workingCopy.setName( CONFIGURATION_NAME );
            workingSet.addCheckConfiguration( workingCopy );
        }

        workingCopy.setDescription( "Maven checkstyle configuration" );
        workingCopy.setLocation( ruleSet.toExternalForm() );
        // local.getAdditionalData().put(
        // RemoteConfigurationType.KEY_CACHE_CONFIG, true );

        // TODO extractCustomProperties( csPlugin );
        workingCopy.getResolvableProperties().add(
                                                   new ResolvableProperty( "checkstyle.cache.file",
                                                                           "${project_loc}/checkstyle-cachefile" ) );

        return workingCopy;
    }

    /**
     * Configure the Checkstyle FileSet to match the maven project compileSourceRoots
     */
    private void createOrUpdateFileSet( IProject project, MavenProject mavenProject,
                                        ProjectConfigurationWorkingCopy copy, ICheckConfiguration checkConfig )
        throws CheckstylePluginException
    {
        // clear any existing filesets
        copy.getFileSets().clear();

        FileSet fileSet = new FileSet( "java-sources", checkConfig );

        fileSet.setEnabled( true );

        List<FileMatchPattern> patterns = new ArrayList<FileMatchPattern>();

        URI projectURI = project.getLocationURI();

        List compileSourceRoots = mavenProject.getCompileSourceRoots();
        for ( Iterator iter = compileSourceRoots.iterator(); iter.hasNext(); )
        {
            String compileSourceRoot = (String) iter.next();

            File compileSourceRootFile = new File( compileSourceRoot );
            URI compileSourceRootURI = compileSourceRootFile.toURI();

            String relativePath = projectURI.relativize( compileSourceRootURI ).getPath();

            patterns.add( new FileMatchPattern( relativePath ) );
        }

        fileSet.setFileMatchPatterns( patterns );

        // add to copy filesets
        copy.getFileSets().add( fileSet );
    }

    /**
     * Retrieve a ruleset as URL by trying various load strategies
     */
    private URL locateRuleSet( ClassLoader classloader, String location )
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
        URL url = classloader.getResource( location );
        if ( url == null )
        {
            console.logError( "Failed to locate Checkstyle configuration " + location );
        }
        else
        {
            // @see bug #2284467 in eclipse-cs
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
     * @todo pull up to m2eclipse, and find a nicer way to resolve full plugin classpath
     */
    private ClassLoader configureClassLoader( MavenEmbedder embedder, MavenProject mavenProject, Plugin mavenPlugin )
    {
        // Let's default to the current context classloader
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // list of jars that will make up classpath
        List<URL> jars = new LinkedList<URL>();

        // add the checkstyle plugin artifact
        Artifact pluginArtifact = null;

        try
        {
            String version = mavenPlugin.getVersion();
            if ( version == null )
            {
                version = Artifact.LATEST_VERSION;
            }

            pluginArtifact =
                embedder.createArtifact( mavenPlugin.getGroupId(), mavenPlugin.getArtifactId(), version, "compile",
                                         "maven-plugin" );
        }
        catch ( Exception e )
        {
            embedder.getLogger().error( "Could not create classpath", e );
        }

        try
        {
            embedder.resolve( pluginArtifact, mavenProject.getRemoteArtifactRepositories(),
                              embedder.getLocalRepository() );
        }
        catch ( ArtifactResolutionException e )
        {
            embedder.getLogger().error( "Could not resolve artifact: " + pluginArtifact );
        }
        catch ( ArtifactNotFoundException e )
        {
            embedder.getLogger().error( "Could not find artifact: " + pluginArtifact );
        }

        if ( pluginArtifact.isResolved() )
        {
            try
            {
                jars.add( pluginArtifact.getFile().toURI().toURL() );
                // TODO What about it's dependencies ???
            }
            catch ( MalformedURLException e )
            {
                embedder.getLogger().error( "Could not create URL for artifact: " + pluginArtifact.getFile() );
            }
        }

        List dependencies = mavenPlugin.getDependencies();
        if ( dependencies != null && dependencies.size() > 0 )
        {
            for ( int i = 0; i < dependencies.size(); i++ )
            {
                Dependency dependency = (Dependency) dependencies.get( i );

                // create artifact based on dependency
                Artifact artifact =
                    embedder.createArtifact( dependency.getGroupId(), dependency.getArtifactId(),
                                             dependency.getVersion(), dependency.getScope(), dependency.getType() );

                // resolve artifact to repository
                try
                {
                    embedder.resolve( artifact, mavenProject.getRemoteArtifactRepositories(),
                                      embedder.getLocalRepository() );
                }
                catch ( ArtifactResolutionException e )
                {
                    embedder.getLogger().error( "Could not resolve artifact: " + artifact );
                }
                catch ( ArtifactNotFoundException e )
                {
                    embedder.getLogger().error( "Could not find artifact: " + artifact );
                }

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
                        embedder.getLogger().error( "Could not create URL for artifact: " + artifact.getFile() );
                    }
                }
            }
        }

        classLoader = new URLClassLoader( jars.toArray( new URL[0] ), classLoader );

        return classLoader;
    }

    /**
     * Extract the configured rulesets from the checkstyle configuration
     */
    private String extractConfiguredCSLocation( Plugin csPlugin )
    {
        Xpp3Dom[] rulesetDoms = null;

        Object configuration = csPlugin.getConfiguration();
        if ( configuration instanceof Xpp3Dom )
        {
            Xpp3Dom configDom = (Xpp3Dom) configuration;
            Xpp3Dom configLocationDom = configDom.getChild( "configLocation" );

            if ( configLocationDom != null )
            {
                return configLocationDom.getValue();
            }
        }

        return null;
    }

    /**
     * Extract the configured properties from the checkstyle configuration
     * 
     * @see http://maven.apache.org/plugins/maven-checkstyle-plugin/examples/custom-property-expansion.html
     */
    private Properties extractCustomProperties( Plugin csPlugin )
    {
        Xpp3Dom[] rulesetDoms = null;
        Properties properties = new Properties();

        Object configuration = csPlugin.getConfiguration();
        if ( configuration instanceof Xpp3Dom )
        {
            Xpp3Dom configDom = (Xpp3Dom) configuration;
            Xpp3Dom propertiesLocationDom = configDom.getChild( "propertiesLocation" );
            if ( propertiesLocationDom != null )
            {
                // TODO load resource from plugin classpath and return as properties
                propertiesLocationDom.getValue();
            }

            Xpp3Dom propertyExpansion = configDom.getChild( "propertyExpansion" );
            if ( propertyExpansion != null )
            {
                String keyValuePair = propertyExpansion.getValue();
                try
                {
                    properties.load( new StringInputStream( keyValuePair ) );
                }
                catch ( IOException e )
                {
                    console.logError( "Failed to parse checkstyle propertyExpansion as properties." );
                }
            }
        }

        return properties;
    }

    /**
     * Extract the configured supression filters from the checkstyle configuration
     * 
     * @see http://maven.apache.org/plugins/maven-checkstyle-plugin/examples/suppressions-filter.html
     */
    private URL extractSupressionFilter( Plugin csPlugin )
    {
        Object configuration = csPlugin.getConfiguration();
        if ( configuration instanceof Xpp3Dom )
        {
            Xpp3Dom configDom = (Xpp3Dom) configuration;
            Xpp3Dom suppressionsLocation = configDom.getChild( "suppressionsLocation" );
            if ( suppressionsLocation != null )
            {
                // TODO load resource from plugin classpath and return as URL
                suppressionsLocation.getValue();
            }
        }
        return null;
    }

    /**
     * Find (if exist) the maven-checkstyle-plugin configuration in the mavenProject
     */
    private Plugin getCheckstylePlugin( MavenProject mavenProject )
    {
        return mavenProject.getPlugin( CHECKSTYLE_PLUGIN_GROUPID + ":" + CHECKSTYLE_PLUGIN_ARTIFACTID );
        // List<Plugin> plugins = mavenProject.getBuildPlugins();
        // for ( Plugin plugin : plugins )
        // {
        // if ( CHECKSTYLE_PLUGIN_GROUPID.equals( plugin.getGroupId() )
        // && CHECKSTYLE_PLUGIN_ARTIFACTID.equals( plugin.getArtifactId() ) )
        // {
        // return plugin;
        // }
        // }
        // List<Plugin> reports = mavenProject.getReportPlugins();
        // for ( Plugin plugin : reports )
        // {
        // if ( CHECKSTYLE_PLUGIN_GROUPID.equals( plugin.getGroupId() )
        // && CHECKSTYLE_PLUGIN_ARTIFACTID.equals( plugin.getArtifactId() ) )
        // {
        // return plugin;
        // }
        // }
        // return null;
    }

}
