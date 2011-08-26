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
import java.util.ArrayList;
import java.util.Set;
import java.lang.ref.*;
import java.beans.*;

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
 * TODO - LSID needs to be added to this class to allow provenance
 * collection to work correctly with BowerBird
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
		// Try to annotate with mime types based on data object type
		if (o instanceof String) {
		    getMetadata().addMIMEType("text/plain");
		}
		else if (o instanceof byte[]) {
		    getMetadata().addMIMEType("application/octet-stream");
		}
		else {
		    getMetadata().addMIMEType("application/X-UNKNOWN-JAVA-TYPE-"+o.getClass().getName());
		}
		for (Iterator i = getMetadata().getMIMETypeList().iterator(); i.hasNext(); ) {
		    sb.append((String)i.next());
		    if (i.hasNext()) {
			sb.append(",");
		    }
		}
		
		// Try to annotate with mime types based on data object type
		String specifiedMIMETypes = sb.toString();
		return ("'"+specifiedMIMETypes+"'");
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
	WeakReference ref = (WeakReference)metadataMap.get(theObject);
	
	//SemanticMarkup theMarkup = (SemanticMarkup)((WeakReference)metadataMap.get(theObject)).get();
	if (ref != null) {
	    return (SemanticMarkup)ref.get();
	}
	else {
	    if (supplyDefault == false) {
		throw new NoMetadataFoundException("No metadata available");
	    }
	    else {
		// Create a new markup object and store
		// it bound to the object specified
		SemanticMarkup theMarkup = new SemanticMarkup(new WeakReference(theObject));
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

    /**
     * Given a desired type, return the BaclavaIterator that
     * provides DataThing objects of this type. If the desired 
     * collection structure is not contained by this DataThing
     * then an exception is thrown.
     * @exception IntrospectionException thrown if the supplied type is not
     * contained within the current DataThing type.
     */
    public BaclavaIterator iterator(String desiredType) 
	throws IntrospectionException {
	String type = desiredType.split("\\'")[0];
	String currentType = getSyntacticType().split("\\'")[0];
	// At this point, we should have split the mime types away from the
	// collection types, so the current type looks like, for example, l() or s(l())
	
	// If the strings are the same then we return an iterator with a single item 
	// in it, namely the current DataThing; this is needed where the enactor has
	// detected that iteration is required somewhere else using the join iterator
	if (type.equals(currentType)) {
	    List dataThingList = new ArrayList();
	    dataThingList.add(this);
	    return new BaclavaIterator(dataThingList);
	}

	// Now need to check that the conversion is valid, so either the
	// input type is the empty string (iterate over everything to produce leaf nodes)
	// or it is a substring of the collection type
	if (type.equals("") || currentType.endsWith(type)) {
	    // See how deep the iterator needs to go.
	    int iterationDepth = (currentType.length() - type.length()) / 2;
	    // Now drill down into the data structure that number of levels, build a list
	    // of all the items into a new collection, iterate over this list building
	    // the DataThing objects and return the iterator over that list (and breathe...)
	    List targetList = new ArrayList();
	    drill(iterationDepth, targetList, (Collection)theDataObject);
	    // Now iterate over the target list creating new DataThing objects from it
	    List dataThingList = new ArrayList();
	    for (Iterator i = targetList.iterator(); i.hasNext(); ) {
		DataThing newThing = new DataThing(i.next());
		// Copy any metadata into the new datathing
		newThing.metadataMap = this.metadataMap;
		dataThingList.add(newThing);
	    }
	    return new BaclavaIterator(dataThingList);
	}
	else {
	    throw new IntrospectionException("Incompatible types for iterator, cannot extract "+type+" from "+getSyntacticType());
	}
	
    }
    
    /**
     * Drill into a collection, adding items to the list if we're at the desired depth,
     * this makes the underlying assumption that the collection contains either collections
     * or objects, but never a mix of both.
     */
    private void drill(int iterationDepth, List targetList, Collection theDataObject) {
	if (iterationDepth == 1) {
	    // Collecting items
	    for (Iterator i = theDataObject.iterator(); i.hasNext(); ) {
		targetList.add(i.next());
	    }
	}
	else {
	    // Iterating further down
	    for (Iterator i = theDataObject.iterator(); i.hasNext(); ) {
		Collection theCollection = (Collection)i.next();
		drill(iterationDepth-1, targetList, theCollection);
	    }
	}
    }

}
