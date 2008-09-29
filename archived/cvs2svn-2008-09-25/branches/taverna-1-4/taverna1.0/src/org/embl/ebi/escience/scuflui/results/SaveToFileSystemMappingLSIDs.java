/** 
 * This file is a component of the Taverna project, 
 * and is licensed under the GNU LGPL. 
 * Copyright Tom Oinn, EMBL-EBI 
 */
package org.embl.ebi.escience.scuflui.results;
import org.embl.ebi.escience.scuflui.ScuflIcons;
import org.embl.ebi.escience.baclava.DataThing;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import javax.swing.JFileChooser;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JComponent;
import java.io.File;
import java.io.IOException;
import org.embl.ebi.escience.scufl.SemanticMarkup;

/** 
 * Store the Map of DataThing objects to disk, using the 
 * collection structure to drive the directory structure 
 * and storing each leaf DataThing item as a single file. 
 * But try to replace LSIDs of outputs in "s with relative 
 * file names. 
 * @author Chris Greenhalgh 
 */

public class SaveToFileSystemMappingLSIDs implements ResultMapSaveSPI {
    /**     
     * Return the standard looking save to disk icon     
     */    
    public Icon getIcon() {	return ScuflIcons.saveIcon;    }    
    
    /**     
     * Get the description for this plugin     
     */    
    public String getDescription() {	
	return ( "Saves the complete set of results to the file system,\n"+
		 "writing each result into its own file or set of directories\n"+
		 "in the case of collections, the directory structure\n"+
		 "mirroring that of the collection and leaf nodes being\n"+
		 "allocated numbers as names starting at zero and incrementing.\n"+
		 "Quoted (\") LSIDs of outputs are replaced with relative file-names.\n"); 
    }
    
    /** 
     * Return the name for this plugin
     */ 
    public String getName() {	
	return "Save to disk as website";
    }
    
