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
package uk.ac.man.cs.img.fetaEngine.commons;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class FetaModelRDF {
	/**
	 * <p>
	 * The RDF model that holds the vocabulary terms
	 * </p>
	 */
	private static Model m_model = ModelFactory.createDefaultModel();

	/**
	 * <p>
	 * The namespace of the vocabalary as a string ({@value})
	 * </p>
	 */
	public static final String NS = "http://www.mygrid.org.uk/ontology#";

	public static final String MYGRID_MOBY_SERVICE_NS = "http://www.mygrid.org.uk/mygrid-moby-service#";

	public static final String DC_PATCHED = "http://protege.stanford.edu/plugins/owl/dc/protege-dc.owl#";

	/**
	 * <p>
	 * The namespace of the vocabalary as a string
	 * </p>
	 * 
	 * @see #NS
	 */
	public static String getURI() {
		return NS;
	}

	/**
	 * <p>
	 * The namespace of the vocabalary as a resource
	 * </p>
	 */

	// GO OVER EACH PREDICATE USING THE RDFS BASED SERVICE ONTOLOGY

	public static final Resource NAMESPACE = m_model.createResource(NS);

	public static final Property hasOperation = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#hasOperation");

	// public static final Property name = m_model.createProperty(
	// "http://www.mygrid.org.uk/mygrid-moby-service#name" );

	// public static final Property description = m_model.createProperty(
	// "http://www.mygrid.org.uk/mygrid-moby-service#description" );

	public static final Property locationURI = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#locationURI");

	public static final Property hasInterfaceLocation = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#hasInterfaceLocation");

	// public static final Property hasCollectionType = m_model.createProperty(
	// "http://www.mygrid.org.uk/mygrid-moby-service#hasCollectionType" );

	public static final Property hasDefaultValue = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#hasDefaultValue");

	public static final Property hasFormat = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#hasFormat");

	public static final Property hasOrganisationDescriptionText = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#hasOrganisationDescriptionText");

	public static final Property hasOrganisationNameText = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#hasOrganisationNameText");

	public static final Property hasParameterDescriptionText = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#hasParameterDescriptionText");

	public static final Property hasParameterNameText = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#hasParameterNameText");

	// public static final Property hasSchemaType = m_model.createProperty(
	// "http://www.mygrid.org.uk/mygrid-moby-service#hasSchemaType" );

	public static final Property hasServiceDescriptionText = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#hasServiceDescriptionText");

	public static final Property hasServiceNameText = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#hasServiceNameText");

	public static final Property hasServiceDescriptionLocation = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#hasServiceDescriptionLocation");

	// public static final Property hasServiceType = m_model.createProperty(
	// "http://www.mygrid.org.uk/mygrid-moby-service#hasServiceType" );

	public static final Property hasOperationDescriptionText = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#hasOperationDescriptionText");

	public static final Property hasOperationNameText = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#hasOperationNameText");

	public static final Property hasTavernaProcessorSpec = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#hasTavernaProcessorSpec");

	// public static final Property hasTransportType = m_model.createProperty(
	// "http://www.mygrid.org.uk/mygrid-moby-service#hasTransportType" );

	public static final Property inputParameter = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#inputParameter");

	public static final Property outputParameter = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#outputParameter");

	public static final Property hasParameterType = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#hasParameterType");

	public static final Property providedBy = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#providedBy");

	// public static final Property mygInstance = m_model.createProperty(
	// "http://www.mygrid.org.uk/mygrid-moby-service#mygInstance" );

	public static final Property performsTask = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#performsTask");

	public static final Resource task = m_model
			.createResource("http://www.mygrid.org.uk/mygrid-moby-service#operationTask");

	public static final Property usesMethod = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#usesMethod");

	public static final Resource method = m_model
			.createResource("http://www.mygrid.org.uk/mygrid-moby-service#operationMethod");

	public static final Property isFunctionOf = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#isFunctionOf");

	public static final Resource application = m_model
			.createResource("http://www.mygrid.org.uk/mygrid-moby-service#operationApplication");

	public static final Property usesResource = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#usesResource");

	public static final Resource resource = m_model
			.createResource("http://www.mygrid.org.uk/mygrid-moby-service#operationResource");

	public static final Property hasResourceContent = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#hasResourceContent");

	public static final Resource resourceContent = m_model
			.createResource("http://www.mygrid.org.uk/mygrid-moby-service#operationResourceContent");

	// public static final Resource collection = m_model.createResource(
	// "http://www.mygrid.org.uk/mygrid-moby-service#collection" );

	public static final Resource serviceDescription = m_model
			.createResource("http://www.mygrid.org.uk/mygrid-moby-service#serviceDescription");

	public static final Resource operation = m_model
			.createResource("http://www.mygrid.org.uk/mygrid-moby-service#operation");

	public static final Resource organisation = m_model
			.createResource("http://www.mygrid.org.uk/mygrid-moby-service#organisation");

	public static final Resource parameter = m_model
			.createResource("http://www.mygrid.org.uk/mygrid-moby-service#parameter");

	public static final Resource parameterNameSpace = m_model
			.createResource("http://www.mygrid.org.uk/mygrid-moby-service#parameterNamespace");

	public static final Property inNamespaces = m_model
			.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#inNamespaces");

	
	public static final Property objectType = m_model
	.createProperty("http://www.mygrid.org.uk/mygrid-moby-service#objectType");

	
	public static final Property DC_PATCHED_Format = m_model
			.createProperty("http://protege.stanford.edu/plugins/owl/dc/protege-dc.owl#format");

	public static final Property DC_PATCHED_Identifier = m_model
			.createProperty("http://protege.stanford.edu/plugins/owl/dc/protege-dc.owl#identifier");

	public static final Property DC_PATCHED_Publisher = m_model
			.createProperty("http://protege.stanford.edu/plugins/owl/dc/protege-dc.owl#publisher");
	// public static final Property namespaceType = m_model.createProperty(
	// "http://www.mygrid.org.uk/mygrid-moby-service#namespaceType" );

}
