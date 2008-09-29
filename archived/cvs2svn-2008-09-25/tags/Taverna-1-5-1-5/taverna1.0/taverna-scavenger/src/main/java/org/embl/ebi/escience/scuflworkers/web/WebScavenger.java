/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.URLBasedScavenger;
import org.embl.ebi.escience.scuflui.workbench.scavenger.spi.ScavengerRegistry;
//import org.embl.ebi.escience.scuflworkers.talisman.TalismanProcessorFactory;
//import org.embl.ebi.escience.scuflworkers.workflow.WorkflowScavenger;
//import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedScavenger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * A scavenger that does a web crawl starting at the specified URL to find scufl
 * xml files. If it finds any, it adds the appropriate WorkflowProcessorFactory
 * nodes to the scavenger tree. If it finds talisman tscript definitions it adds
 * those too. Code modified from that found at
 * http://developer.java.sun.com/developer/technicalArticles/ThirdParty/WebCrawler/
 * 
 * @author Tom Oinn
 */
public class WebScavenger extends Scavenger {
	
	private static final long serialVersionUID = -4238626422988051418L;
	
	private static Logger logger = Logger.getLogger(WebScavenger.class);
	
	//private static final String DISALLOW = "Disallow:";
	
	private DefaultTreeModel treeModel = null;
	
	private DefaultMutableTreeNode progressDisplayNode = new DefaultMutableTreeNode(
	"Searching...");
	
	/**
	 * Creates a new web scavenger, starting the web crawl in a new thread and
	 * returning immediately
	 */
	public WebScavenger(String initialURL, DefaultTreeModel model)
	throws ScavengerCreationException {
		super("Web crawl @ " + initialURL);
		treeModel = model;
		add(progressDisplayNode);
		// set default for URL access
		final URL url;
		try {
			url = new URL(initialURL);
		} catch (MalformedURLException e) {
			throw new ScavengerCreationException("Invalid URL: " + initialURL);
		}
		Thread urlThread = new Thread() {
			public void run() {
				logger.info("Created new web scavenger thread...");
				try {
					getXScuflURLs(url);
				} finally {
					// Remove the status thingie even if we fail
					remove(progressDisplayNode);
					treeModel.nodeStructureChanged((WebScavenger.this));
				}
				logger.info("Done searching " + url);
			}
		};
		urlThread.start();
	}
	
	/**
	 * Build the tree of scavengers by traversing the initialURL and
	 * subpages, and then checking all links for WSDLs and workflows, 
	 * which are addeed to the scavenger tree.
	 * 
	 * @param initialURL
	 */
	void getXScuflURLs(URL initialURL) {

		for (URL url : search(initialURL)) {
			// If the URL ends in 'wsdl' then try to parse it as a wsdl
			// document.
			// (Note that we check getFile, so we support both .wsdl and ?wsdl)
			boolean validURL = false;
			if (url.getFile().toLowerCase().endsWith("wsdl")) {
				progressDisplayNode.setUserObject("Parsing WSDL at: "
						+ url);
				treeModel.nodeChanged(progressDisplayNode);
				validURL = true;
			}
			else if (url.getFile().toLowerCase().endsWith("xml")) {
				progressDisplayNode.setUserObject("Reading : " + url);
				treeModel.nodeChanged(progressDisplayNode);
				validURL = true;
			}
			if (validURL) {
				for (Scavenger scavenger : ScavengerRegistry.instance().getScavengers()) {
					try {
						if (scavenger instanceof URLBasedScavenger) {
							add(((URLBasedScavenger)scavenger).fromURL(url));
						}
					}
					catch (ScavengerCreationException sce) {
						logger.info("Could not add processor for " + url);				
					} 
					catch (OutOfMemoryError e) {
						// In particular, this could happen with
						// http://www.ncbi.nlm.nih.gov/entrez/eutils/soap/eutils.wsdl 					
						logger.error("Out of memory adding processor from " + url);
					}
				}
			}
			
		}
	}
	
	
	boolean robotSafe(URL url) {
		return true;
	}
	
	/**
	 * Check whether there is a robots.txt that would ban access to the URL and
	 * those below it. Currently disabled.
	 */
	/*
	 boolean robotSafe(URL url) {
	 // TODO: Avoid loading robots.txt every time
	  URL urlRobot;
	  try {
	  urlRobot = new URL(url, "/robots.txt");
	  } catch (MalformedURLException e1) {
	  // something weird is happening, so don't trust it
	   return false;
	   }
	   String strCommands;
	   try {
	   InputStream urlRobotStream = urlRobot.openStream();
	   strCommands = IOUtils.toString(urlRobotStream, "latin1");
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
	       */
	
