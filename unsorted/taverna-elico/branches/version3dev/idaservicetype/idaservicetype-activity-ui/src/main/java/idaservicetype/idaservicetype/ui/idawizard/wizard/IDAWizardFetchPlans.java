package idaservicetype.idaservicetype.ui.idawizard.wizard;

import biz.source_code.base64Coder.Base64Coder;
import ch.uzh.ifi.ddis.ida.api.*;
import ch.uzh.ifi.ddis.ida.api.exception.IDAException;
import com.rapid_i.elico.DataTableResponse;
import com.rapid_i.elico.MetaDataServicePortBindingStub;
import com.rapid_i.elico.MetaDataService_ServiceLocator;
import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.impl.EditManagerImpl;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.*;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.utils.Tools;
import org.apache.axis.AxisFault;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.elico.utilities.configuration.RapidAnalyticsPreferences;
import uk.ac.manchester.cs.elico.utilities.configuration.RapidMinerPluginConfiguration;

import idaservicetype.idaservicetype.ui.converter.IDAWorkflowConfiguration;
import idaservicetype.idaservicetype.ui.converter.InputIOObject;
import idaservicetype.idaservicetype.ui.converter.MyDataFlowGenerator;
import idaservicetype.idaservicetype.ui.converter.ProcessOWLMetadata;
import idaservicetype.idaservicetype.ui.idawizard.components.AbstractWizardPanel;
import idaservicetype.idaservicetype.ui.idawizard.components.WizardPanel;

