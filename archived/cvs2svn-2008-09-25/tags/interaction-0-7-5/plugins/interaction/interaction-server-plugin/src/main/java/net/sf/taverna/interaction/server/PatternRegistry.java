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

package net.sf.taverna.interaction.server;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.commons.discovery.tools.SPInterface;
import org.apache.commons.discovery.tools.Service;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 * SPI registry for implementations of InteractionPattern
 * 
 * @author Tom Oinn
 */
public class PatternRegistry {

	private static Logger log = Logger.getLogger(PatternRegistry.class);

	private static List patterns;

	static {
		log.debug("Loading patterns");
		patterns = new ArrayList();
		SPInterface spiIF = new SPInterface(ServerInteractionPattern.class);
		ClassLoaders loaders = new ClassLoaders();
		loaders.put(PatternRegistry.class.getClassLoader());
		Enumeration spe = Service.providers(spiIF, loaders);
		while (spe.hasMoreElements()) {
			ServerInteractionPattern sip = (ServerInteractionPattern) spe
					.nextElement();
			log.debug("  Found pattern for '" + sip.getName() + "'");
			patterns.add(sip);
		}
		log.debug("Done");
	}

	/**
	 * Return a shared instance of an InteractionPattern with a name that
	 * matches the supplied name. If no such is found then return null
	 */
	public static ServerInteractionPattern patternForName(String patternName) {
		for (Iterator i = patterns.iterator(); i.hasNext();) {
			ServerInteractionPattern sip = (ServerInteractionPattern) i.next();
			if (sip.getName().equals(patternName)) {
				return sip;
			}
		}
		return null;
	}

	/**
	 * Get a string array of all the names for interaction patterns within this
	 * registry
	 */
	public static String[] getPatternNames() {
		List names = new ArrayList();
		for (Iterator i = patterns.iterator(); i.hasNext();) {
			ServerInteractionPattern sip = (ServerInteractionPattern) i.next();
			names.add(sip.getName());
		}
		return (String[]) names.toArray(new String[0]);
	}

	/**
	 * Return an XML Document containing all known patterns in a form suited to
	 * be consumed by the Taverna scavenger
	 */
	public static Document getPatternsAsXML() {
		Element root = new Element("patterns");
		Document doc = new Document(root);
		for (Iterator i = patterns.iterator(); i.hasNext();) {
			ServerInteractionPattern sip = (ServerInteractionPattern) i.next();
			Element pattern = new Element("pattern");
			root.addContent(pattern);
			pattern.setAttribute("name", sip.getName());
			// Description
			Element description = new Element("description");
			description.setText(sip.getDescription());
			pattern.addContent(description);
			// Inputs
			for (int j = 0; j < sip.getInputNames().length; j++) {
				Element input = new Element("input");
				input.setAttribute("name", sip.getInputNames()[j]);
				input.setAttribute("type", sip.getInputTypes()[j]);
				pattern.addContent(input);
			}
			// Outputs
			for (int j = 0; j < sip.getOutputNames().length; j++) {
				Element output = new Element("output");
				output.setAttribute("name", sip.getOutputNames()[j]);
				output.setAttribute("type", sip.getOutputTypes()[j]);
				pattern.addContent(output);
			}
		}
		return doc;
	}

}
