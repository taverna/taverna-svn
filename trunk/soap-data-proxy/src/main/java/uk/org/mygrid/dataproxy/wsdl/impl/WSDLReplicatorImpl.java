/*
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
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: WSDLReplicatorImpl.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-28 16:54:10 $
 *               by   $Author: sowen70 $
 * Created on 22 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.wsdl.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jaxen.JaxenException;
import org.jaxen.dom4j.Dom4jXPath;
import org.jdom.input.SAXBuilder;

import uk.org.mygrid.dataproxy.wsdl.WSDLReplicator;

public class WSDLReplicatorImpl implements WSDLReplicator {	
	
	private String rootPath = "";
	private static Logger logger = Logger.getLogger(WSDLReplicatorImpl.class);
	
	public WSDLReplicatorImpl(String rootPath) {
		this.rootPath=rootPath;
		if (!rootPath.endsWith("/")) rootPath+="/";
	}	
		
	
	private Document changeEndpoint(String wsdlID, Document doc) throws JaxenException {	
		Dom4jXPath path = new Dom4jXPath("//wsdl:service/wsdl:port/soap:address");		
		path.addNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
		path.addNamespace("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
		Element el = (Element)path.selectSingleNode(doc);	
		el.attribute("location").setValue(rootPath+"proxy?id="+wsdlID);
		
		return doc;
	}

	public void replicateRemoteWSDL(String wsdlID, String wsdlName, URL wsdlUrl, File destinationDirectory) throws Exception {		
		File wsdlDir = new File(destinationDirectory,wsdlID);
		if (!wsdlDir.exists()) {
			wsdlDir.mkdir();
		}
		else {
			if (!wsdlDir.isDirectory()) {
				throw new IOException("Directory for WSDL already exists as a file ("+wsdlDir.toString()+")");
			}
		}
				
		File replicatedFile = new File(wsdlDir,wsdlName);
		//TODO: handle overwriting existing file.
		replicatedFile.createNewFile();		
		
		Document doc = changeEndpoint(wsdlID,new SAXReader().read(wsdlUrl.openStream()));
		fetchImportedSchemasFromWSDLDoc(doc,wsdlDir,wsdlUrl);
		
		
		XMLWriter writer = new XMLWriter(new FileOutputStream(replicatedFile));
		writer.write(doc);
		writer.close();		
	}
	
	private void fetchImportedSchemasFromWSDLDoc(Document doc, File wsdlDir, URL wsdlURL) throws JaxenException {
		Dom4jXPath path = new Dom4jXPath("//wsdl:types/s:schema/s:import");		
		path.addNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
		path.addNamespace("s", "http://www.w3.org/2001/XMLSchema");
		
		List nodes=path.selectNodes(doc);
		fetchFromSchemaNodeList(wsdlDir, wsdlURL, nodes);
		
		path = new Dom4jXPath("//wsdl:types/s:schema/s:include");		
		path.addNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
		path.addNamespace("s", "http://www.w3.org/2001/XMLSchema");
		
		nodes=path.selectNodes(doc);
		fetchFromSchemaNodeList(wsdlDir, wsdlURL, nodes);
	}
	
	private void fetchImportedSchemasFromSchemaDoc(Document doc, File wsdlDir, URL wsdlURL) throws JaxenException {
		Dom4jXPath path = new Dom4jXPath("//s:schema/s:import");				
		path.addNamespace("s", "http://www.w3.org/2001/XMLSchema");
		
		List nodes=path.selectNodes(doc);
		fetchFromSchemaNodeList(wsdlDir, wsdlURL, nodes);
		
		path = new Dom4jXPath("//s:schema/s:include");				
		path.addNamespace("s", "http://www.w3.org/2001/XMLSchema");
		
		nodes=path.selectNodes(doc);
		fetchFromSchemaNodeList(wsdlDir, wsdlURL, nodes);
	}


	private void fetchFromSchemaNodeList(File wsdlDir, URL wsdlURL, List nodes) throws JaxenException {
		for (Object node : nodes) {
			Element el = (Element)node;
			String schema = el.attributeValue("schemaLocation");
			//FIXME: handle paths like ../schema.xsd and full URL locations http://myhost.net/schema.xsd
			logger.info("Making local copy of imported schema:"+schema+" for wsdl "+wsdlURL.toExternalForm());
			File localCopy=null;
			try {
				URL url = new URL(wsdlURL,schema);
				localCopy=new File(wsdlDir,schema);
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(localCopy)));
				
				String line;
				while ((line = reader.readLine()) != null) {
					writer.write(line+"\n");
				}
				writer.close();
				reader.close();
				
				//recurse to fetch any imbedded imports/includes
				Document schemaDoc = new SAXReader().read(localCopy);
				fetchImportedSchemasFromSchemaDoc(schemaDoc, wsdlDir, wsdlURL);
				
			} catch (MalformedURLException e) {
				logger.error("Error with URL for schema",e);
			}
			catch (IOException e) {
				logger.error("Error copying a local version of remote schema.",e);
				if (localCopy!=null && localCopy.exists()) localCopy.delete();
				
			} catch (DocumentException e) {
				logger.error("Error reading XML of local copy of XML schema document",e);
			}
		}
	}
}
