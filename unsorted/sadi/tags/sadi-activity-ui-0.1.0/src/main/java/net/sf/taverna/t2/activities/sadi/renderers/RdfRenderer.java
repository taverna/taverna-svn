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
package net.sf.taverna.t2.activities.sadi.renderers;

import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.sf.taverna.t2.activities.sadi.SADIUtils;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.sadi.StatementReference;
import net.sf.taverna.t2.renderers.Renderer;
import net.sf.taverna.t2.renderers.RendererException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * 
 * 
 * @author David Withers
 */
public class RdfRenderer implements Renderer {

	public boolean canHandle(String mimeType) {
		return false;
	}

	public boolean canHandle(ReferenceService referenceService, T2Reference reference,
			String mimeType) throws RendererException {
		return getReference(referenceService, reference, StatementReference.class) != null;
	}

	public JComponent getComponent(ReferenceService referenceService, T2Reference reference)
			throws RendererException {
		StatementReference statementReference = getReference(referenceService, reference,
				StatementReference.class);
		Statement statement = statementReference.getValue();
		Node subject = statement.getSubject().asNode();
		Node predicate = statement.getPredicate().asNode();
		Node object = statement.getObject().asNode();
		JTable table = new JTable(new Object[][] { { getName(subject), getName(predicate),
				getName(object) } }, new Object[] { "Subject", "Predicate", "Object" });
		table.setShowGrid(true);
		return new JScrollPane(table);
	}

	public String getType() {
		return "RDF";
	}

	private String getName(Node node) {
		String name = "";
		if (node.isLiteral()) {
			name = node.getLiteral().toString();
		} else {
			name = SADIUtils.uriToId(node.getURI());
		}
		return name;
	}

	@SuppressWarnings("unchecked")
	private <T extends ExternalReferenceSPI> T getReference(ReferenceService referenceService,
			T2Reference reference, Class<T> referenceType) {
		Identified identified = referenceService.resolveIdentifier(reference, null, null);
		if (identified instanceof ReferenceSet) {
			ReferenceSet referenceSet = (ReferenceSet) identified;
			Set<ExternalReferenceSPI> externalReferences = referenceSet.getExternalReferences();
			for (ExternalReferenceSPI externalReference : externalReferences) {
				if (referenceType.isInstance(externalReference)) {
					return (T) externalReference;
				}
			}
		}
		return null;
	}

}
