package idaservicetype.idaservicetype.ui.idawizard.wizard;

import java.awt.BorderLayout;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.Buffer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

import org.apache.axis.AxisFault;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rapid_i.elico.DataTableResponse;
import com.rapid_i.elico.MetaDataServicePortBindingStub;
import com.rapid_i.elico.MetaDataService_ServiceLocator;

import ch.uzh.ifi.ddis.ida.api.GoalFactory;
import ch.uzh.ifi.ddis.ida.api.IDAInterface;
import ch.uzh.ifi.ddis.ida.api.MainGoal;
import ch.uzh.ifi.ddis.ida.api.Plan;
import ch.uzh.ifi.ddis.ida.api.Task;
import ch.uzh.ifi.ddis.ida.api.Tree;
import ch.uzh.ifi.ddis.ida.api.WeightedFeatureValue;
import ch.uzh.ifi.ddis.ida.api.exception.IDAException;
import ch.uzh.ifi.ddis.ida.core.fact.Fact;

import uk.ac.manchester.cs.elico.utilities.configuration.RapidAnalyticsPreferences;
import uk.ac.manchester.cs.elico.utilities.repositorybrowser.RapidAnalyticsRepositoryBrowser;

import idaservicetype.idaservicetype.ui.converter.IDAManager;
import idaservicetype.idaservicetype.ui.converter.IDAWorkflowConfiguration;
import idaservicetype.idaservicetype.ui.converter.InputIOObject;
import idaservicetype.idaservicetype.ui.converter.MyDataFlowGenerator;
import idaservicetype.idaservicetype.ui.converter.ProcessOWLMetadata;
import idaservicetype.idaservicetype.ui.idawizard.components.AbstractWizardPanel;
import idaservicetype.idaservicetype.ui.idawizard.components.WizardPanel;
import idaservicetype.idaservicetype.ui.idawizard.wizard.IDAWizardFetchPlans.ConvertPlansThread;
import idaservicetype.idaservicetype.ui.idawizard.wizard.IDAWizardFetchPlans.FetchMetaDataThread;
import idaservicetype.idaservicetype.ui.idawizard.wizard.IDAWizardFetchPlans.FetchPlansThread;
import idaservicetype.idaservicetype.ui.idawizard.wizard.IDAWizardGoalSelect.StartPlannerThread;
import idaservicetype.idaservicetype.ui.idawizard.wizard.OntologyBrowser.MyNode;

public class IDAWizardTemplate extends AbstractWizardPanel {
    	
    private static final Logger logger = LoggerFactory.getLogger(IDAWizardTemplate.class);
	
    public static final String ID = "ida.templateservice";
    
	JComponent parent;
	
    private OntologyBrowser ontoBrowser;
    
    private RapidAnalyticsRepositoryBrowser reposBrowser;

    private static RapidAnalyticsPreferences preferences;
    
    private InputDataTable inputTable;

    private JTextArea helpDescriptionLabel;
    
    private List<Plan> fetchedPlans;
    
    private Task selectedTask;
    
	private List<Fact> goalFacts;
    
	public IDAWizardTemplate() {
		super(ID, "IDA Wizard Template");
	}

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
                MyNode nodea = (MyNode) ontoBrowser.getLastSelectedPathComponent();
                helpDescriptionLabel.setText(nodea.getToolTipText());
                
                MainGoal goal = (MainGoal) ontoBrowser.getSelectedNode();
                logger.info("Selected MainGoal " + goal.getGoalName());
                
                IDAWizard wizard1 = (IDAWizard) getWizard();
                wizard1.getWorkflowConfiguration().setMainGoal(goal);
                wizard1.getWorkflowConfiguration().getGFactory().setMainGoal(goal);
                
                IDAWorkflowConfiguration config = wizard1.getWorkflowConfiguration();