    /**
     * Show a standard save dialog and dump the results to disk
     */	
    public ActionListener getListener(Map results, JComponent parent) {
	final Map resultMap = results;
	final JComponent parentComponent = parent;
	return new ActionListener() {
		public void actionPerformed(ActionEvent e) {	
		    JFileChooser jfc = new JFileChooser();
		    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);		
		    int returnVal = jfc.showSaveDialog(parentComponent);
		    Map fileNames = new HashMap();				
		    for (Iterator i = resultMap.keySet().iterator(); i.hasNext();) {
			String resultName = (String) i.next();
			DataThing resultValue = (DataThing) resultMap.get(resultName);
			try {
			    guessFileNames(fileNames, resultName, resultValue);	
			} 
			catch (IOException ioe) {
			    JOptionPane.showMessageDialog(parentComponent,
							  "Problem saving results : \n"+ioe.getMessage(),			
							  "Error!",
							  JOptionPane.ERROR_MESSAGE);	
			}	
		    }
		    for (Iterator i = resultMap.keySet().iterator(); i.hasNext();) {
			String resultName = (String) i.next();
			DataThing resultValue = (DataThing) resultMap.get(resultName);
			try {
			    if (returnVal == JFileChooser.APPROVE_OPTION) {	
				File f = jfc.getSelectedFile();
				String name = resultName;
				DataThing mappedResultValue = mapLSIDs(fileNames, resultValue);
				mappedResultValue.writeToFileSystem(f, name);	
			    }
			} 
			catch (IOException ioe) {	
			    JOptionPane.showMessageDialog(parentComponent,
							  "Problem saving results : \n"+ioe.getMessage(),
							  "Error!",
							  JOptionPane.ERROR_MESSAGE);
			}			
		    }
		}
	    };
    }
    
    /** 
     * Populate fileNames map - based on DataThing.writeToFileSystem
     */	
    protected static void guessFileNames(Map fileNames, String resultName, DataThing resultValue) throws java.io.IOException {
	String defaultExtension = ".text";
	String syntacticType = (resultValue.getSyntacticType().split("'"))[1].toLowerCase();
	if (syntacticType.matches(".*text/xml.*")) 
	    {
		defaultExtension = ".xml";
	    }
	else if (syntacticType.matches(".*text/html.*")) 
	    {
		defaultExtension = ".html";
	    }
	else if (syntacticType.matches(".*text/rtf.*")) 
	    {
		defaultExtension = ".rtf";
	    }
	else if (syntacticType.matches(".*image/png.*")) 
	    {
		defaultExtension = ".png";
	    }
	else if (syntacticType.matches(".*image/jpeg.*")) 
	    {
		defaultExtension = ".jpeg";
	    }
	else if (syntacticType.matches(".*image/gif.*")) 
	    {
		defaultExtension = ".gif";
	    }
	else if (syntacticType.matches(".*x-graphviz.*")) 
	    {
		defaultExtension = ".dot.text";
	    }
	//System.out.println("Output "+resultName+" syntype "+syntacticType+" extn "+defaultExtension);
	guessFileNames(fileNames, resultName, resultValue, resultValue.getDataObject(), defaultExtension);
    }
    // URL-style separator only
    static char sep = '/';
    /**
     * Write a specific object to the filesystem
     * this has no access to metadata about the object
     * and so is not particularly clever
     */
    protected static void guessFileNames(Map fileNames, String name, DataThing resultValue, Object o, String defaultExtension) throws IOException 
    {
	String lsid = resultValue.getLSID(o);
	if (o instanceof Collection) 
	    {
		if (lsid!=null && lsid.length()>0)
		    fileNames.put(lsid, name+sep);
		// Create a new directory, iterate over the collection recursively
		// calling this method
		int count = 0;
		Collection c = (Collection)o;
		for (Iterator i = c.iterator(); i.hasNext();) 
		    {
			guessFileNames(fileNames, name+sep+(count++), resultValue, i.next(), defaultExtension);
		    }
	    }
	else 
	    {
		// Write a single item
		if (o instanceof String && defaultExtension == null) 
		    {
			name = name + ".text";
		    }
		else 
		    {
			if (defaultExtension != null) 
			    {
				name = name + defaultExtension;
			    }
		    }
		if (lsid!=null && lsid.length()>0) 
		    {
			//System.out.println("LSID "+lsid+" -> "+name);
			fileNames.put(lsid, name);
		    }
	    }
    }
    
    /** map LSIDs in "s
     */
    protected DataThing mapLSIDs(Map fileNames, DataThing value) 
    {		Object newValue = replaceLSIDsInDataThingValues(fileNames, value, value.getDataObject(), 0);
    DataThing newdt = new DataThing(newValue);
    try 
	{
	    // work around copyMetadataFrom not taking account of having a different data object
	    SemanticMarkup markup = value.getMetadata();
	    newdt.copyMetadataFrom(value);
	    if (markup!=null) 
		{
		    SemanticMarkup newMarkup = newdt.getMetadata();				if (markup.getSemanticType()!=null)					newMarkup.setSemanticType(markup.getSemanticType());				String [] mimeTypes = markup.getMIMETypes();				for (int i=0; mimeTypes!=null && i<mimeTypes.length; i++) 
			{					//System.out.println("copy mime type "+mimeTypes[i]+" ["+i+"]");
			    newMarkup.addMIMEType(mimeTypes[i]);				}			} 
	}
    catch (Exception e) 
	{			System.err.println("ERROR copying semantic markup while mapping lsids: "+e);			e.printStackTrace(System.err);		}		return newdt;	}	/** replace lsids in String values
																										 */
    private Object replaceLSIDsInDataThingValues(Map fileNames, DataThing dt, Object value, int depth) 
    {
	Object newValue = value;
	if (value instanceof String) 
	    {
		String asString = (String)value;
		int index = asString.indexOf("\"urn:lsid:");
		if (index<0)
		    // unchanged
		    return value;
			    
		StringBuffer buffer = new StringBuffer((String)value);
		while (index >= 0) 
		    {
			int from = index+1;
			int len = "urn:lsid:".length();
			while (from+len < buffer.length() &&
			       (Character.isLetterOrDigit(buffer.charAt(from+len)) || buffer.charAt(from+len)==':' || buffer.charAt(from+len)=='.')) 
			    len++;
			String lsid = buffer.substring(from, from+len);
			String filename = (String)fileNames.get(lsid);
			if (filename==null) 
			    {
				System.err.println("WARNING: unknown filename for "+lsid);
			    } 
			else
			    {
				buffer.replace(from, from+len, filename);
				for (int di=0; di<depth; di++)
				    buffer.insert(from,"../");
			    }
			index = buffer.indexOf("\"urn:lsid:", index+1);
		    }
		newValue = buffer.toString();
	    } 
	else if (value instanceof byte[] || value instanceof byte[][])
	    return value;
	else if (value.getClass().isArray()) 
	    {
		for (int i=0; i<java.lang.reflect.Array.getLength(value); i++)
		    java.lang.reflect.Array.set(value, i, 
						replaceLSIDsInDataThingValues(fileNames, dt, 
									      java.lang.reflect.Array.get(value, i), depth+1));
	    }
	else if (value instanceof List) 
	    {
		ArrayList newList = new ArrayList((List)value);
		for (int i=0; i<newList.size(); i++) 
		    newList.set(i, replaceLSIDsInDataThingValues(fileNames, dt, newList.get(i), depth+1));
		newValue = newList;
	    } 
	else
	    System.err.println("MapLSIDs ignoring a "+value.getClass().getName()+" - sorry");

	if (!(newValue==value)) 
	    {
		dt.setLSID(newValue, dt.getLSID(value));
	    }
	return newValue;
    }
}
