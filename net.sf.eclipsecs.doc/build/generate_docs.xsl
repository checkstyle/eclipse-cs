<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml">
    <!--
		parameter to control wheter the navigational menu should be included.
	-->
    <xsl:param name="style"/>

    <xsl:variable name="section"
        select="/*[local-name()='html']/*[local-name()='head']/*[local-name()='meta' and @name='section']/@content"/>
    <xsl:variable name="path-prefix"
        select="/*[local-name()='html']/*[local-name()='head']/*[local-name()='meta' and @name='path-prefix']/@content"/>

    <!-- output as XHTML 1.1 -->
    <xsl:output encoding="UTF-8" method="xml" indent="yes"
        doctype-public="-//W3C//DTD XHTML 1.1//EN"
        doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"/>
    <!-- root template -->
    <xsl:template match="/">
        <html>
            <head>
                <meta http-equiv="CONTENT-TYPE" content="text/html; charset=UTF-8"/>
                <title> eclipse-cs: <xsl:value-of
                        select="/*[local-name()='html']/*[local-name()='head']/*[local-name()='title']"
                    />
                </title>

                <!-- Dependency source files -->
                <script type="text/javascript" src="{$path-prefix}yui/build/yahoo-dom-event/yahoo-dom-event.js">
                    <xsl:comment/>
                </script>
                <script type="text/javascript" src="{$path-prefix}yui/build/animation/animation-min.js">
                    <xsl:comment/>
                </script>
                <script type="text/javascript" src="{$path-prefix}yui/build/container/container_core-min.js">
                    <xsl:comment/>
                </script>

                <!-- Menu source file -->
                <script type="text/javascript" src="{$path-prefix}yui/build/menu/menu-min.js">
                    <xsl:comment/>
                </script>

                <!-- Page-specific script -->
                <script type="text/javascript" src="{$path-prefix}menu.js">
                    <xsl:comment/>
                </script>

                <!-- FancyZoom scripts -->
                <script src="{$path-prefix}fancyzoom/js-global/FancyZoom.js" type="text/javascript">
                    <xsl:comment/>
                </script>
                <script src="{$path-prefix}fancyzoom/js-global/FancyZoomHTML.js" type="text/javascript">
                    <xsl:comment/>
                </script>

                <script type="text/javascript">
                    YAHOO.util.Event.onContentReady("content", setupZoom);</script>

                <link rel="stylesheet" type="text/css"
                    href="{$path-prefix}yui/build/grids/grids-min.css"/>
                <link rel="stylesheet" type="text/css"
                    href="{$path-prefix}yui/build/menu/assets/skins/sam/menu.css"/>
                <link rel="stylesheet" type="text/css" href="{$path-prefix}style.css"/>

            </head>
            <body class="yui-skin-sam">
                <div id="doc" class="yui-t7">
                    <div id="hd" role="banner">
                        <xsl:call-template name="header"/>
                    </div>
                    <div id="bd" role="main">
                        <div id="yui-main">
                            <div class="yui-g">
                                <div id="mainmenu" class="yuimenubar yuimenubarnav">
                                    <div class="bd">
                                        <xsl:if test="$style = 'website'">
                                            <xsl:call-template name="navigation"/>
                                        </xsl:if>
                                    </div>
                                </div>
                                <xsl:if test="$style = 'website'">
                                    <div id="googleads">
                                        <script type="text/javascript">
                                        <xsl:comment>
                                            google_ad_client = "pub-9204862326418919"; /* 728x90, created 5/5/09 */
                                            google_ad_slot = "6310931050"; google_ad_width = 728; google_ad_height = 90;
                                            //
                                        </xsl:comment>
                                    </script>
                                        <script type="text/javascript" src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
                                        <xsl:comment/>
                                    </script>
                                    </div>
                                </xsl:if>
                                <div id="content">
                                    <xsl:apply-templates
                                        select="/*[local-name()='html']/*[local-name()='body']"/>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div id="ft" role="contentinfo">
                        <xsl:call-template name="footer"/>
                    </div>
                </div>
            </body>
        </html>
    </xsl:template>

    <!-- template to simply copy the input site's content -->
    <xsl:template match="/*[local-name()='html']/*[local-name()='body']">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="*">
        <xsl:copy>
            <xsl:apply-templates select="@* | node() | comment() | processing-instruction()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="@* | text() | comment() | processing-instruction()">
        <xsl:copy/>
    </xsl:template>

    <!--
		renders the navigational menu
	-->
    <xsl:template name="navigation">
        <ul class="first-of-type">
            <li class="yuimenubaritem first-of-type">
                <a class="yuimenubaritemlabel" href="{$path-prefix}index.shtml">Main</a>
                <div id="main" class="yuimenu">
                    <div class="bd">
                        <ul>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel" href="{$path-prefix}news.shtml">News</a>
                            </li>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel"
                                    href="{$path-prefix}releasenotes/5.0.0final/release_notes.html"
                                    >Release Notes</a>
                                <div id="releasnotes" class="yuimenu">
                                    <div class="bd">
                                        <ul class="first-of-type">
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="{$path-prefix}releasenotes/5.0.0final/release_notes.html"
                                                  > Release 5.0.0final</a>
                                            </li>
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="{$path-prefix}releasenotes/5.0.0beta4/release_notes.html"
                                                  > Release 5.0.0beta4</a>
                                            </li>
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="{$path-prefix}releasenotes/5.0.0beta3/release_notes.html"
                                                  > Release 5.0.0beta3</a>
                                            </li>
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="{$path-prefix}releasenotes/5.0.0beta2/release_notes.html"
                                                  > Release 5.0.0beta2</a>
                                            </li>
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="{$path-prefix}releasenotes/5.0.0beta1/release_notes.html"
                                                  > Release 5.0.0beta1</a>
                                            </li>
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="{$path-prefix}releasenotes/4.4.2/release_notes.html"
                                                  > Release 4.4.2</a>
                                            </li>
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="{$path-prefix}releasenotes/4.4.1/release_notes.html"
                                                  > Release 4.4.1</a>
                                            </li>
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="{$path-prefix}releasenotes/4.4.0/release_notes.html"
                                                  > Release 4.4.0</a>
                                            </li>
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="{$path-prefix}releasenotes/4.3.3/release_notes.html"
                                                  > Release 4.3.3</a>
                                            </li>
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="{$path-prefix}releasenotes/4.3.2/release_notes.html"
                                                  > Release 4.3.2</a>
                                            </li>
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="{$path-prefix}releasenotes/4.3.1/release_notes.html"
                                                  > Release 4.3.1</a>
                                            </li>
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="{$path-prefix}releasenotes/4.3.0/release_notes.html"
                                                  > Release 4.3.0</a>
                                            </li>
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="{$path-prefix}releasenotes/4.2.1/release_notes.html"
                                                  > Release 4.2.1</a>
                                            </li>
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="{$path-prefix}releasenotes/4.2.0/release_notes.html"
                                                  > Release 4.2.0</a>
                                            </li>
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="{$path-prefix}releasenotes/release_notes_older.html"
                                                  > Older releases</a>
                                            </li>
                                        </ul>
                                    </div>
                                </div>
                            </li>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel" href="#">Get Help</a>
                                <div id="gethelp" class="yuimenu">
                                    <div class="bd">
                                        <ul class="first-of-type">
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="{$path-prefix}faq.html"> FAQ </a>
                                            </li>
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="https://sourceforge.net/forum/?group_id=80344"
                                                  > Get help in the forums</a>
                                            </li>
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="http://sourceforge.net/tracker/?atid=559494&amp;group_id=80344&amp;func=browse"
                                                  > Report a bug</a>
                                            </li>
                                            <li class="yuimenuitem">
                                                <a class="yuimenuitemlabel"
                                                  href="http://sourceforge.net/tracker/?atid=559494&amp;group_id=80344&amp;func=browse"
                                                  > File a feature request</a>
                                            </li>
                                        </ul>
                                    </div>
                                </div>
                            </li>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel" href="{$path-prefix}screen_shots.html">
                                    Screenshots </a>
                            </li>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel" href="{$path-prefix}rad_usage.html"> RAD
                                    6/7 issues</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </li>
            <li class="yuimenubaritem">
                <a class="yuimenubaritemlabel" href="{$path-prefix}downloads.html">Download
                    &amp; Installation</a>
                <div id="download" class="yuimenu">
                    <div class="bd">
                        <ul>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel" href="{$path-prefix}downloads.html"
                                    >Download &amp; Installation</a>
                            </li>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel"
                                    href="https://sourceforge.net/project/showfiles.php?group_id=80344"
                                    >SourceForge Download Page</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </li>
            <li class="yuimenubaritem">
                <a class="yuimenubaritemlabel" href="{$path-prefix}getting_started.html">Getting
                    Started</a>
                <div id="basicdocs" class="yuimenu">
                    <div class="bd">
                        <ul>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel"
                                    href="{$path-prefix}basic_setup_project.html">Setting up a
                                    project</a>
                            </li>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel"
                                    href="{$path-prefix}basic_creating_config.html">Creating a check
                                    configuration</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </li>
            <li class="yuimenubaritem">
                <a class="yuimenubaritemlabel" href="#">Advanced Topics</a>
                <div id="advanceddocs" class="yuimenu">
                    <div class="bd">
                        <ul>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel"
                                    href="{$path-prefix}advanced_file_sets.html">Using filesets </a>
                            </li>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel"
                                    href="{$path-prefix}advanced_filters.html">Plugin filters </a>
                            </li>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel"
                                    href="{$path-prefix}advanced_configtypes.html">Configuration
                                    types</a>
                            </li>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel" href="{$path-prefix}advanced_team.html"
                                    >Team support</a>
                            </li>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel"
                                    href="{$path-prefix}advanced_regularexpressions.html">Using
                                    regular expressions</a>
                            </li>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel"
                                    href="{$path-prefix}advanced_properties.html">Expanding property
                                    placeholders</a>
                            </li>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel"
                                    href="{$path-prefix}advanced_preferences.html">Advanced
                                    Preferences</a>
                            </li>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel" href="{$path-prefix}maven.html"
                                    >Synchronizing with m2eclipse</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </li>
            <li class="yuimenubaritem">
                <a class="yuimenubaritemlabel" href="{$path-prefix}extending.html">Plugin
                    extensions</a>
                <div id="extensiondocs" class="yuimenu">
                    <div class="bd">
                        <ul>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel" href="{$path-prefix}extending.html"
                                    >Building an extension plugin</a>
                            </li>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel"
                                    href="{$path-prefix}extending_custom_checks.html"> Custom
                                    Checkstyle modules</a>
                            </li>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel"
                                    href="{$path-prefix}extending_builtin_configurations.html"
                                    >Providing a built-in configuration</a>
                            </li>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel"
                                    href="{$path-prefix}extending_filters.html">Custom plugin
                                    filters</a>
                            </li>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel" href="{$path-prefix}development.html">
                                    eclipse-cs development environment</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </li>
            <li class="yuimenubaritem">
                <a class="yuimenubaritemlabel" href="#">eclipse-cs Project</a>
                <div id="project" class="yuimenu">
                    <div class="bd">
                        <ul>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel" href="{$path-prefix}developers.html"
                                    >Project members</a>
                            </li>
                            <li class="yuimenuitem">
                                <a class="yuimenuitemlabel"
                                    href="http://sourceforge.net/projects/eclipse-cs/"> SourceForge
                                    project page</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </li>
        </ul>
    </xsl:template>


    <!-- renders the header of the page -->
    <xsl:template name="header">
        <div id="header">
            <table width="100%" cellspacing="0" cellpadding="0">
                <tr>
                    <td style="text-align: left; width=100%" rowspan="2">
                        <img src="{$path-prefix}images/eclipse-cs_logo.png" alt="eclipse-cs Banner"
                            width="246" height="75"/>
                    </td>

                    <td
                        style="vertical-align: bottom; height: 75px; text-align: right; padding-right: 20px; padding-bottom: 10px;">
                        <xsl:if test="$style = 'website'">
                            <form action="http://www.google.com/cse" id="cse-search-box"
                                target="_blank">
                                <div>
                                    <input type="hidden" name="cx"
                                        value="partner-pub-9204862326418919:fottii-3k6j"/>
                                    <input type="hidden" name="ie" value="UTF-8"/>
                                    <input type="text" name="q" size="31"/>
                                    <input type="submit" name="sa" value="Search"/>
                                </div>
                            </form>

                            <script type="text/javascript" src="http://www.google.com/coop/cse/brand?form=cse-search-box&amp;lang=en"><xsl:comment/></script>
                        </xsl:if>
                    </td>

                    <td
                        style="vertical-align: middle; width: 155px; height: 75px; padding-right: 5px;">
                        <a href="http://sourceforge.net/projects/eclipse-cs">
                            <img
                                src="http://sflogo.sourceforge.net/sflogo.php?group_id=80344&amp;type=14"
                                width="150" height="40"
                                alt="Get Eclipse Checkstyle Plug-in at SourceForge.net. Fast, secure and Free Open Source software downloads"
                            />
                        </a>
                        <script type="text/javascript" src="http://www.ohloh.net/p/6568/widgets/project_thin_badge.js"><xsl:comment/></script>
                    </td>

                </tr>
            </table>
        </div>
    </xsl:template>


    <!-- renders the footer of the page -->
    <xsl:template name="footer">
        <div id="footer"> Copyright &#xA9; 2002-2009 David Schneider, <a
                href="http://www.koedderitzsch.net">Lars Ködderitzsch</a>. All Rights Reserved.
        </div>
    </xsl:template>
</xsl:stylesheet>
