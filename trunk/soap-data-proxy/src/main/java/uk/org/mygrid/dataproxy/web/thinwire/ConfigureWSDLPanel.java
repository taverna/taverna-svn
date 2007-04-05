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
 * Filename           $RCSfile: ConfigureWSDLPanel.java,v $
 * Revision           $Revision: 1.14 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-05 13:34:16 $
 *               by   $Author: sowen70 $
 * Created on 6 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.thinwire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import thinwire.ui.Application;
import thinwire.ui.Button;
import thinwire.ui.Component;
import thinwire.ui.Frame;
import thinwire.ui.Label;
import thinwire.ui.Panel;
import thinwire.ui.Tree;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.wsdl.SchemaParser;
import uk.org.mygrid.dataproxy.wsdl.SchemaParsingException;
import uk.org.mygrid.dataproxy.wsdl.impl.AxisBasedSchemaParserImpl;
import uk.org.mygrid.dataproxy.xml.ElementDefinition;

public class ConfigureWSDLPanel extends Panel {
	
	private static Logger logger = Logger.getLogger(ConfigureWSDLPanel.class);

	private WSDLConfig config;
	private SchemaParser parser=null;
	private final Label status = new Label("");
	private final Tree tree = new Tree();	
	private final Button toggleProxy = new Button("Toggle");	
	private List<ElementDefinition> defsSelectedForProxy = new ArrayList<ElementDefinition>();
	
	public ConfigureWSDLPanel(String wsdlID) {		
		config=ProxyConfigFactory.getInstance().getWSDLConfigForID(wsdlID);
		defsSelectedForProxy.addAll(config.getElements());
		
		Label label = new Label(config.getName());		
		
		tree.setBounds(198, 58, 350, 450);
		
		final String wsdl = config.getAddress();
				
		try {
			status.setText("Starting to parse WSDL");			
			getParser().flush(wsdl);
			List<Element> operations=getParser().parseOperations(wsdl);
			
			for (Element el : operations) {
				logger.debug("Operation xml = "+el.asXML());
				addOperationToTree(el);
			}
			status.setText("Finished parsing WSDL, "+operations.size()+" operations found.");
		} catch (SchemaParsingException e){
			logger.error("Error parsing the WSDL "+config.getAddress(),e);
		}						
		
		status.setBounds(0,200,500, 30);
		
		label.setBounds(0, 0, 100, 30);
		getChildren().add(label);		
		
		Button back = new Button("Back");
		back.setBounds(0, 30, 100, 30);
		
		Button commit = new Button("Commit");
		commit.setBounds(0, 60, 100, 30);
		
		tree.addActionListener(Tree.ACTION_DOUBLE_CLICK, new ActionListener() {

			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent arg0) {
				Tree.Item treeItem = tree.getSelectedItem();
				expandTreeItem(treeItem);
			}

					
		});	
		
