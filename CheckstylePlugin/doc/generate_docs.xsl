<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  xmlns="http://www.w3.org/1999/xhtml">

	<!-- parameter to control wheter the navigational menu should be included. -->
	<xsl:param name="style"/>

	<!-- output as XHTML 1.1 -->
	<xsl:output encoding="UTF-8" method="xml" indent="yes" doctype-public="-//W3C//DTD XHTML 1.1//EN" doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"/>

	<!-- root template -->
	<xsl:template match="/">

		<html>
			<head>
				<meta http-equiv="CONTENT-TYPE" content="text/html; charset=UTF-8"/>
				<link type="text/css" href="style.css" rel="stylesheet"/>
				<title>eclipse-cs: <xsl:value-of select="/*[local-name()='html']/*[local-name()='head']/*[local-name()='title']"/></title>
			</head>

			<body>
				<table class="maintable" cellpadding="0" cellspacing="0">
					<tr>
						<xsl:if test="$style = 'website'">
							<td class="navsection">
								<div class="buttonscontainer">
									<xsl:call-template name="navigation"/>
								</div>
							</td>
						</xsl:if>
						<td class="mainsection">
							<table border="0" width="100%" cellpadding="0" cellspacing="0" style="height: 100%">
								<tr>
									<td>
										<xsl:call-template name="header"/>
									</td>
								</tr>
								<tr>
									<td class="content">
										<xsl:apply-templates select="/*[local-name()='html']/*[local-name()='body']/*"/>
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
	<xsl:template match="*">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="@* | text() | comment()">
		<xsl:copy/>
	</xsl:template>

	<!-- renders the left hand side bar containing the navigation and other things -->
	<xsl:template name="navigation">

		<div class="buttons">
			<a href="index.html">
				<img src="images/eclipse-cs.png" alt="eclipse-cs Logo"/>
			</a>
			<a href="screen_shots.html" class="level1">Screenshots</a>
			<a href="getting_started.html" class="level1">Getting Started</a>
			<a href="basic_setup_project.html" class="level2">Setting up a project</a>
			<a href="basic_creating_config.html" class="level2">Creating a check configuration</a>
			<span class="level1">Advanced topics</span>
			<a href="advanced_file_sets.html" class="level2">Configuring File Sets</a>
			<a href="advanced_filters.html" class="level2">Using the plug-in filters</a>
			<a href="advanced_configtypes.html" class="level2">Configuration types</a>
			<a href="advanced_team.html" class="level2">Working with a Team Repository</a>
			<a href="advanced_regularexpressions.html" class="level2">Using Regular Expressions</a>
			<a href="advanced_properties.html" class="level2">Expanding properties</a>
			<a href="advanced_preferences.html" class="level2">Advanced Preferences</a>
			<span class="level1">Extending the Checkstyle Plug-in</span>
			<a href="extending_custom_checks.html" class="level2">Using custom checks</a>
			<a href="extending_custom_metadata.html" class="level3">Metadata explained</a>
			<a href="extending_fragments.html" class="level2">Providing a plugin fragment</a>
			<a href="extending_builtin_configurations.html" class="level3">Provide a built-in configuration</a>
			<a href="extending_filters.html" class="level3">Creating a custom plug-in filter</a>
			<a href="release_notes.html" class="level1">Release Notes</a>
			<a href="faq.html" class="level1">FAQ</a>
			<a href="troubleshooting.html" class="level1">Troubleshooting</a>
			<a href="http://sourceforge.net/projects/eclipse-cs/" class="level1">SourceForge project page</a>
		</div>

		<div class="box navsection-box">
			<h3>Rate this plug-in<br/>@ Eclipse Plugin Central!</h3>
			<form method="post" action="http://www.eclipseplugincentral.com/Web_Links+main.html" style="width:100%">
				<table border="0" width="100%" cellspacing="0" cellpadding="0">
					<tbody>
						<tr>
							<td style="text-align: left;">
								<span style="font-size: 12px; font-weight: bold;">Rating:</span>
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
							<td style="text-align: left; font-weight: bold;">
								<span style="font-size: 12px;">Comment:</span>
							</td>
						</tr>
						<tr>
							<td colspan="3" style="text-align: center;">
								<textarea style="font-size: 12px;" cols="26" rows="7" name="ratingcomments">Please input a comment on why you like or dislike the Eclipse Checkstyle Plug-in.</textarea>
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
			<a href="http://www.ej-technologies.com/products/jprofiler/overview.html" class="imagelink">
				<img src="images/logo_jprofiler01.gif" alt="ej-technologies' JProfiler"/>
			</a>
		</div>
		
		<div style="text-align=right; margin-top: 5px;">
			<a href="http://validator.w3.org/check?uri=referer" class="imagelink">
				<img src="images/valid-xhtml11.png" alt="Valid XHTML 1.1" height="31" width="88"/>
			</a>
			<a href="http://jigsaw.w3.org/css-validator/">
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
						<img src="http://sourceforge.net/sflogo.php?group_id=80344&amp;amp;type=1" alt="SourceForge.net Logo"/>
					</a>
					<a href="http://sourceforge.net/donate/index.php?group_id=80344" class="imagelink">
						<img src="http://images.sourceforge.net/images/project-support.jpg" alt="Support This Project"/>
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
					<a href="#"><img src="images/top_button_left.gif" alt="Back to top"/></a>
				</td>
				<td class="copyright">
					Copyright &#xA9; 2002-2005 David Schneider, <a href="http://www.koedderitzsch.net">Lars Ködderitzsch</a>. All Rights Reserved.
				</td>
				<td style="width: 35px">			
					<a href="#"><img src="images/top_button_right.gif" alt="Back to top"/></a>
				</td>
			</tr>
		</table>
	</xsl:template>
</xsl:stylesheet>