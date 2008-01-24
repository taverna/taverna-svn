#!/usr/bin/env python
"""
Collection of test workflows
"""

UNION_WORKFLOW="""<?xml version="1.0" encoding="UTF-8"?>
<s:scufl xmlns:s="http://org.embl.ebi.escience/xscufl/0.1alpha" version="0.2" log="0">
  <s:workflowdescription lsid="urn:lsid:www.mygrid.org.uk:operation:0S8VX6P6S70" author="" title="" />
  <s:processor name="Echo_list">
    <s:local>org.embl.ebi.escience.scuflworkers.java.EchoList</s:local>
  </s:processor>
  <s:link source="in" sink="Echo_list:inputlist" />
  <s:link source="Echo_list:outputlist" sink="out" />
  <s:source name="in">
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>text/plain</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:source>
  <s:sink name="out" />
</s:scufl>
"""

   
# A simple example workflow AlternateExample.xml by Tom Oinn
SIMPLE_WORKFLOW = """<?xml version="1.0" encoding="UTF-8"?>
<s:scufl xmlns:s="http://org.embl.ebi.escience/xscufl/0.1alpha" version="0.2" log="0">
  <s:workflowdescription lsid="urn:lsid:www.mygrid.org.uk:operation:BD8CRS09KB0" author="Tom Oinn" title="Example of an alternate processor">Trivial workflow which will initially fail, retry twice then fall over to the alternative specified for the FailingThing process.</s:workflowdescription>
  <s:processor name="FooString">
    <s:stringconstant>foo</s:stringconstant>
  </s:processor>
  <s:processor name="BarString">
    <s:stringconstant>bar</s:stringconstant>
  </s:processor>
  <s:processor name="FailingProcessor">
    <s:local maxretries="2" retrydelay="1000" retrybackoff="2.0">org.embl.ebi.escience.scuflworkers.java.TestAlwaysFailingProcessor</s:local>
    <s:alternate>
      <s:local>org.embl.ebi.escience.scuflworkers.java.StringConcat</s:local>
      <s:outputmap key="urgle" value="output" />
      <s:inputmap key="foo" value="string1" />
      <s:inputmap key="bar" value="string2" />
    </s:alternate>
  </s:processor>
  <s:link source="FooString:value" sink="FailingProcessor:foo" />
  <s:link source="BarString:value" sink="FailingProcessor:bar" />
  <s:link source="FailingProcessor:urgle" sink="out" />
  <s:sink name="out" />
</s:scufl>
"""

