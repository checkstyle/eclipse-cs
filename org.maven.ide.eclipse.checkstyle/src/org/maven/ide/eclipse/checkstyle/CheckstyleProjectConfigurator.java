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
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringInputStream;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
    extends AbstractMavenPluginProjectConfigurator
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

            ClassLoader pluginClassLoader = getPluginClassLoader( mavenPlugin, mavenProject, embedder );
            URL ruleSet = extractConfiguredCSLocation( mavenPlugin, pluginClassLoader );
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

            Properties properties = extractCustomProperties( mavenPlugin, pluginClassLoader );
            properties.setProperty( "checkstyle.cache.file", "${project_loc}/checkstyle-cachefile" );

            List props = checkConfig.getResolvableProperties();
            props.clear();
            for ( Map.Entry entry : properties.entrySet() )
            {
                props.add( new ResolvableProperty( (String) entry.getKey(), (String) entry.getValue() ) );
            }

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
     * Extract the configured rulesets from the checkstyle configuration
     */
    private URL extractConfiguredCSLocation( Plugin plugin, ClassLoader pluginClassloader )
    {
        String configLocation = extractMavenConfiguration( plugin, "configLocation" );
        if ( configLocation == null )
        {
            configLocation = "config/sun_checks.xml";
        }
        return locatePluginResource( configLocation, pluginClassloader );
    }

    /**
     * Extract the configured properties from the checkstyle configuration
     *
     * @see http://maven.apache.org/plugins/maven-checkstyle-plugin/examples/custom-property-expansion.html
     */
    private Properties extractCustomProperties( Plugin plugin, ClassLoader pluginClassloader )
    {
        Properties properties = new Properties();
        String propertiesLocation = extractMavenConfiguration( plugin, "propertiesLocation" );
        if ( propertiesLocation != null )
        {
            URL url = locatePluginResource( propertiesLocation, pluginClassloader );
            if ( url == null )
            {
                console.logError( "Failed to resolve propertiesLocation " + propertiesLocation );
            }
            else
            {
                try
                {
                    properties.load( url.openStream() );
                }
                catch ( IOException e )
                {
                    console.logError( "Failed to load properties from " + propertiesLocation );
                }
            }
        }

        String propertyExpansion = extractMavenConfiguration( plugin, "propertyExpansion" );
        if ( propertyExpansion != null )
        {
            try
            {
                properties.load( new StringInputStream( propertyExpansion ) );
            }
            catch ( IOException e )
            {
                console.logError( "Failed to parser checkstyle propertyExpansion " + propertyExpansion );
            }
        }

        return properties;
    }

    /**
     * Extract the configured supression filters from the checkstyle configuration
     *
     * @see http://maven.apache.org/plugins/maven-checkstyle-plugin/examples/suppressions-filter.html
     */
    private URL extractSupressionFilter( Plugin plugin )
    {
        String supressionFilter = extractMavenConfiguration( plugin, "supressionFilter" );
        // TODO load as plugin classpath resource
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
