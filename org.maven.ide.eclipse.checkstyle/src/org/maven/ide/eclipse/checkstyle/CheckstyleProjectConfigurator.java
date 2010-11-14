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

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.config.ICheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.config.ResolvableProperty;
import net.sf.eclipsecs.core.config.configtypes.ConfigurationTypes;
import net.sf.eclipsecs.core.config.configtypes.IConfigurationType;
import net.sf.eclipsecs.core.nature.CheckstyleNature;
import net.sf.eclipsecs.core.projectconfig.FileMatchPattern;
import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.core.projectconfig.IProjectConfiguration;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationFactory;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationWorkingCopy;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

import org.apache.maven.artifact.Artifact;
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

/**
 * @author <a href="nicolas@apache.org">Nicolas De loof</a>
 * @author <a href="Peter.Hayes@fmr.com">Peter Hayes</a>
 */
public class CheckstyleProjectConfigurator
    extends AbstractProjectConfigurator
{

    /**
     *
     */
    private static final String CONFIGURATION_NAME = "maven-chekstyle-plugin";

    private static final String CHECKSTYLE_PLUGIN_GROUPID = "org.apache.maven.plugins";

    private static final String CHECKSTYLE_PLUGIN_ARTIFACTID = "maven-checkstyle-plugin";

    private static final String SITE_PLUGIN_GROUPID = "org.apache.maven.plugins";

    private static final String SITE_PLUGIN_ARTIFACTID = "maven-site-plugin";

    private final IConfigurationType remoteConfigurationType = ConfigurationTypes.getByInternalName( "remote" );

    /**
     * {@inheritDoc}
     * 
     * @see org.maven.ide.eclipse.project.configurator.AbstractProjectConfigurator#configure(org.apache.maven.embedder.MavenEmbedder,
     *      org.maven.ide.eclipse.project.configurator.ProjectConfigurationRequest,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void configure( final ProjectConfigurationRequest request, final IProgressMonitor monitor )
        throws CoreException
    {
        final MavenProject mavenProject = request.getMavenProject();
        final Plugin plugin = getCheckstylePlugin( mavenProject );
        final IProject project = request.getProject();
        if( plugin != null )
        {
            createCheckstyleConfiguration( mavenProject, plugin.getGroupId(), plugin.getArtifactId(),
                plugin.getVersion(), plugin.getDependencies(), plugin.getConfiguration(), project,
                monitor );
            addNature( project, CheckstyleNature.NATURE_ID, monitor );
        }
        else
        {
            getCheckstyleReportPlugin( project, mavenProject, monitor );
        }
    }

    /**
     * Setup the eclipse Checkstyle plugin based on maven plugin configuration and resources
     * 
     * @throws CheckstylePluginException
     */
    private void createCheckstyleConfiguration( final MavenProject mavenProject, final String groupId,
        final String artifactId, final String version, final List dependencies, final Object configuration, 
        final IProject project, final IProgressMonitor monitor )
        throws CoreException
    {
        try
        {
            final IProjectConfiguration projectConfig = ProjectConfigurationFactory.getConfiguration( project );
            final ProjectConfigurationWorkingCopy copy = new ProjectConfigurationWorkingCopy( projectConfig );
            copy.setUseSimpleConfig( false );

            final URL ruleSet =
                createOrUpdateMavenCheckstyle( mavenProject, groupId, artifactId, version, dependencies,
                    configuration, project, monitor );
            if( ruleSet == null )
            {
                return;
            }

            console.logMessage( "Configure checkstyle from ruleSet " + ruleSet );
            final ICheckConfiguration checkConfig = createOrUpdateLocalCheckConfiguration( project, copy, ruleSet );
            if( checkConfig == null )
            {
                return;
            }

            createOrUpdateFileSet( project, mavenProject, copy, checkConfig );

            if( copy.isDirty() )
            {
                copy.store();
            }
        }
        catch( final CheckstylePluginException cpe )
        {
            console.logError( "Failed to configure Checkstyle plugin" + cpe );
        }
    }

    /* Return true if contents are modified, or new */
    private URL createOrUpdateMavenCheckstyle( final MavenProject mavenProject, final String groupId,
        final String artifactId, final String version, final List dependencies, final Object configuration, 
        final IProject project, final IProgressMonitor monitor )
        throws CoreException
    {

        // get a classloader that takes into account plugin dependencies
        final ClassLoader classLoader =
            configureClassLoader( mavenProject, groupId, artifactId, version, dependencies, monitor );

        String configLocation = extractConfiguredCSLocation( configuration );
        if( configLocation == null )
        {
            configLocation = "config/sun_checks.xml";
        }
        final URL url = locateRuleSet( classLoader, configLocation );
        return url;
    }

    private ICheckConfiguration createOrUpdateLocalCheckConfiguration( final IProject project,
        final ProjectConfigurationWorkingCopy projectConfig, final URL ruleSet )
        throws CheckstylePluginException
    {
        final ICheckConfigurationWorkingSet checkConfig = projectConfig.getLocalCheckConfigWorkingSet();

        CheckConfigurationWorkingCopy workingCopy;
        final ICheckConfiguration existing = projectConfig.getLocalCheckConfigByName( CONFIGURATION_NAME );
        if( existing != null )
        {
            if( remoteConfigurationType.equals( existing.getType() ) )
            {
                console.logMessage( "A local Checkstyle configuration allready exists with name " + CONFIGURATION_NAME
                    + ". It will be updated to maven plugin configuration" );
                workingCopy = new CheckConfigurationWorkingCopy( existing, checkConfig );
            }
            else
            {
                console.logError( "A local Checkstyle configuration allready exists with name " + CONFIGURATION_NAME
                    + " with incompatible type" );
                return null;
            }
        }
        else
        {
            workingCopy = new CheckConfigurationWorkingCopy( remoteConfigurationType, checkConfig, false );
        }

        workingCopy.setName( CONFIGURATION_NAME );
        workingCopy.setDescription( "Maven checkstyle configuration" );
        workingCopy.setLocation( ruleSet.toExternalForm() );
        // local.getAdditionalData().put(
        // RemoteConfigurationType.KEY_CACHE_CONFIG, true );

        // TODO extractCustomProperties( csPlugin );
        workingCopy.getResolvableProperties().add(
            new ResolvableProperty( "checkstyle.cache.file", "${project_loc}/checkstyle-cachefile" ) );

        if( existing == null )
        {
            checkConfig.addCheckConfiguration( workingCopy );
        }

        return workingCopy;
    }

    private void createOrUpdateFileSet( final IProject project, final MavenProject mavenProject,
        final ProjectConfigurationWorkingCopy copy, final ICheckConfiguration checkConfig )
        throws CheckstylePluginException
    {
        // clear any existing filesets
        copy.getFileSets().clear();

        final FileSet fileSet = new FileSet( "java-sources", checkConfig );

        fileSet.setEnabled( true );

        final List< FileMatchPattern > patterns = new ArrayList< FileMatchPattern >();

        final URI projectURI = project.getLocationURI();

        final List compileSourceRoots = mavenProject.getCompileSourceRoots();
        for( final Iterator iter = compileSourceRoots.iterator(); iter.hasNext(); )
        {
            final String compileSourceRoot = (String)iter.next();

            final File compileSourceRootFile = new File( compileSourceRoot );
            final URI compileSourceRootURI = compileSourceRootFile.toURI();

            final String relativePath = projectURI.relativize( compileSourceRootURI ).getPath();

            patterns.add( new FileMatchPattern( relativePath ) );
        }

        fileSet.setFileMatchPatterns( patterns );

        // add to copy filesets
        copy.getFileSets().add( fileSet );
    }

    /*
     * Load a ruleset by trying various load strategies
     */
    private URL locateRuleSet( final ClassLoader classloader, final String location )
    {
        // Try filesystem
        final File file = new File( location );
        if( file.exists() )
        {
            try
            {
                return file.toURL();
            }
            catch( final MalformedURLException e )
            {
                // ??
            }
        }

        // try a url
        try
        {
            final URL url = new URL( location );
            url.openStream();
        }
        catch( final MalformedURLException e )
        {
            // Not a valid URL
        }
        catch( final Exception e )
        {
            // Valid URL but does not exist
        }

        // as classloader resource
        URL url = classloader.getResource( location );
        if( url == null )
        {
            console.logError( "Failed to locate Checkstyle configuration " + location );
        }
        else
        {
            // @see bug #2284467 in eclipse-cs
            final String str = url.toString();
            if( str.startsWith( "jar:file:/" ) && (str.charAt( 10 ) != '/') )
            {
                try
                {
                    url = new URL( str.substring( 0, 9 ) + "//localhost" + str.substring( 9 ) );
                }
                catch( final MalformedURLException e )
                {
                    // ??
                }
            }
        }

        return url;
    }

    /*
     * Create a classloader based on the PMD plugin dependencies, if any
     */
    private ClassLoader configureClassLoader( final MavenProject mavenProject, final String groupId,
        final String artifactId, String version, final List dependencies, final IProgressMonitor monitor )
    {
        // Let's default to the current context classloader
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // list of jars that will make up classpath
        final List< URL > jars = new LinkedList< URL >();

        // add the checkstyle plugin artifact
        Artifact pluginArtifact = null;

        if( version == null )
        {
            version = Artifact.LATEST_VERSION;
        }

        try
        {
            pluginArtifact = maven.resolve( groupId, artifactId, version, "jar", null,
                mavenProject.getRemoteArtifactRepositories(), monitor );
        }
        catch( final CoreException e1 )
        {
            console.logError( "Could not resolve artifact: " + groupId + ":" + artifactId );
        }

        if( (pluginArtifact != null) && pluginArtifact.isResolved() )
        {
            try
            {
                jars.add( pluginArtifact.getFile().toURI().toURL() );
                // TODO What about it's dependencies ???
            }
            catch( final MalformedURLException e )
            {
                console.logError( "Could not create URL for artifact: " + pluginArtifact.getFile() );
            }
        }

        if( (dependencies != null) && (dependencies.size() > 0) )
        {
            for( int i = 0; i < dependencies.size(); i++ )
            {
                final Dependency dependency = (Dependency)dependencies.get( i );

                // create artifact based on dependency
                Artifact artifact = null;
                try
                {
                    artifact = maven.resolve( dependency.getGroupId(), dependency.getArtifactId(),
                        dependency.getVersion(), dependency.getType(), dependency.getClassifier(),
                        mavenProject.getRemoteArtifactRepositories(), monitor );
                }
                catch( final CoreException e )
                {
                    console.logError( "Could not resolve artifact: " + groupId + ":" + artifactId );
                }

                // add artifact and its dependencies to list of jars
                if( (artifact != null) && artifact.isResolved() )
                {
                    try
                    {
                        // Use classpath ordering to mimic dependency overrides
                        jars.add( 0, artifact.getFile().toURI().toURL() );
                    }
                    catch( final MalformedURLException e )
                    {
                        console.logError( "Could not create URL for artifact: " + artifact.getFile() );
                    }
                }
            }
        }

        classLoader = new URLClassLoader( jars.toArray( new URL[ 0 ] ), classLoader );

        return classLoader;
    }

    /*
     * Extract the configured rulesets from the pmd configuration
     */
    private String extractConfiguredCSLocation( final Object configuration )
    {
        final Xpp3Dom[] rulesetDoms = null;

        if( configuration instanceof Xpp3Dom )
        {
            final Xpp3Dom configDom = (Xpp3Dom)configuration;
            final Xpp3Dom configLocationDom = configDom.getChild( "configLocation" );

            if( configLocationDom != null )
            {
                return configLocationDom.getValue();
            }
        }

        return null;
    }

    /**
     * @see http://maven.apache.org/plugins/maven-checkstyle-plugin/examples/custom-property-expansion.html
     */
    private Properties extractCustomProperties( final Plugin csPlugin )
    {
        final Xpp3Dom[] rulesetDoms = null;
        final Properties properties = new Properties();

        final Object configuration = csPlugin.getConfiguration();
        if( configuration instanceof Xpp3Dom )
        {
            final Xpp3Dom configDom = (Xpp3Dom)configuration;
            final Xpp3Dom propertiesLocationDom = configDom.getChild( "propertiesLocation" );
            if( propertiesLocationDom != null )
            {
                // TODO load resource from plugin classpath and return as properties
                propertiesLocationDom.getValue();
            }

            final Xpp3Dom propertyExpansion = configDom.getChild( "propertyExpansion" );
            if( propertyExpansion != null )
            {
                final String keyValuePair = propertyExpansion.getValue();
                try
                {
                    properties.load( new StringInputStream( keyValuePair ) );
                }
                catch( final IOException e )
                {
                    console.logError( "Failed to parse checkstyle propertyExpansion as properties." );
                }
            }
        }

        return properties;
    }

    /**
     * @see http://maven.apache.org/plugins/maven-checkstyle-plugin/examples/suppressions-filter.html
     */
    private URL extractSupressionFilter( final Plugin csPlugin )
    {
        final Object configuration = csPlugin.getConfiguration();
        if( configuration instanceof Xpp3Dom )
        {
            final Xpp3Dom configDom = (Xpp3Dom)configuration;
            final Xpp3Dom suppressionsLocation = configDom.getChild( "suppressionsLocation" );
            if( suppressionsLocation != null )
            {
                // TODO load resource from plugin classpath and return as URL
                suppressionsLocation.getValue();
            }
        }
        return null;
    }

    /**
     * Find (if exist) the maven-checkstyle-plugin configuration in the mavenProject
     * 
     * @param mavenProject the maven project.
     * @return the checkstyle plugin.
     */
    private Plugin getCheckstylePlugin( final MavenProject mavenProject )
    {
        final List< Plugin > plugins = mavenProject.getBuildPlugins();
        for( final Plugin plugin : plugins )
        {
            if( CHECKSTYLE_PLUGIN_GROUPID.equals( plugin.getGroupId() )
                && CHECKSTYLE_PLUGIN_ARTIFACTID.equals( plugin.getArtifactId() ) )
            {
                return plugin;
            }
        }

        return null;
    }

    /**
     * Find the checkstyle plugin in the site reportset.
     * 
     * @param project the eclipse project.
     * @param mavenProject the maven project.
     * @param monitor the progress monitor.
     * @throws CoreException if there is a problem reading the checkstyle info.
     */
    private void getCheckstyleReportPlugin( final IProject project, final MavenProject mavenProject,
        final IProgressMonitor monitor )
        throws CoreException
    {
        final List< Plugin > plugins = mavenProject.getBuildPlugins();
        outerLoop: for( final Plugin plugin : plugins )
        {
            if( SITE_PLUGIN_GROUPID.equals( plugin.getGroupId() )
                && SITE_PLUGIN_ARTIFACTID.equals( plugin.getArtifactId() ) )
            {
                final Object configuration = plugin.getConfiguration();
                if( configuration instanceof Xpp3Dom )
                {
                    final Xpp3Dom reportPlugins = ((Xpp3Dom)configuration).getChild( "reportPlugins" );
                    if( reportPlugins != null )
                    {
                        for( final Xpp3Dom reportPlugin : reportPlugins.getChildren( "plugin" ) )
                        {
                            final Xpp3Dom groupId = reportPlugin.getChild( "groupId" );
                            final Xpp3Dom artifactId = reportPlugin.getChild( "artifactId" );

                            if( (groupId != null) && (artifactId != null) &&
                                groupId.getValue().equals( CHECKSTYLE_PLUGIN_GROUPID ) &&
                                artifactId.getValue().equals( CHECKSTYLE_PLUGIN_ARTIFACTID ) )
                            {
                                final Xpp3Dom checkstyleConfig = reportPlugin.getChild( "configuration" );

                                createCheckstyleConfiguration( mavenProject, plugin.getGroupId(),
                                    plugin.getArtifactId(), plugin.getVersion(), plugin.getDependencies(), 
                                    checkstyleConfig, project, monitor );
                                addNature( project, CheckstyleNature.NATURE_ID, monitor );

                                break outerLoop;
                            }
                        }
                    }
                }
            }
        }
    }

}
