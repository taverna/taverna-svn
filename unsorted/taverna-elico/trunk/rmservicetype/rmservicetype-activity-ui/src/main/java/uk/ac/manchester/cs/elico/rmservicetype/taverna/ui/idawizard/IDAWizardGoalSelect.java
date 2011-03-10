package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.idawizard;

import ch.uzh.ifi.ddis.ida.api.GoalFactory;
import ch.uzh.ifi.ddis.ida.api.MainGoal;
import ch.uzh.ifi.ddis.ida.api.Tree;
import ch.uzh.ifi.ddis.ida.api.exception.IDAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.elico.converter.IDAManager;
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
public class IDAWizardGoalSelect extends AbstractWizardPanel {

    private static final Logger logger = LoggerFactory.getLogger(IDAWizardGoalSelect.class);

    public static final String ID = "ida.goalselect";

    private OntologyBrowser ontoBrowser;

    public IDAWizardGoalSelect() {
        super(ID, "Select Data Mining Goal");
    }

    JComponent parent;
    @Override
    protected void createUI(JComponent parent) {
        this.parent = parent;
        setInstructions("Please select a data mining Goal");
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

        logger.info("Starting IDA planner");

        final ProgressDialog dlg = new ProgressDialog(this, "Starting planner");

        ProgressThreadListener listener = new ProgressThreadListener() {

            public void processComplete() {
                dlg.setVisible(false);
            }

            public void handle(IDAException idaException) {
                dlg.setVisible(false);
                JOptionPane.showMessageDialog(parent, "Error starting planner!\n\n" + idaException.getMessage());
                getWizard().dispose();
            }

            public void updateProgress(int i) {
                dlg.getProgressBar().setValue(i);
            }

            public void updateMessage(String s) {
                dlg.updateText(s);
            }
        };

        StartPlannerThread thread = new StartPlannerThread(wizard, listener);
        thread.start();
        dlg.setVisible(true);


    }


    public class StartPlannerThread extends Thread {

        private IDAWizard w;
        private IDAManager m;
        private ProgressThreadListener listener;

        public StartPlannerThread (IDAWizard w, ProgressThreadListener listener) {
            this.w = w;

            String pathToFlora = w.getIdaConnectionPrefs().getPathToFlora();
            String pathToTemp = w.getIdaConnectionPrefs().getPathToTmpDir();

            if ("".equals(pathToFlora) || "".equals(pathToTemp)) {
                logger.error("path to flora or tmp cannot be empty");
                fireStartPlannerError(new IDAException("path to flora or tmp cannot be empty"));

            }

            logger.debug("path to flora: " + pathToFlora + ", path to temp dir" + pathToTemp);

            this.m = new IDAManager(pathToFlora, pathToTemp);
            this.listener = listener;
        }

        public void run() {

            try {

                listener.updateProgress(5);
                listener.updateMessage("Starting the planner...");
                m.startPlanner();
                listener.updateProgress(30);

                listener.updateProgress(50);
                listener.updateMessage("Creating Goal specification...");

                GoalFactory goalFactory = m.getIDAInterface().createEmptyGoalSpecification();
                listener.updateProgress(60);

                w.getWorkflowConfiguration().setGFactory(goalFactory);
                listener.updateProgress(70);

                listener.updateMessage("Building the Goal tree...");
                Tree<MainGoal> mainGoals = w.getIDAManager().getIDAInterface().getMainGoals();
                listener.updateProgress(80);

                listener.updateMessage("Starting Wizard...");
                ontoBrowser.setRootNode(mainGoals);
                ontoBrowser.expand();
                listener.updateProgress(100);
                fireProcessComplete();
            } catch (IDAException e) {
                fireStartPlannerError(e);  //To change body of catch statement use File | Settings | File Templates.
            }


        }

        private void fireProcessComplete () {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    listener.processComplete();
                }
            });
        }
        private void fireStartPlannerError (final IDAException e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    listener.handle(e);
                }
            });
        }



    }

    @Override
    public void aboutToHidePanel() {
        super.aboutToHidePanel();    //To change body of overridden methods use File | Settings | File Templates.

        IDAWizard wizard = (IDAWizard) getWizard();

        MainGoal goal = (MainGoal) ontoBrowser.getSelectedNode();
        logger.info("Selected MainGoal " + goal.getGoalName());
        wizard.getWorkflowConfiguration().setMainGoal(goal);
        wizard.getWorkflowConfiguration().getGFactory().setMainGoal(goal);


    }

    public Object getNextPanelDescriptor () {
        return IDAWizardTaskSelect.ID;
    }

}
