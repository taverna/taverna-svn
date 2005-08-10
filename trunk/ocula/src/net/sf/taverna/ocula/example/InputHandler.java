/*
 * Copyright 2005 University of Manchester
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
package net.sf.taverna.ocula.example;

import java.util.Map;

import net.sf.taverna.ocula.Ocula;
import net.sf.taverna.ocula.action.Processor;

import org.apache.log4j.Logger;

/**
 * Simple class used by the InputExample.
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 *
 */
public class InputHandler implements Processor	{

    private static Logger log = Logger.getLogger(InputHandler.class);
    
    public void process(Map nameText, Ocula ocula) {
	String name = (String) nameText.get("nameField");
	String country = (String) nameText.get("countryField");
	String phone = (String) nameText.get("phoneField");
	Person person = new Person(name, country, phone);
	ocula.putContext("user", person);
	try {
	ocula.load(Thread.currentThread().getContextClassLoader().
		    getResource("net/sf/taverna/ocula/example/example2.xml"));
	}
	// In a real app something appropriate should be to deal
	// with these exceptions. Here we just write it to the log.
	catch(Exception e) {
	    log.error("Error loading page", e);
	}
    }
}
