/**
 * 
 */
package uk.org.mygrid.sogsa.sbs.semanticAnnotation;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.openanzo.client.DatasetService;
import org.openanzo.client.LocalGraph;
import org.openanzo.client.RemoteGraph;
import org.openanzo.common.exceptions.AnzoException;
import org.openanzo.model.impl.query.QueryResult;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.StatementCollector;

import uk.org.mygrid.sogsa.sbs.utils.Util;

/**
 * @author paolo
 * 
 *
 */
public class OPMGraphAnnotator {

	private DatasetService dss = null;
	private String DUMMY_KEY = "http://123";  // only used to refer to the temp localGraph that we are going to query

	private final String QUERY_ALL_PROCESS_NODES = "query_allProcessNodes.txt";
	private final String QUERY_PROCESS_RESULT_NODES = "query_ProcessResultNodes.txt";
	private final String QUERY_PROCESS_INPUT_NODES = "query_ProcessInputNodes.txt";
	

	private SemanticServiceRegistry ssr = null;
	private org.openrdf.model.URI  namedGraphUri = null;
	private SSSD serviceAnnotations = null;

	public OPMGraphAnnotator(DatasetService dss) {
		this.dss = dss;
		this.ssr = new SemanticServiceRegistry();  
	}

	// 2- ?result wasGeneratedBy ?s  where ?s is a known service
	// here ?result is annotated with the (only) output from ?s, in the example Entrez_Gene_Record

	// 3- ?s used ?input  where ?s is a known service
	// here ?input is annotated, e.g. dbgeneQuery -- note that this is not in the myGrid ontology!!

	// load RDF and issue a SPARQL query to it

	public LocalGraph annotateRDF(String rdfContent) throws AnzoException  {

		// 1 - fetch all process nodes to see if we recognize any service in our registry
		// in this case, the service can be annotated

		org.openrdf.model.URI  namedGraphUri = null;
		SSSD serviceAnnotations = null;

		// query the input RDF looking for recognizable patterns of the form:
		// 1- resource that is a known service name. For example, NCBI_Web_Entrez_Service
		// here the service is annotated with its corresponding semantic class, in the example Entrez_GenBank_Protein		
		URI serviceName = annotateService(rdfContent);

		if (serviceName == null) {

			System.out.println("no services with associated annotations found");
			return dss.getLocalGraph(getNamedGraphUri(), false, false); // no annotable services found
		}

		annotateServiceOutput(serviceName);

		annotateServiceInput(serviceName);

		return dss.getLocalGraph(getNamedGraphUri(), false, false);
	}


