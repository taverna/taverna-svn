/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.reference.sadi;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import net.sf.taverna.t2.reference.AbstractExternalReference;
import net.sf.taverna.t2.reference.DereferenceException;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferencedDataNature;
import net.sf.taverna.t2.reference.ValueCarryingExternalReference;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ca.wilkinsonlab.sadi.utils.RdfUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.ResourceUtils;

/**
 * Contains and references an RDFNode value
 * 
 * @author David Withers
 */
public class RDFReference extends AbstractExternalReference implements
		ValueCarryingExternalReference<RDFNode> {

	private static final Logger log = Logger.getLogger(RDFReference.class);
	private RDFNode node;

	/**
	 * Fakes a de-reference operation, returning a byte stream over the node.
	 */
	public InputStream openStream(ReferenceContext context) {
		try {
			String result = "";
//			if (node == null) {
//				result = "";
//			} else if (node.isLiteral()) {
//				result = ((Literal) node).getLexicalForm();
//			} else if (node.isResource()) {
//				Resource resource = (Resource) node;
//				if (resource.isURIResource()) {
//					result = resource.getURI();
//				} else {
//					Model model = ResourceUtils.reachableClosure(resource);
//					StringWriter stringWriter = new StringWriter();
//					model.write(stringWriter, "RDF/XML-ABBREV");
//					result = stringWriter.toString();
//				}
//			}
			result = getContents();
			return new ByteArrayInputStream(result.getBytes(getCharset()));
		} catch (UnsupportedEncodingException e) {
			throw new DereferenceException(e);
		}
	}

	/**
	 * Default resolution cost of 0.0f whatever the node
	 */
	@Override
	public float getResolutionCost() {
		return 0.0f;
	}

	/**
	 * Data nature set to 'ReferencedDataNature.TEXT'
	 */
	@Override
	public ReferencedDataNature getDataNature() {
		return ReferencedDataNature.TEXT;
	}

	/**
	 * Character encoding set to 'UTF-8' by default
	 */
	@Override
	public String getCharset() {
		return "UTF-8";
	}

	/**
	 * String representation for testing, returns <code>node{CONTENTS}</code>
	 */
	@Override
	public String toString() {
		return "node{" + node + "}";
	}

	public String getContents() {
		String string;
		if (node == null) {
			string = "";
		} else if (node.isLiteral()) {
			string = node.asLiteral().toString();
		} else {
			Resource resource = node.asResource();
			if (resource.isAnon()) {
				// inputs to SADI services cannot be anonymous nodes...
				resource = ResourceUtils.renameResource(resource, RdfUtils.createUniqueURI());
			}
			StringWriter stringWriter = new StringWriter();
			stringWriter.append(String.format("<%s>\n", resource.getURI()));
			Model model = ResourceUtils.reachableClosure(resource);
			model.write(stringWriter, "N3");
			model.close();
			string = stringWriter.toString();
		}
		return string;
	}

	public void setContents(String contentsAsString) {
		if (StringUtils.isEmpty(contentsAsString)) {
			node = null;
		} else {
			try {
				BufferedReader reader = new BufferedReader(new StringReader(contentsAsString));
				String header = reader.readLine();
				if (header.startsWith("<") && header.endsWith(">")) {
					// this is a URI resource...
					Model model = ModelFactory.createDefaultModel();
					model.read(reader, null, "N3");
					node = model.getResource(StringUtils.substring(header, 1, -1));
				} else {
					// this is a literal...
					node = RdfUtils.createTypedLiteral(contentsAsString);
				}
			} catch (Exception e) {
				log.error(String.format("error converting to RDF\n%s", contentsAsString), e);
				node = ResourceFactory.createPlainLiteral(contentsAsString);
			}
		}
	}
	
	public RDFNode getValue() {
		return node;
	}

	public void setValue(RDFNode node) {
		this.node = node;
	}

	public Class<RDFNode> getValueType() {
		return RDFNode.class;
	}

	public Long getApproximateSizeInBytes() {
		return -1L;
	}

	@Override
	public ExternalReferenceSPI clone() throws CloneNotSupportedException {
		RDFReference result = new RDFReference();
		result.setContents(this.getContents());
		return result;
	}

}
