/*
 *
 * Copyright (C) 2003 The University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 */

package uk.ac.man.cs.img.fetaEngine.store.load;

/**
 * 
 * @author alperp
 */

// Utility Imports
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Code modified from that found at
 * http://developer.java.sun.com/developer/technicalArticles/ThirdParty/WebCrawler/
 */
public class WebCrawler {

	/**
	 * Creates a new web crawler
	 */
	public WebCrawler() {
		// set default for URL access
		// final String theURL = initialURL;

	}

	public List getXMLURLs(String initialURL) throws FetaLoadException {
		String[] allURLs;
		List allPedroXMLURLS = new ArrayList();
		try {
			allURLs = search(initialURL);
		} catch (MalformedURLException mue) {
			throw new FetaLoadException("Cannot crawl from an invalid URL");
		}

		for (int i = 0; i < allURLs.length; i++) {
			try {
				// If the URL ends in 'xml' then add it to the list of Pedro XML
				// document URLs
				if (allURLs[i].toLowerCase().endsWith("xml")) {
					try {
						System.out.println("Loading XML at : " + allURLs[i]);
						allPedroXMLURLS.add(allURLs[i]);
					} catch (Exception e) {
						//
					}
				} else {
					// do nothing
				}

			} catch (Exception e) {
				throw new FetaLoadException(e.getMessage());
			}
		}
		return allPedroXMLURLS;
	}

	
	
	public List getXMLandRDFURLs(String initialURL) throws FetaLoadException {
		String[] allURLs;
		List allFetaDescURLS = new ArrayList();
		try {
			allURLs = search(initialURL);
		} catch (MalformedURLException mue) {
			throw new FetaLoadException("Cannot crawl from an invalid URL");
		}

		for (int i = 0; i < allURLs.length; i++) {
			try {
				// If the URL ends in 'xml' then add it to the list of Pedro XML
				// document URLs
				if (allURLs[i].toLowerCase().endsWith("xml")||allURLs[i].toLowerCase().endsWith("rdf")) {
					try {
						System.out.println("Loading desc at : " + allURLs[i]);
						allFetaDescURLS.add(allURLs[i]);
					} catch (Exception e) {
						//
					}
				} else {
					// do nothing
				}

			} catch (Exception e) {
				throw new FetaLoadException(e.getMessage());
			}
		}
		return allFetaDescURLS;
	}

	
	/**
	 * Return an array of strings of URLs of Pedro XML files found by a web
	 * crawl from the initial URL.
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
			strURL = (String) vectorToSearch.elementAt(0);
			System.out.println("Examining : " + strURL);

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
				while ((index = lowerCaseContent.indexOf("<a", index)) != -1) {
					if ((index = lowerCaseContent.indexOf("href", index)) == -1)
						break;
					if ((index = lowerCaseContent.indexOf("=", index)) == -1)
						break;
					index++;
					String remaining = content.substring(index);
					StringTokenizer st = new StringTokenizer(remaining,
							"\t\n\r\">#");
					String strLink = st.nextToken();
					URL urlLink;
					try {
						urlLink = new URL(url, strLink);
						strLink = urlLink.toString();
					} catch (MalformedURLException e) {
						continue;
					}
					boolean validURLToSearch = true;
					// System.out.println("Examining link : "+strLink);
					// only look at http links
					if (urlLink.getProtocol().compareTo("http") != 0) {
						// System.out.println(" - not http");
						validURLToSearch = false;
					}
					// If there is a '?' in the url then reject it
					if (strLink.indexOf("?") > 0) {
						validURLToSearch = false;
						// System.out.println("Not searching "+strLink+", it has
						// a query in it");
					}
					// Only look at links that are 'below' the original one in
					// the web heirarchy
					if (strLink.toLowerCase().startsWith(
							initialURL.toLowerCase()) == false) {
						validURLToSearch = false;
						// System.out.println("Not searching "+strLink+",
						// doesn't start with "+initialURL);
					}
					// If the link ends with .txt or .xml then we don't want to
					// search any more
					if (strLink.toLowerCase().endsWith(".xml")
							|| strLink.toLowerCase().endsWith(".rdf")
							|| strLink.toLowerCase().endsWith(".txt")
							|| strLink.toLowerCase().endsWith("wsdl")) {
						validURLToSearch = false;
					}
					try {
						// check to see if this URL has already been
						// searched or is going to be searched
						if ((!vectorSearched.contains(strLink))
								&& (!vectorToSearch.contains(strLink))) {

							if (validURLToSearch) {
								vectorToSearch.addElement(strLink);
							}
						}

						// if the proper type, add it to the results list
						// unless we have already seen it
						if (strLink.indexOf(".xml") > -1 ||
							strLink.indexOf(".rdf") > -1	|| 
							strLink.toLowerCase().endsWith("wsdl")) {
							if (vectorMatches.contains(strLink) == false) {
								listMatches.add(strLink);
								vectorMatches.addElement(strLink);
								numberFound++;
							}
						}
					} catch (Exception e) {
						continue;
					}
				}
			} catch (IOException e) {
				break;
			}

			numberSearched++;
		}
		return (String[]) (listMatches.toArray(new String[0]));
	}

	/*
	 * public static void main(String[] args) { try { List xmlURLArray = new
	 * ArrayList(); System.out.println("Crawling started with initial URL ..."
	 * +"http://cvs.mygrid.org.uk/cgi-bin/viewcvs.cgi/mygrid/feta/etc/");
	 * WebCrawler crwlr = new
	 * WebCrawler("http://cvs.mygrid.org.uk/cgi-bin/viewcvs.cgi/mygrid/feta/etc/sampleData/test/");
	 * xmlURLArray =
	 * crwlr.getXMLURLs("http://cvs.mygrid.org.uk/cgi-bin/viewcvs.cgi/mygrid/feta/etc/sampleData/test/");
	 * 
	 *  } catch (Exception e) { e.printStackTrace(); } }
	 */
}
