/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.factory;

import org.embl.ebi.escience.baclava.BaclavaIterator;
import org.embl.ebi.escience.baclava.DataThing;

// JDOM Imports
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.*;

import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;



/**
 * Test the creation and type display of the various supported
 * Java object types
 * @author Tom Oinn
 */
public class DataThingFactoryTest {

    public static void main(String[] args) {
	try {
	    String[] theStringArray = {"hello","world","string","array"};
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
	    
	    // Test the LSID functionality
	    System.out.println("Some LSID stuff... : ");
	    theDataThing = DataThingFactory.bake(theStringArray);
	    theDataThing.setLSID(theDataThing,"LSID:mainDataThingID");
	    theDataThing.setLSID(theDataThing.getDataObject(),"LSID:theStringArrayThing");
	    for (int i = 0; i < theStringArray.length; i++) {
		theDataThing.setLSID(theStringArray[i], "LSID:anItem:"+i);
	    }
	    print(theDataThing);
	    System.out.println("Testing iteration over the above...");
	    BaclavaIterator i = theDataThing.iterator("''");
	    for (;i.hasNext();) {
		DataThing innerThing = (DataThing)(i.next());
		print(innerThing);
	    }
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
	    XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
	    System.out.println(xo.outputString(doc));
	}
	catch (Exception ex) {
	    //
	}
    }

}
