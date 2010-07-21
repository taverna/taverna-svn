/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.dataviewer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sf.taverna.raven.SplashScreen;
import net.sf.taverna.raven.launcher.Launchable;
import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.lang.ui.ExtensionFileFilter;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPIRegistry;

/**
 * A viewer tool for DataThing objects saved in a Baclava file. Baclava format 
 * is an XML format that represents serialised DataThing objects that typically 
 * represent inputs or outputs of a workflow. The content of the DataThing objects 
 * in the file is Base64 encoded so it is difficult to manually look at the 
 * Baclava file - hence this viewer. The tool can be 
 * launched from the command line or LSID launchpad to view the XML form of 
 * DataThing object Map loaded form the Baclava file. The main method takes a 
 * single filename as its command line 
 * argument and creates a JFrame with the data renderer in.
 * 
 * @author Tom Oinn
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class DataViewerTool extends JFrame implements Launchable{
	
	// Splash screen of the app
	private static final String DATAVIEWER_SPLASHSCREEN = "/dataviewer-splash.png";

	// Title of the app
	private static final String WINDOW_TITLE = "Taverna DataViewer";
	
	// Instructions on how to use the Viewer after it starts up
	private static final String INSTRUCTIONS_TEXT = "<html><body>To load and view a Baclava data file, select <strong><em>Open</em></strong> from <strong><em>File</em></strong> menu and choose the file.</body></html>";
	
	// Menu items
	private static final String FILE_MENU_NAME = "File";
	private static final String OPEN_FILE_ACTION_NAME = "Open file...";
	private static final String RECENT_FILE_ACTION_MENU = "Recent files";
	private static final String CLOSE_FILE_ACTION_NAME = "Close file";
	private static final String SAVE_ALL_ACTION_NAME = "Save all values";
	private static final String  QUIT_VIEWER_ACTION_NAME = "Quit DataViewer";
	
	private JTabbedPane tabs;

	// Maps of DataThingS to the name of the port they came out from
	private Map<String, DataThing> resultDataThingMap;

	final JFileChooser fc = new JFileChooser();

	private ReferenceService referenceService;

	// Welcome panel with instructions on how to load data file
	private JPanel instructionsPanel;

	// Map of tab names to tab panels, orderer alphabetically
	private TreeMap<String, DataViewPanel> namesToTabsMap;

	// Save data File menu item
	private JMenuItem saveAllMenuItem;
	private SaveAllAction saveAllAction;

	// Registry of all existing 'save all' actions, each one can save data
	// in a different format
	private static SaveAllResultsSPIRegistry saveAllResultsRegistry = SaveAllResultsSPIRegistry.getInstance();	

	// Close data File menu item
	private JMenuItem closeMenuItem;
	
	// Recently opened data files
	RecentItems recentFiles = new RecentItems(10, Preferences.userNodeForPackage(DataViewerTool.class));

	// Path to the currently opened file
	String currentFilePath;
	
	// Recently opened data files menu item
	private JMenu recentFilesMenuItem;
	
	public static void main(String[] args) throws Exception {
			
		SplashScreen splash = null;		
		URL splashScreenURL = DataViewerTool.class.getResource(DATAVIEWER_SPLASHSCREEN);
		if (splashScreenURL != null && !GraphicsEnvironment.isHeadless()) {
			splash = SplashScreen.getSplashScreen(splashScreenURL, 2500);
			splash.setClosable();
		} 

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		DataViewerTool viewer = new DataViewerTool();
		if (args.length == 1) {
			viewer.load(new File(args[0]));
		}
		else{
			viewer.setInstructions();
		}
		
		if (splash != null){
			splash.requestClose();
		}
		// We want the splash to show a bit longer so we set a timeout on it
		// but the main app window shows on top of it once it is ready (normally
		// before the splash has timedout) which is not nice so we wait here until 
		// the splash disappears
		while (splash.isActive()){
			// do nothing
		}
		viewer.setVisible(true);
	}

	public DataViewerTool() {
				
		super(WINDOW_TITLE);
		
		referenceService = createReferenceServiceBean();
		
		setBounds(100, 100, 800, 500);
		
		// Quit this app when the big window closes.
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		tabs = new JTabbedPane();
		namesToTabsMap = new TreeMap<String, DataViewPanel>();
		
		saveAllAction = new SaveAllAction();
		
		setJMenuBar(createMenuBar());		
	}

	private void setInstructions() {
		if (instructionsPanel == null){
			instructionsPanel = new JPanel();
			instructionsPanel.setBorder(new EmptyBorder(10, 10, 20, 10));
		}
		instructionsPanel.add(new JLabel(INSTRUCTIONS_TEXT));
	
		getContentPane().add(instructionsPanel);
	}
	
	@SuppressWarnings("unchecked")
	private void load(File file) throws Exception {
		InputStream is = new FileInputStream(file);
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(is);
		
		close();
		
		getContentPane().add(tabs);

		resultDataThingMap = DataThingXMLFactory.parseDataDocument(doc);
				
		for (Iterator i = resultDataThingMap.keySet().iterator(); i.hasNext();) {
			String resultName = (String) i.next();
			DataThing resultDataThingValue = resultDataThingMap.get(resultName);
			DataViewPanel tab = new DataViewPanel(resultDataThingValue, referenceService);
			tab.setSaveAllButtonAction(saveAllAction);
			namesToTabsMap.put(resultName, tab);
		}
		
		for (String tabName : namesToTabsMap.keySet()){
			tabs.add(tabName, namesToTabsMap.get(tabName));
		}
		
		setTitle(WINDOW_TITLE + ": " + file.getAbsolutePath());
		saveAllAction.setEnabled(true);
		closeMenuItem.setEnabled(true);
		recentFiles.push(file.getAbsolutePath()); // save the file under recently opened files
		currentFilePath = file.getAbsolutePath();
		updateRecentFilesMenu();
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		// File menu to allow saving of the data thing to disk
		JMenu fileMenu = new JMenu(FILE_MENU_NAME);
		
		JMenuItem exitMenuItem = new JMenuItem(QUIT_VIEWER_ACTION_NAME);
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		JMenuItem openMenuItem = new JMenuItem(new OpenFileAction());
		
		closeMenuItem = new JMenuItem(CLOSE_FILE_ACTION_NAME, WorkbenchIcons.closeIcon);
		closeMenuItem.setEnabled(false);
		closeMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		
		recentFilesMenuItem = new JMenu(RECENT_FILE_ACTION_MENU);
		updateRecentFilesMenu();
		
		saveAllMenuItem = new JMenuItem(saveAllAction);
		saveAllAction.setEnabled(false);
		
		fileMenu.add(openMenuItem);
		fileMenu.add(recentFilesMenuItem);
		fileMenu.add(closeMenuItem);
		fileMenu.add(saveAllMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(exitMenuItem);
		
		menuBar.add(fileMenu);
		return menuBar;
	}

	private void updateRecentFilesMenu() {
		if (recentFilesMenuItem.getMenuComponentCount() > 0){
			recentFilesMenuItem.removeAll();
		}
		for (String filePath : recentFiles.getItems()){
			File file = new File(filePath);
			OpenFileAction openFileAction =  new OpenFileAction(file);
			JMenuItem recentFileMenuItem = new JMenuItem(openFileAction);
			recentFileMenuItem.setText(filePath);
			recentFilesMenuItem.add(recentFileMenuItem);
			if (currentFilePath != null){
				if (filePath.equals(currentFilePath)){
					recentFileMenuItem.setEnabled(false);
				}
			}
		}		
	}
	
	private void close() {
		tabs.removeAll();
		resultDataThingMap = new HashMap<String, DataThing>();
		namesToTabsMap = new TreeMap<String, DataViewPanel>();
		
		if (instructionsPanel != null){
			getContentPane().remove(instructionsPanel);
		}
		
		setTitle(WINDOW_TITLE);
		saveAllAction.setEnabled(false);
		closeMenuItem.setEnabled(false);
		currentFilePath = null;
		
		updateRecentFilesMenu();
	}
	
	protected void saveAll() {
		
		if (tabs.getComponents().length == 0) {
			return; // nothing to save
		}
		
		String title = "Data saver";
		
		final JDialog dialog = new JDialog(this, title, true);
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(this);
		JPanel panel = new JPanel(new BorderLayout());
		DialogTextArea explanation = new DialogTextArea();
		explanation.setText("Select data to save");
		explanation.setColumns(40);
		explanation.setEditable(false);
		explanation.setOpaque(false);
		explanation.setBorder(new EmptyBorder(5, 10, 5, 20));
		explanation.setFocusable(false);
		explanation.setFont(new JLabel().getFont()); // make the font the same as for other components in the dialog
		panel.add(explanation, BorderLayout.NORTH);
		final Map<String, JCheckBox> selectedCheckBoxes = new TreeMap<String, JCheckBox>(); // names to checkboxes map
		final Map<String, Object> selectedData = new TreeMap<String, Object> (); // names to data objects map
		final Set<SaveAllResultsSPI> actionSet = new HashSet<SaveAllResultsSPI>();
		
		JPanel checkBoxesPanel = new JPanel();
		checkBoxesPanel.setBorder(new CompoundBorder(new EmptyBorder(new Insets(5,10,5,10)), new EtchedBorder(EtchedBorder.LOWERED)));
		checkBoxesPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(5,10,5,10);
		checkBoxesPanel.add(new JLabel("Data items:"), gbc);				

		gbc.insets = new Insets(0,10,5,10);
		for (final String tabName : namesToTabsMap.keySet()){
			final JCheckBox checkBox = new JCheckBox(tabName);
			checkBox.setSelected(true);
			selectedCheckBoxes.put(tabName, checkBox);
			
			DataViewPanel tab = namesToTabsMap.get(tabName);
			final T2Reference dataReference = tab.getDataReference();
			// Get the data object represented in tab
			Object object = tab.getDataObject();
			selectedData.put(tabName, object);
				
			checkBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (checkBox.isSelected()){
						selectedCheckBoxes.put(tabName, checkBox);
						selectedData.put(tabName, dataReference);
					}
					else{
						selectedCheckBoxes.remove(tabName);
						selectedData.remove(tabName);
					}					
				}
			});
			
			gbc.gridy++;
			checkBoxesPanel.add(checkBox, gbc);
			gbc.gridy++;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
		}
		
		panel.add(checkBoxesPanel, BorderLayout.CENTER);

		JPanel buttonsBar = new JPanel();
		buttonsBar.setLayout(new FlowLayout());
		// Get all existing 'Save result' actions
		List<SaveAllResultsSPI> saveActions = saveAllResultsRegistry.getSaveResultActions();
		for (SaveAllResultsSPI spi : saveActions){
			SaveAllResultsSPI action = (SaveAllResultsSPI) spi.getAction();
			actionSet.add(action);
			JButton saveButton = new JButton((AbstractAction) action);
			action.setChosenReferences(selectedData);
			action.setParent(dialog);
			buttonsBar.add(saveButton);
		}
		JButton cancelButton = new JButton("Cancel", WorkbenchIcons.closeIcon);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});
		buttonsBar.add(cancelButton);
		panel.add(buttonsBar, BorderLayout.SOUTH);
		panel.revalidate();
		dialog.add(panel);
		dialog.pack();
		dialog.setVisible(true);
	}		

	protected ReferenceService createReferenceServiceBean() {
		ApplicationContext appContext = new ClassPathXmlApplicationContext(
				DataManagementConfiguration.IN_MEMORY_CONTEXT);
		return (ReferenceService) appContext
				.getBean("t2reference.service.referenceService");
	}
	
	/**
	 * Action to open and load a Baclava file.
	 *
	 */
	private class OpenFileAction extends AbstractAction {
		
		private File file;

		// Lets user select a Baclava file to load from a FileChooser
		public OpenFileAction() {
			super(OPEN_FILE_ACTION_NAME, WorkbenchIcons.openIcon);
		}
		
		// Loads a given Baclava file
		public OpenFileAction(File file) {
			super(file.getAbsolutePath());
			this.file = file;
		}
		
		public void actionPerformed(ActionEvent e) {

			if (file == null){
				Preferences prefs = Preferences.userNodeForPackage(DataViewerTool.class);
				String curDir = prefs.get("currentDir", System.getProperty("user.home"));
				fc.resetChoosableFileFilters();
				fc.setFileFilter(new ExtensionFileFilter(
						new String[] { "xml" }));
				fc.setCurrentDirectory(new File(curDir));
				int returnVal = fc.showOpenDialog(DataViewerTool.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					prefs.put("currentDir", fc.getCurrentDirectory()
							.toString());
				}
				else{
					return;
				}
			}
			
			try {
				if (file == null){
					DataViewerTool.this.load(fc.getSelectedFile());
				}
				else{
					DataViewerTool.this.load(file);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(DataViewerTool.this,
						"Could not open file: \n\n" + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Action to save the data in various formats selected by the user.
	 *
	 */
	private class SaveAllAction extends AbstractAction {

		public SaveAllAction() {
	        super(SAVE_ALL_ACTION_NAME, WorkbenchIcons.saveAllIcon);
	    }
		
	    public void actionPerformed(ActionEvent e) {
	        saveAll();
	        if (e.getSource() instanceof JButton){
	        	((JButton)e.getSource()).getParent().requestFocusInWindow(); // loose the focus from the button
	        }
	    }
	}

	public int launch(String[] args) throws Exception {
		main(args);
		return 0;
	}

}