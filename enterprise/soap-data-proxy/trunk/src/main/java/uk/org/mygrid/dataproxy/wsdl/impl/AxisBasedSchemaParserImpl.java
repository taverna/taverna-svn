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
 * Filename           $RCSfile: AxisBasedSchemaParserImpl.java,v $
 * Revision           $Revision: 1.9 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-18 16:09:53 $
 *               by   $Author: sowen70 $
 * Created on 6 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.wsdl.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis.wsdl.gen.NoopFactory;
import org.apache.axis.wsdl.symbolTable.DefinedType;
import org.apache.axis.wsdl.symbolTable.ElementDecl;
import org.apache.axis.wsdl.symbolTable.SymbolTable;
import org.apache.axis.wsdl.symbolTable.TypeEntry;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.xml.sax.SAXException;

import uk.org.mygrid.dataproxy.wsdl.WSDLParser;
import uk.org.mygrid.dataproxy.wsdl.WSDLParsingException;

/**
 * An implementation of WSDLParser based around Axis 1, using SymbolTable to process the wsdl and
 * provide the operations and details about the types.
 * 
 * @author Stuart Owen
 */

public class AxisBasedSchemaParserImpl implements WSDLParser {

	private static Logger logger = Logger
			.getLogger(AxisBasedSchemaParserImpl.class);

	private static Map<String, SymbolTable> tableMap = Collections
			.synchronizedMap(new HashMap<String, SymbolTable>());	

	@SuppressWarnings("unchecked")
	public Element expandType(String wsdlUrl, Element type)
			throws WSDLParsingException {

		if (type.elements().size()>0) {
			throw new WSDLParsingException("Type {"+type.getNamespaceURI()+"}"+type.getQualifiedName()+" is already expanded");
		}
		
		SymbolTable table = getSymbolTable(wsdlUrl);
		QName qname = new QName(type.getName(), type.getNamespace());
		TypeEntry typeEntry = null;
		
		typeEntry = table.getElement(convertQName(qname));
		if (typeEntry == null) {
			typeEntry = table.getType(convertQName(qname));
		}
		if (typeEntry != null) {
			if (!typeEntry.isBaseType()) {

				if (typeEntry.getRefType() != null) {
					if (typeEntry instanceof DefinedType) {
						type.addElement(convertQName(typeEntry.getRefType()
										.getQName()));
					} else {
						if (typeEntry.getRefType().getContainedElements()==null) {
							//FIXME: getContainedElements is unexpectedly null							
						}
						else {
							populateWithContainedElements(type, typeEntry.getRefType()
									.getContainedElements());
						}
					}
				} else {
					if (typeEntry.getContainedElements() != null) {
						Vector<ElementDecl> containedElements = (Vector<ElementDecl>) typeEntry
								.getContainedElements();
						populateWithContainedElements(type, containedElements);
					} else {
						type.addElement(convertQName(typeEntry.getComponentType()));
					}
				}
			}
		}

		else {
			logger.warn("No partType defined for element: " + type.asXML());
		}

		return type;
	}

	private void populateWithContainedElements(Element type,
			Vector<ElementDecl> containedElements) {		
		for (ElementDecl elementEntry : containedElements) {
			Element addedElement = type.addElement(new QName(elementEntry
					.getType().getQName().getLocalPart(),new Namespace("",elementEntry.getQName().getNamespaceURI())));
			
			if (logger.isDebugEnabled()) {
				logger.debug("Element qname ={"+elementEntry.getQName().getNamespaceURI()+"}"+elementEntry.getQName().getLocalPart());
				logger.debug("Element type qname ={"+elementEntry.getType().getQName().getNamespaceURI()+"}"+elementEntry.getType().getQName().getLocalPart());
			}
			
			// work around for bug
			// http://issues.apache.org/jira/browse/AXIS-2105
			// is to parse the QName and take after the last '>' as the name.
			// getName always returns null.
			int x = elementEntry.getQName().getLocalPart().lastIndexOf(">");
			if (x > -1) {				
				String name = elementEntry.getQName().getLocalPart().substring(
						x + 1);
				addedElement.addAttribute("name", name);
				if (logger.isDebugEnabled()) logger.debug("Extracted name from "+elementEntry.getQName().getLocalPart()+" as " + name);
			}			
		}		
	}

	private SymbolTable getSymbolTable(String wsdlUrl)
			throws WSDLParsingException {
		SymbolTable symbolTable = tableMap.get(wsdlUrl);
		if (symbolTable == null) {
			try {
				symbolTable = populateSymbolTable(wsdlUrl);
				tableMap.put(wsdlUrl, symbolTable);
			} catch (Exception e) {
				logger.error("Error processing the wsdl:" + wsdlUrl, e);
				throw new WSDLParsingException(
						"Error processing wsdl schema:" + e.getMessage(), e);
			}
		}
		return symbolTable;
	}

	public void flush(String wsdlUrl) {
		tableMap.remove(wsdlUrl);
	}

	private SymbolTable populateSymbolTable(String wsdlUrl) throws IOException,
			WSDLException, SAXException, ParserConfigurationException {

		SymbolTable symbolTable = new SymbolTable(new NoopFactory()
				.getBaseTypeMapping(), true, false, false);
		symbolTable.populate(wsdlUrl);
		return symbolTable;
	}

	public List<Element> parseOperations(String wsdlUrl)
			throws WSDLParsingException {
		List<Element> result = new ArrayList<Element>();

		SymbolTable symbolTable = getSymbolTable(wsdlUrl);
		Definition def = symbolTable.getDefinition();
		for (Object o : def.getBindings().values()) {
			if (o instanceof Binding) {
				List operations = ((Binding) o).getPortType().getOperations();
				for (Object op : operations) {
					if (op instanceof Operation) {
						Operation operation = (Operation) op;
						Document doc = DocumentFactory.getInstance()
								.createDocument();
						Element operationElement = doc.addElement("operation");
						operationElement.addElement("name").setText(
								operation.getName());

						Element elements = operationElement
								.addElement("elements");
						for (Object partObj : operation.getOutput()
								.getMessage().getParts().values()) {
							if (partObj instanceof Part) {
								Part part = (Part) partObj;
								Element element = elements
										.addElement("element");
								if (part.getTypeName() != null) {
									Element added = element
											.addElement(convertQName(part
													.getTypeName()));
									added.addAttribute("partType", "typename");
									added.addAttribute("name", part.getName());
								} else if (part.getElementName() != null) {
									Element added = element
											.addElement(convertQName(part
													.getElementName()));
									added.addAttribute("partType",
											"elementname");
									added.addAttribute("name", part.getName());
								} else {
									logger
											.warn("No elementName or typeName for the part:"
													+ part);
								}
							}
						}
						result.add(doc.getRootElement());
					}
				}
			}
		}
		return result;
	}

	private QName convertQName(javax.xml.namespace.QName qname) {
		QName result = new QName(qname.getLocalPart(), new Namespace(qname
				.getPrefix(), qname.getNamespaceURI()));
		return result;
	}

	private javax.xml.namespace.QName convertQName(QName qname) {
		javax.xml.namespace.QName result = new javax.xml.namespace.QName(qname
				.getNamespaceURI(), qname.getName());
		return result;
	}
}
