package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

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

	private String operationApplication;

	private String operationResource;

	private String operationResourceContent;

	private String operationSpec;

	/** Creates a new instance of FetaRDFWrapper */
	public FetaRDFWrapper(String operID) throws Exception {

		// tokenize location, service name and operation name
		String[] tokens = operID.split("\\$");
		if (tokens.length != 3) {
			Exception e = new Exception();
			throw e;
		}

		this.operationName = tokens[2];
		this.serviceName = tokens[1];
		this.descriptionLocation = tokens[0];

		try {
			URL descURL = new URL(descriptionLocation);
			Model m1 = ModelFactory.createDefaultModel();
			m1.read(descURL.openStream(), null);

			String[] arr = { "serv", "oper" };

			String query = " SELECT  ?serv, ?oper \n"
					+ " WHERE (?serv, mg:hasServiceNameText, ?servName), \n"
					+ " (?serv, mg:hasOperation, ?oper), \n"
					+ " (?oper, mg:hasOperationNameText, ?operName) \n"
					+ "  AND (?operName =~ /" + operationName
					+ "/ &&  ?servName =~ /" + serviceName + "/ ) \n"
					+ " USING  mg for <" + FetaModelRDF.MYGRID_MOBY_SERVICE_NS
					+ ">\n";

			Vector reslts = processQuery(m1, query, arr);
			if (reslts.size() > 0) {
				Vector localResults = (Vector) reslts.get(0);
				Resource serviceResource = (Resource) localResults.get(0);
				Resource operationResource = (Resource) localResults.get(1);

				this.operationDescriptionText = getLiteralObjectValue(m1,
						operationResource, FetaModelRDF.hasOperationNameText);
				this.serviceDescriptionText = getLiteralObjectValue(m1,
						serviceResource, FetaModelRDF.hasServiceDescriptionText);
				this.organisationName = getLiteralObjectValue(m1,
						serviceResource, FetaModelRDF.hasOrganisationNameText);
				this.locationURL = getLiteralObjectValue(m1, serviceResource,
						FetaModelRDF.locationURI);
				this.serviceInterfaceLocation = getLiteralObjectValue(m1,
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

				// this.operationMethod = getTypedObjectValue(operationResource,
				// FetaModelXSD.OPER_METHOD);
				// this.operationTask =
				// getFirstSubElementValue(operationResource,
				// FetaModelXSD.OPER_TASK);
				// this.operationResource =
				// getFirstSubElementValue(operationResource,
				// FetaModelXSD.OPER_RESOURCE);
				// this.operationSpec =
				// getFirstSubElementValue(operationResource,
				// FetaModelXSD.OPERATION_SPEC);

			}
		}// try
		catch (Exception e) {

			throw e;
		}

	}

	public String getLiteralObjectValue(Model modl, Resource oper, Property prop) {
		Selector selector = new SimpleSelector(oper, prop, (RDFNode) null);
		StmtIterator iter1 = (StmtIterator) modl.listStatements(selector);
		String valStr = null;
		if (iter1.hasNext()) {

			while (iter1.hasNext()) {
				Statement stmt = iter1.nextStatement();
				Literal val = (Literal) stmt.getObject();
				valStr = val.getString();

			}
		}
		return valStr;

	}

	public String getResourceObjectValue(Model modl, Resource oper,
			Property prop) {
		Selector selector = new SimpleSelector(oper, prop, (RDFNode) null);
		StmtIterator iter1 = (StmtIterator) modl.listStatements(selector);
		String valStr = null;
		if (iter1.hasNext()) {

			while (iter1.hasNext()) {
				Statement stmt = iter1.nextStatement();
				Resource val = (Resource) stmt.getObject();

			}
		}
		return valStr;

	}

	/* Operation Related */
	public String getOperationName() {
		return this.operationName;
	}

	public String getOperationDescriptionText() {
		return this.operationDescriptionText;
	}

	/* Operation Annotation Related */
	public String getOperationMethod() {
		return this.operationMethod;
	}

	public String getOperationTask() {
		return this.operationTask;
	}

	/*
	 * public String getOperationApplication(){ return
	 * this.operationApplication; }
	 */
	public String getOperationResource() {
		return this.operationResource;
	}

	/*
	 * public String getOperationResourceContent(){ return
	 * this.operationResourceContent; }
	 */
	public String getOperationSpec() {
		return this.operationSpec;
	}

	/* Service Related */
	public String getServiceName() {
		return this.serviceName;
	}

	public String getDescriptionLocation() {
		return this.descriptionLocation;
	}

	public String getServiceDescriptionText() {
		return this.serviceDescriptionText;
	}

	public ServiceType getServiceType() {
		return this.serviceType;
	}

	public String getServiceInterfaceLocation() {
		return this.serviceInterfaceLocation;
	}

	public String getLocationURL() {
		return this.locationURL;
	}

	public String getOrganisationName() {
		return this.organisationName;
	}

	/* We do not have any setter methods! */

	public static Vector processQuery(Model m, String rdqlQuery,
			String[] variableNames) {

		Vector results = new Vector();
		try {
			Query query = new Query(rdqlQuery);
			query.setSource(m);
			QueryExecution qe = new QueryEngine(query);
			QueryResults queryResults = qe.exec();

			for (Iterator iter = queryResults; iter.hasNext();) {
				ResultBinding rb = (ResultBinding) iter.next();

				Vector localResult = new Vector();
				results.addElement(localResult);
				for (int i = 0; i < variableNames.length; i++) {
					String variableName = variableNames[i];
					RDFNode r = (RDFNode) rb.get(variableName);
					localResult.addElement(r);
				}
			}
			queryResults.close();
		} catch (Exception ex) {
			System.err.println("Exception: " + ex);
			ex.printStackTrace(System.err);
		}
		return results;
	}

}
