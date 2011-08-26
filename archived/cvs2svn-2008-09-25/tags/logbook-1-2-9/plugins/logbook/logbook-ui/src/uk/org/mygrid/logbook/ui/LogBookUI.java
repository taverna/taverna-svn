package uk.org.mygrid.logbook.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceException;
import uk.org.mygrid.datalineage.DataLineageVisualiser;
import uk.org.mygrid.logbook.ui.util.ProcessRun;
import uk.org.mygrid.logbook.ui.util.Utilities;
import uk.org.mygrid.logbook.ui.util.Workflow;
import uk.org.mygrid.logbook.ui.util.WorkflowRun;
import uk.org.mygrid.provenance.LogBookException;
import uk.org.mygrid.provenance.dataservice.DataService;
import uk.org.mygrid.provenance.dataservice.DataServiceException;
import uk.org.mygrid.provenance.util.LogBookConfigurationNotFoundException;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

/**
 * Root UI for Taverna's LogBook.
 * 
 * @author Matthew Gamble
 * @author dturi
 * @version $Id: LogBookUI.java,v 1.1 2007-12-14 12:48:38 stain Exp $
 */
public class LogBookUI extends JPanel implements UIComponentSPI {

	public static enum UIType {
		MEMORY, DB;

		public static UIType getDefault() {
			return MEMORY;
		}

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String TITLE = "Log Book";

	public static Logger logger = Logger.getLogger(LogBookUI.class);

	public static void main(String[] args) {
		String ravenProfile = ClassLoader
				.getSystemResource("raven-profile.xml").toString();
		System.setProperty("raven.profile", ravenProfile);
		System.setProperty("raven.eclipse", "true");
		try {
			File tmpDir = File.createTempFile("taverna", "raven");
			Repository tempRepository = LocalRepository.getRepository(tmpDir);
			TavernaSPIRegistry.setRepository(tempRepository);

			LogBookUI eib;
			String type = null;
			if (args.length > 0) {
				String prefix = "type=";
				for (String arg : args) {
					if (arg.startsWith(prefix)) {
						type = arg.substring(prefix.length());
						break;
					}
				}
			}
			if (type != null)
				eib = new LogBookUI(UIType.valueOf(type));
			else
				eib = new LogBookUI();
			JFrame frame = new JFrame();
			frame.getContentPane().add(eib);
			frame.setTitle(TITLE);

			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					System.exit(0);
				}
			});
			frame.setSize(new Dimension(500, 900));
			frame.setVisible(true);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String getName() {
		return TITLE;
	}

	public javax.swing.ImageIcon getIcon() {
		return TavernaIcons.windowDiagram;
	}

	private ScuflModel scuflModel = new ScuflModel();

	private LogBookUIModel logBookUIModel;

	private JTextField filterText;

	private JComboBox filterList;

	private JScrollPane workflowscrollPane;

	private WorkflowRunsTreeTableModel workflowRunsTreeTableModel;

	private ProcessRunsPane processRunsPane;

	private WorkflowRunsTreeTable workflowRunsTreeTable;

	private JTabbedPane workflowRunsPane;

	private JPanel workflowRunsPanel;

	private JToolBar processRunsToolBarPanel;

	private JToolBar workflowRunsToolBarPanel;

	private ReloadAction reloadAction;

	private RerunAction rerunAction;

	private DeleteAction deleteAction;
	
	private RdfExportAction rdfExportAction;
	
	private DataLineageAction dataLineageAction;

	private Properties configuration;

	private UIType type;

	public LogBookUI() {
		this(UIType.getDefault());
	}

