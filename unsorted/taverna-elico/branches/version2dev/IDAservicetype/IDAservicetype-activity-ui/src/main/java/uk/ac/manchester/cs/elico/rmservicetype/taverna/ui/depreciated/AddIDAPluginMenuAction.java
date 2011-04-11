package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.depreciated;

import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.views.graph.actions.DesignOnlyAction;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import uk.ac.manchester.cs.elico.converter.IDAWorkflowConfiguration;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidAnalyticsPreferences;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.config.RapidMinerPluginConfiguration;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.idawizard.IDAWizard;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URI;/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Simon Jupp<br>
 * Date: Mar 1, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class AddIDAPluginMenuAction extends DesignOnlyAction {

    private static final String ADD_IDA = "e-LICO IDA planner";

    private static Edits edits = EditsRegistry.getEdits();

    private static EditManager editManager = EditManager.getInstance();

    public AddIDAPluginMenuAction () {
        super();
        putValue(NAME, ADD_IDA);
        ImageIcon icon = new ImageIcon(getClass().getResource("/elico-small.gif"));
        putValue(SMALL_ICON, icon);
        putValue(SHORT_DESCRIPTION, "The e-LICO Intelligent Data-mining Assistant");
    }

    public void actionPerformed(ActionEvent actionEvent) {

        // current dataflow
        Dataflow currentDataflow = (Dataflow) ModelMap.getInstance().getModel(ModelMapConstants.CURRENT_DATAFLOW);

        RapidAnalyticsPreferences prefs = getPreferences();
//        prefs.setRepositoryLocation("http://rpc295.cs.man.ac.uk:8081");
//        prefs.setUsername("jupp");
//        prefs.setPassword("jupppwd");
//        prefs.setPathToFlora("/Applications/Unix/flora2");
//        prefs.setPathToTmpDir("/Users/simon/tmp/elico/flora2/");

        if (prefs != null) {
            IDAWizard wizard = new IDAWizard(new IDAWorkflowConfiguration(), prefs, new JFrame());
            wizard.setCurrentDataFlow(currentDataflow, edits, editManager);
//        wizard.setAuth();
            wizard.showModalDialog();
        }
        else {
            JOptionPane.showMessageDialog(new JFrame(),
                    new JLabel("<html>Please set the Rapid Analytics repository location <br> " +
                            " and flora location in the preferences panel</html>"));
            
        }

    }

    private RapidAnalyticsPreferences getPreferences() {

        RapidMinerPluginConfiguration config = RapidMinerPluginConfiguration.getInstance();
        String repos = config.getProperty(RapidMinerPluginConfiguration.RA_REPOSITORY_LOCATION);
        String pathToFlora = config.getProperty(RapidMinerPluginConfiguration.FL_LOCATION);
        String pathToTmpdir = config.getProperty(RapidMinerPluginConfiguration.FL_TEMPDIR);
        System.err.println("Got repository location: " + repos);
        if (repos.equals("") || pathToFlora.equals("") || pathToTmpdir.equals("")) {
            return null;
        }

        RapidAnalyticsPreferences pref = new RapidAnalyticsPreferences();
        pref.setRepositoryLocation(repos);
        pref.setPathToFlora(pathToFlora);
        pref.setPathToTmpDir(pathToTmpdir);

        CredentialManager credManager = null;
        try {
            credManager = CredentialManager.getInstance();
            UsernamePassword username_password = credManager.getUsernameAndPasswordForService(URI.create(repos), true, null);
            pref.setUsername(username_password.getUsername());
            pref.setPassword(username_password.getPasswordAsString());

        } catch (CMException e) {
            e.printStackTrace();
            return null;
        }


        return pref;

    }

}
