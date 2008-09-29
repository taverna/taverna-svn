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

import org.w3c.dom.Node;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Unit of processing for handling a g
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2007-12-14 12:47:46 $
 */
public interface XMLNodeConverter {

	/**
	 * Create an RDF resource to represent the given XML node in the model,
	 * including attaching known properties.
	 * 
	 * @param node
	 *            the XML dom node to be parsed
	 * @param root
	 *            the RDF resource node which is the parent of the parsed tree
	 * @param model
	 *            the RDF model into which new statements should be inserted
	 * @param caller
	 *            the XMLConverter which is calling parse which can be
	 *            recursively invoked to parse subtrees
	 * @return the RDF node (Literal or Resource) which represents the parsed
	 *         subtree
	 */
	public RDFNode parse(Node node, Resource root, Model model,
			XMLConverter caller);

}
