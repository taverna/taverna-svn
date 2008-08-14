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
	
	private SemanticServiceRegistry ssr = null;
	
	public OPMGraphAnnotator(DatasetService dss) {
		this.dss = dss;
		this.ssr = new SemanticServiceRegistry();  
	}

	// query the input RDF looking for recognizable patterns of the form:
	// 1- resource that is a known service name. For example, NCBI_Web_Entrez_Service
	// here the service is annotated with its corresponding semantic class, in the example Entrez_GenBank_Protein

	// 2- ?result wasGeneratedBy ?s  where ?s is a known service
	// here ?result is annotated with the (only) output from ?s, in the example Entrez_Gene_Record

	// 3- ?s used ?input  where ?s is a known service
	// here ?input is annotated, e.g. dbgeneQuery -- note that this is not in the myGrid ontology!!

	// load RDF and issue a SPARQL query to it

	public String annotateRDF(String rdfContent)  {

		// 1 - fetch all process nodes to see if we recognize any service in our registry
		// in this case, the service can be annotated

		String queryResult = new String();

		QueryResult result = null;

		String query = Util.textFileToContent(QUERY_ALL_PROCESS_NODES);

		org.openrdf.model.URI  namedGraphUri = null;
		
		try {

			// upload RDF content to a temp graph that Anzo can query
			namedGraphUri = createGraphForQuery(rdfContent, dss);

			// then query this graph
			result = dss.execQuery(Collections.singleton(namedGraphUri), Collections.<org.openrdf.model.URI> emptySet(), query);

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

		if (result != null) {  // assume it's a select query

			TupleQueryResult selectResult = result.getSelectResult();

			List<String> bindingNames = selectResult.getBindingNames();

			// is there any service to annotate among the processes the executed according to this log?
			try {
				while (selectResult.hasNext()) {

					BindingSet bs = selectResult.next();

					for (Binding b:bs) {
						System.out.println("binding: "+b.getName()+ " -> "+b.getValue());
						
						URI serviceName = new URI(b.getValue().toString());
						SSSD serviceAnnotations = ssr.getSSSD(serviceName);
						
						if (serviceAnnotations != null) {
							System.out.println("found annotations for service "+b.getValue());
							
							// annotate the service itself and then navigate the graph looking for its inputs and outputs
							// NOTE: limited to 1 in / 1 out at the moment
							
							URI serviceClass = serviceAnnotations.getServiceClass();
							
							org.openrdf.model.URI obj  = dss.getValueFactory().createURI(serviceClass.toString());
							org.openrdf.model.URI pred = dss.getValueFactory().createURI("rdf:type");
							org.openrdf.model.URI subj = dss.getValueFactory().createURI(serviceName.toString());
							
							// add new triple to the graph
							LocalGraph g = dss.getLocalGraph(namedGraphUri, false, false);
							
							g.add(subj, pred, obj);
							
							System.out.println("statement added (maybe!)");
						}
					}
				}
			} catch (QueryEvaluationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AnzoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return null; // TODO
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
}