import javax.swing.*;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
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
    
    JComponent parent;

    public IDAWizardFetchPlans() {
        super(ID, "Fetch workflow plans");
    }

    @Override
    protected void createUI(JComponent parent) {

        this.parent = parent;
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

        parent.add(inputPanel, BorderLayout.NORTH);
        parent.add(new JScrollPane(plansTable), BorderLayout.CENTER);

    }


    public void fetchPlans() {

        plansTable.removeAll();

        logger.info("Fetching plans");

        final ProgressDialog dlg = new ProgressDialog(this, "Fetching plans");
        dlg.getProgressBar().setIndeterminate(true);
        ProgressThreadListener listener = new ProgressThreadListener() {

            public void processComplete() {
                dlg.setVisible(false);
            }

            public void handle(IDAException idaException) {
                dlg.setVisible(false);
                JOptionPane.showMessageDialog(parent, "Error fetching plans\n\n" + idaException.getMessage());
                getWizard().dispose();
            }

            public void handle(OWLOntologyCreationException e) {
                dlg.setVisible(false);
            }

            public void updateProgress(int i) {
                dlg.getProgressBar().setValue(i);
            }

            public void updateMessage(String s) {
                dlg.updateText(s);
            }
        };

        IDAWizard wizard = (IDAWizard) getWizard();
        
        FetchPlansThread thread = new FetchPlansThread(wizard, listener);
        thread.start();
        dlg.setVisible(true);
    }

    @Override
    public void aboutToDisplayPanel() {
        super.aboutToDisplayPanel();    //To change body of overridden methods use File | Settings | File Templates.

        // fetch the meta data based on the input data
        IDAWizard wizard = (IDAWizard) getWizard();

        logger.info("Fetching meta data");

        final ProgressDialog dlg = new ProgressDialog(this, "Fetching meta-data");

        ProgressThreadListener listener = new ProgressThreadListener() {

            public void processComplete() {
                dlg.setVisible(false);
            }

            public void handle(IDAException idaException) {
                dlg.setVisible(false);
            }

            public void handle(OWLOntologyCreationException e) {
                dlg.setVisible(false);
                JOptionPane.showMessageDialog(parent, "Error fetching data\n\n" + e.getMessage());
                getWizard().dispose();
            }

            public void updateProgress(int i) {
                dlg.getProgressBar().setValue(i);
            }

            public void updateMessage(String s) {
                dlg.updateText(s);
            }
        };

        FetchMetaDataThread thread = new FetchMetaDataThread(wizard, listener);
        thread.start();
        dlg.setVisible(true);

    }

    @Override
    public void aboutToHidePanel() {
        super.aboutToHidePanel();

        IDAWizard wizard = (IDAWizard) getWizard();

        logger.info("Converting plans to T2Flow");

        final ProgressDialog dlg = new ProgressDialog(this, "Converting plans to T2Flow");
        dlg.getProgressBar().setIndeterminate(true);

        ProgressThreadListener listener = new ProgressThreadListener() {

            public void processComplete() {
                dlg.setVisible(false);
            }

            public void handle(IDAException idaException) {
                dlg.setVisible(false);
            }

            public void handle(OWLOntologyCreationException e) {
                dlg.setVisible(false);
                JOptionPane.showMessageDialog(parent, "Error fetching data\n\n" + e.getMessage());
                getWizard().dispose();
            }

            public void updateProgress(int i) {
                dlg.getProgressBar().setValue(i);
            }

            public void updateMessage(String s) {
                dlg.updateText(s);
            }
        };

        ConvertPlansThread thread = new ConvertPlansThread(wizard, plansTable.getSelectedPlans(), listener);
        thread.start();
        dlg.setVisible(true);

        try {
            wizard.getIDAManager().shutdownPlanner();
        } catch (IDAException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public class FetchMetaDataThread extends Thread {

        private ProgressThreadListener listener;

        private RapidAnalyticsPreferences connectionPrefs;

        private IDAWorkflowConfiguration workflowConfig;

		private RapidAnalyticsPreferences preferences;

		private UsernamePassword username_password;
		
		private IDAWizard wizard;

        public FetchMetaDataThread (IDAWizard w, ProgressThreadListener listener) {
            connectionPrefs = w.getIdaConnectionPrefs();
            wizard = w;
            sortPreferences();
            workflowConfig = w.getWorkflowConfiguration();
            this.listener = listener;
        }

        public void sortPreferences() {
            
    		preferences = getPreferences();
            if (preferences != null) {
                CredentialManager credManager;
                try {
                    credManager = CredentialManager.getInstance();
                    username_password = credManager.getUsernameAndPasswordForService(URI.create(preferences.getBrowserServiceLocation()), true, null);

                    wizard.setUsername(username_password.getUsername());
                    wizard.setPassword(username_password.getPasswordAsString());
                } catch (CMException e) {
                    e.printStackTrace();

                }
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
            System.err.println("Got repository location: " + repos);
            if (repos.equals("")) {
                return null;
            }

            RapidAnalyticsPreferences pref = new RapidAnalyticsPreferences();
            pref.setRepositoryLocation(repos);
            return pref;

        }
        
        public void run() {

            listener.updateProgress(5);

            MetaDataService_ServiceLocator service = new MetaDataService_ServiceLocator();

            listener.updateProgress(15);

            try {
                MetaDataServicePortBindingStub stub = new MetaDataServicePortBindingStub(new URL(connectionPrefs.getMetaDataServiceLocation()), service);
                stub.setUsername(wizard.getUsername());
                stub.setPassword(wizard.getPassword());

                GoalFactory goalFactory = workflowConfig.getGFactory();
                // add data requirements
                String mainGoalID = goalFactory.setMainGoal(workflowConfig.getMainGoal());
                goalFactory.addUseTask(mainGoalID, workflowConfig.getTask());

                listener.updateProgress(20);

                int prog = 80/workflowConfig.getInputIOObjects().size();
                for (InputIOObject ioOb : workflowConfig.getInputIOObjects()) {

                    System.err.println("Getting meta data for: " + ioOb.getFilePath() + ":" + ioOb.getDataRequirement().getRoleName());
                    DataTableResponse response = stub.getOWLIndividualsFromRepository(ioOb.getBaseURL(), ioOb.getDataRequirement().getID(), ioOb.getFilePath());
                    logger.error("Error message: " + response.getErrorMessage());
                    String owl = response.getOwlXML();
                    logger.debug("Meta-data response as OWL:" + owl);

                    try {
                        ProcessOWLMetadata process = new ProcessOWLMetadata(owl, goalFactory);

                        goalFactory = process.getGoalFactory();
                        //OWLClass clss = process.getIOObjectClass();
                        //OWLIndividual ind = process.getIOObjectInd(clss);
                        String ioUri =  ioOb.getBaseURL() + "#" + ioOb.getDataRequirement().getID();


                        logger.info("Adding io object input data requirement: " + ioUri);


                        goalFactory.addDataRequirement(mainGoalID, ioOb.getDataRequirement(), URI.create(ioUri));
                        workflowConfig.setGFactory(goalFactory);
                        prog = 20+prog;
                        listener.updateProgress(prog);

                    } catch (OWLOntologyCreationException e) {
                        fireFetchDataError(e);
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (IDAException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                listener.updateProgress(100);
                fireProcessComplete();

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

        private void fireProcessComplete () {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    listener.processComplete();
                }
            });
        }
        private void fireFetchDataError (final OWLOntologyCreationException e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    listener.handle(e);
                }
            });
        }
    }


    public class FetchPlansThread extends Thread {

        private ProgressThreadListener listener;

        private IDAWizard wizard;
        private IDAWorkflowConfiguration workflowConfig;


        public FetchPlansThread (IDAWizard w, ProgressThreadListener listener) {
            wizard = w;
            workflowConfig = w.getWorkflowConfiguration();
            this.listener = listener;
        }

        public void run() {

            IDAInterface ida = wizard.getIDAManager().getIDAInterface();
            Task task = workflowConfig.getTask();
            GoalFactory gFactory = workflowConfig.getGFactory();
            
            SpinnerNumberModel numberModel = (SpinnerNumberModel) numberOfPlans.getModel();
            int number = numberModel.getNumber().intValue();
            logger.info("Number of plans: " + number);

            try {
                java.util.List<Plan> plans = ida.getPlans(task, gFactory.getFacts(), number);
                plansTable.addPlans(plans);
                listener.updateProgress(90);
                logger.info("Finished getting plans");
            } catch (IDAException e) {
                fireStartPlannerError(e);
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            fireProcessComplete();

        }

        private void fireStartPlannerError (final IDAException e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    listener.handle(e);
                }
            });
        }

        private void fireProcessComplete () {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    listener.processComplete();
                }
            });
        }
        private void fireFetchDataError (final OWLOntologyCreationException e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    listener.handle(e);
                }
            });
        }
    }

    public class ConvertPlansThread extends Thread {

        private ProgressThreadListener listener;

        private IDAWizard wizard;
        private IDAWorkflowConfiguration workflowConfig;
        private java.util.List<Plan> plans;

        public ConvertPlansThread (IDAWizard w, java.util.List<Plan> p, ProgressThreadListener listener) {
            this.wizard = w;
            this.workflowConfig = w.getWorkflowConfiguration();
            this.plans = p;
            this.listener = listener;
        }

        public void run() {

            // if there are selected plans
            for (Plan p : plansTable.getSelectedPlans()) {
                logger.info("Generating plan: " + p.toString());
                System.out.println(" GENERATED PLAN " + p.toString());
                //Dataflow current = wizard.getCurrentDF();
                final Dataflow current = FileManager.getInstance().getCurrentDataflow();
               
                //Edits edits = wizard.getEdits();
                Edits edits = new EditsImpl();

                MyDataFlowGenerator dfg = new MyDataFlowGenerator();

                Dataflow newdf = dfg.getDataFlow(p);
                this.wizard.setFinalDataflow(newdf);
                System.out.println(" SERIALISED " + this.wizard.getIDAManager().getIDAInterface().writePlan(dfg.getPlan()).toString());
                IDAWorkflowConfiguration idawfconfig = this.wizard.getWorkflowConfiguration();
              
                Buffer buf = this.wizard.getIDAManager().getIDAInterface().writePlan(dfg.getPlan());
                
                this.wizard.setWorkflowConfiguration(idawfconfig);
                
				//ByteBuffer bufa = (ByteBuffer) this.wizard.getIDAManager().getIDAInterface().writePlan(dfg.getPlan());
				//System.out.println(" bufa " + bufa.toString());
				//Object obj = bufa.array();
				//System.out.println(" plan toString " + p.toString() + " " + obj.toString());
				
				//StringBuffer sBuf = new StringBuffer();
		
				// Create a StringBuffer so that we can convert the bytes to a String
				//StringBuffer response = new StringBuffer();

				//bufa.flip();

				// Create a CharSet that knows how to encode and decode standard text (UTF-8)
				//Charset charset = Charset.forName("UTF-8");

				// Decode the buffer to a String using the CharSet and append it to our buffer
				//response.append( charset.decode( bufa ) );

				// Output the response
				//System.out.println( "Data read from client " + response.toString() );


                /*
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
	                	
	                	EditManager man = new EditManagerImpl();
	                	
	                	man.doDataflowEdit(current, new CompoundEdit(addEdits));
	                } catch (EditException e) {
	                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
	                }
                */
            }


            fireProcessComplete();



        }

        private void fireStartPlannerError (final IDAException e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    listener.handle(e);
                }
            });
        }

        private void fireProcessComplete () {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    listener.processComplete();
                }
            });
        }
        private void fireFetchDataError (final OWLOntologyCreationException e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    listener.handle(e);
                }
            });
        }
    }
    
    public Object getBackPanelDescriptor() {
        return IDAWizardDataSelect.ID;
    }

    public Object getNextPanelDescriptor () {
        return WizardPanel.FINISH;
    }
}
