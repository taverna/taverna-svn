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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import uk.ac.man.cs.img.fetaEngine.commons.FetaModelXSD;

/**
 * 
 * @author alperp
 */
public class FetaLoad {

	public Map loadFromFileSystem(String path, char pathType) {
		return loadFromFileSystem(new File(path));

	}

	public Map loadFromFileSystem(File filePath) {
		List locationList = new ArrayList();
		loadFromFileSystem0(filePath, locationList);

		return readFetaDescriptions(locationList, 'f');
	}

	private void loadFromFileSystem0(File filePath, List list) {
		try {
			// recurse file system. Circular symlinks will kill
			// this. Need to store list of files.
			if (filePath.isDirectory()) {
				loadFromFileSystemDirectory(filePath, list);
			} else {
				loadFromFileSystemFile(filePath, list);
			}

		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	private void loadFromFileSystemFile(File file, List list) {

		if (file.getName().endsWith("xml")) {

			list.add(file.toString());
		}
	}

	private void loadFromFileSystemDirectory(File directory, List list) {
		File[] children = directory.listFiles();
		for (int i = 0; i < children.length; i++) {
			loadFromFileSystem0(children[i], list);
		}
	}

	public Document readtoDOM(File filePath) throws FetaLoadException {
		try {
			Document document;
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			System.out
					.println("Feta LOAD...Loading XML description file from  --->"
							+ filePath);
			document = builder.parse(filePath);

			return document;
		} catch (Exception e) {
			// Debug only
			e.printStackTrace();
			return null;
		}

	}

	public Document readtoDOM(String fileURL) throws FetaLoadException {
		try {
			Document document;
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			System.out
					.println("Feta LOAD...Loading XML description file from  --->"
							+ fileURL);
			document = builder.parse(fileURL);
			return document;
		} catch (Exception e) {
			// Debug only
			e.printStackTrace();
			return null;
		}

	}

	public Map loadFromWeb(String initialURL) {
		try {

			System.out
					.println("Obtaning locations for Feta descriptions from web location registry at:  \n"
							+ initialURL);
			WebCrawler crwlr = new WebCrawler();
			System.out.println("Done searching.");
			return readFetaDescriptions(crwlr.getXMLURLs(initialURL), 'u');
		}

		catch (FetaLoadException fe) {
			fe.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map loadFromUDDI(String inquiryURL) {

		try {

			System.out
					.println("Obtaning locations for Feta descriptions from UDDI registry at:  \n"
							+ inquiryURL.toString());
			UDDICrawler crwlr = new UDDICrawler();
			System.out.println("Done searching.");
			return readFetaDescriptions(crwlr.getXMLURLs(inquiryURL), 'u');
		}

		catch (FetaLoadException fe) {
			fe.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map readFetaDescriptions(List listOfLocations, char mode) {
		Map mapOfDOMs = new HashMap();

		try {

			for (int i = 0; i < listOfLocations.size(); i++) {

				Document doc;
				URL documentLocationURL;

				if (mode == 'u') {
					documentLocationURL = new URL((String) listOfLocations
							.get(i));
					doc = readtoDOM((String) listOfLocations.get(i));
				} else {
					File tmpFile = new File((String) listOfLocations.get(i));
					documentLocationURL = tmpFile.toURL();
					doc = readtoDOM(tmpFile);
				}
				if (doc == null)
					continue;
				Element serviceDescriptions = doc.getDocumentElement();
				NodeList serviceDescriptionList = (NodeList) serviceDescriptions
						.getElementsByTagName(FetaModelXSD.SERVICE_DESC);

				for (int j = 0; j < serviceDescriptionList.getLength(); j++) {
					Element serviceDescriptionLocation = doc
							.createElement("serviceDescriptionLocation");
					// NOTE THAT WE DO NOT HAVE THIS ELEMENT IN OUR SCHEMA!!
					/*
					 * We add an additional element to the DOM tree that holds
					 * the location of the pedro description that is being
					 * loaded in to Feta !!!This element does not exist in the
					 * schema we use within Pedro!!! This manipulation will
					 * later be removed when we migrate to MIR integration and
					 * start returning LSIDs instead of URIs.....
					 */

					serviceDescriptionLocation.appendChild(doc
							.createTextNode((String) listOfLocations.get(i)));
					serviceDescriptionList.item(j).appendChild(
							serviceDescriptionLocation);
				}
				mapOfDOMs.put(documentLocationURL.toString(), doc);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mapOfDOMs;

	}

	public Map load(FetaSourceRepository rep) {

		if (rep.getRepositoryType() == RepositoryType.UDDI) {
			return loadFromUDDI(rep.getRepositoryLocation().toString());
		} else if (rep.getRepositoryType() == RepositoryType.WEB) {
			return loadFromWeb(rep.getRepositoryLocation().toString());
		} else if (rep.getRepositoryType() == RepositoryType.FILE) {
			return loadFromFileSystem(new File(rep.getRepositoryLocation()
					.getFile()));
		} else {
			return null;
		}

	}

}