# A simple example concatinating inputs, fishbowlsoup.xml by Stian
# Soiland
ITERATION_WORKFLOW="""<?xml version="1.0" encoding="UTF-8"?>
<s:scufl xmlns:s="http://org.embl.ebi.escience/xscufl/0.1alpha" version="0.2" log="0">
  <s:workflowdescription lsid="urn:lsid:www.mygrid.org.uk:operation:D8YG5M4O0N2" author="Stian Soiland" title="Fish bowl soup">Concatinate the inputs with a string constant, producing a list of outputs</s:workflowdescription>
  <s:processor name="Concatenate_two_strings">
    <s:local>org.embl.ebi.escience.scuflworkers.java.StringConcat</s:local>
    <s:iterationstrategy>
      <i:cross xmlns:i="http://org.embl.ebi.escience/xscufliteration/0.1beta10">
        <i:iterator name="string1" />
        <i:iterator name="string2" />
      </i:cross>
    </s:iterationstrategy>
  </s:processor>
  <s:processor name="String_Constant" boring="true">
    <s:stringconstant>bowl</s:stringconstant>
  </s:processor>
  <s:link source="food" sink="Concatenate_two_strings:string1" />
  <s:link source="Concatenate_two_strings:output" sink="result" />
  <s:link source="String_Constant:value" sink="Concatenate_two_strings:string2" />
  <s:source name="food">
    <s:metadata>
      <s:description>The inputs to be concatinated with the bowl</s:description>
    </s:metadata>
  </s:source>
  <s:sink name="result">
    <s:metadata>
      <s:description>The inputs concatinated with bowl</s:description>
    </s:metadata>
  </s:sink>
</s:scufl>
"""
# A complex example workflow probeset_workflow.xml by Paul Fisher
COMPLEX_WORKFLOW = """<?xml version="1.0" encoding="UTF-8"?>
<s:scufl xmlns:s="http://org.embl.ebi.escience/xscufl/0.1alpha" version="0.2" log="0">
  <s:workflowdescription
  lsid="urn:lsid:www.mygrid.org.uk:operation:NXIYI8FZ5K0" 
  author="Paul Fisher" title="Probeset workflow">
  A way to probe probesets in a workflow
  </s:workflowdescription>
  <s:processor name="species" boring="true">
    <s:stringconstant>mus_musculus</s:stringconstant>
  </s:processor>
  <s:processor name="split_by_regex_2">
    <s:local>org.embl.ebi.escience.scuflworkers.java.SplitByRegex</s:local>
  </s:processor>
  <s:processor name="regex1" boring="true">
    <s:stringconstant>\n</s:stringconstant>
  </s:processor>
  <s:processor name="merge_probesets">
    <s:local>org.embl.ebi.escience.scuflworkers.java.StringListMerge</s:local>
  </s:processor>
  <s:processor name="Add_ncbi_to_string">
    <s:workflow>
      <s:scufl version="0.2" log="0">
        <s:workflowdescription lsid="urn:lsid:www.mygrid.org.uk:operation:P81DV9PQW02" author="" title="" />
        <s:processor name="add_ncbi_to_string">
          <s:beanshell>
            <s:scriptvalue>String[] split = input.split("\n");
Vector nonEmpty = new Vector();
for (int i = 0; i &lt; split.length; i++) 
{		
	String trimmed = split[i].trim();
	nonEmpty.add(trimmed);	
}
String output = "";
for (int i = 0; i &lt; nonEmpty.size(); i++)
{
	output = output + "ncbi-geneid:" + (String) (nonEmpty.elementAt(i) + "\n");
}</s:scriptvalue>
            <s:beanshellinputlist>
              <s:beanshellinput s:syntactictype="'text/plain'">input</s:beanshellinput>
            </s:beanshellinputlist>
            <s:beanshelloutputlist>
              <s:beanshelloutput s:syntactictype="'text/plain'">output</s:beanshelloutput>
            </s:beanshelloutputlist>
          </s:beanshell>
        </s:processor>
        <s:link source="Gi_numbers" sink="add_ncbi_to_string:input" />
        <s:link source="add_ncbi_to_string:output" sink="Kegg_strings" />
        <s:source name="Gi_numbers" />
        <s:sink name="Kegg_strings" />
      </s:scufl>
    </s:workflow>
  </s:processor>
  <s:processor name="Remove_swiss_nulls">
    <s:workflow>
      <s:scufl version="0.2" log="0">
        <s:workflowdescription lsid="urn:lsid:www.mygrid.org.uk:operation:P81DV9PQW02" author="" title="" />
        <s:processor name="remove_Nulls">
          <s:beanshell>
            <s:scriptvalue>String[] split = input.split("\n");
Vector nonEmpty = new Vector();
for (int i = 0; i &lt; split.length; i++){
   if (!(split[i].equals("")))
   {
       nonEmpty.add(split[i].trim());
   }
}
String[] non_empty = new String[nonEmpty.size()];
for (int i = 0; i &lt; non_empty.length; i ++)
{
   non_empty[i] = nonEmpty.elementAt(i);
}
String output = "";
for (int i = 0; i &lt; non_empty.length; i++)
{
	output = output + (String) (non_empty[i] + "\n");
}</s:scriptvalue>
            <s:beanshellinputlist>
              <s:beanshellinput s:syntactictype="'text/plain'">input</s:beanshellinput>
            </s:beanshellinputlist>
            <s:beanshelloutputlist>
              <s:beanshelloutput s:syntactictype="'text/plain'">output</s:beanshelloutput>
            </s:beanshelloutputlist>
          </s:beanshell>
        </s:processor>
        <s:link source="input_string" sink="remove_Nulls:input" />
        <s:link source="remove_Nulls:output" sink="removed_nulls" />
        <s:source name="input_string" />
        <s:sink name="removed_nulls" />
      </s:scufl>
    </s:workflow>
  </s:processor>
  <s:processor name="comma" boring="true">
    <s:stringconstant>,</s:stringconstant>
  </s:processor>
  <s:processor name="split_gene_ids">
    <s:beanshell>
      <s:scriptvalue>String[] split = input.split("\n");
Vector nonEmpty = new Vector();
for (int i = 0; i &lt; split.length; i++) 
{		
	String trimmed = split[i].trim();
	String[] trimmedSplit = trimmed.split("\t");
	if (trimmedSplit.length == 2)
	{
	    nonEmpty.add(trimmedSplit[1].trim());	
	}
}
String output = "";
for (int i = 0; i &lt; nonEmpty.size(); i++)
{
	output = output + (String) (nonEmpty.elementAt(i) + "\n");
}</s:scriptvalue>
      <s:beanshellinputlist>
        <s:beanshellinput s:syntactictype="'text/plain'">input</s:beanshellinput>
      </s:beanshellinputlist>
      <s:beanshelloutputlist>
        <s:beanshelloutput s:syntactictype="'text/plain'">output</s:beanshelloutput>
      </s:beanshelloutputlist>
    </s:beanshell>
  </s:processor>
  <s:processor name="merge_pathways">
    <s:local>org.embl.ebi.escience.scuflworkers.java.StringListMerge</s:local>
  </s:processor>
  <s:processor name="split_by_regex">
    <s:local>org.embl.ebi.escience.scuflworkers.java.SplitByRegex</s:local>
  </s:processor>
  <s:processor name="Concatenate_two_strings">
    <s:local>org.embl.ebi.escience.scuflworkers.java.StringConcat</s:local>
    <s:iterationstrategy>
      <i:dot xmlns:i="http://org.embl.ebi.escience/xscufliteration/0.1beta10">
        <i:iterator name="string2" />
        <i:iterator name="string1" />
      </i:dot>
    </s:iterationstrategy>
  </s:processor>
  <s:processor name="lister">
    <s:arbitrarywsdl>
      <s:wsdl>http://phoebus.cs.man.ac.uk:8081/axis/EnsemblListner.jws?wsdl</s:wsdl>
      <s:operation>lister</s:operation>
    </s:arbitrarywsdl>
  </s:processor>
  <s:processor name="getcurrentdatabase">
    <s:description>Retrieves the current databases from ENSEMBL for a species</s:description>
    <s:soaplabwsdl>http://phoebus.cs.man.ac.uk:1977/axis/services/qtl_analysis.getcurrentdatabase</s:soaplabwsdl>
  </s:processor>
  <s:processor name="Parse_swiss_ids">
    <s:workflow>
      <s:scufl version="0.2" log="0">
        <s:workflowdescription lsid="urn:lsid:www.mygrid.org.uk:operation:P81DV9PQW02" author="" title="" />
        <s:processor name="options" boring="true">
          <s:stringconstant>swiss</s:stringconstant>
        </s:processor>
        <s:processor name="parse_swiss">
          <s:beanshell>
            <s:scriptvalue>String[] split = input.split("\n");
Vector nonEmpty = new Vector();
for (int i = 0; i &lt; split.length; i++) 
{		
	String trimmed = split[i].trim();
	String[] trimmedSplit = trimmed.split(":");
	if (trimmedSplit.length == 2)
	{
	    nonEmpty.add(trimmedSplit[1].trim());	
	}
}
String output = "";
for (int i = 0; i &lt; nonEmpty.size(); i++)
{
	output = output + (String) (nonEmpty.elementAt(i) + "\n");
}</s:scriptvalue>
            <s:beanshellinputlist>
              <s:beanshellinput s:syntactictype="'text/plain'">input</s:beanshellinput>
            </s:beanshellinputlist>
            <s:beanshelloutputlist>
              <s:beanshelloutput s:syntactictype="'text/plain'">output</s:beanshelloutput>
            </s:beanshelloutputlist>
          </s:beanshell>
        </s:processor>
        <s:processor name="parse_ddbj_gene_info">
          <s:description>extract information from geneGeneInfo processor at http://xml.nig.ac.jp/wsdl/Ensembl.wsdl</s:description>
          <s:soaplabwsdl>http://phoebus.cs.man.ac.uk:1977/axis/services/seq_analysis.parse_ddbj_gene_info</s:soaplabwsdl>
        </s:processor>
        <s:link source="gene_info" sink="parse_ddbj_gene_info:file_direct_data" />
        <s:link source="options:value" sink="parse_ddbj_gene_info:options" />
        <s:link source="parse_ddbj_gene_info:output" sink="parse_swiss:input" />
        <s:link source="parse_swiss:output" sink="swiss_ids" />
        <s:source name="gene_info" />
        <s:sink name="swiss_ids" />
      </s:scufl>
    </s:workflow>
  </s:processor>
  <s:processor name="probeset_to_gene">
    <s:description>Gets ENSEMBL gene IDS that are based on the probeset names passed to it</s:description>
    <s:soaplabwsdl>http://phoebus.cs.man.ac.uk:1977/axis/services/qtl_analysis.probeset_to_gene</s:soaplabwsdl>
  </s:processor>
  <s:processor name="Kegg_gene_ids_all_species">
    <s:arbitrarywsdl>
      <s:wsdl>http://soap.genome.jp/KEGG.wsdl</s:wsdl>
      <s:operation>bconv</s:operation>
    </s:arbitrarywsdl>
  </s:processor>
  <s:processor name="Ensembl_gene_info">
    <s:workflow>
      <s:scufl version="0.2" log="0">
        <s:workflowdescription lsid="urn:lsid:www.mygrid.org.uk:operation:P81DV9PQW02" author="" title="" />
        <s:processor name="getGeneInfo">
          <s:description>get gene information</s:description>
          <s:arbitrarywsdl>
            <s:wsdl>http://xml.nig.ac.jp/wsdl/Ensembl.wsdl</s:wsdl>
            <s:operation>getGeneInfo</s:operation>
          </s:arbitrarywsdl>
        </s:processor>
        <s:link source="gene_in_region" sink="getGeneInfo:geneId" />
        <s:link source="getGeneInfo:Result" sink="gene_info" />
        <s:source name="gene_in_region" />
        <s:sink name="gene_info" />
      </s:scufl>
    </s:workflow>
  </s:processor>
  <s:processor name="get_pathways_by_genes">
    <s:arbitrarywsdl>
      <s:wsdl>http://soap.genome.jp/KEGG.wsdl</s:wsdl>
      <s:operation>get_pathways_by_genes</s:operation>
    </s:arbitrarywsdl>
  </s:processor>
  <s:processor name="Swissprot_to_Gi">
    <s:workflow>
      <s:scufl version="0.2" log="0">
        <s:workflowdescription lsid="urn:lsid:www.mygrid.org.uk:operation:P81DV9PQW02" author="" title="" />
        <s:processor name="regex" boring="true">
          <s:stringconstant>\n</s:stringconstant>
        </s:processor>
        <s:processor name="database" boring="true">
          <s:stringconstant>gene</s:stringconstant>
        </s:processor>
        <s:processor name="remove_Nulls">
          <s:beanshell>
            <s:scriptvalue>String[] split = input.split("\n");
Vector nonEmpty = new Vector();
for (int i = 0; i &lt; split.length; i++){
   if (!(split[i].equals("")))
   {
       nonEmpty.add(split[i].trim());
   }
}
String[] non_empty = new String[nonEmpty.size()];
for (int i = 0; i &lt; non_empty.length; i ++)
{
   non_empty[i] = nonEmpty.elementAt(i);
}
String output = "";
for (int i = 0; i &lt; non_empty.length; i++)
{
	output = output + (String) (non_empty[i] + "\n");
}</s:scriptvalue>
            <s:beanshellinputlist>
              <s:beanshellinput s:syntactictype="'text/plain'">input</s:beanshellinput>
            </s:beanshellinputlist>
            <s:beanshelloutputlist>
              <s:beanshelloutput s:syntactictype="'text/plain'">output</s:beanshelloutput>
            </s:beanshelloutputlist>
          </s:beanshell>
        </s:processor>
        <s:processor name="parametersXML">
          <s:local>
            org.embl.ebi.escience.scuflworkers.java.XMLInputSplitter
            <s:extensions>
              <s:complextype optional="false" unbounded="false" typename="eSearchRequest" name="parameters">
                <s:elements>
                  <s:basetype optional="true" unbounded="false" typename="string" name="db" />
                  <s:basetype optional="true" unbounded="false" typename="string" name="term" />
                  <s:basetype optional="true" unbounded="false" typename="string" name="WebEnv" />
                  <s:basetype optional="true" unbounded="false" typename="string" name="QueryKey" />
                  <s:basetype optional="true" unbounded="false" typename="string" name="usehistory" />
                  <s:basetype optional="true" unbounded="false" typename="string" name="tool" />
                  <s:basetype optional="true" unbounded="false" typename="string" name="email" />
                  <s:basetype optional="true" unbounded="false" typename="string" name="field" />
                  <s:basetype optional="true" unbounded="false" typename="string" name="reldate" />
                  <s:basetype optional="true" unbounded="false" typename="string" name="mindate" />
                  <s:basetype optional="true" unbounded="false" typename="string" name="maxdate" />
                  <s:basetype optional="true" unbounded="false" typename="string" name="datetype" />
                  <s:basetype optional="true" unbounded="false" typename="string" name="RetStart" />
                  <s:basetype optional="true" unbounded="false" typename="string" name="RetMax" />
                  <s:basetype optional="true" unbounded="false" typename="string" name="rettype" />
                  <s:basetype optional="true" unbounded="false" typename="string" name="sort" />
                </s:elements>
              </s:complextype>
            </s:extensions>
          </s:local>
        </s:processor>
        <s:processor name="split_by_regex">
          <s:local>org.embl.ebi.escience.scuflworkers.java.SplitByRegex</s:local>
        </s:processor>
        <s:processor name="merge_gis">
          <s:local>org.embl.ebi.escience.scuflworkers.java.StringListMerge</s:local>
        </s:processor>
        <s:processor name="parse_ncbi_protein_xml">
          <s:description>Extracts a GenBank id from an eSearch XML output file</s:description>
          <s:soaplabwsdl>http://phoebus.cs.man.ac.uk:1977/axis/services/text_mining.parse_ncbi_protein_xml</s:soaplabwsdl>
        </s:processor>
        <s:processor name="run_eSearch">
          <s:arbitrarywsdl>
            <s:wsdl>http://eutils.ncbi.nlm.nih.gov/entrez/eutils/soap/eutils.wsdl</s:wsdl>
            <s:operation>run_eSearch</s:operation>
          </s:arbitrarywsdl>
        </s:processor>
        <s:link source="database:value" sink="parametersXML:db" />
        <s:link source="input" sink="split_by_regex:string" />
        <s:link source="merge_gis:concatenated" sink="remove_Nulls:input" />
        <s:link source="parametersXML:output" sink="run_eSearch:parameters" />
        <s:link source="parse_ncbi_protein_xml:output" sink="merge_gis:stringlist" />
        <s:link source="regex:value" sink="split_by_regex:regex" />
        <s:link source="remove_Nulls:output" sink="removed_nulls" />
        <s:link source="run_eSearch:parameters" sink="parse_ncbi_protein_xml:new_direct_data" />
        <s:link source="split_by_regex:split" sink="parametersXML:term" />
        <s:source name="input" />
        <s:sink name="removed_nulls" />
      </s:scufl>
    </s:workflow>
  </s:processor>
  <s:processor name="merge_gene_pathways">
    <s:local>org.embl.ebi.escience.scuflworkers.java.StringListMerge</s:local>
  </s:processor>
  <s:processor name="mergePathways_2">
    <s:local>org.embl.ebi.escience.scuflworkers.java.StringListMerge</s:local>
  </s:processor>
  <s:link source="Add_ncbi_to_string:Kegg_strings" sink="Kegg_gene_ids_all_species:string" />
  <s:link source="Concatenate_two_strings:output" sink="merge_gene_pathways:stringlist" />
  <s:link source="Ensembl_gene_info:gene_info" sink="Parse_swiss_ids:gene_info" />
  <s:link source="probeset_list" sink="split_by_regex:string" />
  <s:link source="Kegg_gene_ids_all_species:return" sink="split_gene_ids:input" />
  <s:link source="Parse_swiss_ids:swiss_ids" sink="Remove_swiss_nulls:input_string" />
  <s:link source="Remove_swiss_nulls:removed_nulls" sink="Swissprot_to_Gi:input" />
  <s:link source="Swissprot_to_Gi:removed_nulls" sink="Add_ncbi_to_string:Gi_numbers" />
  <s:link source="comma:value" sink="merge_probesets:seperator" />
  <s:link source="get_pathways_by_genes:return" sink="merge_pathways:stringlist" />
  <s:link source="getcurrentdatabase:output" sink="probeset_to_gene:database" />
  <s:link source="lister:listerReturn" sink="get_pathways_by_genes:genes_id_list" />
  <s:link source="mergePathways_2:concatenated" sink="merged_kegg_pathways" />
  <s:link source="merge_pathways:concatenated" sink="Concatenate_two_strings:string2" />
  <s:link source="merge_pathways:concatenated" sink="mergePathways_2:stringlist" />
  <s:link source="merge_gene_pathways:concatenated" sink="gene_and_pathway" />
  <s:link source="merge_probesets:concatenated" sink="probeset_to_gene:probeset_list" />
  <s:link source="probeset_to_gene:output" sink="split_by_regex_2:string" />
  <s:link source="regex1:value" sink="split_by_regex:regex" />
  <s:link source="regex1:value" sink="split_by_regex_2:regex" />
  <s:link source="species:value" sink="getcurrentdatabase:species" />
  <s:link source="split_by_regex:split" sink="merge_probesets:stringlist" />
  <s:link source="split_by_regex_2:split" sink="Concatenate_two_strings:string1" />
  <s:link source="split_by_regex_2:split" sink="Ensembl_gene_info:gene_in_region" />
  <s:link source="split_gene_ids:output" sink="lister:file" />
  <s:source name="probeset_list" />
  <s:sink name="merged_kegg_pathways" />
  <s:sink name="gene_and_pathway" />
</s:scufl>
"""

