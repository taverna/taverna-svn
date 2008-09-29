<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:owl="http://www.w3.org/2002/07/owl#"
	version="1.0">
	
	<!-- author dturi $Id: provenance.xsl,v 1.1 2007-12-14 12:53:03 stain Exp $ -->
	
	<xsl:output method="text"/>
	
	<xsl:variable name="lcletters">abcdefghijklmnopqrstuvwxyz</xsl:variable>
	<xsl:variable name="ucletters">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
	
	<xsl:variable name="package">uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology</xsl:variable>
	
	<xsl:template match="rdf:RDF">
		<xsl:text>package </xsl:text><xsl:value-of select="$package"/><xsl:text>;&#10;</xsl:text>
		<xsl:text>/**&#10;</xsl:text>
		<xsl:text> * Java constants for classes and predicates in provenance.owl.&#10;</xsl:text>
		<xsl:text> * Automatically generated from provenance.owl using provenance.xsl.&#10;</xsl:text>
		<xsl:text> */&#10;</xsl:text>
		<xsl:text>public class ProvenanceOntologyConstants { &#10;</xsl:text>
		<xsl:text>&#10;</xsl:text>
		<xsl:text>	public static final String NS = "http://www.mygrid.org.uk/provenance#"; &#10;</xsl:text>
		<xsl:text>&#10;</xsl:text>
		<xsl:text>/**&#10;</xsl:text>
		<xsl:text> * Java constants for classes in provenance.owl.&#10;</xsl:text>
		<xsl:text> * Automatically generated from provenance.owl using provenance.xsl.&#10;</xsl:text>
		<xsl:text> */&#10;</xsl:text>
		<xsl:text>	public static class Classes { &#10;</xsl:text>
		<xsl:text>&#10;</xsl:text>
    <xsl:apply-templates select="//owl:Class"/>
		<xsl:text>	}&#10;</xsl:text>
		<xsl:text>&#10;</xsl:text>
		<xsl:text>/**&#10;</xsl:text>
		<xsl:text> * Java constants for object properties in provenance.owl.&#10;</xsl:text>
		<xsl:text> * Automatically generated from provenance.owl using provenance.xsl.&#10;</xsl:text>
		<xsl:text> */&#10;</xsl:text>
		<xsl:text>	public static class ObjectProperties { &#10;</xsl:text>
		<xsl:text>&#10;</xsl:text>
    <xsl:apply-templates select="//owl:ObjectProperty"/>
		<xsl:text>	}&#10;</xsl:text>
		<xsl:text>&#10;</xsl:text>
		<xsl:text>/**&#10;</xsl:text>
		<xsl:text> * Java constants for datatype properties in provenance.owl.&#10;</xsl:text>
		<xsl:text> * Automatically generated from provenance.owl using provenance.xsl.&#10;</xsl:text>
		<xsl:text> */&#10;</xsl:text>
		<xsl:text>	public static class DatatypeProperties { &#10;</xsl:text>
		<xsl:text>&#10;</xsl:text>
		<xsl:apply-templates select="//owl:DatatypeProperty"/>
		<xsl:text>	}&#10;</xsl:text>
		<xsl:text>}</xsl:text>
  </xsl:template>
	
	<xsl:template match="owl:Class">
		<xsl:call-template name = "provenance_constant"/>
	</xsl:template>
	
	<xsl:template match="owl:ObjectProperty">
		<xsl:call-template name = "provenance_constant"/>
	</xsl:template>
		
	<xsl:template match="owl:DatatypeProperty">
		<xsl:call-template name = "provenance_constant"/>
	</xsl:template>
	
	<xsl:template name="provenance_constant">
		<xsl:if test="@rdf:ID!=''">
        <xsl:text>		public static final String </xsl:text>
					<xsl:value-of select="translate(@rdf:ID,$lcletters,$ucletters)"/>
				<xsl:text> = NS + "</xsl:text><xsl:value-of select="@rdf:ID"/>";
				<xsl:text>&#10;</xsl:text>
		</xsl:if>
	</xsl:template>
	
</xsl:stylesheet>