                inputTable.getDataRequirements().addAll(config.getMainGoal().getDataRequirement());

            }
        });
                
        helpDescriptionLabel = new JTextArea();
        helpDescriptionLabel.setEditable(false);

        inputPanel.setSize(100, 100);
        inputPanel.add(ontoBrowser);
       
        parent.add(inputPanel, BorderLayout.NORTH);
        parent.add(helpDescriptionLabel, BorderLayout.PAGE_END);

        // end of goal select
        inputTable = new InputDataTable();

        reposBrowser = new RapidAnalyticsRepositoryBrowser() {
            @Override
            public void fileSelectedButtonPress() {
                super.fileSelectedButtonPress();
                inputTable.addItem(reposBrowser.getChosenRepositoryPath());
            }
        };


        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                           new JScrollPane (reposBrowser), new JScrollPane(inputTable));
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(800);
        parent.add(splitPane, BorderLayout.CENTER);
        
       
	}
	
    public void aboutToDisplayPanel() {
        super.aboutToDisplayPanel();

        //
        IDAWizard wizard1 = (IDAWizard) getWizard();
        IDAWorkflowConfiguration config1 = wizard1.getWorkflowConfiguration();

        System.out.println(" IDA PREFERENCES --> " + wizard1.getIdaConnectionPrefs().getRepositoryLocation());
        
        //
        getWizard().setNextFinishButtonEnabled(true);
      
        IDAWizard wizard = (IDAWizard) getWizard();

        logger.info("Starting IDA planner");
        
        // data select panel
        //IDAWizard wizard = (IDAWizard) getWizard();
        IDAWorkflowConfiguration config = wizard.getWorkflowConfiguration();

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

        StartPlannerThread thread = new StartPlannerThread(wizard, listener);
        thread.start();
        dlg.setVisible(true);
        
        reposBrowser.setPreferences(wizard1.getIdaConnectionPrefs());
        reposBrowser.initialiseTreeContents();
        
        //IDAWizard wizarda = (IDAWizard) getWizard();

        //Object go = new String("PredictiveModelling");
        //MainGoal goal = (MainGoal) go;
        
        //logger.info("Selected MainGoal " + goal.getGoalName());
        //wizarda.getWorkflowConfiguration().setMainGoal(goal);
        //wizarda.getWorkflowConfiguration().getGFactory().setMainGoal(goal);
        
        //inputTable.getDataRequirements().addAll(config1.getMainGoal().getDataRequirement());
         
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
                goalFacts = goalFactory.getFacts();
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
       
        System.out.println("	ABOUT TO HIDE PANEL");
        IDAWizard wizard1 = (IDAWizard) getWizard();
        IDAWorkflowConfiguration config1 = wizard1.getWorkflowConfiguration();

        inputTable.getDataRequirements().addAll(config1.getMainGoal().getDataRequirement());
        
        if (inputTable.getDataRequirements().isEmpty()) {
            getWizard().setNextFinishButtonEnabled(false);
        } else {
        	
        }
        
        IDAWizard wizard = (IDAWizard) getWizard();
        IDAWorkflowConfiguration config = wizard.getWorkflowConfiguration();
        
        for (InputIOObject re : inputTable.getSelectInputFiles()) {
            System.out.println(re.getFilePath() + " -> " + re.getDataRequirement().getRoleName());
        }

        config.setInputDataPath(inputTable.getSelectInputFiles());

        setTask();
        
        fetchMetaData(); 

        fetchPlans();
    
        
        // danger
        /*
        List<WeightedFeatureValue> weightedFeature = null;
        try {
        	weightedFeature = wizard.getIDAManager().getIDAInterface().getCaseIndex(selectedTask, goalFacts);
		} catch (IDAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println(" output " + fetchedPlans.get(0).toString());
        PlanProcessConverter2 conv = new PlanProcessConverter2(fetchedPlans.get(0), weightedFeature);
        com.rapidminer.Process pro = null;
        try {
			pro = conv.convert();
		} catch (OperatorCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConversionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println(" DANGER " + pro.toString());
        */
        //
        finishUp();

    }
    
    public void setTask() {
    	
    	 // set task
        IDAWizard wizard = (IDAWizard) getWizard();
        Tree<Task> tasks = null;
        try {
        	tasks = wizard.getIDAManager().getIDAInterface().getTasks();
		} catch (IDAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        System.out.println(" TASKS " + tasks.toString() + "  " + tasks.getChildren().get(0).getData().getTaskName());
    	//tasks.getChildren().get(0).getData().getTaskName()
        for (int i = 0 ; i < tasks.getNumberOfChildren() ; i++) {
        	
        	Task currentTask = tasks.getChildren().get(i).getData();
        	if (currentTask.getTaskName().equals(wizard.getPredefinedTaskName())) {
        		
                wizard.getWorkflowConfiguration().setTask(currentTask);
        		selectedTask = currentTask;
        	}
        }
    }
    
    public void finishUp() {
    	
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

         ConvertPlansThread thread = new ConvertPlansThread(wizard, fetchedPlans, listener);
         thread.start();
         dlg.setVisible(true);

         try {
             wizard.getIDAManager().shutdownPlanner();
         } catch (IDAException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
    	
    }
    
    public void fetchMetaData(){
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
    
    public void fetchPlans() {

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
            
            int number = 1;
            logger.info("Number of plans: " + number);

            try {
                java.util.List<Plan> plans = ida.getPlans(task, gFactory.getFacts(), number);
                //plansTable.addPlans(plans);
                fetchedPlans = plans;
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
    
    public class FetchMetaDataThread extends Thread {

        private ProgressThreadListener listener;

        private RapidAnalyticsPreferences connectionPrefs;

        private IDAWorkflowConfiguration workflowConfig;

        public FetchMetaDataThread (IDAWizard w, ProgressThreadListener listener) {
            connectionPrefs = w.getIdaConnectionPrefs();
            workflowConfig = w.getWorkflowConfiguration();
            this.listener = listener;
        }

        public void run() {

            listener.updateProgress(5);

            MetaDataService_ServiceLocator service = new MetaDataService_ServiceLocator();

            listener.updateProgress(15);

            try {
                MetaDataServicePortBindingStub stub = new MetaDataServicePortBindingStub(new URL(connectionPrefs.getMetaDataServiceLocation()), service);
                stub.setUsername(connectionPrefs.getUsername());
                stub.setPassword(String.valueOf(connectionPrefs.getPassword()));

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
            for (Plan p : fetchedPlans) {
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
                
                //PlanProcessConverter2 con = new PlanProcessConverter2(p, tempRectangles);
                
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
    
    
    public Object getNextPanelDescriptor () {
        return WizardPanel.FINISH;
    }
    
	public static void main(String[] args) {
		
		RapidAnalyticsPreferences prefs = new RapidAnalyticsPreferences();
		prefs.setRepositoryLocation("http://rpc295.cs.man.ac.uk:8081");
        prefs.setUsername("rishi");
        prefs.setPassword("rishipwd");
        prefs.setPathToTmpDir("/Users/Rishi/Desktop/e-LICO_Development/IDA_stuff/tmp/");
        prefs.setPathToFlora("/Users/Rishi/Desktop/e-LICO_Development/IDA_stuff/flora2/");

        preferences = prefs;
        
        IDAWizard wizard = new IDAWizard(new IDAWorkflowConfiguration(), prefs, new JFrame(), true);
        wizard.showModalDialog();
        
        
	}

}
