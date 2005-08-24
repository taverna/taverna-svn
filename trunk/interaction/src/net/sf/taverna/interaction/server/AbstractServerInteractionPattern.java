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

import org.jdom.*;
import java.util.*;
import java.io.*;
import org.jdom.input.*;
import org.apache.log4j.Logger;

/**
 * Partial implementation of the ServerInteractionPattern interface
 * that derives the various port names, description and service name
 * from an XML configuration file. For a class 'FooClass' this xml
 * file should be named FooClass.metadata.xml and located in the same
 * package, this will then be used by the default constructor in this
 * superclass to populate the basic fields.<p>
 * The XML file should contain something like the following:<pre>
 * &lt;pattern name="base.AcceptReject"&gt;
 *   &lt;description&gt;Accept or reject a single item of data&lt;/description&gt;
 *   &lt;input name="data" type="'text/plain'"/&gt;
 *   &lt;output name="decision" type="'text/plain'"/&gt;
 * &lt;/pattern&gt;
 * </pre>
 * @author Tom Oinn
 */
public abstract class AbstractServerInteractionPattern 
    implements ServerInteractionPattern {
    
    private List inputNames, outputNames, inputTypes, outputTypes;
    private String description, patternName;
    private static Logger log = Logger.getLogger(AbstractServerInteractionPattern.class);

    /**
     * Locate the appropriate XML config file, parse it and
     * populate the simple textual metadata from it
     */
    public AbstractServerInteractionPattern() {
	inputNames = new ArrayList();
	outputNames = new ArrayList();
	inputTypes = new ArrayList();
	outputTypes = new ArrayList();
	description = "No description available";
	patternName = "noname";
	String[] nameParts = getClass().getName().split("\\.");
	String name = nameParts[nameParts.length - 1];
	try {
	    SAXBuilder builder = new SAXBuilder(false);
	    InputStream is = getClass().getResourceAsStream(name+".metadata.xml");
	    if (is == null) {
		log.error("Unable to locate metadata file '"+name+".metadata.xml'");
		return;
	    }
	    Document metadata = builder.build(is);
	    // Expect document of form...
	    // <pattern name="foo.bar.PatternName">
	    //   <description>....</description>
	    //   <input name="..." type="...">*
	    //   <output name="..." type="...">*
	    // </pattern>
	    Element pattern = metadata.getRootElement();
	    this.patternName = pattern.getAttributeValue("name");
	    this.description = pattern.getChild("description").getTextTrim();
	    List inputs = pattern.getChildren("input");
	    for (Iterator i = inputs.iterator(); i.hasNext();) {
		Element e = (Element)i.next();
		String inputName = e.getAttributeValue("name");
		String inputType = e.getAttributeValue("type");
		inputNames.add(inputName);
		inputTypes.add(inputType);
	    }
	    List outputs = pattern.getChildren("output");
	    for (Iterator i = outputs.iterator(); i.hasNext();) {
		Element e = (Element)i.next();
		String outputName = e.getAttributeValue("name");
		String outputType = e.getAttributeValue("type");
		outputNames.add(outputName);
		outputTypes.add(outputType);
	    }
	}
	catch (JDOMException jde) {
	    log.error("Can't parse xml metadata document for '"+name+".metadata.xml'");
	}
	catch (IOException ioe) {
	    log.error("Unable to open stream for '"+name+".metadata.xml'");
	}
	catch (NullPointerException npe) {
	    log.error("Unexpected document structure for '"+name+".metadata.xml'");
	}
    }
    
    /**
     * Return an array of names for the inputs defined in getInputTypes
     */
    public final String[] getInputNames() {
	return (String[])inputNames.toArray(new String[0]);
    }
    /**
     * Return an array of Taverna style syntactic type strings
     * corresponding to the input data for this interaction pattern
     */
    public final String[] getInputTypes() {
	return (String[])inputTypes.toArray(new String[0]);
    }
    /**
     * Return an array of output names
     */
    public final String[] getOutputNames() {
	return (String[])outputNames.toArray(new String[0]);
    }
    /**
     * Return an array of Taverna style syntactic type strings
     * corresponding to the output data for this interaction pattern
     */
    public final String[] getOutputTypes() {
	return (String[])outputTypes.toArray(new String[0]);
    }
    /**
     * Return a free text description
     */
    public final String getDescription() {
	return this.description;
    }
    /**
     * Return a name for this interaction pattern. If the name
     * contains a '.' character this may be interpreted by a
     * browser interface as representing categories, so for 
     * example the name 'edit.sequence.Artemis' could be placed
     * in an 'edit' category with subcategory 'sequence'. Names
     * MUST be unique within a given interaction server.
     */
    public final String getName() {
	return this.patternName;
    }
    
}
