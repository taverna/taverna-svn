/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import org.embl.ebi.escience.scuflworkers.talisman.TalismanProcessorFactory;
import org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessorFactory;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedScavenger;

// Utility Imports
import java.util.StringTokenizer;
import java.util.Vector;

// IO Imports
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

// JDOM Imports
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

// Network Imports
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;




/**
 * A scavenger that does a web crawl starting at the specified
 * URL to find scufl xml files. If it finds any, it adds the 
 * appropriate WorkflowProcessorFactory nodes to the scavenger
 * tree. If it finds talisman tscript definitions it adds those
 * too.
 * Code modified from that found at 
 * http://developer.java.sun.com/developer/technicalArticles/ThirdParty/WebCrawler/
 * @author Tom Oinn
 */
public class WebScavenger extends Scavenger {

    public static final String DISALLOW = "Disallow:";
        

    private DefaultTreeModel treeModel = null;
    private DefaultMutableTreeNode progressDisplayNode = new DefaultMutableTreeNode("Searching...");
    
    /**
     * Creates a new web scavenger, starting the web
     * crawl in a new thread and returning immediately
     */
    public WebScavenger(String initialURL, DefaultTreeModel model) throws ScavengerCreationException {
	super("Web crawl @ "+initialURL);
	treeModel = model;
	add(progressDisplayNode);
	// set default for URL access
	final String theURL = initialURL;
	//URLConnection.setDefaultAllowUserInteraction(false);
	Thread urlThread = new Thread() {
		public void run() {
		    try {
			System.out.println("Created new thread...");
			getXScuflURLs(theURL);
			
			remove(progressDisplayNode);
			treeModel.nodeStructureChanged((TreeNode)(WebScavenger.this));
			System.out.println("Done searching.");
		    }
		    catch (ScavengerCreationException sce) {
			//
		    }
		}
	    };
	urlThread.start();
    }
    
    void getXScuflURLs(String initialURL) throws ScavengerCreationException {
	String[] allURLs;
	try {
	    allURLs = search(initialURL);
	}
	catch (MalformedURLException mue) {
	    throw new ScavengerCreationException("Cannot crawl from an invalid URL");
	}
	SAXBuilder sb = new SAXBuilder(false);
	for (int i = 0; i < allURLs.length; i++) {
	    try {
		// If the URL ends in 'wsdl' then try to parse it as a wsdl document
		if (allURLs[i].toLowerCase().endsWith("wsdl")) {
		    try {	    
			progressDisplayNode.setUserObject("Parsing WSDL at : "+allURLs[i]);
			treeModel.nodeChanged(progressDisplayNode);
			add(new WSDLBasedScavenger(allURLs[i]));
		    }
		    catch (ScavengerCreationException sce) {
			//
		    }			   
		}
		else {
		    progressDisplayNode.setUserObject("Reading : "+allURLs[i]);
		    treeModel.nodeChanged(progressDisplayNode);
		    // If this is an XScufl url then add a new WorkflowProcessorFactory to the node
		    Document doc = sb.build(new InputStreamReader(new URL(allURLs[i]).openStream()));
		    Element root = doc.getRootElement();
		    if (root.getName().equals("scufl")) {
			WorkflowProcessorFactory wpf = new WorkflowProcessorFactory(allURLs[i]);
			add(new DefaultMutableTreeNode(wpf));
		    }
		    else if (root.getName().equals("tscript")) {
			TalismanProcessorFactory tpf = new TalismanProcessorFactory(allURLs[i]);
			add(new DefaultMutableTreeNode(tpf));
		    }
		}
	    }
	    catch (Exception e) {
		throw new ScavengerCreationException(e.getMessage());
	    }
	}
    }

    boolean robotSafe(URL url) {
	return true;
    }

    /**
     * Check whether there is a robots.txt that would ban access to
     * the URL and those below it
     */
    boolean robotSafeOld(URL url) {
       	String strHost = url.getHost();
	// form URL of the robots.txt file
	String strRobot = "http://" + strHost + "/robots.txt";
	URL urlRobot;
	try { 
	    urlRobot = new URL(strRobot);
	} catch (MalformedURLException e) {
	    // something weird is happening, so don't trust it
	    return false;
	}
	String strCommands;
	try {
	    InputStream urlRobotStream = urlRobot.openStream();
	    // read in entire file
	    byte b[] = new byte[1000];
	    int numRead = urlRobotStream.read(b);
	    strCommands = new String(b, 0, numRead);
	    while (numRead != -1) {
		numRead = urlRobotStream.read(b);
		if (numRead != -1) {
		    String newCommands = new String(b, 0, numRead);
		    strCommands += newCommands;
		}
	    }
	    urlRobotStream.close();
	} catch (IOException e) {
	    // if there is no robots.txt file, it is OK to search
	    return true;
	}
	
	// assume that this robots.txt refers to us and 
	// search for "Disallow:" commands.
	String strURL = url.getFile();
	int index = 0;
	while ((index = strCommands.indexOf(DISALLOW, index)) != -1) {
	    index += DISALLOW.length();
	    String strPath = strCommands.substring(index);
	    StringTokenizer st = new StringTokenizer(strPath);
	    if (!st.hasMoreTokens())
		break;
	    String strBadPath = st.nextToken();
	    // if the URL starts with a disallowed path, it is not safe
	    if (strURL.indexOf(strBadPath) == 0)
		return false;
	}
	return true;
    }
    
