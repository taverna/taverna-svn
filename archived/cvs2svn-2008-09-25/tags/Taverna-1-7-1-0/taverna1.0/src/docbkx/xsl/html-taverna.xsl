<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    version="1.0">

<!-- This defines the path to the base dockbook xsl (available from http://docbook.sourceforge.net/)
	 The path may vary for on different platforms.
  -->
<xsl:import href="file:////opt/local/share/xsl/docbook-xsl/html/chunk.xsl"/>

<xsl:param name="toc.section.depth" select="4"></xsl:param>
<xsl:param name="use.id.as.filename" select="1"></xsl:param>
<xsl:param name="chunk.section.depth" select="2"></xsl:param>
<xsl:param name="html.stylesheet" select="'html.css'"></xsl:param>
<xsl:param name="generate.section.toc.level" select="1"></xsl:param>
<xsl:param name="section.autolabel" select="1"></xsl:param>
<xsl:param name="section.autolabel" select="1"></xsl:param>

<xsl:param name="generate.toc">
appendix  toc,title
article   toc,title
book      toc,title
chapter   toc,table,title
part      toc,title
preface   toc,title
qandadiv  toc
qandaset  toc
reference toc,title
sect1     toc
sect2     toc
sect3     toc
sect4     toc
sect5     toc
section   toc,title
set       toc,title
</xsl:param>


</xsl:stylesheet>