# An old style workflow with <source>name</source> instead of 
# <source name="name" />   (Note how this is still the same namespace
# and version... ARGH!)
OLD_WORKFLOW="""<?xml version="1.0" encoding="UTF-8"?>
<s:scufl xmlns:s="http://org.embl.ebi.escience/xscufl/0.1alpha" version="0.2" log="0">
  <s:processor name="format">
    <s:stringconstant>-w</s:stringconstant>
  </s:processor>
  <s:processor name="FASTA">
    <s:stringconstant>-F</s:stringconstant>
  </s:processor>
  <s:processor name="Protein_type">
    <s:stringconstant>nonplant</s:stringconstant>
  </s:processor>
  <s:processor name="nr">
    <s:stringconstant>nr</s:stringconstant>
  </s:processor>
  <s:processor name="tblastn">
    <s:stringconstant>tblastn</s:stringconstant>
  </s:processor>
  <s:processor name="blastp">
    <s:stringconstant>blastp</s:stringconstant>
  </s:processor>
  <s:processor name="PSORTII">
    <s:description>Predicts peptide localisation signals</s:description>
    <s:soaplabwsdl>http://phoebus.cs.man.ac.uk:8081/axis/services/seq_analysis::psortiiwrapper</s:soaplabwsdl>
  </s:processor>
  <s:processor name="blastwrapper2">
    <s:description>Wrapper for BLAST at NCBI</s:description>
    <s:soaplabwsdl>http://phoebus.cs.man.ac.uk:8081/axis/services/seq_analysis::blastwrapper2</s:soaplabwsdl>
  </s:processor>
  <s:processor name="sumoplot">
    <s:description>wrapper to run SUMOplot</s:description>
    <s:soaplabwsdl>http://phoebus.cs.man.ac.uk:8081/axis/services/seq_analysis::sumoplot</s:soaplabwsdl>
  </s:processor>
  <s:processor name="targetp">
    <s:description>wrapper to run TargetP</s:description>
    <s:soaplabwsdl>http://phoebus.cs.man.ac.uk:8081/axis/services/seq_analysis::targetp</s:soaplabwsdl>
  </s:processor>
  <s:processor name="signalp">
    <s:description>wrapper to run SignalP</s:description>
    <s:soaplabwsdl>http://phoebus.cs.man.ac.uk:8081/axis/services/seq_analysis::signalp</s:soaplabwsdl>
  </s:processor>
  <s:processor name="blastwrapper21">
    <s:description>Wrapper for BLAST at NCBI</s:description>
    <s:soaplabwsdl>http://phoebus.cs.man.ac.uk:8081/axis/services/seq_analysis::blastwrapper2</s:soaplabwsdl>
  </s:processor>
  <s:processor name="copyright">
    <s:description>script to generate copyright statement</s:description>
    <s:soaplabwsdl>http://phoebus.cs.man.ac.uk:8081/axis/services/documentation::copyright</s:soaplabwsdl>
  </s:processor>
  <s:processor name="PepStats">
    <s:description>Protein statistics</s:description>
    <s:soaplabwsdl>http://industry.ebi.ac.uk/soap/soaplab/protein_composition::pepstats</s:soaplabwsdl>
  </s:processor>
  <s:processor name="proteinraw">
    <s:description>InterProScan with output in TXT format, for protein</s:description>
    <s:soaplabwsdl>http://industry.ebi.ac.uk/soap/soaplab/interproscan::proteinraw</s:soaplabwsdl>
  </s:processor>
  <s:processor name="iPSORT">
    <s:description>Predicts peptide localisation signals</s:description>
    <s:soaplabwsdl>http://phoebus.cs.man.ac.uk:8081/axis/services/seq_analysis::ipsortwrapper</s:soaplabwsdl>
  </s:processor>
  <s:processor name="HelixTurnHelix">
    <s:description>Report nucleic acid binding motifs</s:description>
    <s:soaplabwsdl>http://industry.ebi.ac.uk/soap/soaplab/protein_2d_structure::helixturnhelix</s:soaplabwsdl>
  </s:processor>
  <s:processor name="pscan">
    <s:description>Scans proteins using PRINTS</s:description>
    <s:soaplabwsdl>http://industry.ebi.ac.uk/soap/soaplab/protein_motifs::pscan</s:soaplabwsdl>
  </s:processor>
  <s:processor name="pepcoil">
    <s:description>Predicts coiled coil regions</s:description>
    <s:soaplabwsdl>http://industry.ebi.ac.uk/soap/soaplab/protein_2d_structure::pepcoil</s:soaplabwsdl>
  </s:processor>
  <s:processor name="epestfind">
    <s:description>Finds PEST motifs as potential proteolytic
                  cleavage sites</s:description>
    <s:soaplabwsdl>http://industry.ebi.ac.uk/soap/soaplab/protein_motifs::epestfind</s:soaplabwsdl>
  </s:processor>
  <s:processor name="pepwindow">
    <s:description>Displays protein hydropathy</s:description>
    <s:soaplabwsdl>http://industry.ebi.ac.uk/soap/soaplab/protein_composition::pepwindow</s:soaplabwsdl>
  </s:processor>
  <s:link source="PROTEINFASTA" sink="PepStats:sequence_direct_data" />
  <s:link source="PROTEINFASTA" sink="PSORTII:proteinseq_direct_data" />
  <s:link source="PROTEINFASTA" sink="iPSORT:proteinseq_direct_data" />
  <s:link source="PROTEINFASTA" sink="HelixTurnHelix:sequence_direct_data" />
  <s:link source="FASTA:value" sink="iPSORT:fasta" />
  <s:link source="format:value" sink="PSORTII:format" />
  <s:link source="PepStats:outfile" sink="PepStats_Out" />
  <s:link source="PSORTII:output" sink="ProteinLocation" />
  <s:link source="HelixTurnHelix:outfile" sink="HTH_NucBindingSites" />
  <s:link source="iPSORT:output" sink="ProteinLocation_2" />
  <s:link source="Protein_type:value" sink="iPSORT:proteintype" />
  <s:link source="PROTEINFASTA" sink="signalp:seqfile_direct_data" />
  <s:link source="signalp:outfile_url" sink="SP_url" />
  <s:link source="signalp:outfile_seq" sink="SP_outseq" />
  <s:link source="PROTEINFASTA" sink="sumoplot:seqfile_direct_data" />
  <s:link source="sumoplot:output" sink="sumo_out" />
  <s:link source="copyright:out" sink="COPYRIGHT_INFO" />
  <s:link source="PROTEINFASTA" sink="blastwrapper2:query_file_direct_data" />
  <s:link source="blastp:value" sink="blastwrapper2:program" />
  <s:link source="nr:value" sink="blastwrapper2:database" />
  <s:link source="blastwrapper2:output" sink="Blastp_nr_out" />
  <s:link source="PROTEINFASTA" sink="blastwrapper21:query_file_direct_data" />
  <s:link source="nr:value" sink="blastwrapper21:database" />
  <s:link source="tblastn:value" sink="blastwrapper21:program" />
  <s:link source="blastwrapper21:output" sink="tblastn_nr_out" />
  <s:link source="PROTEINFASTA" sink="targetp:seqfile_direct_data" />
  <s:link source="targetp:outfile_url" sink="TargetP_out" />
  <s:link source="PROTEINFASTA" sink="pscan:sequence_direct_data" />
  <s:link source="pscan:outfile" sink="pscan_out" />
  <s:link source="PROTEINFASTA" sink="pepcoil:sequence_direct_data" />
  <s:link source="pepcoil:outfile" sink="pepcoil_out" />
  <s:link source="PROTEINFASTA" sink="epestfind:sequence_direct_data" />
  <s:link source="epestfind:outfile" sink="ePEST_out" />
  <s:link source="PROTEINFASTA" sink="proteinraw:sequence" />
  <s:link source="proteinraw:result" sink="InterproRaw_out" />
  <s:link source="epestfind:Graphics_in_PNG" sink="epest_GraphicsPNG" />
  <s:link source="PROTEINFASTA" sink="pepwindow:sequence_direct_data" />
  <s:link source="pepwindow:Graphics_in_PNG" sink="pepwindow_Graphics" />
  <s:source>
    PROTEINFASTA
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>text/plain</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:source>
  <s:sink>
    PepStats_Out
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>text/plain</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:sink>
  <s:sink>
    HTH_NucBindingSites
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>text/plain</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:sink>
  <s:sink>
    ProteinLocation
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>text/plain</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:sink>
  <s:sink>
    ProteinLocation_2
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>text/plain</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:sink>
  <s:sink>
    SP_url
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>text/plain</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:sink>
  <s:sink>
    SP_outseq
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>text/plain</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:sink>
  <s:sink>
    sumo_out
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>text/plain</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:sink>
  <s:sink>
    COPYRIGHT_INFO
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>text/plain</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:sink>
  <s:sink>
    Blastp_nr_out
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>text/plain</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:sink>
  <s:sink>
    tblastn_nr_out
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>text/plain</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:sink>
  <s:sink>
    TargetP_out
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>text/plain</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:sink>
  <s:sink>
    pscan_out
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>text/plain</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:sink>
  <s:sink>
    pepcoil_out
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>text/plain</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:sink>
  <s:sink>
    ePEST_out
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>text/plain</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:sink>
  <s:sink>
    InterproRaw_out
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>text/plain</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:sink>
  <s:sink>
    epest_GraphicsPNG
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>application/octet-stream</s:mimeType>
        <s:mimeType>image/png</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:sink>
  <s:sink>
    pepwindow_Graphics
    <s:metadata>
      <s:mimeTypes>
        <s:mimeType>application/octet-stream</s:mimeType>
        <s:mimeType>image/png</s:mimeType>
      </s:mimeTypes>
    </s:metadata>
  </s:sink>
</s:scufl>
"""