	private void annotateServiceOutput(URI serviceName) throws AnzoException {

		String queryResult = new String();
		QueryResult result = null;

		System.out.println("annotating service output");
		
		String query = Util.textFileToContent(QUERY_PROCESS_RESULT_NODES);

		// then query this graph
		result = dss.execQuery(Collections.singleton(getNamedGraphUri()), Collections.<org.openrdf.model.URI> emptySet(), query);

		if (result == null) return; // nothing to annotate

		TupleQueryResult selectResult = result.getSelectResult();
		List<String> bindingNames = selectResult.getBindingNames();

		System.out.println("target service: ["+serviceName+"]");
		try {
			while (selectResult.hasNext()) {

				BindingSet bs = selectResult.next();

				Binding serviceBinding = bs.getBinding("s");
				Binding resultBinding  = bs.getBinding("r");
				
				String sb= serviceBinding.getValue().toString();
				
				System.out.println("found service ["+sb+"]");
				
				if (sb.equals(serviceName.toASCIIString())) {
					
					System.out.println("found binding for service "+serviceName);
					
					String resultResource = resultBinding.getValue().toString();
					
					URI serviceOutputClass = serviceAnnotations.getServiceOutputMessageClass();

					org.openrdf.model.URI obj  = dss.getValueFactory().createURI(serviceOutputClass.toString());
					org.openrdf.model.URI pred = dss.getValueFactory().createURI("rdf:type");
					org.openrdf.model.URI subj = dss.getValueFactory().createURI(resultResource);

					// add new triple to the graph
					LocalGraph g = dss.getLocalGraph(getNamedGraphUri(), false, false);

					g.add(subj, pred, obj);

					System.out.println("statement added: "+subj+ "  " + pred+ "   " + obj);
				}				
			}
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}


	
	
	private void annotateServiceInput(URI serviceName) throws AnzoException {

		String queryResult = new String();
		QueryResult result = null;

		System.out.println("annotating service input");
		
		String query = Util.textFileToContent(QUERY_PROCESS_INPUT_NODES);

		// then query this graph
		result = dss.execQuery(Collections.singleton(getNamedGraphUri()), Collections.<org.openrdf.model.URI> emptySet(), query);

		if (result == null) return; // nothing to annotate

		TupleQueryResult selectResult = result.getSelectResult();
		List<String> bindingNames = selectResult.getBindingNames();

		System.out.println("target service: ["+serviceName+"]");
		try {
			while (selectResult.hasNext()) {

				BindingSet bs = selectResult.next();

				Binding serviceBinding = bs.getBinding("s");
				Binding resultBinding  = bs.getBinding("i");
				
				String sb= serviceBinding.getValue().toString();
				
				System.out.println("found service ["+sb+"]");
				
				if (sb.equals(serviceName.toASCIIString())) {
					
					System.out.println("found binding for service "+serviceName);
					
					String resultResource = resultBinding.getValue().toString();
					
					URI serviceOutputClass = serviceAnnotations.getServiceInputMessageClass();

					org.openrdf.model.URI obj  = dss.getValueFactory().createURI(serviceOutputClass.toString());
					org.openrdf.model.URI pred = dss.getValueFactory().createURI("rdf:type");
					org.openrdf.model.URI subj = dss.getValueFactory().createURI(resultResource);

					// add new triple to the graph
					LocalGraph g = dss.getLocalGraph(getNamedGraphUri(), false, false);

					g.add(subj, pred, obj);

					System.out.println("statement added: "+subj+ "  " + pred+ "   " + obj);
				}				
			}
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}

	
	private URI annotateService(String rdfContent) throws AnzoException {

		String queryResult = new String();
		QueryResult result = null;
		URI serviceName = null;

		String query = Util.textFileToContent(QUERY_ALL_PROCESS_NODES);

		try {

			// upload RDF content to a temp graph that Anzo can query
			setNamedGraphUri(createGraphForQuery(rdfContent, dss));

			// then query this graph
			result = dss.execQuery(Collections.singleton(getNamedGraphUri()), Collections.<org.openrdf.model.URI> emptySet(), query);

		} catch (AnzoException e) {
			System.out.println(e);
		} catch (IllegalArgumentException e) {
			System.out.println(e);
		} catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (result == null) return null;

		TupleQueryResult selectResult = result.getSelectResult();
		List<String> bindingNames = selectResult.getBindingNames();

		// is there any service to annotate among the processes the executed according to this log?
		try {
			while (selectResult.hasNext()) {

				BindingSet bs = selectResult.next();

				for (Binding b:bs) {
					System.out.println("binding: "+b.getName()+ " -> "+b.getValue());

					serviceName = new URI(b.getValue().toString());
					setServiceAnnotations(ssr.getSSSD(serviceName));

					if (serviceAnnotations != null) {
						System.out.println("found annotations for service "+b.getValue());

						// annotate the service itself and then navigate the graph looking for its inputs and outputs
						// NOTE: limited to 1 in / 1 out at the moment

						URI serviceClass = serviceAnnotations.getServiceClass();

						org.openrdf.model.URI obj  = dss.getValueFactory().createURI(serviceClass.toString());
						org.openrdf.model.URI pred = dss.getValueFactory().createURI("rdf:type");
						org.openrdf.model.URI subj = dss.getValueFactory().createURI(serviceName.toString());

						// add new triple to the graph
						LocalGraph g = dss.getLocalGraph(getNamedGraphUri(), false, false);

						g.add(subj, pred, obj);

						System.out.println("statement added: "+subj+ "  " + pred+ "   " + obj);

					}
				}
			}
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return serviceName;

	}



	private org.openrdf.model.URI createGraphForQuery(String rdfContent, DatasetService dss) throws AnzoException, RDFParseException, RDFHandlerException, IOException {

		org.openrdf.model.URI namedGraphUri = dss.getValueFactory().createURI(DUMMY_KEY);

		LocalGraph localGraph = dss.getLocalGraph(namedGraphUri, true, false);		

		StatementCollector sc = new StatementCollector();
		RDFParser parser = Rio.createParser(RDFFormat.RDFXML);
		parser.setRDFHandler(sc);
		parser.parse(new StringReader(rdfContent), "");

		dss.begin();

		for (Statement statement : sc.getStatements()) {
			localGraph.add(statement);
		}
		dss.commit();

		return namedGraphUri;
	}

	/**
	 * @return the namedGraphUri
	 */
	public org.openrdf.model.URI getNamedGraphUri() {
		return namedGraphUri;
	}

	/**
	 * @param namedGraphUri the namedGraphUri to set
	 */
	public void setNamedGraphUri(org.openrdf.model.URI namedGraphUri) {
		this.namedGraphUri = namedGraphUri;
	}

	/**
	 * @return the serviceAnnotations
	 */
	public SSSD getServiceAnnotations() {
		return serviceAnnotations;
	}

	/**
	 * @param serviceAnnotations the serviceAnnotations to set
	 */
	public void setServiceAnnotations(SSSD serviceAnnotations) {
		this.serviceAnnotations = serviceAnnotations;
	}    
}
