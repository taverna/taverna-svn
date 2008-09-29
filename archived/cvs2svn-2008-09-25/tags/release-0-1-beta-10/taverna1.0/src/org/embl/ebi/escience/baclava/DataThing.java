/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava;

import java.beans.IntrospectionException;
import java.lang.ref.WeakReference;
import javax.swing.ImageIcon;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scufl.SemanticMarkup;

// Utility Imports
import java.util.*;

import java.io.*;

// JDOM Imports
import org.jdom.Element;

import org.embl.ebi.escience.baclava.BaclavaIterator;
import org.embl.ebi.escience.baclava.LSIDProvider;
import org.embl.ebi.escience.baclava.NoMetadataFoundException;
import java.lang.ArrayIndexOutOfBoundsException;
import java.lang.ClassLoader;
import java.lang.Object;
import java.lang.RuntimeException;
import java.lang.String;
import java.lang.StringBuffer;



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
    protected HashMap lsid = new HashMap();
    public static LSIDProvider SYSTEM_DEFAULT_LSID_PROVIDER = null;
    
    // This array contains mime types, when asked for its most
    // interesting type this list is checked and the first match
    // returned.
    private static String[] interestingTypes = new String[]{"text/html","text/xml","text/rtf","text/x-graphviz",
						   "text/plain","image/png","image/jpeg",
						   "image/gif"};
    
    static {
	// Interrogate the system properties and instantiate
	// a single static instance of the LSIDProvider
	// implementation if such is found, otherwise leave
	// it as null
	String providerClassName = System.getProperty("taverna.lsid.providerclass");
	if (providerClassName!=null) {
	    try {
		Class providerClass = Class.forName(providerClassName);
		SYSTEM_DEFAULT_LSID_PROVIDER = (LSIDProvider)providerClass.newInstance();
	    }
	    catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
    }

    public DataThing(DataThing other) {
        this.theDataObject = other.theDataObject;
        this.metadataMap.putAll(other.metadataMap);
        this.myMarkup = new SemanticMarkup(other.myMarkup);
        this.lsid.putAll(other.lsid);
    }
    

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
     * Get the LSID map object->LSID
     */
    public Map getLSIDMap() {
	return this.lsid;
    }

    /**
     * Populate all unassigned LSID values using the
     * system default LSID provider
     */
    public void fillLSIDValues() {
	if (SYSTEM_DEFAULT_LSID_PROVIDER!=null) {
	    try {
		fillLSIDValues(SYSTEM_DEFAULT_LSID_PROVIDER);
	    }
	    catch (Throwable ex) {
		ex.printStackTrace();
	    }
	}
    }

    /**
     * Populate all unassigned LSID values in the
     * LSID map from the supplied LSIDProvider.
     * This traverses the object contained within
     * the DataThing as well as the DataThing itself
     * and provides LSID values where there are
     * none defined.
     */
    public void fillLSIDValues(LSIDProvider provider) {
	// First check the DataThing itself
	String selfValue = (String)(lsid.get(this));
	if (selfValue == null || selfValue.equals("")) {
	    lsid.put(this, provider.getID("datathing"));
	}
	// Recursively populate the data object lsid map
	doInternalLSIDFill(theDataObject, provider);
    }
    private void doInternalLSIDFill(Object o, LSIDProvider provider) {
	String lsidValue = (String)(lsid.get(o));
	if (lsidValue == null || lsidValue.equals("")) {
	    lsid.put(o, provider.getID("datathing"));
	}
	if (o instanceof Collection) {
	    Iterator i = ((Collection)o).iterator();
	    for (; i.hasNext(); ) {
		doInternalLSIDFill(i.next(), provider);
	    }
	}
	else {
	    // got to the leaf
	    return;
	}
    }


    /**
     * Set the LSID of the named object to the specified
     * value.
     */
    public void setLSID(Object target, String id) {
	if (id != null) {
	    lsid.put(target, id);
	}
    }

    /**
     * Get the LSID of the named object, returns the
     * empty string if there is no such mapping
     */
    public String getLSID(Object target) {
	return (lsid.get(target)!=null)?(String)(lsid.get(target)):"";
    }

    /**
     * Get the object with the supplied LSID or return
     * null if there isn't one
     */
    public Object getDataObjectWithLSID(String LSID) {
	for (Iterator i = lsid.keySet().iterator(); i.hasNext();) {
	    Object key = i.next();
	    Object value = lsid.get(key);
	    //System.out.println("LSID value found : "+value);
	    if (((String)value).equals(LSID)) {
		return key;
	    }
	}
	return null;
    }

    /**
     * Return an array of all the LSIDs that this DataThing's 
     * LSID map contains as values
     */
    public String[] getAllLSIDs() {
	return (String[])lsid.values().toArray(new String[0]);
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
	theDataObject = DataThingFactory.convertObject(o);
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

    public String getMostInterestingMIMETypeForObject(Object o) {
	String typeString = getSyntacticTypeForObject(o);
	//System.out.println("Got types : "+typeString);
	String mimeTypes = typeString.split("'")[1].toLowerCase();
	for (int i = 0; i < interestingTypes.length; i++) {
	    if (mimeTypes.matches(".*"+interestingTypes[i]+".*")) {
		return interestingTypes[i];
	    }
	}
	try {
	    return mimeTypes.split(",")[0];
	}
	catch (Exception ex) {
	    return "null";
	}
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
     * Copy the markup from the supplied DataThing object to this
     * one. This is mainly used when the data has been repackaged,
     * to preserve eg. LSID values.
     */
    public void copyMetadataFrom(DataThing source) {
	lsid.putAll(source.lsid);
	metadataMap.putAll(source.metadataMap);
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
		SemanticMarkup theMarkup = new SemanticMarkup(theObject);
		this.metadataMap.put(theObject, new WeakReference(theMarkup));
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
     * Extract a child object as a DataThing. This assumes that
     * the object referenced is actually within this DataThing
     * object, behaviour is undefined otherwise. Although we could
     * check this and impose it as a constraint the performance
     * hit of traversing the entire original DataThing collection
     * structure and doing potentially expensive equivalence
     * computations is probably not worth it. Use with care.
     * @return a view on an object contained within this DataThing
     * as a new DataThing
     */
    public DataThing extractChild(Object child) {
	DataThing result = new DataThing(this);
	result.theDataObject = child;
	return result;
    }
    
    /**
     * Iterate over all imediate children.
     * If there are no children, return an iterator over nothing.
     * All children will be viewed as DataThing instances.
     *
     * @return an Iterator over all children
     */
    public Iterator childIterator()
    {
        if (theDataObject instanceof Collection) {
            List dataThingList = new ArrayList();
            for (Iterator i = ((Collection) theDataObject).iterator();
                 i.hasNext();) {
                dataThingList.add(extractChild(i.next()));
            }
            return dataThingList.iterator();
        } else {
            return Collections.EMPTY_LIST.iterator();
        }
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
	String type = null;
	String currentType = null;
	try {
	    type = desiredType.split("\\'")[0];
	}
	catch (ArrayIndexOutOfBoundsException aioobe) {
	    type = "";
	}
	try {
	    currentType = getSyntacticType().split("\\'")[0];
	}
	catch (ArrayIndexOutOfBoundsException aioobe) {
	    currentType = "";
	}
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
	    List indexList = new ArrayList();
	    drill(iterationDepth, targetList, indexList, new int[0], (Collection)theDataObject);
	    // Now iterate over the target list creating new DataThing objects from it
	    List dataThingList = new ArrayList();
	    for (Iterator i = targetList.iterator(); i.hasNext(); ) {
		DataThing newThing = new DataThing(i.next());
		// Copy any metadata into the new datathing
		newThing.metadataMap.putAll(this.metadataMap);
		newThing.lsid.putAll(this.lsid);
		dataThingList.add(newThing);
	    }
	    return new BaclavaIterator(dataThingList, indexList);
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
    private void drill(int iterationDepth, List targetList, List indexList, int[] currentIndex, Collection theDataObject) {
	if (iterationDepth == 1) {
	    // Collecting items
	    int localIndex = 0;
	    for (Iterator i = theDataObject.iterator(); i.hasNext(); ) {
		targetList.add(i.next());
		indexList.add(append(currentIndex, localIndex++));
	    }
	}
	else {
	    // Iterating further down
	    int localIndex = 0;
	    for (Iterator i = theDataObject.iterator(); i.hasNext(); ) {
		Collection theCollection = (Collection)i.next();
		drill(iterationDepth-1, targetList, indexList, append(currentIndex, localIndex++), theCollection);
	    }
	}
    }
    /**
     * Utility class, append an int onto an int array and return the new array
     */
    private int[] append(int[] list, int head) {
	int[] newlist = new int[list.length+1];
	for (int i = 0; i<list.length; i++) {
	    newlist[i] = list[i];
	}
	newlist[list.length] = head;
	return newlist;
    }


    public String toString()
    {
        String datStr = theDataObject.toString();
        boolean trimmed = false;
        int nl = datStr.indexOf('\n');
        if (nl > -1) {
            datStr = datStr.substring(0, nl);
            trimmed = true;
        }
        if(datStr.length() > 30) {
            datStr = datStr.substring(0, 27);
            trimmed = true;
        }
        if(trimmed) {
            datStr += "...";
        }


        return super.toString() +
                "\n\tdataObject=" + datStr +
                "\n\tmetaData=" + metadataMap +
                "\n\tmarkup=" + myMarkup +
                "\n\tlsid=" + (String)(lsid.get(this)) +
                "\n";
    }
    
    /**
     * Writes the contents of the DataThing into the
     * specified directory using the given name. If 
     * there is only one item a single file is created
     * otherwise a directory structure mirroring the collection
     * structure is built.
     */
    public void writeToFileSystem(File destination, String name) throws IOException {
	String defaultExtension = ".text";
	String syntacticType = (getSyntacticType().split("'"))[1].toLowerCase();
	if (syntacticType.matches(".*text/xml.*")) {
	    defaultExtension = ".xml";
	}
	else if (syntacticType.matches(".*text/html.*")) {
	    defaultExtension = ".html";
	}
	else if (syntacticType.matches(".*text/rtf.*")) {
	    defaultExtension = ".rtf";
	}
	else if (syntacticType.matches(".*image/png.*")) {
	    defaultExtension = ".png";
	}
	else if (syntacticType.matches(".*image/jpeg.*")) {
	    defaultExtension = ".jpeg";
	}
	else if (syntacticType.matches(".*image/gif.*")) {
	    defaultExtension = ".gif";
	}
	else if (syntacticType.matches(".*x-graphviz.*")) {
	    defaultExtension = ".dot.text";
	}
	DataThing.writeObjectToFileSystem(destination, name, theDataObject, defaultExtension);
    }
    /**
     * Write a specific object to the filesystem
     * this has no access to metadata about the object
     * and so is not particularly clever
     */
    public static void writeObjectToFileSystem(File destination, String name, Object o, String defaultExtension) throws IOException {
	// If the destination is not a directory then set the destination
	// directory to the parent and the name to the filename
	// i.e. if the destination is /tmp/foo.text and this exists
	// then set destination to /tmp/ and name to 'foo.text'
	if (destination.exists() && destination.isFile()) {
	    name = destination.getName();
	    destination = destination.getParentFile();
	}
	if (destination.exists() == false) {
	    // Create the directory structure if not already present
	    destination.mkdirs();
	}
	writeDataObject(destination, name, o, defaultExtension);
    }
    static char sep = File.separatorChar;
    private static void writeDataObject(File destination, String name, Object o, String defaultExtension) throws IOException {
	if (o instanceof Collection) {
	    // Create a new directory, iterate over the collection recursively
	    // calling this method
	    File targetDir = new File(destination.toString()+sep+name);
	    targetDir.mkdir();
	    int count = 0;
	    Collection c = (Collection)o;
	    for (Iterator i = c.iterator(); i.hasNext();) {
		writeDataObject(targetDir, ""+count++, i.next(), defaultExtension);
	    }
	}
	else {
	    // Write a single item
	    if (o instanceof String && defaultExtension == null) {
		name = name + ".text";
	    }
	    else {
		if (defaultExtension != null) {
		    name = name + defaultExtension;
		}
	    }
	    File targetFile = new File(destination.toString()+sep+name);
	    FileOutputStream fos = new FileOutputStream(targetFile);
	    if (o instanceof byte[]) {
		fos.write((byte[])o);
		fos.flush();
		fos.close();
	    }
	    else if (o instanceof String) {
		PrintWriter out = new PrintWriter(new OutputStreamWriter(fos));
		out.print((String)o);
		out.flush();
		out.close();
	    }
	}
    }
}
