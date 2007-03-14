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
 * Filename           $RCSfile: WSDLListPanel.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-03-14 16:56:16 $
 *               by   $Author: sowen70 $
 * Created on 5 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.thinwire;

import java.util.List;

import org.apache.log4j.Logger;

import thinwire.ui.Application;
import thinwire.ui.Button;
import thinwire.ui.Component;
import thinwire.ui.Frame;
import thinwire.ui.Hyperlink;
import thinwire.ui.MessageBox;
import thinwire.ui.Panel;
import thinwire.ui.TextField;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.ui.layout.TableLayout;
import uk.org.mygrid.dataproxy.configuration.ProxyConfig;
import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;

public class WSDLListPanel extends Panel {
	
	private static Logger logger = Logger.getLogger(WSDLListPanel.class);
	
	public WSDLListPanel () {		
		TableLayout layout = new TableLayout();
		setLayout(layout);
		refresh();
	}
	
	public void refresh() {
		
		//setVisible(false);
		setScrollType(Panel.ScrollType.ALWAYS);
		TableLayout layout=(TableLayout)getLayout();
		layout.getColumns().clear();
		layout.getRows().clear();
		List<WSDLConfig> configs = ProxyConfigFactory.getInstance().getWSDLConfigs();
				
		for (int c=0;c<6;c++) {
			TableLayout.Column col = new TableLayout.Column(0);			
			layout.getColumns().add(col);			
		}
				
		for (int i=0;i<configs.size();i++) {
			layout.getRows().add(new TableLayout.Row(0));			
		}
				
		int i=0;
		for (TableLayout.Row row : layout.getRows()) {				
			final  WSDLConfig config = configs.get(i++);
			TextField tf = new TextField(config.getWSDLID());	
			row.set(0,tf);
			tf=new TextField(config.getName());
			row.set(1,tf);
			tf=new TextField(config.getAddress());
			row.set(2,tf);
//			FIXME: hardcoded deployment location
			Hyperlink link = new Hyperlink("WSDL","http://localhost:8080/data-proxy/viewwsdl?id="+config.getWSDLID());					
			row.set(3,link);
			Button button = new Button("Configure");
			row.set(4,button);
			
			button.addActionListener(Button.ACTION_CLICK,new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {					
					configureClicked(config.getWSDLID());
				}				
			});
			
			Button deleteButton = new Button("Delete");
			row.set(5,deleteButton);
			
			deleteButton.addActionListener(Button.ACTION_CLICK, new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					deleteClicked(config);
				}				
			});								
		}			
	}
	
	private void deleteClicked(WSDLConfig wsdlConfig) {
						
		//FIXME: clicking the 'X' is taken to mean Ok.
		int response=MessageBox.confirm(null,"Delete WSDL", "Are you sure you want to delete the WSDL defined with the name "+wsdlConfig.getName(),"Ok|Cancel");
		
		if (response==0) {		
			ProxyConfigFactory.getInstance().deleteWSDLConfig(wsdlConfig);
			try {
				ProxyConfigFactory.writeConfig();
			} catch (Exception e) {
				logger.error("Error writing proxy config",e);
			}
			refresh();
		}
	}
	
	private void configureClicked(String wsdlID) {
		Frame frame=Application.current().getFrame();
		frame.getChildren().clear();
		final Component comp = new ConfigureWSDLPanel(wsdlID);		
		comp.setBounds(0,0,Application.current().getFrame().getInnerWidth(), Application.current().getFrame().getInnerHeight());
		comp.setVisible(true);
		frame.getChildren().add(comp);
		
	}
	
}
