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
 * Filename           $RCSfile: AboutBox.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-01-12 14:00:27 $
 *               by   $Author: stain $
 * Created on 14 Dec 2006
 *****************************************************************/
package org.embl.ebi.escience.scuflui.workbench;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.raven.spi.ProfileFactory;
import net.sf.taverna.tools.Bootstrap;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.shared.UIUtils;

@SuppressWarnings("serial")
public class AboutBox extends JDialog {
	
	private static Logger logger = Logger.getLogger(AboutBox.class);

	private JPanel jContentPane = null;
	private JPanel detailsPane = null;
	private JLabel splashscreen = null;
	
	private Color BACKGROUND_COLOUR = new Color(0xEEEEEE);

	/**
	 * This method initializes 
	 * 
	 */
	public AboutBox() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setTitle("About Taverna 1.5");
        this.setName("About Box");
        this.setContentPane(getJContentPane());        
        setResizable(false);
        pack();
        			
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());			
			jContentPane.add(getSplashscreen(), BorderLayout.NORTH);
			jContentPane.add(new JPanel(),BorderLayout.CENTER);
			jContentPane.add(getDetailsPane(), BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDetailsPane() {
		if (detailsPane == null) {
			detailsPane = new JPanel();
			detailsPane.setLayout(new GridLayout(5,1));	
			detailsPane.setBackground(BACKGROUND_COLOUR);			
			detailsPane.add(new JLabel(getVersion(),JLabel.CENTER));															
			detailsPane.add(new JLabel("Project Website: ",JLabel.CENTER));			
			detailsPane.add(getProjectSiteLabel());	
			detailsPane.add(new JLabel("Mailing Lists: ",JLabel.CENTER));
			detailsPane.add(getUserGroupsLabel());
			detailsPane.setBorder(BorderFactory.createEtchedBorder());			
		}
		return detailsPane;
	}
	
	private JTextField getProjectSiteLabel() {		
		final JTextField result=new JTextField("http://taverna.sourceforge.net");		
		result.setFocusable(true);
		
		result.setForeground(Color.BLUE);
		result.setBackground(BACKGROUND_COLOUR);	
		result.setBorder(null);
		result.setEditable(false);	
		result.setHorizontalAlignment(JTextField.CENTER);		
		
		result.addMouseListener(new MouseAdapter() {				
			public void mouseClicked(MouseEvent e) {
				launchURL(result.getText());
			}				
		});		
		return result;
	}
	
	private JTextField getUserGroupsLabel() {
		JTextField result=getProjectSiteLabel(); //cheat! - get the project label and change the text :)
		result.setText("http://taverna.sourceforge.net/index.php?doc=lists.html");
		return result;
	}
	
	private void launchURL(String address) {
		UIUtils.launchBrowser(address);
	}
	
	private String getVersion() {
		String version="Unknown";
		Profile prof = ProfileFactory.getInstance().getProfile();
		if (prof != null) {
			if (prof.getVersion() != null) {
				version = prof.getVersion();
			}			
		}
		return "Version " + version;
	}

	/**
	 * This method initializes splashscreen	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JLabel getSplashscreen() {
		if (splashscreen == null) {			
			try {
				URL splashScreenUrl=Bootstrap.getSplashScreenURL();
				ImageIcon image=new ImageIcon(splashScreenUrl);
				splashscreen = new JLabel(image);				
			}
			catch(MalformedURLException e) {
				logger.error("Malformed URL for splashscreen",e);
			}
		}
		return splashscreen;
	}
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		new AboutBox().show();
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
