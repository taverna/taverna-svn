/**
 * 
 */
package uk.org.mygrid.sogsa.sbs.semanticAnnotation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author paolo
 * the contents for this registry are currently hard-coded, should be populated from a SAWSDL description of the service<br/>
 * holds a map <service name> --> <SSSD>  (SimpleSemanticServiceDescriptor). The service name is actually an 
 * RDF resource (a URI)
 */
public class SemanticServiceRegistry {
	
	Map<URI, SSSD> registry = new HashMap<URI, SSSD>();

	public SemanticServiceRegistry() {
		
		SSSD entry = null;

		// NCBI Entrez for gene lookup
		entry = new SSSD();		
		try {
			entry.setServiceClass(new URI("http://www.mygrid.org.uk/ontology#Entrez_GenBank_protein"));
			entry.setServiceInputMessageClass(new URI("http://www.mygrid.org.uk/ontology#dbgeneQuery"));
			entry.setServiceOutputMessageClass(new URI("http://www.mygrid.org.uk/ontology#Entrez_Gene_ID"));
			entry.setServiceOpClass(new URI("http://www.owl-ontologies.com/unnamed.owl#run_eFetch_dbGene"));
			
			registry.put(new URI("http://www.owl-ontologies.com/unnamed.owl#NCBI_Web_Entrez_GeneLookup_Service"), entry);
			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// NCBI Entrez for pubmed abstracts lookup
		entry = new SSSD();		
		try {
			entry.setServiceClass(new URI("http://www.owl-ontologies.com/unnamed.owl#Entrez_Pubmed"));
			entry.setServiceInputMessageClass(new URI("http://www.mygrid.org.uk/ontology#dbpubmedQuery"));
			entry.setServiceOutputMessageClass(new URI("http://www.mygrid.org.uk/ontology#PubMed_id"));
			entry.setServiceOpClass(new URI("http://www.owl-ontologies.com/unnamed.owl#run_eFetch_dbGene"));
			
			registry.put(new URI("http://www.owl-ontologies.com/unnamed.owl#NCBI_Web_Entrez_PubmedLookup_Service"), entry);
			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	public SSSD getSSSD(URI key) {
		return registry.get(key);
	}

}

