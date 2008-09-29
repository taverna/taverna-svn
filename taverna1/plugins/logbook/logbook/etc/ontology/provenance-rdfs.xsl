<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:owl="http://www.w3.org/2002/07/owl#"
	version="1.0">
	
	<!-- author dturi $Id: provenance-rdfs.xsl,v 1.1 2007-12-14 12:53:00 stain Exp $ -->
	
	<xsl:output method="text"/>
	
	<xsl:variable name="lcletters">abcdefghijklmnopqrstuvwxyz</xsl:variable>
	<xsl:variable name="ucletters">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
	
	<xsl:variable name="package">uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology</xsl:variable>
	
	<xsl:template match="rdf:RDF">
		<xsl:text>package </xsl:text><xsl:value-of select="$package"/><xsl:text>;&#10;</xsl:text>
		<xsl:text>&#10;</xsl:text>
		<xsl:text>import org.openrdf.model.URI;</xsl:text>
		<xsl:text>&#10;</xsl:text>
		<xsl:text>import org.openrdf.model.impl.URIImpl;</xsl:text>
		<xsl:text>&#10;</xsl:text>
		<xsl:text>/**&#10;</xsl:text>
		<xsl:text> * Java constants for classes and predicates in provenance.rdfs.&#10;</xsl:text>
		<xsl:text> * Automatically generated from provenance.rdfs using provenance-rdfs.xsl.&#10;</xsl:text>
		<xsl:text> */&#10;</xsl:text>
		<xsl:text>public class ProvenanceSchemaConstants { &#10;</xsl:text>
		<xsl:text>&#10;</xsl:text>
		<xsl:text>	public static final String NS = "http://www.mygrid.org.uk/provenance#"; &#10;</xsl:text>
		<xsl:text>&#10;</xsl:text>
		<xsl:text>/**&#10;</xsl:text>
		<xsl:text> * Java constants for classes in provenance.rdfs.&#10;</xsl:text>
		<xsl:text> * Automatically generated from provenance.rdfs using provenance-rdfs.xsl.&#10;</xsl:text>
		<xsl:text> */&#10;</xsl:text>
		<xsl:text>	public static class Classes { &#10;</xsl:text>
		<xsl:text>&#10;</xsl:text>
    <xsl:apply-templates select="//rdfs:Class"/>
		<xsl:text>	}&#10;</xsl:text>
		<xsl:text>&#10;</xsl:text>
		<xsl:text>/**&#10;</xsl:text>
		<xsl:text> * Java constants for properties in provenance.rdfs.&#10;</xsl:text>
		<xsl:text> * Automatically generated from provenance.rdfs using provenance-rdfs.xsl.&#10;</xsl:text>
		<xsl:text> */&#10;</xsl:text>
		<xsl:text>	public static class Properties { &#10;</xsl:text>
		<xsl:text>&#10;</xsl:text>
    <xsl:apply-templates select="//rdf:Property"/>
		<xsl:text>	}&#10;</xsl:text>
		<xsl:text>&#10;</xsl:text>
		<xsl:text>}</xsl:text>
  </xsl:template>
	
	<xsl:template match="rdfs:Class">
		<xsl:call-template name = "provenance_constant"/>
	</xsl:template>
	
	<xsl:template match="rdf:Property">
		<xsl:call-template name = "provenance_constant"/>
	</xsl:template>
	
	<xsl:template name="provenance_constant">
		<xsl:if test="@rdf:ID!=''">
        <xsl:text>		public static final URI </xsl:text>
					<xsl:value-of select="translate(@rdf:ID,$lcletters,$ucletters)"/>
				<xsl:text> = new URIImpl(NS + "</xsl:text><xsl:value-of select="@rdf:ID"/>");
				<xsl:text>&#10;</xsl:text>
		</xsl:if>
	</xsl:template>
	
</xsl:stylesheet>