	/**
	 * Return URLs of XScufl files and WSDL descriptions
	 * found by a web crawl from the initial URL.
	 */
	private Iterable<URL> search(URL initialURL) {
		// NOTE: LinkedList is not thread safe
		Queue<URL> searchQueue = new LinkedList<URL>();
		searchQueue.isEmpty();
		// Note that an URL is in visits even if its just in searchQueue
		Set<URL> visits = new HashSet<URL>();
		// maintain order in matches, which is the result set of wsdls and xscufls
		Set<URL> services = new LinkedHashSet<URL>();		
		
		// We'll start with the initial URL
		searchQueue.add(initialURL);
		visits.add(initialURL);
		
		// We'll only traverse URLs that go below our initial address. We'll 
		// check all links to see if they are workflows or wsdl's, though.
		String root = initialURL.toString().toLowerCase();
		
		// Main loop, search, width first
		// TODO: Avoid extensive depth search, could possibly go on forever
		while (!searchQueue.isEmpty()) {
			// Since we're the same thread inputting to the queue, 
			// remove() should not throw NoSuchElementException 
			// after !isempty()
			URL url = searchQueue.remove();								
			progressDisplayNode.setUserObject("Examining : " + url);
			treeModel.nodeChanged(progressDisplayNode);			
			
			// can only search http: protocol URLs
			if (url.getProtocol().compareTo("http") != 0) {
				logger.info("Ignoring non-http URL " + url);
				continue;
			}
			// test to make sure it is robot-safe before searching
			if (!robotSafe(url)) {
				logger.info("Skipping robot.txt forbidden URL " + url);
				continue;
			}
							
			logger.debug("extracting links for "+url);			
			
			for (URL link : extractLinks(url)) {
				logger.debug("Extracted URL="+url);
				String lowercaseLink = link.toString().toLowerCase();
				// By default, we'll follow the link for more URLs
				boolean traverseURL = true;
				// but only look at http links
				if (link.getProtocol().compareTo("http") != 0) {
					traverseURL = false;
				}
				// If there is a '?' in the url then don't traverse down, avoids
				// getting trapped in (some) dynamic pages
				// (it might be a ?wsdl though, so we'll still check the
				// content)
				if (link.getQuery() != null) {
					traverseURL = false;
				}
				// Only look at links that are 'below' the original one in
				// the web hierarchy. Note that we check with the original's folder, 
				// ie. to the last /
				if (!lowercaseLink.startsWith(root)) {
					traverseURL = false;
				}
				// If the link ends with .txt or .xml then we don't want to
				// search it for links
				if (lowercaseLink.endsWith(".xml") || lowercaseLink.endsWith(".txt")
						|| lowercaseLink.endsWith("wsdl")) {
					traverseURL = false;
				}
				// If it's a new URL, we'll visit it
				if (traverseURL && !visits.contains(link) && robotSafe(link)) {					
					visits.add(link);
					searchQueue.add(link);					
				}
				// If we think it's a workflow or a WSDL document, we'll consider
				// it as a possible match
				if (lowercaseLink.indexOf(".xml") > -1
						|| lowercaseLink.toLowerCase().endsWith("wsdl")) {									
					services.add(link);											
				}
			}			
		}		
		logger.debug("Found "+services.size()+" services.");
		return services;
	}
	
	/**
	 * Extract links from a HTML page (given as a String).
	 * <p>
	 * Not exactly strictly parsing the HTML, but roughly searches for 
	 * <a href=..> links, which should work for most pages.
	 * 
	 * @param url Address of HTML page to search for links
	 * @return An iterable of URLs from the page, possibly empty if an error occured or no links were found
	 */
	private Iterable<URL> extractLinks(URL url) {
		// In order, but without duplicates
		Set<URL> links = new LinkedHashSet<URL>();
		String html = fetch(url);
		if (html == null) {
			// Return an empty iterator
			return links;
		}		
		// Look for links using a rough HTML search
		String lowerCaseContent = html.toLowerCase();
		int index = 0;
		while ((index = lowerCaseContent.indexOf("<a", index)) != -1) {
			if ((index = lowerCaseContent.indexOf("href", index)) == -1)
				break;
			if ((index = lowerCaseContent.indexOf("=", index)) == -1)
				break;
			index++;
			String remaining = html.substring(index);
			StringTokenizer st = new StringTokenizer(remaining,
			"\t\n\r\">#");
			String strLink = st.nextToken();							
			try {
				URL urlLink = new URL(url, strLink);
				links.add(urlLink);
			} catch (MalformedURLException e) {
				continue;
			}		
		}
		return links;
	}
	
	
	/**
	 * Load a page from a URL, return as String.
	 * <p>
	 * This is done with user interaction disabled and a 
	 * low (1s) connection timeout, and an assumption of character
	 * encoding to be latin1.
	 * <p>
	 * If an error occurs, null is returned.
	 * 
	 * @param url Address of resource
	 * @return String of resource
	 */
	private String fetch(URL url) {
		String content;
		// try opening the URL, avoid pop-ups
		try {
			URLConnection urlConnection = url.openConnection();
			urlConnection.setAllowUserInteraction(false);
			// 1 second timeout max
			urlConnection.setConnectTimeout(1000);
			InputStream urlStream = urlConnection.getInputStream();
			// FIXME: Should only read the first megabyte! People might link to
			// movies, etc.				
			// Assumes encoding latin1.. but since we only care about URLs anyway,
			// this shouldn't matter much
			try {				
				content = IOUtils.toString(urlStream, "latin1");
			} catch (OutOfMemoryError e) {
				logger.error("Out of memory retrieving " + url);				
				return null;
			} finally {
				urlStream.close();
			}
		} catch (IOException e) {
			logger.info("Could not read " + url);
			return null;
		}
		return content;
	}
}
