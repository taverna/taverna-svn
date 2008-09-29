/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.ocula.validation;

import org.jdom.Element;
import org.jdom.Document;

import org.apache.commons.discovery.tools.Service;
import org.apache.commons.discovery.tools.SPInterface;
import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Enumeration;

/**
 * Validates a single page definition by recursively walking the definition
 * structure and calling any registered instances of PageValidationSPI
 * @author Tom Oinn
 */
public class PageValidator {

    static List validators = null;
    static Logger log = Logger.getLogger(PageValidator.class);

    /**
     * Create a new PageValidator, loading the available validators from
     * the SPI registry defined by PageValidationSPI if required
     */
    public PageValidator() {
	createRegistry();
    }
    
    private static synchronized void createRegistry() {
	if (validators == null) {
	    log.info("Loading PageValidationSPI registry : ");
	    validators = new ArrayList();
	    SPInterface spiIF = new SPInterface(PageValidationSPI.class);
	    ClassLoaders loaders = new ClassLoaders();
	    loaders.put(PageValidator.class.getClassLoader());
	    Enumeration spe = Service.providers(spiIF, loaders);
	    while (spe.hasMoreElements()) {
		PageValidationSPI spi = (PageValidationSPI)spe.nextElement();
		log.info("\t"+spi.getClass().getName());
		validators.add(spi);
	    }
	    log.info("Finished loading PageValidationSPI registry.");
	}
    }
    
    /**
     * Validate a page definition in the form of a Document object
     * and return an array of any PageValidationException objects
     * thrown by the registered validation components.
     * @return array of page validation errors, if this is zero
     * length the page is valid.
     * @param definition the Document to validate
     */
    public PageValidationException[] validate(Document definition) {
	List errorList = new ArrayList();
	Element rootElement = definition.getRootElement();
	validateAnElement(errorList, rootElement);
	return (PageValidationException[])errorList.toArray(new PageValidationException[0]);
    }
    private void validateAnElement(List errorList, Element element) {
	for (Iterator i = validators.iterator(); i.hasNext();) {
	    PageValidationSPI validator = (PageValidationSPI)i.next();
	    if (validator.accept(element)) {
		try {
		    validator.validate(element);
		}
		catch(PageValidationException pve) {
		    errorList.add(pve);
		}
	    }
	}
	List children = element.getChildren();
	for (Iterator i = children.iterator(); i.hasNext();) {
	    validateAnElement(errorList, (Element)i.next());
	}
    }

}
