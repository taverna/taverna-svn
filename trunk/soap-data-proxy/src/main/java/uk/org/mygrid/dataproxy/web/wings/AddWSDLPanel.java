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
 * Filename           $RCSfile: AddWSDLPanel.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-05 13:47:37 $
 *               by   $Author: sowen70 $
 * Created on 4 Apr 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.wings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jaxen.JaxenException;
import org.jaxen.dom4j.Dom4jXPath;
import org.wings.SButton;
import org.wings.SForm;
import org.wings.SGridLayout;
import org.wings.SLabel;
import org.wings.SOptionPane;
import org.wings.SPanel;
import org.wings.STextField;

import uk.org.mygrid.dataproxy.configuration.ProxyConfig;
import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.configuration.impl.NewWSDLConfig;

@SuppressWarnings("serial")
public class AddWSDLPanel extends SPanel {

	private static Logger logger = Logger.getLogger(AddWSDLPanel.class);
	
	private STextField nameField = new STextField();
	private STextField locationField = new STextField();
	private WSDLTable wsdlTable;
	
	public AddWSDLPanel(WSDLTable wsdlTable) {
		this.wsdlTable=wsdlTable;
		SForm form = new SForm(new SGridLayout(2));
		form.add(new SLabel("Name:"));
		form.add(nameField);
		form.add(new SLabel("Address:"));
		form.add(locationField);
		form.add(new SLabel());
		
		SButton button = new SButton("Add");
		form.add(button);
		
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (nameField.getText().length()<=0) {
					SOptionPane.showMessageDialog(AddWSDLPanel.this, "You must provide a name","Name required");
				}
				else if (locationField.getText().length()<=0) {
					SOptionPane.showMessageDialog(AddWSDLPanel.this, "You must provide a location","Location required");
				}
				else {
					addWSDL();
				}
			}			
		});
		
		add(form);
	}
	
	private void addWSDL() {
		String name=nameField.getText();
		String location=locationField.getText();
		logger.info("Adding new WSDL for location:"+location+", with name:"+name);
		try {			
			NewWSDLConfig config = new NewWSDLConfig();
			config.setWSDLID(ProxyConfigFactory.getUniqueWSDLID());
			config.setEndpoint(fetchServiceEndpoint(location));
			config.setName(name);
			config.setAddress(location);
			
			ProxyConfigFactory.getInstance().addWSDLConfig(config);
			ProxyConfigFactory.writeConfig();
			wsdlTable.update();
		}
		catch(Exception e) {
			logger.error("Error adding new WSDL",e);
			//TODO: report back error
		}
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