		tree.addActionListener(Tree.ACTION_CLICK, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (tree.getSelectedItem().getUserObject()!=null && tree.getSelectedItem().getUserObject() instanceof Element) {
					toggleProxy.setEnabled(true);
				}
				else {
					toggleProxy.setEnabled(false);
				}				
			}								
		});
				
		toggleProxy.setBounds(0,90, 100, 30);
		toggleProxy.setEnabled(false);
		
		getChildren().add(back);
		getChildren().add(commit);
		getChildren().add(status);
		getChildren().add(tree);		
		getChildren().add(toggleProxy);				
		
		back.addActionListener(Button.ACTION_CLICK, new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {				
				Frame frame=Application.current().getFrame();
				frame.getChildren().clear();
				final Component comp = new NewWSDLPanel();	
				comp.setBounds(0,0,Application.current().getFrame().getInnerWidth(), Application.current().getFrame().getInnerHeight());
				comp.setVisible(true);
				frame.getChildren().add(comp);
			}			
		});	
		
		commit.addActionListener(Button.ACTION_CLICK, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				commitClicked(config);
			}			
		});
	
		
		toggleProxy.addActionListener(Button.ACTION_CLICK, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				toggleClicked(tree.getSelectedItem());
			}			
		});		
	}
	
	@SuppressWarnings("unchecked")
	private void expandTreeItem(Tree.Item treeItem) {
		Element el = (Element)treeItem.getUserObject();
		
		if (el!=null && el.elements().size()==0) {		
			if (el==null) {
				logger.error("Unable to find element associated with "+treeItem.getText());					
			}
			else {
				try {
					el=getParser().expandType(config.getAddress(), el);
					logger.info("Expanded element XML = "+el.asXML());
				} catch (SchemaParsingException e) {
					logger.error("An error occurred expanding the type element:"+e);
				}
				if (el.elements().size()>0) {
					treeItem.getChildren().clear();
					for (Element child : (List<Element>)el.elements()) {
						addTypeElementToTreeNode(treeItem, child);
					}
				}
			}
		}
	}	
	
	private void commitClicked(WSDLConfig config) {
		List<ElementDefinition> elementDefs = config.getElements();
		elementDefs.clear();
		elementDefs.addAll(defsSelectedForProxy);
		try {
			ProxyConfigFactory.writeConfig();
		} catch (Exception e) {
			logger.error("Error writing config",e);
		}
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
	
	private void toggleClicked(Tree.Item selectedItem) {
		
		if (selectedItem.getText().startsWith("XXX: ")) {
			toggleOff(selectedItem);
		}
		else {
			toggleOn(selectedItem);
		}		
	}
	
	private void toggleOff(Tree.Item selectedItem) {
		selectedItem.setText(selectedItem.getText().replaceAll("XXX: ",""));
		Element typeElement = (Element)selectedItem.getUserObject();		
		ElementDefinition def = createElementDefFromXML(typeElement);
		defsSelectedForProxy.remove(def);
	}
	
	private void toggleOn(Tree.Item selectedItem) {
		selectedItem.setText("XXX: "+selectedItem.getText());
		
		Element typeElement = (Element)selectedItem.getUserObject();
		ElementDefinition def = createElementDefFromXML(typeElement);
		defsSelectedForProxy.add(def);
		if (logger.isDebugEnabled()) {
			Element parent = typeElement;
			while (parent.getParent()!=null) parent=parent.getParent();
			logger.debug("Type element toggled on: "+parent.asXML());
		}		
	}
	
	@SuppressWarnings("unchecked")
	private void addOperationToTree(Element operationElement) {
		String name = operationElement.elementTextTrim("name");
		Tree.Item treeItem = new Tree.Item(name);
		Tree.Item root = tree.getRootItem();
		root.getChildren().add(treeItem);
		
		Element elements = operationElement.element("elements");
		if (elements!=null) {
			for (Element element : (List<Element>)elements.elements("element")) {				
				addTypeElementToTreeNode(treeItem,(Element)element.elements().get(0));				
			}
		}		
	}
	
	private void addTypeElementToTreeNode(Tree.Item treeItem, Element element) {
		
		String name=element.attributeValue("name");
		if (name==null) name="";
		String type = element.getName();
		
		Tree.Item typeItem = new Tree.Item(name+":"+type);
		if (matchesSelectedElementDefs(element)) typeItem.setText("XXX: "+typeItem.getText());
		typeItem.setUserObject(element);		
		treeItem.getChildren().add(typeItem);		
	}
	
	private SchemaParser getParser() {
		if (parser==null) {
			parser=new AxisBasedSchemaParserImpl();
		}
		return parser;
	}
	
	private boolean matchesSelectedElementDefs(Element element) {
		ElementDefinition def = createElementDefFromXML(element); 
		
		for (ElementDefinition storedDef : defsSelectedForProxy) {
			if (storedDef.matches(def)) {
				return storedDef.getPath().equals(def.getPath());
			}
		}
		return false;
	}
	
	
}
