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
 * Filename           $RCSfile: OpenWorkflowFromURLAction.java,v $
 * Revision           $Revision: 1.8 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-18 14:40:15 $
 *               by   $Author: sowen70 $
 * Created on 20 Nov 2006
 *****************************************************************/
package org.embl.ebi.escience.scuflui.actions;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.embl.ebi.escience.scuflui.TavernaIcons;

/**
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class OpenWorkflowFromURLAction extends OpenWorkflowFromFileAction {
        
        private Component parentComponent;
        
        public OpenWorkflowFromURLAction(Component parentComponent) {
                super(parentComponent);
        }
        
        protected void initialise() {
                putValue(SMALL_ICON, TavernaIcons.openurlIcon);
                putValue(NAME, "Open workflow location...");
                putValue(SHORT_DESCRIPTION, "Open a workflow from the web into a new worklflow");
        }
        
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
                Preferences prefs = Preferences.userNodeForPackage(OpenWorkflowFromURLAction.class);
                String currentUrl = prefs
                        .get("currentUrl", "http://");
                
                try {
                        String url = (String) JOptionPane.showInputDialog(parentComponent,
                                "Enter the URL of a workflow definition to load",
                                "Workflow URL", JOptionPane.QUESTION_MESSAGE, null, null,
                                currentUrl);
                        if (url != null) {
                                HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
                                connection.setRequestProperty("Accept", "text/xml");
                                
                                int code = connection.getResponseCode();
                                if (code==401) { //authentication required.
                                        PasswordInput input =  new PasswordInput((JFrame)parentComponent);
                                        input.setUrl(new URL(url));
                                        input.setSize(new Dimension(323,222));
                                        input.setLocationRelativeTo(parentComponent);
                                        input.setVisible(true);
                                        
                                        if (input.getPassword()!=null && input.getUsername()!=null) {
                                                connection = (HttpURLConnection) new URL(url).openConnection();
                                                String userPassword = input.getUsername()+":"+input.getPassword();
                                                String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
                                                connection.setRequestProperty("Authorization", "Basic " + encoding);
                                                connection.setRequestProperty("Accept", "text/xml");
                                                openFromURL(connection);
                                                prefs.put("currentUrl",url);
                                        }
                                } else {
                                        openFromURL(new URL(url).openConnection());
                                        prefs.put("currentUrl", url);
                                }
                        }
                } catch (Exception ex) {
                        JOptionPane.showMessageDialog(parentComponent,
                                "Problem opening workflow from web : \n" + ex.getMessage(),
                                "Error!", JOptionPane.ERROR_MESSAGE);
                }
        }
        
}
