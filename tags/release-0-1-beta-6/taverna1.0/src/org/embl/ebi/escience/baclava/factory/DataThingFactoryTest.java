/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.factory;

import org.embl.ebi.escience.baclava.DataThing;

// JDOM Imports
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;




/**
 * Test the creation and type display of the various supported
 * Java object types
 * @author Tom Oinn
 */
public class DataThingFactoryTest {

    public static void main(String[] args) {
	try {
	    String[] theStringArray = {"hello","world","this","is","a","string","array"};
	    String theString = "Hello I am a string";
	    byte[] theByteArray = theString.getBytes();
	    String theXMLString = "<foo>bar</foo>";
	    DataThing theDataThing;
	    
	    // Should have a type of l('text/plain')
	    System.out.println("String array : ");
	    print(DataThingFactory.bake(theStringArray));
	    print(new DataThing(DataThingFactory.bake(theStringArray).getElement()));
	    
	    // Should have type of 'text/plain'
	    System.out.println("String : ");
	    print(DataThingFactory.bake(theString));
	    
	    // Should have type of 'application/octet-stream'
	    System.out.println("Byte array : ");
	    print(DataThingFactory.bake(theByteArray));
	    
	    // Should have type of 'text/xml' assigned by writing
	    // to the SemanticMarkup object associated with the 
	    // String in the DataThing
	    System.out.println("String as xml : ");
	    theDataThing = DataThingFactory.bake(theXMLString);
	    theDataThing.getMetadataForObject(theXMLString, true).addMIMEType("text/xml");
	    print(theDataThing);
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	}
    
	
	
    }

    private static void print(DataThing d) {
	System.out.println(d.getSyntacticType());
	printDocument(d.getElement());
    }

    private static void printDocument(Element e) {
	try {
	    Document doc = new Document(e);
	    XMLOutputter xo = new XMLOutputter();
	    xo.setIndent("  ");
	    xo.setNewlines(true);
	    System.out.println(xo.outputString(doc));
	}
	catch (Exception ex) {
	    //
	}
    }

}