    /**
     * Return an array of strings of URLs of XScufl files found
     * by a web crawl from the initial URL.
     */
    private String[] search(String initialURL) throws MalformedURLException {
	int numberSearched = 0;
	int numberFound = 0;
	Vector vectorMatches = new Vector();
	Vector vectorToSearch = new Vector();
	Vector vectorSearched = new Vector();
	Vector listMatches = new Vector();
	if (initialURL.length() == 0) {
	    return new String[0];
	}
	String strURL = initialURL;
	
	vectorToSearch.addElement(initialURL);
	
	while (vectorToSearch.size() > 0) {
	    // get the first element from the to be searched list
	    strURL = (String)vectorToSearch.elementAt(0);
	    progressDisplayNode.setUserObject("Examining : "+strURL);
	    treeModel.nodeChanged(progressDisplayNode);
	    URL url = new URL(strURL);
	    // mark the URL as searched (we want this one way or the other)
	    vectorToSearch.removeElementAt(0);
	    vectorSearched.addElement(strURL);
	    // System.out.println(strURL);
	    // can only search http: protocol URLs
	    if (url.getProtocol().compareTo("http") != 0) {
		break;
	    }
	    // test to make sure it is before searching
	    if (!robotSafe(url)) {
		break;
	    }
	    try {
		// try opening the URL
		URLConnection urlConnection = url.openConnection();
		urlConnection.setAllowUserInteraction(false);
		InputStream urlStream = url.openStream();
		// search the input stream for links
		// first, read in the entire URL
		byte b[] = new byte[1000];
		int numRead = urlStream.read(b);
		String content = new String(b, 0, numRead);
		while (numRead != -1) {
		    numRead = urlStream.read(b);
		    if (numRead != -1) {
			String newContent = new String(b, 0, numRead);
			content += newContent;
		    }
		}
		urlStream.close();
		
		String lowerCaseContent = content.toLowerCase();
		int index = 0;
		while ((index = lowerCaseContent.indexOf("<a", index)) != -1)
		    {
			if ((index = lowerCaseContent.indexOf("href", index)) == -1) 
			    break;
			if ((index = lowerCaseContent.indexOf("=", index)) == -1) 
			    break;
			index++;
			String remaining = content.substring(index);
			StringTokenizer st = new StringTokenizer(remaining, "\t\n\r\">#");
			String strLink = st.nextToken();
			URL urlLink;
			try {
			    urlLink = new URL(url, strLink);
			    strLink = urlLink.toString();
			} catch (MalformedURLException e) {
			    continue;
			}
			boolean validURLToSearch = true;
			//System.out.println("Examining link : "+strLink);
			// only look at http links
			if (urlLink.getProtocol().compareTo("http") != 0) {
			    //System.out.println("  - not http");
			    validURLToSearch=false;
			}
			// If there is a '?' in the url then reject it
			if (strLink.indexOf("?")>0) {
			    validURLToSearch=false;
			    //System.out.println("Not searching "+strLink+", it has a query in it");
			}
			// Only look at links that are 'below' the original one in
			// the web heirarchy
			if (strLink.toLowerCase().startsWith(initialURL.toLowerCase()) == false) {
			    validURLToSearch=false;
			    //System.out.println("Not searching "+strLink+", doesn't start with "+initialURL);
			}
			// If the link ends with .txt or .xml then we don't want to search any more
			if (strLink.toLowerCase().endsWith(".xml") || 
			    strLink.toLowerCase().endsWith(".txt") ||
			    strLink.toLowerCase().endsWith("wsdl")) {
			    validURLToSearch = false;
			}
			try {
			    // try opening the URL
			    //URLConnection urlLinkConnection = urlLink.openConnection();
			    //urlLinkConnection.setAllowUserInteraction(false);
			    //InputStream linkStream = urlLink.openStream();
			    //String strType = urlLinkConnection.guessContentTypeFromStream(linkStream);
			    //linkStream.close();
			    // if another page, add to the end of search list
			    //System.out.println(strType);
			    // check to see if this URL has already been 
			    // searched or is going to be searched
			    if ((!vectorSearched.contains(strLink)) 
				&& (!vectorToSearch.contains(strLink))) {
				// test to make sure it is robot-safe!
				if (robotSafe(urlLink) && validURLToSearch) {
				    vectorToSearch.addElement(strLink);
				}
			    }
			    
			    
			    // if the proper type, add it to the results list
			    // unless we have already seen it
			    if (strLink.indexOf(".xml")>-1 || strLink.toLowerCase().endsWith("wsdl")) {
				if (vectorMatches.contains(strLink) == false) {
				    listMatches.add(strLink);
				    vectorMatches.addElement(strLink);
				    numberFound++;
				}
			    }
			} 
			catch (Exception e) {
			    continue;
			}
		    }
	    } catch (IOException e) {
		break;
	    }
	    
	    numberSearched++;
	}
	return (String[])(listMatches.toArray(new String[0]));
    }
}
