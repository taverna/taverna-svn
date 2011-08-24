package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.idawizard;

import ch.uzh.ifi.ddis.ida.api.exception.IDAException;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.elico.converter.IDAManager;
import uk.ac.manchester.cs.elico.converter.IDAWorkflowConfiguration;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidAnalyticsPreferences;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.idawizard.components.Wizard;

import javax.swing.*;
import java.awt.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

/*
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
 * Date: Feb 23, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class IDAWizard extends Wizard {

    private static final Logger logger = LoggerFactory.getLogger(IDAWizard.class);

    private IDAWorkflowConfiguration config;

    private IDAManager manager;

    private Dataflow currentDF;

    private Edits edits;

    private EditManager editsManager;

    public EditManager getEditsManager() {
        return editsManager;
    }


    public RapidAnalyticsPreferences getIdaConnectionPrefs() {
        return idaConnectionPrefs;
    }

    private RapidAnalyticsPreferences idaConnectionPrefs;

    public IDAWizard(IDAWorkflowConfiguration config, RapidAnalyticsPreferences prefs, Frame owner) {

        super(owner);
        idaConnectionPrefs = prefs;

        logger.info("Initialising IDA wizard");

        registerWizardPanel(IDAWizardGoalSelect.ID, new IDAWizardGoalSelect());
        registerWizardPanel(IDAWizardTaskSelect.ID, new IDAWizardTaskSelect());
        registerWizardPanel(IDAWizardDataSelect.ID, new IDAWizardDataSelect());
        registerWizardPanel(IDAWizardFetchPlans.ID, new IDAWizardFetchPlans());


        String pathToFlora = idaConnectionPrefs.getPathToFlora();
        String pathToTemp = idaConnectionPrefs.getPathToTmpDir();

        this.config = config;
        this.manager = new IDAManager(pathToFlora, pathToTemp);


        setCurrentPanel(IDAWizardGoalSelect.ID);
    }


    public void shutdownPlanner () {
        if (manager != null) {
            try {
                manager.shutdownPlanner();
            } catch (IDAException e) {
                logger.error("Error shutting down planner: ");
                e.printStackTrace();
            }
        }
    }

    public void setIDAManager (IDAManager man) {
        this.manager = man;
    }

    public IDAWorkflowConfiguration getWorkflowConfiguration () {
        return config;
    }
    public IDAManager getIDAManager() {
        return manager;
    }

    public static void main(String[] args) {
//        IDAConnectionPreferences prefs = new IDAConnectionPreferences("http://rpc295.cs.man.ac.uk:8081", "jupp", "jupppwd" );

        RapidAnalyticsPreferences prefs = new RapidAnalyticsPreferences();
        prefs.setRepositoryLocation("http://rpc295.cs.man.ac.uk:8081");
        prefs.setUsername("jupp");
        prefs.setPassword("jupppwd");
        prefs.setPathToTmpDir("/Users/simon/tmp/elico/flora2/");
        prefs.setPathToFlora("/Applications/Unix/flora2");


        IDAWizard wizard = new IDAWizard(new IDAWorkflowConfiguration(), prefs, new JFrame());
        wizard.setAuth();
         wizard.showModalDialog();
    }

    private void setAuth() {
        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication ("jupp", "jupppwd".toCharArray());
            }
        };

        Authenticator.setDefault(auth);
    }

    public Dataflow getCurrentDF() {
        return currentDF;
    }

    public Edits getEdits() {
        return edits;
    }

    public void setCurrentDataFlow(Dataflow currentDataflow, Edits edits, EditManager editManager) {
        this.editsManager = editManager;
        this.currentDF = currentDataflow;
        this.edits = edits;

    }
}