	public LogBookUI(UIType type) {
		super();
		this.type = type;
		try {
			this.configuration = ProvenanceConfigurator.getConfiguration();
			buildBrowser();
		} catch (LogBookConfigurationNotFoundException e) {
			Object[] options = { "Ok" };
			JOptionPane.showOptionDialog(null,
					"Please set the two MySQL databases for LogBook.",
					"LogBook settings missing", JOptionPane.OK_OPTION,
					JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		} catch (MetadataServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ReloadAction getReloadAction() {
		return reloadAction;
	}

	public DeleteAction getDeleteAction() {
		return deleteAction;
	}

	public RerunAction getRerunAction() {
		return rerunAction;
	}

	public DataLineageAction getDataLineageAction() {
		return dataLineageAction;
	}

	public RdfExportAction getRdfExportAction() {
		return rdfExportAction;
	}

	public void buildBrowser() throws MetadataServiceException, DataServiceException {
		setPreferredSize(new Dimension(500, 900));
		setLayout(new BorderLayout());
		Cursor hourglass = new Cursor(Cursor.WAIT_CURSOR);
		setCursor(hourglass);

		switch (type) {
		case DB:
			logBookUIModel = new LogBookUIRemoteModel(scuflModel, configuration);
			break;
		default:
			logBookUIModel = new LogBookUIMemoryModel(scuflModel, configuration);
		}
		createWorkflowRunsPane();
		createProcessRunsPane();

		this.setMinimumSize(new Dimension(500, 700));
		validate();
		Cursor normal = new Cursor(Cursor.DEFAULT_CURSOR);
		setCursor(normal);
	}

	private void createWorkflowRunsPane() {
		workflowRunsToolBarPanel = new JToolBar();
		workflowRunsToolBarPanel.setFloatable(false);
		workflowRunsToolBarPanel.setRollover(true);
		workflowRunsToolBarPanel.setMaximumSize(new Dimension(2000, 30));
		workflowRunsToolBarPanel.setBorderPainted(true);

		workflowRunsToolBarPanel.add(new JButton(new RefreshAction()));
		workflowRunsToolBarPanel.addSeparator();

		// workflowToolBarPanel.add(new JLabel("Search:"));

		// workflowToolBarPanel.add(FilterList);
		workflowRunsPanel = new JPanel();
		workflowRunsPanel.setLayout(new BorderLayout());
		workflowRunsPanel.setBorder(BorderFactory.createEtchedBorder());
		workflowRunsPanel.setMaximumSize(new Dimension(99999, 150));

		DefaultMutableTreeNode workflowsTreeNode = new DefaultMutableTreeNode(
				"Your Workflows");
		workflowRunsTreeTableModel = new WorkflowRunsTreeTableModel(
				workflowsTreeNode, logBookUIModel.getWorkflowObjects());

		createFilterText();

		workflowRunsToolBarPanel.add(filterText);
		workflowRunsToolBarPanel.add(new FilterAction());
		workflowRunsToolBarPanel.addSeparator();

		workflowRunsPanel.add(workflowRunsToolBarPanel, BorderLayout.NORTH);

		workflowRunsTreeTable = new WorkflowRunsTreeTable(
				workflowRunsTreeTableModel, logBookUIModel);
		workflowRunsTreeTable.setMaximumSize(new Dimension(500, 110));
		workflowRunsTreeTable.getTree().setCellRenderer(
				new WorkflowRunsTreeTableRenderer());
		ToolTipManager.sharedInstance().registerComponent(
				workflowRunsTreeTable.getTree());
		workflowRunsTreeTable.getColumnModel().getColumn(0).setMinWidth(300);
		// workflowInstanceTreeTable.setCellEditor();

		workflowscrollPane = new JScrollPane(workflowRunsTreeTable);

		workflowscrollPane.setPreferredSize(new Dimension(9999, 190));
		workflowscrollPane.getViewport().setBackground(java.awt.Color.WHITE);
		workflowRunsPanel.setMinimumSize(new Dimension(200, 150));
		workflowRunsPanel.add(workflowscrollPane, BorderLayout.CENTER);

		workflowRunsPane = new JTabbedPane();
		workflowRunsPane.add("Workflows", workflowRunsPanel);
		// workflowTabs.add("Labels",labelsPane);
		workflowRunsPane.setMaximumSize(new Dimension(9999, 300));
		add(workflowRunsPane, BorderLayout.NORTH);

		// filterPanel = new JPanel();

		// JLabel title = new JLabel("Title");
		// JLabel lsid = new JLabel("LSID");
		// JLabel startDate = new JLabel("Start Date:");
		// JLabel endDate = new JLabel("End Date:");
		// JLabel author = new JLabel ("Authour");
		// JTextArea authorText = new JTextArea();
		// JTextArea startDateText = new JTextArea();
		// JTextArea endDateText = new JTextArea();

		workflowRunsTreeTable
				.addMouseListener(createWorkflowRunsTreeTableMouseListener());
	}

	private MouseAdapter createWorkflowRunsTreeTableMouseListener() {
		return new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				int row = workflowRunsTreeTable.getSelectedRow();
				if (row == 0) { // root
					reloadAction.setEnabled(false);
					disableProcessRunActions();
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					return;
				}
				reloadAction.setEnabled(true);
				Cursor hourGlass = new Cursor(Cursor.WAIT_CURSOR);
				setCursor(hourGlass);
				processRunsPane.clearIntermediateResults();
				TreePath path = workflowRunsTreeTable.getTree()
						.getSelectionPath();
				if (path == null) {
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					return;
				}
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path
						.getLastPathComponent();
				Object userObject = selectedNode.getUserObject();
				if (userObject instanceof WorkflowRun
						&& !(userObject instanceof Workflow)) {
					enableProcessRunActions();
					if (workflowRunsPane.getTabCount() > 1)
						workflowRunsPane.remove(1);

					WorkflowRun workflowRun = (WorkflowRun) userObject;
					List<ProcessRun> processes = logBookUIModel
							.getProcessesForWorkflowRun(workflowRun);
					processRunsPane.setProcessData(workflowRun, processes);
					processRunsPane.removeResults();
					Map<String, DataThing> outputs = logBookUIModel
							.getWorkflowOutputs(workflowRun);
					processRunsPane.addResults(outputs);
					processRunsPane.removeInputs();
					Map<String, DataThing> inputs = logBookUIModel
							.getWorkflowInputs(workflowRun);
					if (!inputs.isEmpty())
						processRunsPane.addInputs(inputs);
					processRunsPane.updateWorkflowModel(workflowRun);

				} else {
					disableProcessRunActions();
					processRunsPane.removeInputs();
					processRunsPane
							.updateWorkflowModel(getWorkflow(selectedNode));
				}

				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

		};
	}

	private void createProcessRunsPane() {
		processRunsToolBarPanel = new JToolBar();
		processRunsToolBarPanel.setFloatable(false);
		processRunsToolBarPanel.setRollover(true);
		processRunsToolBarPanel.setMaximumSize(new Dimension(2000, 30));
		processRunsToolBarPanel.setBorderPainted(true);

		reloadAction = new ReloadAction();
		processRunsToolBarPanel.add(new JButton(reloadAction));
		processRunsToolBarPanel.addSeparator();

		rerunAction = new RerunAction();
		processRunsToolBarPanel.add(new JButton(rerunAction));
		processRunsToolBarPanel.addSeparator();

		deleteAction = new DeleteAction();
		processRunsToolBarPanel.add(new JButton(deleteAction));
		processRunsToolBarPanel.addSeparator();
		
		rdfExportAction = new RdfExportAction();
		processRunsToolBarPanel.add(new JButton(rdfExportAction));
		processRunsToolBarPanel.addSeparator();
		
		dataLineageAction = new DataLineageAction();
		processRunsToolBarPanel.add(new JButton(dataLineageAction));
		processRunsToolBarPanel.addSeparator();

		processRunsPane = new ProcessRunsPane(logBookUIModel);
		processRunsPane.setMinimumSize(new Dimension(100, 500));

		JPanel analysisPanel = new JPanel();
		analysisPanel.setLayout(new BorderLayout());
		analysisPanel.setBorder(BorderFactory.createEtchedBorder());
		analysisPanel.setMaximumSize(new Dimension(99999, 150));

		add(analysisPanel, BorderLayout.CENTER);

		analysisPanel.add(processRunsToolBarPanel, BorderLayout.NORTH);
		analysisPanel.add(processRunsPane, BorderLayout.CENTER);
	}

	private void createFilterText() {
		String[] ListItems = { "Date From", "Date To", "Title", "Author" };
		filterList = new JComboBox(ListItems);
		filterList.setMaximumSize(new Dimension(100, 20));
		filterText = new JTextField();
		filterText.setMaximumSize(new Dimension(90, 20));
		filterText.setMinimumSize(new Dimension(100, 20));
		filterText.setPreferredSize(new Dimension(100, 20));

		filterText.addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent e) {

				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					new FilterAction().actionPerformed(null);

				} else if (e.isActionKey()) {
					new FilterAction().actionPerformed(null);

				}
			}

			public void keyTyped(KeyEvent e) {

			}

			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {

					new FilterAction().actionPerformed(null);

				}

			}

		});
	}

	static boolean open;

	public boolean isOpen() {
		return open;
	}

	public void attachToModel(ScuflModel model) {
		open = true;
		Properties p = configuration;
		if (p == null) {
			Object[] options = { "Ok" };
			JOptionPane
					.showOptionDialog(
							null,
							"The Required properties file provenance.properties does not exist\n"
									+ "you will be unable to view or store provenance data",
							"Disabled", JOptionPane.YES_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options,
							options[0]);

			return;
		}

		// if (p.getProperty("mygrid.kave.type") == null) {
		// Object[] options = { "Ok" };
		// JOptionPane
		// .showOptionDialog(
		// null,
		// "The Required property mygrid.kave.type in the file
		// provenance.properties does not exist \n"
		// + "you will be unable to view or store provenance data",
		// "Disabled", JOptionPane.YES_OPTION,
		// JOptionPane.QUESTION_MESSAGE, null, options,
		// options[0]);
		//
		// return;
		// }

		Cursor hourglass = new Cursor(Cursor.WAIT_CURSOR);
		setCursor(hourglass);

		new Thread() {
			public void run() {
				try {
					buildBrowser();
				} catch (Exception ex) {
					logger.warn(ex);
				}
			}
		}.start();

	}

	public void detachFromModel() {

	}

	/**
	 * Returns the Workflow associated with the selectedNode
	 * 
	 * @param selectedNode
	 * @return the Workflow associated with the selectedNode
	 */
	private Workflow getWorkflow(DefaultMutableTreeNode selectedNode) {
		Object userObject = selectedNode.getUserObject();
		Workflow result = (Workflow) userObject;
		return result;
	}

	public class RefreshAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public RefreshAction() {
			putValue(SMALL_ICON, TavernaIcons.refreshIcon);
			putValue(NAME, "Refresh");
			putValue(SHORT_DESCRIPTION, "Refresh Workflow List...");

		}

		public void actionPerformed(ActionEvent e) {

			Cursor hourglass = new Cursor(Cursor.WAIT_CURSOR);
			setCursor(hourglass);

			try {
				refresh();
			} catch (MetadataServiceException ex) {
				logger.error(ex);
			} catch (DataServiceException ex) {
				logger.error(ex);
			}

			Cursor normal = new Cursor(Cursor.DEFAULT_CURSOR);
			setCursor(normal);

		}

	}

	public class ReloadAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ReloadAction() {
			putValue(SMALL_ICON, LogBookIcons.reloadIcon);
			putValue(NAME, "Reload");
			putValue(SHORT_DESCRIPTION, "Reload Highlighted workflow...");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {

			int row = workflowRunsTreeTable.getSelectedRow();
			if (row > 0) {
				Cursor hourGlass = new Cursor(Cursor.WAIT_CURSOR);
				setCursor(hourGlass);
				TreePath path = workflowRunsTreeTable.getTree()
						.getSelectionPath();
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path
						.getLastPathComponent();
				Object userObject = selectedNode.getUserObject();

				if (userObject instanceof Workflow) {

					logBookUIModel.reloadWorkFlow(((Workflow) userObject)
							.getLsid());

				} else {
					try {
						logBookUIModel
								.reloadWorkFlow(((WorkflowRun) userObject)
										.getWorkflowInitialId());
					} catch (LogBookException e1) {
						logger.warn(e1);
					}
				}

				hourGlass = Cursor.getDefaultCursor();
				setCursor(hourGlass);
			}

		}
	}

	public class DeleteAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public DeleteAction() {
			// putValue(SMALL_ICON, ScuflIcons.tickIcon);
			putValue(SMALL_ICON, TavernaIcons.deleteIcon);
			putValue(NAME, "Delete");
			putValue(SHORT_DESCRIPTION, "Delete highlighted workflow");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {

			Object[] options = { "Continue", "Cancel" };
			int n = JOptionPane.showOptionDialog(null,
					"Deleting the workflow run will be irreversible:\n"
							+ "are you sure you want to continue?",
					"Confirm Delete", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {

				int row = workflowRunsTreeTable.getSelectedRow();
				if (row > 0) {
					Cursor hourGlass = new Cursor(Cursor.WAIT_CURSOR);
					setCursor(hourGlass);
					TreePath path = workflowRunsTreeTable.getTree()
							.getSelectionPath();
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path
							.getLastPathComponent();
					Object userObject = selectedNode.getUserObject();

					logBookUIModel.deleteWorkFlow(((WorkflowRun) userObject)
							.getLsid());
					try {
						refresh();
					} catch (MetadataServiceException ex) {
						logger.error(ex);
					} catch (DataServiceException ex) {
						logger.error(ex);
					}
					hourGlass = Cursor.getDefaultCursor();
					setCursor(hourGlass);
				}
			}

		}
	}

	public class RerunAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public RerunAction() {
			// putValue(SMALL_ICON, ScuflIcons.tickIcon);
			putValue(SMALL_ICON, LogBookIcons.rerunIcon);
			putValue(NAME, "Rerun");
			putValue(SHORT_DESCRIPTION, "Rerun highlighted workflow");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			int row = workflowRunsTreeTable.getSelectedRow();
			if (row > 0) {
				Cursor hourGlass = new Cursor(Cursor.WAIT_CURSOR);
				setCursor(hourGlass);
				TreePath path = workflowRunsTreeTable.getTree()
						.getSelectionPath();
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path
						.getLastPathComponent();
				Object userObject = selectedNode.getUserObject();

				WorkflowRun workflowRun = ((WorkflowRun) userObject);
				// enactorBrowserModel.smartRerunWorkflow(workflowRun);
				logBookUIModel.rerunWorkflow(workflowRun);

				hourGlass = Cursor.getDefaultCursor();
				setCursor(hourGlass);
			}
		}
	}
	
	public class RdfExportAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public RdfExportAction() {
			putValue(SMALL_ICON, LogBookIcons.rdfIcon);
			putValue(NAME, "Export");
			putValue(SHORT_DESCRIPTION, "Export highlighted workflow to RDF and save to file");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {

			int row = workflowRunsTreeTable.getSelectedRow();
			if (row > 0) {
				Cursor hourGlass = new Cursor(Cursor.WAIT_CURSOR);
				setCursor(hourGlass);
				TreePath path = workflowRunsTreeTable.getTree()
						.getSelectionPath();
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path
						.getLastPathComponent();
				Object o = selectedNode.getUserObject();

				WorkflowRun workflowRun;
				if (o instanceof WorkflowRun) {
					workflowRun = (WorkflowRun) o;
				} else {
					return;
				}
				String rdf = logBookUIModel.toRDF(workflowRun.getLsid());
				Utilities.exportRDF(rdf);

				hourGlass = Cursor.getDefaultCursor();
				setCursor(hourGlass);
			}

		}
	}

	public class DataLineageAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public DataLineageAction() {
			putValue(SMALL_ICON, TavernaIcons.dataLinkIcon);
			putValue(NAME, "Data Lineage");
			putValue(SHORT_DESCRIPTION, "Show data lineage of highlighted workflow");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {

			int row = workflowRunsTreeTable.getSelectedRow();
			if (row > 0) {
				Cursor hourGlass = new Cursor(Cursor.WAIT_CURSOR);
				setCursor(hourGlass);
				TreePath path = workflowRunsTreeTable.getTree()
						.getSelectionPath();
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path
						.getLastPathComponent();
				Object o = selectedNode.getUserObject();

				WorkflowRun workflowRun;
				if (o instanceof WorkflowRun) {
					workflowRun = (WorkflowRun) o;
				} else {
					return;
				}
				try {
					String lsid = workflowRun.getLsid();
					MetadataService metadataService = logBookUIModel
							.getMetadataService();
					DataService dataService = logBookUIModel.getDataService();
					DataLineageVisualiser dataLineageVisualiser = new DataLineageVisualiser(
							metadataService, dataService, lsid);
					JFrame frame = new JFrame(DataLineageVisualiser.TITLE);
					frame.getContentPane().add(dataLineageVisualiser);
					frame.setSize(DataLineageVisualiser.FRAME_DIMENSION);
					frame.setVisible(true);
				} catch (Exception ex) {
					logger.error(ex);
				}


				hourGlass = Cursor.getDefaultCursor();
				setCursor(hourGlass);
			}

		}
	}

	
	public class showParentWorkflowAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public showParentWorkflowAction() {
			putValue(NAME, "show parent workflow");
			putValue(SHORT_DESCRIPTION,
					"Shows this nested workflows parent workflow");

		}

		public void actionPerformed(ActionEvent e) {
			TreePath path = workflowRunsTreeTable.getTree().getSelectionPath();
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path
					.getLastPathComponent();
			Object userObject = selectedNode.getUserObject();
			WorkflowRun w = (WorkflowRun) userObject;
			if (w.isNestedWorkflowRun()) {

				// tree.get

			}

		}
	}

	public class FilterAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public FilterAction() {
			// putValue(SMALL_ICON, ScuflIcons.tickIcon);
			putValue(NAME, "Search");
			putValue(SHORT_DESCRIPTION, "search test...");

		}

		public void actionPerformed(ActionEvent e) {

			workflowRunsTreeTableModel.setTitleFilterString(filterText
					.getText());
			workflowRunsTreeTableModel.update();
			workflowRunsTreeTable.getTree().repaint();

			// CreateNewLabel label = new CreateNewLabel(null,null);
		}

	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

	private void refresh() throws MetadataServiceException,
			DataServiceException {
		// reset the user workflows using the current settings
		logBookUIModel.refresh();
		workflowRunsTreeTableModel.setWorkflows(logBookUIModel
				.getWorkflowObjects());
		workflowRunsTreeTableModel.reset();
		// reset all of the other panels panels
		processRunsPane.setEnabledAt(processRunsPane.indexOfTab("Results"),
				false);
		processRunsPane.setProcessData(null, null);
		processRunsPane.clearIntermediateResults();
		processRunsPane.getParent().repaint();
		reloadAction.setEnabled(false);
		rerunAction.setEnabled(false);
		deleteAction.setEnabled(false);
	}

	private void disableProcessRunActions() {
		rerunAction.setEnabled(false);
		deleteAction.setEnabled(false);
		rdfExportAction.setEnabled(false);
		dataLineageAction.setEnabled(false);
	}

	private void enableProcessRunActions() {
		rerunAction.setEnabled(true);
		deleteAction.setEnabled(true);
		rdfExportAction.setEnabled(true);
		dataLineageAction.setEnabled(true);
	}

}
