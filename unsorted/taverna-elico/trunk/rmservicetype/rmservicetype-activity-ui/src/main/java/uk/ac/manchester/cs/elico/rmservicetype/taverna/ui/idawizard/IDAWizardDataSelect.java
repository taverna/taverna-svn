package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.idawizard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.elico.converter.IDAWorkflowConfiguration;
import uk.ac.manchester.cs.elico.converter.InputIOObject;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.idawizard.components.AbstractWizardPanel;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.view.RapidAnalyticsRepositoryBrowser;

import javax.swing.*;
import java.awt.*;/*
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
 * Date: Feb 25, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class IDAWizardDataSelect extends AbstractWizardPanel {

    private static final Logger logger = LoggerFactory.getLogger(IDAWizardDataSelect.class);

    public static final String ID = "ida.dataselect";


    private RapidAnalyticsRepositoryBrowser reposBrowser;

    private InputDataTable inputTable;

    public IDAWizardDataSelect() {
        super(ID, "Select Data Input");
    }

    @Override
    protected void createUI(JComponent parent) {

        setInstructions("Please select your input data");
        parent.setLayout(new BorderLayout());

        inputTable = new InputDataTable();

        reposBrowser = new RapidAnalyticsRepositoryBrowser() {
            @Override
            public void fileSelectedButtonPress() {
                super.fileSelectedButtonPress();
                inputTable.addItem(reposBrowser.getChosenRepositoryPath());
            }
        };
        parent.add(new JScrollPane (reposBrowser), BorderLayout.NORTH);
        parent.add(new JScrollPane(inputTable), BorderLayout.CENTER);

//        parent.add(inputPanel);
    }

    public void aboutToDisplayPanel() {
        super.aboutToDisplayPanel();

        IDAWizard wizard = (IDAWizard) getWizard();
        IDAWorkflowConfiguration config = wizard.getWorkflowConfiguration();

        reposBrowser.setPreferences(wizard.getIdaConnectionPrefs());
        reposBrowser.initialiseTreeContents();


        inputTable.getDataRequirements().addAll(config.getMainGoal().getDataRequirement());
    }

    @Override
    public void aboutToHidePanel() {
        super.aboutToHidePanel();    //To change body of overridden methods use File | Settings | File Templates.
        if (inputTable.getDataRequirements().isEmpty()) {
            getWizard().setNextFinishButtonEnabled(false);
        }
        
        IDAWizard wizard = (IDAWizard) getWizard();
        IDAWorkflowConfiguration config = wizard.getWorkflowConfiguration();

        for (InputIOObject re : inputTable.getSelectInputFiles()) {
            System.out.println(re.getFilePath() + " -> " + re.getDataRequirement().getRoleName());
        }

        config.setInputDataPath(inputTable.getSelectInputFiles());

    }

    public Object getBackPanelDescriptor() {
        return IDAWizardGoalSelect.ID;
    }

    public Object getNextPanelDescriptor () {
        return IDAWizardFetchPlans.ID;
    }
}