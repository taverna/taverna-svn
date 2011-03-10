package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.idawizard;

import ch.uzh.ifi.ddis.ida.api.GoalFactory;
import ch.uzh.ifi.ddis.ida.api.IDAInterface;
import ch.uzh.ifi.ddis.ida.api.Plan;
import ch.uzh.ifi.ddis.ida.api.Task;
import ch.uzh.ifi.ddis.ida.api.exception.IDAException;
import com.rapid_i.elico.DataTableResponse;
import com.rapid_i.elico.MetaDataServicePortBindingStub;
import com.rapid_i.elico.MetaDataService_ServiceLocator;
import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.workflowmodel.*;
import net.sf.taverna.t2.workflowmodel.utils.Tools;
import org.apache.axis.AxisFault;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.elico.converter.DataFlowGenerator;
import uk.ac.manchester.cs.elico.converter.IDAWorkflowConfiguration;
import uk.ac.manchester.cs.elico.converter.InputIOObject;
import uk.ac.manchester.cs.elico.converter.ProcessOWLMetadata;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidAnalyticsPreferences;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.idawizard.components.AbstractWizardPanel;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.idawizard.components.WizardPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;/*
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
 * Date: Feb 28, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class IDAWizardFetchPlans extends AbstractWizardPanel{

    private static final Logger logger = LoggerFactory.getLogger(IDAWizardFetchPlans.class);

    public static final String ID = "ida.fetchplans";

    public static final String NWFName = "IDA_Plan";

    private JSpinner numberOfPlans;

    private PlansTable plansTable;

    public IDAWizardFetchPlans() {
        super(ID, "Fetch workflow plans");
    }

    @Override
    protected void createUI(JComponent parent) {

        setInstructions("Choose the number of plans you want to fetch, then select plans to be imported into Taverna");
        parent.setLayout(new BorderLayout());

        Box inputPanel = new Box(BoxLayout.X_AXIS);
        JLabel label = new JLabel("Number of plans ");

        SpinnerNumberModel startSpinnerModel = new SpinnerNumberModel();
        startSpinnerModel.setMinimum(1);
        startSpinnerModel.setValue(10);
        numberOfPlans = new JSpinner(startSpinnerModel);
        numberOfPlans.setMinimumSize(new Dimension(10, 4));
        JButton button = new JButton(new AbstractAction("Fetch Plans") {

            public void actionPerformed(ActionEvent actionEvent) {
                fetchPlans();
            }
        }) ;

        inputPanel.add(label);
        inputPanel.add(Box.createHorizontalStrut(4));
        inputPanel.add(numberOfPlans);
        inputPanel.add(Box.createHorizontalStrut(8));
        inputPanel.add(button);
        inputPanel.add(Box.createHorizontalStrut(12));

        plansTable = new PlansTable();

        parent.add(new JScrollPane(plansTable), BorderLayout.CENTER);
        parent.add(inputPanel, BorderLayout.SOUTH);

    }


    public void fetchPlans() {

        plansTable.removeAll();
        
        IDAWizard wizard = (IDAWizard) getWizard();
        IDAWorkflowConfiguration workflowConfig = wizard.getWorkflowConfiguration();

        IDAInterface ida = wizard.getIDAManager().getIDAInterface();
        Task task = workflowConfig.getTask();
        GoalFactory gFactory = workflowConfig.getGFactory();

        SpinnerNumberModel numberModel = (SpinnerNumberModel) numberOfPlans.getModel();
        int number = numberModel.getNumber().intValue();
        logger.info("Number of plans: " + number);
        try {
            java.util.List<Plan> plans = ida.getPlans(task, gFactory.getFacts(), number);
            plansTable.addPlans(plans);
            logger.info("Finished getting plans");
        } catch (IDAException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    @Override
    public void aboutToDisplayPanel() {
        super.aboutToDisplayPanel();    //To change body of overridden methods use File | Settings | File Templates.

        // fetch the meta data based on the input data

        IDAWizard wizard = (IDAWizard) getWizard();
        RapidAnalyticsPreferences connectionPrefs = wizard.getIdaConnectionPrefs();
        IDAWorkflowConfiguration workflowConfig = wizard.getWorkflowConfiguration();

        MetaDataService_ServiceLocator service = new MetaDataService_ServiceLocator();

        try {
            MetaDataServicePortBindingStub stub = new MetaDataServicePortBindingStub(new URL(connectionPrefs.getMetaDataServiceLocation()), service);
            stub.setUsername(connectionPrefs.getUsername());
            stub.setPassword(String.valueOf(connectionPrefs.getPassword()));

            GoalFactory goalFactory = workflowConfig.getGFactory();
            // add data requirements
            String mainGoalID = goalFactory.setMainGoal(workflowConfig.getMainGoal());
            goalFactory.addUseTask(mainGoalID, workflowConfig.getTask());

            
            for (InputIOObject ioOb : workflowConfig.getInputIOObjects()) {
                logger.info("Getting meta data for: " + ioOb.getFilePath());
                DataTableResponse response = stub.getOWLIndividualsFromRepository(ioOb.getBaseURL(), ioOb.getBasePrefix(), ioOb.getFilePath());
                logger.error("Error message: " + response.getErrorMessage());
                String owl = response.getOwlXML();
                logger.debug("Meta-data response as OWL:" + owl);

                try {
                    ProcessOWLMetadata process = new ProcessOWLMetadata(owl, goalFactory);
                    OWLClass clss = process.getIOObjectClass();
                    OWLIndividual ind = process.getIOObjectInd(clss);

                    logger.info("Adding io object input data requirement: " + ind.getURI().toString());


                    goalFactory.addDataRequirement(mainGoalID, ioOb.getDataRequirement(), ind.getURI());

                } catch (OWLOntologyCreationException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IDAException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        } catch (AxisFault axisFault) {
            // todo handle no connection error
            axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IDAException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    @Override
    public void aboutToHidePanel() {
        super.aboutToHidePanel();

        IDAWizard wizard = (IDAWizard) getWizard();
        // if there are selected plans
        for (Plan p : plansTable.getSelectedPlans()) {
            logger.info("Generating plan: " + p.toString());

            Dataflow current = wizard.getCurrentDF();
            Edits edits = wizard.getEdits();

            DataFlowGenerator dfg = new DataFlowGenerator();

            Dataflow newdf = dfg.getDataFlow(p);

            java.util.List<Edit<?>> addEdits = new ArrayList<Edit<?>>();

            DataflowActivity nestedDataflowActivity = new DataflowActivity();
            String name = Tools.uniqueProcessorName(NWFName, current);
            Processor nestedProc = edits.createProcessor(name);
            addEdits.add(edits.getAddProcessorEdit(current, nestedProc));
            addEdits.add(edits.getDefaultDispatchStackEdit(nestedProc));
            addEdits.add(edits.getAddActivityEdit(nestedProc,
                    nestedDataflowActivity));
            addEdits.add(edits.getMapProcessorPortsForActivityEdit(nestedProc));

            addEdits.add(edits.getConfigureActivityEdit(nestedDataflowActivity,
                    newdf));



            try {
                wizard.getEditsManager().doDataflowEdit(current, new CompoundEdit(addEdits));
            } catch (EditException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }

        try {
            wizard.getIDAManager().shutdownPlanner();
        } catch (IDAException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    public Object getBackPanelDescriptor() {
        return IDAWizardDataSelect.ID;
    }

    public Object getNextPanelDescriptor () {
        return WizardPanel.FINISH;
    }
}
