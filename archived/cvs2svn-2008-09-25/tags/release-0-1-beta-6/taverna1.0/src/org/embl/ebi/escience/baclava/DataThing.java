/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava;

import javax.swing.ImageIcon;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scufl.SemanticMarkup;

// Utility Imports
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

// JDOM Imports
import org.jdom.Element;




/**
 * A simple wrapper around an arbitrary Collection
 * object which allows lookup and storage of any 
 * metadata within the collection or its children.
 * In addition, there is an object of metadata concerning
 * the DataThing itself.
 * You obtain a DataThing by invoking the bake operation
 * on the DataThingFactory in the factory subpackage,
 * this is to allow the factory to sensibly configure such
 * things as types and underlying collections.
 * @author Tom Oinn
 */
public class DataThing {

    protected Object theDataObject;
    protected HashMap metadataMap = new HashMap();
    protected SemanticMarkup myMarkup;

    /**
     * Construct a new DataThing from the supplied
     * XML Jdom Element. Delegates to the 
     * DataThingXMLFactory for almost all the real
     * work here.
    */
    public DataThing(Element e) {
	myMarkup = new SemanticMarkup(this);
	theDataObject = DataThingXMLFactory.configureDataThing(e, this); 
    }

    /**
     * Create and bind a new SemanticMarkup
     * object to the DataThing itself, it's
     * not totally clear there's a need for 
     * this but it does no harm so why not?
     */
    public DataThing(Object o) {
	if (o == null) {
	    throw new RuntimeException("Attempt to create a null data object, definitely not allowed!");
	}
	theDataObject = o;
	myMarkup = new SemanticMarkup(this);
    }

    /**
     * Get a display icon for this DataThing,
     * currently this is based on the MIME
     * type from the syntactic type string.
     */
    public ImageIcon getIcon() {
	String baseMIMEType = (getSyntacticType().split("'")[1].toLowerCase()).split("/")[0];
	return new ImageIcon(ClassLoader.getSystemResource("org/embl/ebi/escience/baclava/icons/"+baseMIMEType+".png"));
    }

    /**
     * Return the SemanticMarkup object associated
     * with the DataThing itself
     */
    public SemanticMarkup getMetadata() {
	return this.myMarkup;
    }
    
    /**
     * Get the underlying data object, this is
     * the first level of the data document.
     */
    public Object getDataObject() {
	return this.theDataObject;
    }
    
    /**
     * Get the syntax type of this DataThing. The type
     * string is based around application of the collection
     * type constructors to a base MIME type. For example,
     * t(s('text/plain')) is a tree of sets of TEXT/PLAIN
     * items. The MIME type may be a comma separated list
     * of types. Possible type constructors are t(..) for
     * trees, s(..) for sets, l(..) for lists and p(..) for
     * partial orders.<p>
     * I would imagine that we'll mostly be dealing with
     * types of 'text/plain', lists of same and maybe the
     * occasional 'image/png' or similar, but I think this
     * has enough flexibility to cover most things.<p>
     * The type string "null" represents and empty DataThing
     * and is the default value returned if the collection
     * is empty.
     */
    public String getSyntacticType() {
	return getSyntacticTypeForObject(this.theDataObject);
    }

    public String getSyntacticTypeForObject(Object o) {
	if (o instanceof Collection) {
	    if (((Collection)o).isEmpty()) {
		if (o instanceof Set) {
		    return "s('null')";
		}
		return "l('null')";
	    }
	    else {
		// Pull the first object out of the collection and recurse
		Object innerObject = ((Collection)o).iterator().next();
		if (o instanceof Set) {
		    return ("s("+getSyntacticTypeForObject(innerObject)+")");
		}
		else if (o instanceof List) {
		    return ("l("+getSyntacticTypeForObject(innerObject)+")");
		}
		// No idea what the collection is, return the most general
		// type constructor for a partial order
		return ("p("+getSyntacticTypeForObject(innerObject)+")");
	    }
	}
	else {
	    // Not a collection, first see if there is any metadata
	    // associated with the object that we can use to determine
	    // the mime types
	    try {
		SemanticMarkup markup = getMetadataForObject(o, false);
		List mimeTypeList = markup.getMIMETypeList();
		if (mimeTypeList.isEmpty()) {
		    // If there is no MIME information in the markup object
		    // then we have to revert to guesswork.
		    throw new NoMetadataFoundException();
		}
		else {
		    // Return a comma seperated list of MIME types within
		    // single quotes.
		    StringBuffer sb = new StringBuffer();
		    sb.append("'");
		    for (Iterator i = mimeTypeList.iterator(); i.hasNext(); ) {
			String mimeType = (String)i.next();
			sb.append(mimeType);
			if (i.hasNext()) {
			    sb.append(",");
			}
		    }
		    sb.append("'");
		    return sb.toString();
		}
	    }
	    catch (NoMetadataFoundException nmfe) {
		StringBuffer sb = new StringBuffer();
		if (getMetadata().getMIMETypeList().isEmpty() == false) {
		    for (Iterator i = getMetadata().getMIMETypeList().iterator(); i.hasNext(); ) {
			sb.append(","+(String)i.next());
		    }
		}
		String specifiedMIMETypes = sb.toString();
		// Make a 'best guess' at the appropriate type
		if (o instanceof String) {
		    return "'text/plain"+specifiedMIMETypes+"'";
		}
		else if (o instanceof byte[]) {
		    return "'application/octet-stream"+specifiedMIMETypes+"'";
		}	    
		// Fallback, return special unknown type thing
		return ("'application/X-UNKNOWN-JAVA-TYPE-"+o.getClass().getName()+specifiedMIMETypes+"'");
	    }
	    
	}
    }

    /**
     * Get the SemanticMarkup associated with an object
     * in this DataThing. If there is no such metadata 
     * available the behavious depends upon the value
     * of the supplyDefaults parameter. If false, then
     * a NoMetadataFoundException is thrown, if true a 
     * new SemanticMarkup object is created, stored in 
     * the dictionary and returned to the caller.
     */
    public SemanticMarkup getMetadataForObject(Object theObject, 
					       boolean supplyDefault) 
	throws NoMetadataFoundException {
	SemanticMarkup theMarkup = (SemanticMarkup)metadataMap.get(theObject);
	if (theMarkup != null) {
	    return theMarkup;
	}
	else {
	    if (supplyDefault == false) {
		throw new NoMetadataFoundException("No metadata available");
	    }
	    else {
		// Create a new markup object and store
		// it bound to the object specified
		theMarkup = new SemanticMarkup(theObject);
		this.metadataMap.put(theObject, theMarkup);
		return theMarkup;
	    }
	}
    }

    /**
     * Return the JDom Element corresponding to this DataThing
     * represented as XML
     */
    public Element getElement() {
	return DataThingXMLFactory.getElement(this);
    }

}
