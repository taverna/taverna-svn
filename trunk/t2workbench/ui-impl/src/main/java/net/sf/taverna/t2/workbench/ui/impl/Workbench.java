package net.sf.taverna.t2.workbench.ui.impl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.log.ConsoleLog;
import net.sf.taverna.raven.log.Log;
import net.sf.taverna.t2.ui.menu.impl.MenuManager;

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

	private Workbench() {
		// Initialisation done by getInstance()
	}

	private void makeGUI() {
		setLayout(new BorderLayout());
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLookAndFeel();
		
		JSplitPane toolBarPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		add(toolBarPanel, BorderLayout.NORTH);

		JToolBar toolBar = menuManager.createToolBar();
		toolBarPanel.add(toolBar);

		setSize(new Dimension(500, 500));

		WorkbenchZBasePane basePane = new WorkbenchZBasePane();
		basePane.setRepository(appRuntime.getRavenRepository());
		perspectives = new WorkbenchPerspectives(basePane, toolBar);
		perspectives.initialisePerspectives();
		add(basePane, BorderLayout.CENTER);
		
		// Need to do this last as it references perspectives
		JMenuBar menuBar = menuManager.createMenuBar();
		setJMenuBar(menuBar);
	}

	public static final synchronized Workbench getInstance() {
		if (instance == null) {
			instance = new Workbench();
			instance.makeGUI();
		}
		return instance;
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
