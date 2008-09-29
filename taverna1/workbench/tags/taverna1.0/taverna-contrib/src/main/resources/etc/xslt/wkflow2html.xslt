<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:s="http://org.embl.ebi.escience/xscufl/0.1alpha"  version="1.0"
>
<xsl:output media-type="text/html" method="html" omit-xml-declaration="no" indent="yes"/>
<xsl:template match="/">
	<xsl:variable name="title" select="//s:workflowdescription/@title"/>
	<xsl:variable name="author" select="s:workflowdescription/@author"/>
	<xsl:variable name="description" select="//s:workflowdescription"/>
	<html>
		<head>
			<title><xsl:value-of select="$title"/></title>
			<style type="text/css">
			
				.label{
					font-family: Verdana;
					font-weight: bold;
					font-size: 10;
				}
				
				.subhead{
					font-family:Verdana;
					font-weight: bold;
					font-size: 12pt;
					background-color: #888888;
					color: white;
				}
				
				.heading{
					font-family:Verdana;
					font-weight: bold;
					font-size: 18pt;
					background-color: #336699;
					color: white;
				}
			
			
			</style>
		</head>
		<body>
			<table>
				<tr><td class="heading" colspan="2">Workflow Documentation</td></tr>
				<tr><td class="label">Workflow Title:</td><td><xsl:value-of select="$title"/></td></tr>
				<tr><td class="label">Workflow Author:</td><td><xsl:value-of select="$author"/></td></tr>
				<tr><td class="label">Workflow Description:</td><td><xsl:value-of select="$description"/></td></tr>
				
				<xsl:if test="count(//s:source) &gt; 0">
				<tr><td class="subhead" colspan="2">Input Values</td></tr>
				<tr><td>Name</td><td>Description</td><td>MIME-Types</td><td>Semantic Type</td></tr>
				<xsl:for-each select="//s:source">
					<tr>
						<td><xsl:value-of select="@name"/></td>
						<td><xsl:value-of select="s:metadata/s:description"/></td>
						<td>
							<xsl:for-each select="s:metadata/s:mimeTypes/s:mimeType">
								<xsl:value-of select="current()"/> 
							</xsl:for-each>
						</td>
						<td>
							<xsl:value-of select="s:metadata/s:semanticType"/>
						</td>
					</tr>
				</xsl:for-each>
				</xsl:if>
				<tr><td class="subhead" colspan="2">Output Values</td></tr>
				<tr><td>Name</td><td>Description</td><td>MIME-Types</td><td>Semantic Type</td></tr>
				<xsl:for-each select="//s:sink">
					<tr>
						<td><xsl:value-of select="@name"/></td>
						<td><xsl:value-of select="s:metadata/s:description"/></td>
						<td>
							<xsl:for-each select="s:metadata/s:mimeTypes/s:mimeType">
								<xsl:value-of select="current()"/> 
							</xsl:for-each>
						</td>
						<td>
							<xsl:value-of select="s:metadata/s:semanticType"/>
						</td>
					</tr>
				</xsl:for-each>
			</table>
		
		</body>
	</html>
</xsl:template>
</xsl:stylesheet>
