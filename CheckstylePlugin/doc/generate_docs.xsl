<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns="http://www.w3.org/1999/xhtml">
	<!-- parameter to control wheter the navigational menu should be included. -->
	<xsl:param name="style"/>
	<!-- output as XHTML 1.1 -->
	<xsl:output encoding="UTF-8" method="xml" indent="yes" doctype-public="-//W3C//DTD XHTML
		1.1//EN" doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"/>
	<!-- root template -->
	<xsl:template match="/">
		<html>
			<head>
				<meta http-equiv="CONTENT-TYPE" content="text/html; charset=UTF-8"/>
				<link type="text/css" href="style.css" rel="stylesheet"/>
				<title>eclipse-cs: <xsl:value-of
						select="/*[local-name()='html']/*[local-name()='head']/*[local-name()='title']"
					/></title>
			</head>
			<body>
				<table class="maintable" cellpadding="0" cellspacing="0">
					<tr>
						<xsl:if test="$style = 'website'">
							<td class="navsection">
								<xsl:call-template name="navigation"/>
							</td>
						</xsl:if>
						<td class="mainsection">
							<table border="0" width="100%" cellpadding="0" cellspacing="0"
								style="height: 100%">
								<tr>
									<td>
										<xsl:call-template name="header"/>
									</td>
								</tr>
								<tr>
									<td class="content">
										<xsl:apply-templates
											select="/*[local-name()='html']/*[local-name()='body']"
										/>
									</td>
								</tr>
								<tr>
									<td class="footer">
										<xsl:call-template name="footer"/>
									</td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
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
	<!-- renders the left hand side bar containing the navigation and other things -->
	<xsl:template name="navigation">
		<xsl:variable name="section"
			select="/*[local-name()='html']/*[local-name()='head']/*[local-name()='meta' and
			@name='section']/@content"/>
		<div class="buttons">
			<a href="index.html" style="padding-top: 58px; border-top: 0px solid #FFFFFF; background-image: url(images/eclipse-cs.png); background-repeat: no-repeat;">
				<!--<img src="images/eclipse-cs.png" alt="eclipse-cs Logo"/>-->
				The Checkstyle Plug-in for Eclipse
			</a>
			<a href="screen_shots.html" class="level1">Screenshots</a>
			<a href="release_notes.html" class="level1">Release Notes</a>
			<a href="faq.html" class="level1">FAQ</a>
			<a href="troubleshooting.html" class="level1">Troubleshooting</a>
			<xsl:if test="$section != 'docs-basic'">
				<a href="getting_started.html" class="level1"><img src="images/plus.gif" alt=""
					/>Getting Started</a>
			</xsl:if>
			<xsl:if test="$section = 'docs-basic'">
				<a href="getting_started.html" class="level1"><img src="images/minus.gif" alt=""
					/>Getting Started</a>
				<a href="basic_setup_project.html" class="level2">Setting up a project</a>
				<a href="basic_creating_config.html" class="level2">Creating a check
				configuration</a>
			</xsl:if>
			<xsl:if test="$section != 'docs-adv'">
				<a href="advanced_file_sets.html" class="level1"><img src="images/plus.gif" alt=""
					/>Advanced topics</a>
			</xsl:if>
			<xsl:if test="$section = 'docs-adv'">
				<a href="advanced_file_sets.html" class="level1"><img src="images/minus.gif" alt=""
					/>Advanced topics</a>
				<a href="advanced_file_sets.html" class="level2">Configuring File Sets</a>
				<a href="advanced_filters.html" class="level2">Using the plug-in filters</a>
				<a href="advanced_configtypes.html" class="level2">Configuration types</a>
				<a href="advanced_team.html" class="level2">Working with a Team Repository</a>
				<a href="advanced_regularexpressions.html" class="level2">Using Regular Expressions</a>
				<a href="advanced_properties.html" class="level2">Expanding properties</a>
				<a href="advanced_preferences.html" class="level2">Advanced Preferences</a>
			</xsl:if>
			<xsl:if test="$section != 'docs-ext'">
				<a href="extending_custom_checks.html" class="level1"><img src="images/plus.gif"
						alt=""/>Extending the Checkstyle Plug-in</a>
			</xsl:if>
			<xsl:if test="$section = 'docs-ext'">
				<a href="extending_custom_checks.html" class="level1"><img src="images/minus.gif"
						alt=""/>Extending the Checkstyle Plug-in</a>
				<a href="extending_custom_checks.html" class="level2">Using custom checks</a>
				<a href="extending_custom_metadata.html" class="level2">Metadata explained</a>
				<a href="extending_fragments.html" class="level2">Providing a plugin fragment</a>
				<a href="extending_builtin_configurations.html" class="level2">Provide a built-in
					configuration</a>
				<a href="extending_filters.html" class="level2">Creating a custom plug-in filter</a>
			</xsl:if>
			<xsl:if test="$section != 'project'">
				<a href="news.shtml" class="level1"><img src="images/plus.gif" alt=""/>eclipse-cs
					project</a>
			</xsl:if>
			<xsl:if test="$section = 'project'">
				<a href="news.shtml" class="level1"><img src="images/minus.gif" alt=""/>eclipse-cs
					project</a>
				<a href="developers.html" class="level2">Project members</a>
				<a href="http://sourceforge.net/projects/eclipse-cs/" class="level2">SourceForge
					project page</a>
				<a
					href="http://sourceforge.net/tracker/?atid=559494&amp;group_id=80344&amp;func=browse"
					class="level2">Report a bug</a>
				<a
					href="http://sourceforge.net/tracker/?atid=559494&amp;group_id=80344&amp;func=browse"
					class="level2">File a feature request</a>
				<a href="https://sourceforge.net/forum/?group_id=80344" class="level2">Get help in
					the Forums</a>
				<a href="https://sourceforge.net/project/showfiles.php?group_id=80344"
					class="level2">Downloads</a>
			</xsl:if>
		</div>
		<div class="box navsection-box">
			<h3>Rate this plug-in<br/>@ Eclipse Plugin Central!</h3>
			<form method="post" action="http://www.eclipseplugincentral.com/Web_Links+main.html"
				style="width:100%">
				<table border="0" width="100%" cellspacing="0" cellpadding="0">
					<tbody>
						<tr>
							<td style="text-align: left;">
								<span>Rating:</span>
							</td>
							<td style="text-align: left;">
								<select name="rating">
									<option selected="selected">--</option>
									<option>10</option>
									<option>9</option>
									<option>8</option>
									<option>7</option>
									<option>6</option>
									<option>5</option>
									<option>4</option>
									<option>3</option>
									<option>2</option>
									<option>1</option>
								</select>
							</td>
							<td style="text-align: left;">
								<span style="font-size: 9px;">(10 best, 1 worst)</span>
							</td>
						</tr>
						<tr>
							<td style="text-align: left;">
								<span>Comment:</span>
							</td>
						</tr>
						<tr>
							<td colspan="3" style="text-align: center;">
								<textarea cols="25" rows="7" name="ratingcomments"
									style="font-siz:10px;">Please input a comment on why you like or dislike the Eclipse Checkstyle Plug-in.</textarea>
							</td>
						</tr>
						<tr>
							<td style="text-align: center;" colspan="3">
								<input type="hidden" name="ratinglid" value="376"/>
								<input type="hidden" name="ratinguser" value="outside"/>
								<input type="hidden" name="req" value="addrating"/>
								<input type="submit" value="Vote!"/>
							</td>
						</tr>
					</tbody>
				</table>
			</form>
		</div>
		<div class="box navsection-box">
			<h3>Kindly endorsed by</h3>
			<a href="http://www.ej-technologies.com/products/jprofiler/overview.html"
				class="imagelink">
				<img src="images/logo_jprofiler01.gif" alt="ej-technologies' JProfiler"/>
			</a>
			<br/>
			<br/>
			<a href="http://www.oxygenxml.com" class="imagelink">
				<img src="images/logoOxygen.gif" alt="oXygen xml editor"/>
			</a>
		</div>
		<div style="text-align=right; margin-top: 5px;">
			<a href="http://validator.w3.org/check?uri=referer" class="imagelink">
				<img src="images/valid-xhtml11.png" alt="Valid XHTML 1.1" height="31" width="88"/>
			</a>
			<a href="http://jigsaw.w3.org/css-validator/" class="imagelink">
				<img src="images/valid-css.png" alt="Valid CSS" height="31" width="88"/>
			</a>
		</div>
	</xsl:template>
	<!-- renders the header of the page -->
	<xsl:template name="header">
		<table class="header" cellspacing="0" cellpadding="0">
			<tr>
				<td style="text-align: left; height: 75px; width=100%">
					<img src="images/banner.png" alt="eclipse-cs Banner"/>
				</td>
				<td style="vertical-align: middle; width: 75px; height: 75px;">
					<a href="http://sourceforge.net/" class="imagelink">
						<img
							src="http://sourceforge.net/sflogo.php?group_id=80344&amp;amp;type=1"
							alt="SourceForge.net Logo"/>
					</a>
					<a href="http://sourceforge.net/donate/index.php?group_id=80344"
						class="imagelink">
						<img src="http://images.sourceforge.net/images/project-support.jpg"
							alt="Support This Project"/>
					</a>
				</td>
			</tr>
		</table>
	</xsl:template>
	<!-- renders the footer of the page -->
	<xsl:template name="footer">
		<table cellpadding="0" cellspacing="0" border="0" width="100%" style="text-align:top;">
			<tr>
				<td style="width: 35px">
					<a href="#">
						<img src="images/top_button_left.gif" alt="Back to top"/>
					</a>
				</td>
				<td class="copyright"> Copyright &#xA9; 2002-2006 David Schneider, <a
						href="http://www.koedderitzsch.net">Lars Ködderitzsch</a>. All Rights
					Reserved. </td>
				<td style="width: 35px">
					<a href="#">
						<img src="images/top_button_right.gif" alt="Back to top"/>
					</a>
				</td>
			</tr>
		</table>
	</xsl:template>
</xsl:stylesheet>
