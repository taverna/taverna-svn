package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.man.cs.img.fetaEngine.commons.FetaModelRDF;
import uk.ac.man.cs.img.fetaEngine.commons.ServiceType;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdql.Query;
import com.hp.hpl.jena.rdql.QueryEngine;
import com.hp.hpl.jena.rdql.QueryExecution;
import com.hp.hpl.jena.rdql.QueryResults;
import com.hp.hpl.jena.rdql.ResultBinding;

public class FetaRDFWrapper implements IServiceModelFiller {

	private static Logger logger = Logger.getLogger(FetaRDFWrapper.class);
	
	private String operationName;

	private String serviceName;

	private String descriptionLocation;

	private String operationDescriptionText;

	private String serviceDescriptionText;

	private String serviceInterfaceLocation;

	private String locationURL;

	private ServiceType serviceType;

	private String organisationName;

	private String operationTask;

	private String operationMethod;

	//private String operationApplication;

	private String operationResource;

	//private String operationResourceContent;

	private String operationSpec;

	/** 
	 * Create a new instance of FetaRDFWrapper 
	 * 
	 * @throws IOException
	 */
	public FetaRDFWrapper(String operID) throws IOException {
		logger.debug("Finding feta service description " + operID);
		// tokenize location, service name and operation name
		String[] tokens = operID.split("\\$");
		if (tokens.length != 3) {
			throw new IllegalArgumentException("Could not parse operation id " + operID);
		}

		descriptionLocation = tokens[0];
		serviceName = tokens[1];
		operationName = tokens[2];

		URL descURL = new URL(descriptionLocation);
		Model m1 = ModelFactory.createDefaultModel();
		m1.read(descURL.openStream(), null);
		String query = " SELECT  ?serv, ?oper \n"
			+ " WHERE (?serv, mg:hasServiceNameText, ?servName), \n"
			+ " (?serv, mg:hasOperation, ?oper), \n"
			+ " (?oper, mg:hasOperationNameText, ?operName) \n"
			+ "  AND (?operName =~ /" + operationName
			+ "/ &&  ?servName =~ /" + serviceName + "/ ) \n"
			+ " USING  mg for <" + FetaModelRDF.MYGRID_MOBY_SERVICE_NS
			+ ">\n";

		List<Map<String, RDFNode>> results = processQuery(m1, query);
		if (results.size() < 0) {
			logger.warn("Could not find " + operID);
			throw new IllegalArgumentException("Could not find operation " + operID);
		}
		Map<String, RDFNode> localResults = results.get(0);
		
		Resource serviceResource = (Resource) localResults.get("serv");
		Resource operationResource = (Resource) localResults.get("oper");

		operationDescriptionText = getLiteralObjectValue(m1,
			operationResource, FetaModelRDF.hasOperationNameText);
		serviceDescriptionText = getLiteralObjectValue(m1,
			serviceResource, FetaModelRDF.hasServiceDescriptionText);
		organisationName = getLiteralObjectValue(m1,
			serviceResource, FetaModelRDF.hasOrganisationNameText);
		locationURL = getLiteralObjectValue(m1, serviceResource,
			FetaModelRDF.locationURI);
		serviceInterfaceLocation = getLiteralObjectValue(m1,
			serviceResource, FetaModelRDF.hasInterfaceLocation);
		String serviceTypeStr = getLiteralObjectValue(m1,
			serviceResource, FetaModelRDF.DC_PATCHED_Format);

		// not really we should not do this
		if (serviceTypeStr == null) {
			this.serviceType = ServiceType.WSDL;
		} else {
			this.serviceType = ServiceType
			.getTypeForRDFLiteralString(serviceTypeStr);
		}

		if (serviceType == ServiceType.BIOMOBY) {

			this.organisationName = getLiteralObjectValue(m1, null,
				FetaModelRDF.DC_PATCHED_Publisher);

		}

//		 operationMethod =
//			getTypedObjectValue(operationResource, FetaModelXSD.OPER_METHOD);
//		operationTask =
//			getFirstSubElementValue(operationResource, FetaModelXSD.OPER_TASK);
//		operationResource =
//			getFirstSubElementValue(operationResource,
//				FetaModelXSD.OPER_RESOURCE);
//		operationSpec =
//			getFirstSubElementValue(operationResource,
//				FetaModelXSD.OPERATION_SPEC);
	}

	public String getLiteralObjectValue(Model modl, Resource oper, Property prop) {
		Selector selector = new SimpleSelector(oper, prop, (RDFNode) null);
		StmtIterator iter1 = modl.listStatements(selector);
		while (iter1.hasNext()) {
			Statement stmt = iter1.nextStatement();
			Literal val = (Literal) stmt.getObject();
			return val.getString();
		}
		return null;
	}

	/* Operation Related */
	public String getOperationName() {
		return operationName;
	}

	public String getOperationDescriptionText() {
		return operationDescriptionText;
	}

	/* Operation Annotation Related */
	public String getOperationMethod() {
		return operationMethod;
	}

	public String getOperationTask() {
		return operationTask;
	}

	/*
	 * public String getOperationApplication(){ return
	 * this.operationApplication; }
	 */
	public String getOperationResource() {
		return operationResource;
	}

	/*
	 * public String getOperationResourceContent(){ return
	 * this.operationResourceContent; }
	 */
	public String getOperationSpec() {
		return operationSpec;
	}

	/* Service Related */
	public String getServiceName() {
		return serviceName;
	}

	public String getDescriptionLocation() {
		return descriptionLocation;
	}

	public String getServiceDescriptionText() {
		return serviceDescriptionText;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public String getServiceInterfaceLocation() {
		return serviceInterfaceLocation;
	}

	public String getLocationURL() {
		return locationURL;
	}

	public String getOrganisationName() {
		return organisationName;
	}

	/* We do not have any setter methods! */

	@SuppressWarnings("unchecked")
	public static List<Map<String, RDFNode>> processQuery(Model m, String rdqlQuery) {

		List<Map<String, RDFNode>> results = new ArrayList<Map<String, RDFNode>>();
		try {
			Query query = new Query(rdqlQuery);
			query.setSource(m);
			QueryExecution qe = new QueryEngine(query);
			QueryResults queryResults = qe.exec();
			try {
				for (Iterator iter = queryResults; iter.hasNext();) {
					ResultBinding rb = (ResultBinding) iter.next();
					Map<String, RDFNode> localResult = new HashMap<String, RDFNode>();
					results.add(localResult);
					
					Iterator<String> namesIter = rb.names();
					while (namesIter.hasNext()) {
						String variableName = namesIter.next();
						RDFNode r = (RDFNode) rb.get(variableName);
						localResult.put(variableName, r);
					}
				} 
			} finally {
				queryResults.close();
			}
		} catch (Exception ex) {
			logger.error("Could not query " + rdqlQuery, ex);
		}
		return results;
	}

	public Map<String, String> getInputParameters() {
		// TODO Perform SparQL Query for parameters
		return null;
	}

	public Map<String, String> getOutputParameters() {
		// TODO Perform SparQL Query for parameters
		return null;
	}

}
