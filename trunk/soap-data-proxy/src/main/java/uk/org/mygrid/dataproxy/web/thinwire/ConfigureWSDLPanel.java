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
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-03-06 15:43:54 $
 *               by   $Author: sowen70 $
 * Created on 6 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.thinwire;

import thinwire.ui.Application;
import thinwire.ui.Button;
import thinwire.ui.Component;
import thinwire.ui.Frame;
import thinwire.ui.Label;
import thinwire.ui.Panel;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;

public class ConfigureWSDLPanel extends Panel {

	private WSDLConfig config;
	
	public ConfigureWSDLPanel(String wsdlID) {		
		config=ProxyConfigFactory.getInstance().getWSDLConfigForID(wsdlID);
		Label label = new Label(config.getName());
		label.setBounds(0, 0, 100, 30);
		getChildren().add(label);		
		
		Button back = new Button("Back");
		back.setBounds(0, 30, 100, 30);
		
		getChildren().add(back);
		
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
	}
	
}
