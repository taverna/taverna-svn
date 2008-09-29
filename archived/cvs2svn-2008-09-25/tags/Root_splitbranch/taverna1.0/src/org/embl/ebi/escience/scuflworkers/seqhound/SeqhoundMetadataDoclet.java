/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.seqhound;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;

/**
 * A Doclet to run over the org.blueprint.seqhound class
 * which can then extract the information specified and
 * store it into an XML file which the processor can then
 * read from to fetch method parameter names and processor
 * descriptions.
 * @author Tom Oinn
 */
public class SeqhoundMetadataDoclet {

    public static boolean start(RootDoc root) {
	// Get the ClassDoc for the seqhound client API
	ClassDoc cd = root.classNamed("org.blueprint.seqhound.SeqHound");
	MethodDoc[] methods = cd.methods();
	Element services = new Element("services");
	for (int i = 0; i < methods.length; i++) {
	    // Check whether the method is an external one
	    String methodName = methods[i].name();
	    if (SeqhoundScavenger.isInternal(methodName) == false) {
		Element service = new Element("service");
		service.setAttribute("name",methodName);
		services.addContent(service);
		
		Element description = new Element("description");
		description.setText(methods[i].commentText());
		service.addContent(description);
		
		Parameter[] params = methods[i].parameters();
		Element parameterList = new Element("parameters");
		service.addContent(parameterList);
		for (int j = 0; j < params.length; j++) {
		    Element parameter = new Element("parameter");
		    parameter.setAttribute("name",params[j].name());
		    parameterList.addContent(parameter);
		}
	    }
	}
	XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
	String xml = xo.outputString(services);
	try {
	    PrintWriter out = new PrintWriter(new FileWriter(new File("./src/org/embl/ebi/escience/scuflworkers/seqhound/services.xml")));
	    out.println(xml);
	    out.flush();
	    out.close();
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	return true;
    }
    
}
