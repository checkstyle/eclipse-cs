<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xalan="http://xml.apache.org/xslt">
    <xsl:output indent="yes" method="xml" xalan:indent-amount="4"/>
    <xsl:template match="*">
        <xsl:copy>
            <xsl:apply-templates select="@* | node() | comment() | processing-instruction()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="@* | text() | comment() | processing-instruction()">
        <xsl:copy/>
    </xsl:template>
</xsl:stylesheet>
