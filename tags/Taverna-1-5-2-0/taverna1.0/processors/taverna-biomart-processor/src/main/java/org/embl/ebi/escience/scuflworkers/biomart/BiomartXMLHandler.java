/*
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
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: BiomartXMLHandler.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-07-10 14:07:42 $
 *               by   $Author: sowen70 $
 * Created on 17-Mar-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.biomart;

import org.biomart.martservice.MartQuery;
import org.biomart.martservice.MartServiceXMLHandler;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.XMLHandler;
import org.jdom.Element;
import org.jdom.Namespace;

/**
  * Utility class for serializing Biomart classes to XML.
  * 
 * @author David Withers
 */
public class BiomartXMLHandler implements XMLHandler {
	public static Namespace NAMESPACE = Namespace.getNamespace("biomart",
			"http://org.embl.ebi.escience/xscufl-biomart/0.1alpha");

	public static final String BIOMART_ELEMENT = "biomart";

	/*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflworkers.XMLHandler#elementForProcessor(org.embl.ebi.escience.scufl.Processor)
     */
	public Element elementForProcessor(Processor p) {
		BiomartProcessor processor = (BiomartProcessor) p;
		Element element = new Element(BIOMART_ELEMENT, XScufl.XScuflNS);
		element.addContent(MartServiceXMLHandler.martQueryToElement(processor
				.getQuery(), NAMESPACE));
		return element;
	}

	/*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflworkers.XMLHandler#elementForFactory(org.embl.ebi.escience.scuflworkers.ProcessorFactory)
     */
	public Element elementForFactory(ProcessorFactory pf) {
		BiomartProcessorFactory factory = (BiomartProcessorFactory) pf;
		Element element = new Element(BIOMART_ELEMENT, XScufl.XScuflNS);
		element.addContent(MartServiceXMLHandler.martQueryToElement(factory
				.getQuery(), NAMESPACE));
		return element;
	}

	/*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflworkers.XMLHandler#getFactory(org.jdom.Element)
     */
	public ProcessorFactory getFactory(Element element) {
		Element biomart = element.getChild(BIOMART_ELEMENT, XScufl.XScuflNS);
		MartQuery query = MartServiceXMLHandler.elementToMartQuery(biomart
				.getChild(MartServiceXMLHandler.MART_QUERY_ELEMENT, NAMESPACE),
				NAMESPACE);
		BiomartProcessorFactory factory = new BiomartProcessorFactory(query);
		return factory;
	}

	/*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflworkers.XMLHandler#loadProcessorFromXML(org.jdom.Element,
     *      org.embl.ebi.escience.scufl.ScuflModel, java.lang.String)
     */
	public Processor loadProcessorFromXML(Element element, ScuflModel model,
			String name) throws ProcessorCreationException,
			DuplicateProcessorNameException, XScuflFormatException {
		Element biomart = element.getChild(BIOMART_ELEMENT, XScufl.XScuflNS);
		MartQuery query = MartServiceXMLHandler.elementToMartQuery(biomart
				.getChild(MartServiceXMLHandler.MART_QUERY_ELEMENT, NAMESPACE),
				NAMESPACE);
		BiomartProcessor processor = new BiomartProcessor(model, name, query);
		return processor;
	}

}
