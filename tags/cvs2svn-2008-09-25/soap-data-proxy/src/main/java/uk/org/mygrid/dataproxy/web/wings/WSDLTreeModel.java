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
 * Filename           $RCSfile: WSDLTreeModel.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-18 16:09:52 $
 *               by   $Author: sowen70 $
 * Created on 3 Apr 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.wings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.wsdl.WSDLParser;
import uk.org.mygrid.dataproxy.wsdl.WSDLParsingException;
import uk.org.mygrid.dataproxy.wsdl.impl.AxisBasedSchemaParserImpl;
import uk.org.mygrid.dataproxy.xml.ElementDefinition;

@SuppressWarnings("serial")
public class WSDLTreeModel extends DefaultTreeModel{
	
	private static Logger logger = Logger.getLogger(WSDLTreeModel.class);
	private WSDLConfig config;
	private static WSDLParser parser;
	
	private List<ElementDefinition> selectedForProxy = new ArrayList<ElementDefinition>();
	
	public WSDLTreeModel(WSDLConfig config) {
		super(new DefaultMutableTreeNode("root"));
		this.config=config;				
	}
	
	public void populate() throws WSDLParsingException {
		logger.debug("populating tree model for WSDL: "+config.getName());		
		WSDLParser parser = getParser();
		selectedForProxy.addAll(config.getElements());
		
		List<Element> operations = parser.parseOperations(config.getAddress());
		
		for (Element operation : operations) {
			addOperation(operation);
		}				
	}
	
	public List<ElementDefinition> getSelectedForProxy() {
		return selectedForProxy;
	}
	
	@SuppressWarnings("unchecked")
	private void addOperation(Element operationElement) throws WSDLParsingException {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)getRoot();		
		DefaultMutableTreeNode operationNode = new OperationNode();
		operationNode.setUserObject(operationElement);		
		root.add(operationNode);
		List<String> parentTypes = new ArrayList<String>();
		
		Element elements = operationElement.element("elements");
		if (elements!=null) {
			for (Element element : (List<Element>)elements.elements("element")) {				
				addTypeElementToNode(operationNode,(Element)element.elements().get(0),parentTypes);				
			}
		}
	}
	
	private void addTypeElementToNode(DefaultMutableTreeNode parentNode, Element element,List<String>parentTypes) throws WSDLParsingException {				
		String type=element.getName();
		if (!parentTypes.contains(type) && parentTypes.size()<6) {
			parentTypes.add(type);
			TypeNode typeNode = new TypeNode();
			if (matchesSelectedElementDefs(element)) typeNode.setSelected(true);
			typeNode.setUserObject(element);
			parentNode.add(typeNode);
			if (element.elements().size()==0) {
				logger.info("Expanding type:"+type);
				expandTypeElement(typeNode,element,parentTypes);
			}
			parentTypes.remove(type);
		}		
	}
	
	@SuppressWarnings("unchecked")
	private void expandTypeElement(DefaultMutableTreeNode parentNode, Element typeElement,List<String> parentTypes) throws WSDLParsingException {
		getParser().expandType(config.getAddress(), typeElement);
		if (typeElement.elements().size()>0) {
			for (Element childElement : (List<Element>)typeElement.elements()) {
				addTypeElementToNode(parentNode, childElement,parentTypes);
			}
		}		
	}
	
	private WSDLParser getParser() {
		if (parser==null) {
			parser=new AxisBasedSchemaParserImpl();
		}
		return parser;
	}
	
	private ElementDefinition createElementDefFromXML(Element el) {
		String path=convertToPath(el);
		String operation=getOperationFromTypeElement(el);			
		logger.info("path="+path);						
		String name=determineExpectedElementName(el);
		ElementDefinition def = new ElementDefinition(name,el.getNamespaceURI(),path,operation);
		return def;
	}
	
	private String getOperationFromTypeElement(Element el) {
		Element parent = el;
		while (parent!=null && !parent.getName().equals("operation")) {
			parent=parent.getParent();
		}
		if (parent!=null) {
			return parent.element("name").getTextTrim();
		}
		else {
			return null;
		}
	}
	
	private String convertToPath(Element el) {
		String result="*/";
		List<Element>elements = new ArrayList<Element>();
		Element parent = el;
		while(parent != null && !parent.getName().equals("element")) {
			elements.add(parent);
			parent = parent.getParent();
		}		
		
		Collections.reverse(elements);
		
		for (Element element : elements) {
			System.out.println(element.asXML());									
			String name = determineExpectedElementName(element);
			result+=name+"/";
		}
			
		if (result.endsWith("/")) result = result.substring(0,result.length()-1);
		return result;
	}
	
	private String determineExpectedElementName(Element element) {
		String name;
		if ("elementname".equals(element.attributeValue("partType"))) {
			name=element.getName();					
		}
		else {
			if (element.attributeValue("name")!=null) {
				name=element.attributeValue("name");
			}
			else {
				name="*";
			}
		}
		return name;
	}
	
	private boolean matchesSelectedElementDefs(Element element) {
		ElementDefinition def = createElementDefFromXML(element); 
		
		for (ElementDefinition storedDef : selectedForProxy) {
			if (storedDef.matches(def)) {
				return storedDef.getPath().equals(def.getPath());
			}
		}
		return false;
	}
	
	public void toggleNode(TypeNode node) {
		Element el = (Element)node.getUserObject();			
		ElementDefinition def = createElementDefFromXML(el);
		if (node.isSelected()) {
			selectedForProxy.remove(def);
			node.setSelected(false);
		}
		else {
			selectedForProxy.add(def);
			node.setSelected(true);
		}
		
	}
}
