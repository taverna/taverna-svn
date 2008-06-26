package net.sf.taverna.t2.workbench.ui.impl;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import net.sf.taverna.raven.SplashScreen;
import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.log.ConsoleLog;
import net.sf.taverna.raven.log.Log;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.file.FileManager;

import org.apache.log4j.Logger;

/**
 * The main workbench frame.
 * 
 * @author David Withers
 * @author Stian Soiland-Reyes
 * 
 */
public class Workbench extends JFrame {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(Workbench.class);

	private ApplicationRuntime appRuntime = ApplicationRuntime.getInstance();

	private static Workbench instance;

	private MenuManager menuManager = MenuManager.getInstance();

	private WorkbenchPerspectives perspectives;

	private JToolBar perspectiveToolBar;

	private Workbench() {
		// Initialisation done by getInstance()
	}

	private void makeGUI() {
		setLayout(new GridBagLayout());
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLookAndFeel();
		setSize(new Dimension(700, 500));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.LINE_START;
		JPanel toolbarPanel = makeToolbarPanel();
		
		add(toolbarPanel, gbc);

		WorkbenchZBasePane basePane = makeBasePane();

		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 1;
		gbc.weightx = 0.1;
		gbc.weighty = 0.1;
		add(basePane, gbc);

		// Need to do this last as it references perspectives
		JMenuBar menuBar = menuManager.createMenuBar();
		setJMenuBar(menuBar);
	}

	protected WorkbenchZBasePane makeBasePane() {
		WorkbenchZBasePane basePane = new WorkbenchZBasePane();
		basePane.setRepository(appRuntime.getRavenRepository());
		perspectives = new WorkbenchPerspectives(basePane, perspectiveToolBar);
		perspectives.initialisePerspectives();
		return basePane;
	}

	protected JPanel makeToolbarPanel() {
		JPanel toolbarPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.LINE_START;

		JToolBar generatedToolbar = menuManager.createToolBar();
		generatedToolbar.setFloatable(false);
		toolbarPanel.add(generatedToolbar, gbc);

		perspectiveToolBar = new JToolBar("Perspectives");
		perspectiveToolBar.setFloatable(false);
		gbc.gridy = 1;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		toolbarPanel.add(perspectiveToolBar, gbc);
		
		return toolbarPanel;
	}

	public static final synchronized Workbench getInstance() {
		if (instance == null) {
			instance = new Workbench();
			instance.initialize();
		}
		return instance;
	}

	protected void initialize() {
		makeGUI();
		FileManager.getInstance().newDataflow();
		SplashScreen splash = SplashScreen.getSplashScreen();
		if (splash != null) {
			splash.setClosable();
			splash.requestClose();
		}
	}

	public void exit() {
		System.exit(0);
	}

	private void setLookAndFeel() {
		// String landf = MyGridConfiguration
		// .getProperty("taverna.workbench.themeclass");
		boolean set = false;

		// if (landf != null) {
		// try {
		// UIManager.setLookAndFeel(landf);
		// logger.info("Using " + landf + " Look and Feel");
		// set = true;
		// } catch (Exception ex) {
		// logger.error(
		// "Error using theme defined by taverna.workbench.themeclass as "
		// + landf, ex);
		// }
		// }

		if (!set) {
			try {
				UIManager
						.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
				logger.info("Using Synthetica Look and Feel");
			} catch (Exception ex) {
				try {
					if (!(System.getProperty("os.name").equals("Linux"))) {
						UIManager.setLookAndFeel(UIManager
								.getSystemLookAndFeelClassName());
						logger.info("Using "
								+ UIManager.getSystemLookAndFeelClassName()
								+ " Look and Feel");
						set = true;
					} else {
						logger.info("Using default Look and Feel");
						set = true;
					}
				} catch (Exception ex2) {
					ex2.printStackTrace();
				}

			}
		}
	}

	public static void main(String[] args) throws IOException {
		System.setProperty("raven.eclipse", "true");
		Log.setImplementation(new ConsoleLog());
		Workbench workbench = getInstance();
		workbench.setVisible(true);
	}

	public WorkbenchPerspectives getPerspectives() {
		return perspectives;
	}

}
