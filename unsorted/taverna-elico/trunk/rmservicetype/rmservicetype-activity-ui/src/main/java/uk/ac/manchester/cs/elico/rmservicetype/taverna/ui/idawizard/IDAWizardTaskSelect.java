package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.idawizard;

import ch.uzh.ifi.ddis.ida.api.Task;
import ch.uzh.ifi.ddis.ida.api.exception.IDAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.idawizard.components.AbstractWizardPanel;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
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
 * Date: Feb 24, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class IDAWizardTaskSelect extends AbstractWizardPanel {

    private static final Logger logger = LoggerFactory.getLogger(IDAWizardTaskSelect.class);

    public static final String ID = "ida.taskselect";

    private OntologyBrowser ontoBrowser;

    public IDAWizardTaskSelect() {
        super(ID, "Select Data Mining Task");
    }

    @Override
    protected void createUI(JComponent parent) {

        setInstructions("Please select a data mining Task");
        parent.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        ontoBrowser = new OntologyBrowser(false);
        ontoBrowser.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                           ontoBrowser.getLastSelectedPathComponent();
                getWizard().setNextFinishButtonEnabled(node.isLeaf());

            }
        });

        inputPanel.add(ontoBrowser);

        parent.add(inputPanel, BorderLayout.CENTER);

    }

    public void aboutToDisplayPanel() {
        super.aboutToDisplayPanel();
        getWizard().setNextFinishButtonEnabled(false);
        IDAWizard wizard = (IDAWizard) getWizard();
        try {
            logger.info("About to create Task tree");
            ontoBrowser.setRootNode(wizard.getIDAManager().getIDAInterface().getTasks());
            ontoBrowser.expand();

        } catch (IDAException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    @Override
    public void aboutToHidePanel() {
        super.aboutToHidePanel();    //To change body of overridden methods use File | Settings | File Templates.

        IDAWizard wizard = (IDAWizard) getWizard();

//        ArrayList<Task> ts = new ArrayList<Task>();

        Task ts = (Task) ontoBrowser.getSelectedNode();
        logger.info("Selected Task: " + ((Task) ts).getTaskName());
//        for (Object t : ontoBrowser.getSelectedNodes()) {
//            logger.info("Selected Task " + ((Task) t).getTaskName());
//            ts.add((Task) t);
//        }
        wizard.getWorkflowConfiguration().setTask(ts);
        String goalName = wizard.getWorkflowConfiguration().getMainGoal().getGoalName();
        logger.info("Assigning goal to task with goal id: " + goalName);


//        try {
//            wizard.getWorkflowConfiguration().getGFactory().addUseTask(goalName, ts);
//        } catch (IDAException e) {
//            // major error!
//        }

    }

    public Object getBackPanelDescriptor() {
        return IDAWizardGoalSelect.ID;
    }

    public Object getNextPanelDescriptor () {
        return IDAWizardDataSelect.ID;
    }

}
