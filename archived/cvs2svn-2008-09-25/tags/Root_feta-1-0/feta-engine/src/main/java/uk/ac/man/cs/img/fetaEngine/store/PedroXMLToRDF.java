/*
 *
 * Copyright (C) 2003 The University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 */
package uk.ac.man.cs.img.fetaEngine.store;

/**
 * @author alperp
 * 
 */
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import uk.ac.man.cs.img.fetaEngine.commons.FetaModelRDF;
import uk.ac.man.cs.img.fetaEngine.commons.FetaModelXSD;
import uk.ac.man.cs.img.fetaEngine.store.load.FetaLoad;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.RDF;

public class PedroXMLToRDF {

	/** The services in RDF format */
	Model services;

	/** A configured XMLConverter suitable for MyGrid */
	XMLConverter myGridConverter;


	public static void main(String[] args){
		try{
			String operationURI = "file:///c:/desc.xml";
			List locList = new ArrayList();
			locList.add(operationURI);
			PedroXMLToRDF rdfConverter = new PedroXMLToRDF();
			URL operationURL = new URL(operationURI);
			Map docAsRDF = new HashMap();
			docAsRDF = new FetaLoad().readFetaDescriptions(locList, 'u');
			String RDFXMLStr = rdfConverter.convertToRdfXml((Document) docAsRDF.get(operationURL.toString()));
			System.out.println(RDFXMLStr);
		}catch(Exception exp){
			
			
		}

	}
	
	public PedroXMLToRDF() {

		init();

	}

