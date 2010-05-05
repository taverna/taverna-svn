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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import net.sf.taverna.t2.activities.sadi.SADIUtils;
import net.sf.taverna.t2.reference.AbstractExternalReference;
import net.sf.taverna.t2.reference.DereferenceException;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferencedDataNature;
import net.sf.taverna.t2.reference.ValueCarryingExternalReference;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Contains and references a Jena Statement value
 * 
 * @author David Withers
 */
public class StatementReference extends AbstractExternalReference implements
		ValueCarryingExternalReference<Statement> {

	private Statement statement;

	/**
	 * Fakes a de-reference operation, returning a byte stream over the Statement.
	 */
	public InputStream openStream(ReferenceContext arg0) {
		try {
			Node node = statement.getObject().asNode();
			String result = "";
			if (node.isLiteral()) {
				result = node.getLiteralValue().toString();
			} else if (node.isURI()) {
//				result = SADIUtils.uriToId(node.getURI());
				result = node.getURI();
			}
			return new ByteArrayInputStream(result.getBytes(getCharset()));
		} catch (UnsupportedEncodingException e) {
			throw new DereferenceException(e);
		}
	}

	/**
	 * Default resolution cost of 0.0f whatever the statement
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
	 * String representation for testing, returns <code>statement{CONTENTS}</code>
	 */
	@Override
	public String toString() {
		return "statement{" + statement + "}";
	}

	public String getContents() {
		Model model = ModelFactory.createDefaultModel();
		model.add(statement);
		StringWriter stringWriter = new StringWriter();
		model.write(stringWriter);
		return stringWriter.toString();
	}

	public void setContents(String contentsAsString) {
		Model model = ModelFactory.createDefaultModel();
		model.read(new StringReader(contentsAsString), null);
		statement = model.listStatements().next();
	}
	
	public Statement getValue() {
		return statement;
	}

	public void setValue(Statement statement) {
		this.statement = statement;

	}

	public Class<Statement> getValueType() {
		return Statement.class;
	}

}
