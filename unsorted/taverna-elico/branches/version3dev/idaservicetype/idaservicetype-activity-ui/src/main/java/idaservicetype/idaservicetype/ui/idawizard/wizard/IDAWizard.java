package idaservicetype.idaservicetype.ui.idawizard.wizard;

import ch.uzh.ifi.ddis.ida.api.exception.IDAException;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import idaservicetype.idaservicetype.ui.converter.IDAManager;
import idaservicetype.idaservicetype.ui.converter.IDAWorkflowConfiguration;
import idaservicetype.idaservicetype.ui.idawizard.components.Wizard;
import uk.ac.manchester.cs.elico.utilities.configuration.RapidAnalyticsPreferences;

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
    
    private Dataflow finalDataflow;

    private Edits edits;

    private EditManager editsManager;
    
    private boolean isTemplate = false;
    
    private String predefinedTaskName;
    
    private RapidAnalyticsPreferences idaConnectionPrefs;
    
    public EditManager getEditsManager() {
        return editsManager;
    }

    public RapidAnalyticsPreferences getIdaConnectionPrefs() {
        return idaConnectionPrefs;
    }
    
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

    public IDAWizard(IDAWorkflowConfiguration config, RapidAnalyticsPreferences prefs, Frame owner, boolean template) {

        super(owner);
        idaConnectionPrefs = prefs;

        logger.info("Initialising IDA wizard");

        registerWizardPanel(IDAWizardTemplate.ID, new IDAWizardTemplate());
        
        //registerWizardPanel(IDAWizardGoalSelect.ID, new IDAWizardGoalSelect());
        //registerWizardPanel(IDAWizardTaskSelect.ID, new IDAWizardTaskSelect());
        //registerWizardPanel(IDAWizardDataSelect.ID, new IDAWizardDataSelect());
        //registerWizardPanel(IDAWizardFetchPlans.ID, new IDAWizardFetchPlans());

        String pathToFlora = idaConnectionPrefs.getPathToFlora();
        String pathToTemp = idaConnectionPrefs.getPathToTmpDir();

        this.config = config;
        this.manager = new IDAManager(pathToFlora, pathToTemp);


        setCurrentPanel(IDAWizardTemplate.ID);
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
    
    public void setWorkflowConfiguration(IDAWorkflowConfiguration conf) {
    	this.config = conf;
    }
    
    public IDAManager getIDAManager() {
        return manager;
    }

    public static void main(String[] args) {

        RapidAnalyticsPreferences prefs = new RapidAnalyticsPreferences();
        prefs.setRepositoryLocation("http://rpc295.cs.man.ac.uk:8081");
        prefs.setUsername("rishi");
        prefs.setPassword("rishipwd");
        prefs.setPathToTmpDir("/Users/Rishi/Desktop/e-LICO_Development/IDA_stuff/tmp/");
        prefs.setPathToFlora("/Users/Rishi/Desktop/e-LICO_Development/IDA_stuff/flora2/");

        IDAWizard wizard = new IDAWizard(new IDAWorkflowConfiguration(), prefs, new JFrame());
        wizard.setAuth();
        wizard.showModalDialog();
    }

    private void setAuth() {
        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication ("rishi", "rishipwd".toCharArray());
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

	public void setFinalDataflow(Dataflow finalDataflow) {
		this.finalDataflow = finalDataflow;
	}

	public Dataflow getFinalDataflow() {
		return finalDataflow;
	}

	public void setTemplate(boolean isTemplate) {
		this.isTemplate = isTemplate;
	}

	public boolean isTemplate() {
		return isTemplate;
	}

	public String getPredefinedTaskName() {
		return predefinedTaskName;
	}

	public void setPredefinedTaskName(String predefinedTaskName) {
		this.predefinedTaskName = predefinedTaskName;
	}
}
