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
 * Filename           $RCSfile: NewWSDLPanel.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-03-20 16:56:10 $
 *               by   $Author: sowen70 $
 * Created on 6 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.thinwire;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jaxen.JaxenException;
import org.jaxen.dom4j.Dom4jXPath;

import thinwire.ui.Button;
import thinwire.ui.Component;
import thinwire.ui.Label;
import thinwire.ui.Panel;
import thinwire.ui.TextField;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.ui.layout.Layout;
import thinwire.ui.layout.TableLayout;
import thinwire.ui.style.Color;
import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.configuration.impl.NewWSDLConfig;

public class NewWSDLPanel extends Panel {

	private static Logger logger = Logger.getLogger(NewWSDLPanel.class);
	
	private final Label status = new Label();
	Button addButton = new Button("Add");
	private WSDLListPanel listPanel = new WSDLListPanel();
	
	public NewWSDLPanel() {
		
		Layout layout = new TableLayout(new double[][]{{0,0,0,0,0},{30,30,30,30,50,50,50,50}});		
		
		final TextField comp = new TextField("http://");		
		comp.setBounds(351, 195, 150, 25);
		comp.setVisible(true);		
		
		final TextField name = new TextField("");
		name.setBounds(351, 195, 150, 25);
		name.setVisible(true);
							
		status.setVisible(false);	
		status.getStyle().getFont().setSize(14);
				
		addButton.addActionListener(Button.ACTION_CLICK, new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (name.getText().length()<=0) {
					updateStatus("You must provide a name for the wsdl");
				}
				else {
					try {					
						status.setVisible(false);
						status.getStyle().getFont().setColor(Color.BLACK);
						addWSDL(comp.getText(), name.getText());
					} catch (MalformedURLException exception) {
						status.getStyle().getFont().setColor(Color.RED);
						updateStatus("Invalid URL: "+exception.getMessage());					
					} catch (Exception exception) {
						status.getStyle().getFont().setColor(Color.RED);
						updateStatus("An error occurred processing the WSDL: "+exception.getMessage());
					}
					addButton.setEnabled(true);
					listPanel.refresh();
				}
			}			
		});
		
		setLayout(layout);		
		
		List<Component>components = getChildren();
		
		components.add(name.setLimit("1,2,2,1"));
		components.add(comp.setLimit("1,1,2,1"));				
		
		components.add(new Label("Name:").setLimit("0,2,1,1"));
		components.add(new Label("WSDL:").setLimit("0,1,1,1"));
		
		components.add(addButton.setLimit("4,2,1,1"));		
		components.add(status.setLimit("0,3,5,1"));	
		components.add(listPanel.setLimit("1,4,3,3"));		
	}
	
	private void updateStatus(String statusText) {
		if (!status.isVisible()) status.setVisible(true);
		status.setText(statusText);
	}
	
	private void addWSDL(String wsdlLocation,String wsdlName) throws Exception {
		addButton.setEnabled(false);		
		updateStatus("Starting to process WSDL");
		updateStatus("Making local copy of WSDL");		
			
		
		String wsdlID=ProxyConfigFactory.getUniqueWSDLID();		
		
		NewWSDLConfig config = new NewWSDLConfig();
		config.setWSDLID(wsdlID);
		config.setEndpoint(fetchServiceEndpoint(wsdlLocation));
		config.setName(wsdlName);
		config.setWSDLFilename(wsdlName+".wsdl");
		config.setAddress(wsdlLocation);
		
		ProxyConfigFactory.getInstance().addWSDLConfig(config);
		
		updateStatus("Saving new configuration");
		
		ProxyConfigFactory.writeConfig();
		
		updateStatus("Processing the WSDL is complete. ID="+wsdlID);
		
		logger.info("New WSDL added with ID="+wsdlID);
	}
	
	private String fetchServiceEndpoint(String wsdlLocation) throws DocumentException, IOException, JaxenException {
		URL url = new URL(wsdlLocation);
		Document doc = new SAXReader().read(url.openStream());
		Dom4jXPath path = new Dom4jXPath("//wsdl:service/wsdl:port/soap:address");		
		path.addNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
		path.addNamespace("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
		Element el = (Element)path.selectSingleNode(doc);
		return el.attributeValue("location");
	}	
}