	/**
	 * Initialize.
	 */
	public void init() {

		Map serviceTypes = new HashMap();
		serviceTypes.put("soaplab service", "soaplab");
		serviceTypes.put("wsdl service", "wsdl");
		serviceTypes.put("workflow Service", "scufl");
		serviceTypes.put("biomoby service", "moby");
		serviceTypes.put("seqhound service", "seqhound");
		serviceTypes.put("local java widget", "localjava");
		serviceTypes.put("talisman service", "talisman");
		serviceTypes.put("biomart service", "biomart");
		serviceTypes.put("beanshell service", "beanshell");
		serviceTypes.put("inferno service", "inferno");

		// Configure an XMLConverter instance to handle the MyGrid mapping
		myGridConverter = new XMLConverter();

		myGridConverter.register(FetaModelXSD.SERVICE_DESCS,
				new XMLConverter.ConvertClass(null, null));

		myGridConverter.register(FetaModelXSD.SERVICE_DESC,
				new XMLConverter.ConvertClass(null,
						FetaModelRDF.serviceDescription));

		myGridConverter.register(FetaModelXSD.ORGANISATION,
				new XMLConverter.ConvertClass(FetaModelRDF.providedBy,
						FetaModelRDF.organisation));

		myGridConverter.register(FetaModelXSD.ORGANISATION_NAME,
				new XMLConverter.ConvertLiteral(
						FetaModelRDF.hasOrganisationNameText));

		myGridConverter.register(FetaModelXSD.ORGANISATION_DESC_TEXT,
				new XMLConverter.ConvertLiteral(
						FetaModelRDF.hasOrganisationDescriptionText));

		myGridConverter.register(FetaModelXSD.LOCATION_URL,
				new XMLConverter.ConvertLiteral(FetaModelRDF.locationURI));

		myGridConverter.register(FetaModelXSD.INTERFACE_WSDL,
				new XMLConverter.ConvertLiteral(
						FetaModelRDF.hasInterfaceLocation));

		myGridConverter.register(FetaModelXSD.SERV_DESC_TEXT,
				new XMLConverter.ConvertLiteral(
						FetaModelRDF.hasServiceDescriptionText));

		myGridConverter
				.register(FetaModelXSD.SERVICE_NAME,
						new XMLConverter.ConvertLiteral(
								FetaModelRDF.hasServiceNameText));

		myGridConverter.register(FetaModelXSD.SERVICE_TYPE,
				new XMLConverter.ConvertLiteralWithLookupTable(
						FetaModelRDF.DC_PATCHED_Format, serviceTypes));

		myGridConverter.register("serviceDescriptionLocation",
				new XMLConverter.ConvertLiteral(
						FetaModelRDF.hasServiceDescriptionLocation));

		myGridConverter.register(FetaModelXSD.OPERATION_NAME,
				new XMLConverter.ConvertLiteral(
						FetaModelRDF.hasOperationNameText));

		myGridConverter.register(FetaModelXSD.OPER_DESC_TEXT,
				new XMLConverter.ConvertLiteral(
						FetaModelRDF.hasOperationDescriptionText));

		myGridConverter.register(FetaModelXSD.OPERATION_SPEC,
				new XMLConverter.ConvertLiteral(
						FetaModelRDF.hasTavernaProcessorSpec));

		myGridConverter.register(FetaModelXSD.SERVICE_OPERATION,
				new XMLConverter.ConvertClass(null, FetaModelRDF.operation));

		myGridConverter.register(FetaModelXSD.OPERATIONS,
				new XMLConverter.ConvertProperty(FetaModelRDF.hasOperation));

		myGridConverter.register(FetaModelXSD.OPERATION_INPUTS,
				new XMLConverter.ConvertProperty(FetaModelRDF.inputParameter));

		myGridConverter.register(FetaModelXSD.OPERATION_OUTPUTS,
				new XMLConverter.ConvertProperty(FetaModelRDF.outputParameter));

		myGridConverter.register(FetaModelXSD.PARAMETER,
				new XMLConverter.ConvertClass(null, FetaModelRDF.parameter));

		myGridConverter.register(FetaModelXSD.PARAMETER_NAME,
				new XMLConverter.ConvertLiteral(
						FetaModelRDF.hasParameterNameText));

		myGridConverter.register(FetaModelXSD.PARAMETER_DESC,
				new XMLConverter.ConvertLiteral(
						FetaModelRDF.hasParameterDescriptionText));

		// myGridConverter.register(FetaModelXSD.IS_CONFIG_PARAM,
		// new XMLConverter.ConvertBoolean(FetaModelRDF.isConfiguration));

		/*
		 * myGridConverter.register(FetaModelXSD.SEMANTIC_TYPE, new
		 * XMLConverter.ConvertResourceDoubleProp(FetaModelRDF.mygInstance,
		 * RDF.type, FetaModelRDF.NS));
		 */
		myGridConverter.register(FetaModelXSD.SEMANTIC_TYPE,
				new XMLConverter.ConvertTypedResource(
						FetaModelRDF.inNamespaces, RDF.type,
						FetaModelRDF.parameterNameSpace, FetaModelRDF.NS));

		myGridConverter.register(FetaModelXSD.OPER_TASK,
				new XMLConverter.ConvertTypedResource(
						FetaModelRDF.performsTask, RDF.type, FetaModelRDF.task,
						FetaModelRDF.NS));

		myGridConverter.register(FetaModelXSD.OPER_RESOURCE,
				new XMLConverter.ConvertTypedResource(
						FetaModelRDF.usesResource, RDF.type,
						FetaModelRDF.resource, FetaModelRDF.NS));

		myGridConverter.register(FetaModelXSD.OPER_RESOURCE_CONTENT,
				new XMLConverter.ConvertTypedResource(
						FetaModelRDF.hasResourceContent, RDF.type,
						FetaModelRDF.resourceContent, FetaModelRDF.NS));

		myGridConverter.register(FetaModelXSD.OPER_METHOD,
				new XMLConverter.ConvertTypedResource(FetaModelRDF.usesMethod,
						RDF.type, FetaModelRDF.method, FetaModelRDF.NS));

		myGridConverter.register(FetaModelXSD.OPER_APP,
				new XMLConverter.ConvertTypedResource(
						FetaModelRDF.isFunctionOf, RDF.type,
						FetaModelRDF.application, FetaModelRDF.NS));

		myGridConverter.register(FetaModelXSD.PARAM_FORMAT,	new XMLConverter.ConvertTypedResource(
				FetaModelRDF.objectType, RDF.type,
				null, FetaModelRDF.NS));

		
		myGridConverter.register(FetaModelXSD.PARAM_COLLECTION_TYPE, new XMLConverter.ConvertTypedResource(
				FetaModelRDF.hasParameterType, RDF.type,
				null, FetaModelRDF.MYGRID_MOBY_SERVICE_NS));
		
		}

	/**
	 * Return the RDF model containing the services data
	 */
	public String convertToRdfXml(org.w3c.dom.Document doc) throws IOException {

		// Load and convert the DOM trees to RDF
		try {

			services = myGridConverter.generateRDFModel(doc);
			services.setNsPrefix("mygdomain", FetaModelRDF.NS);
			services.setNsPrefix("MygMobyService",
					FetaModelRDF.MYGRID_MOBY_SERVICE_NS);

			// Write it out --just to see the RDF--
			StringWriter resultWriter = new StringWriter();
			services.write(resultWriter, "RDF/XML-ABBREV");
			resultWriter.flush();
			System.out.println("RDF generated for the given XML file");
			// System.out.println(resultWriter.toString());
			return resultWriter.toString();

		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return null;
	}

	public Model getModel() {
		return this.services;
	}

}
