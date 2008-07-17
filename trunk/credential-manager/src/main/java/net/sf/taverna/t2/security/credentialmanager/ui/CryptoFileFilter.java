package net.sf.taverna.t2.security.credentialmanager.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * File filter for filtering against various file extensions.
 * 
 * @author Alexandra Nenadic
 */
public class CryptoFileFilter extends javax.swing.filechooser.FileFilter 
{
	/** Description of the filter */
	private String description;
	
	/** Array of file extensions to filter against */
	private ArrayList<String> exts = new ArrayList<String>();
	
    /**
     * Construct a CryptoFileFilter for a set of related file extensions.
     *
     * @param extList Array of file extensions
     * @param sDescription Short collective description for the file extensions
     */
    public CryptoFileFilter(String [] extList, String sDescription)
    {
        for (int i = 0; i < extList.length; i++) {
            addType (extList[i]);
        }
        description = sDescription;
    }

	private void addType(String s) {
		exts.add(s);
	}

	/** Return true if the given file is accepted by this filter. */
	public boolean accept(File f) 
	{
		// Little trick: if you don't do this, only directory names
		// ending in one of the extentions appear in the window.
		if (f.isDirectory()) 
	    {
	    	return true;
	    } 
	    else if (f.isFile()) 
	    {
	    	Iterator<String> it = exts.iterator();
	    	while (it.hasNext()) {
	        if (f.getName().toLowerCase().endsWith((String) it.next()))
	        	return true;
	    	}
	    }
	    // A file that didn't match.
	    return false;
	  }

	  /** Set the printable description of this filter. */
	  public void setDescription(String s) {
	    description = s;
	  }

	  /** Return the printable description of this filter. */
	  public String getDescription() {
	    return description;
	  }
	}
