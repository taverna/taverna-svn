/*******************************************************************************
 * Copyright (C) 2007-2010 The University of Manchester   
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
package net.sf.taverna.t2.workbench.ui.impl;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import net.sf.taverna.osx.OSXAdapter;
import net.sf.taverna.osx.OSXApplication;
import net.sf.taverna.raven.SplashScreen;
import net.sf.taverna.raven.appconfig.ApplicationConfig;
import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.ui.perspectives.CustomPerspective;
import net.sf.taverna.t2.workbench.MainWindow;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.ShutdownSPI;
import net.sf.taverna.t2.workbench.StartupSPI;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.events.SetCurrentDataflowEvent;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.helper.Helper;
import net.sf.taverna.t2.workbench.ui.impl.configuration.ui.T2ConfigurationFrame;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

import org.apache.log4j.Logger;

/**
 * The main workbench frame.
 * 
 * @author David Withers
 * @author Stian Soiland-Reyes
 * 
 */
public class Workbench extends JFrame {

	private static final String NIMBUS = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";

	private OSXAppListener osxAppListener = new OSXAppListener();

	private static final String LAUNCHER_LOGO_PNG = "/launcher_logo.png";

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(Workbench.class);

	private ApplicationRuntime appRuntime = ApplicationRuntime.getInstance();
	private ApplicationConfig appConfig = ApplicationConfig.getInstance();
	private MenuManager menuManager = MenuManager.getInstance();
	private FileManager fileManager = FileManager.getInstance();
	private EditManager editManager = EditManager.getInstance();

	private WorkbenchPerspectives perspectives;

	private JToolBar perspectiveToolBar;

	private WorkbenchZBasePane basePane;

	private boolean isInitialized = false;

	private class WindowClosingListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			exit();
		}
	}

	private static class Singleton {
		private static Workbench instance = new Workbench();
	}

	public static final Workbench getInstance() {
		boolean initializing = false;
		synchronized (Singleton.class) {
			if (!Singleton.instance.isInitialized) {
				Singleton.instance.isInitialized = true;
				initializing = true;
			}
		}
		if (initializing) {
			MainWindow.setMainWindow(Singleton.instance);
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						Singleton.instance.initialize();
					}
				});
			} catch (InterruptedException e) {
				throw new RuntimeException(
						"Interrupted while initializing workbench", e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Could not initialize workbench", e
						.getCause());
			}
		}
		return Singleton.instance;
	}

	/**
	 * @see #getInstance()
	 */
	protected Workbench() {
	}

	private void makeGUI() {
		setLayout(new GridBagLayout());

		addWindowListener(new WindowClosingListener());
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		Helper.setKeyCatcher(this);

		URL launcherLogo = getClass().getResource(LAUNCHER_LOGO_PNG);
		if (launcherLogo != null) {
			ImageIcon imageIcon = new ImageIcon(launcherLogo);
			setIconImage(imageIcon.getImage());
		}
		setTitle(appConfig.getTitle());

		OSXApplication.setListener(osxAppListener);

		// Set the size and position of the Workbench to the last
		// saved values or use the default ones the first time it is launched
		loadSizeAndLocationPrefs();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.LINE_START;
		JPanel toolbarPanel = makeToolbarPanel();

		add(toolbarPanel, gbc);

		basePane = makeBasePane();

		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 1;
		gbc.weightx = 0.1;
		gbc.weighty = 0.1;
		add(basePane, gbc);

		/*
		 * Need to do this <b>last</b> as it references perspectives
		 */
		JMenuBar menuBar = menuManager.createMenuBar();
		setJMenuBar(menuBar);
	}

	protected WorkbenchZBasePane makeBasePane() {
		WorkbenchZBasePane basePane = new WorkbenchZBasePane();
		basePane.setRepository(appRuntime.getRavenRepository());
		perspectives = new WorkbenchPerspectives(basePane, perspectiveToolBar);
		return basePane;
	}

	public void makeNamedComponentVisible(String componentName) {
		basePane.makeNamedComponentVisible(componentName);
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

	protected void initialize() {
		
		setLookAndFeel();

		// Call the startup hooks
		if (!callStartupHooks()) {
			System.exit(0);
		}
		makeGUI();
		fileManager.newDataflow();
		editManager.addObserver(DataflowEditsListener.getInstance());
		SplashScreen splash = SplashScreen.getSplashScreen();
		if (splash != null) {
			splash.setClosable();
			splash.requestClose();
		}

		// Register a listener with FileManager so whenever a current workflow
		// is set
		// we make sure we are in the design perspective
		fileManager.addObserver(new SwitchToWorkflowPerspective());
	}

	/**
	 * Calls the startup methods on all the {@link StartupSPI}s. If any startup
	 * method returns <code>false</code> (meaning that the Workbench will not
	 * function at all) then this method returns <code>false</code>.
	 */
	private boolean callStartupHooks() {
		boolean startup = true;
		SPIRegistry<StartupSPI> registry = new SPIRegistry<StartupSPI>(
				StartupSPI.class);
		List<StartupSPI> instances = registry.getInstances();
		Collections.sort(instances, new Comparator<StartupSPI>() {
			public int compare(StartupSPI o1, StartupSPI o2) {
				return o1.positionHint() - o2.positionHint();
			}
		});
		for (StartupSPI startupSPI : instances) {
			if (!startupSPI.startup()) {
				startup = false;
				break;
			}
		}
		return startup;
	}

	public void exit() {
		if (callShutdownHooks()) {
			System.exit(0);
		}

	}

	void savePerspectives() {
		// Save the perspectives to XML files
		try {
			PerspectiveSPI currentPerspective = (PerspectiveSPI) ModelMap
					.getInstance().getModel(
							ModelMapConstants.CURRENT_PERSPECTIVE);
			if (currentPerspective != null
					&& currentPerspective instanceof CustomPerspective) {
				((CustomPerspective) currentPerspective).update(basePane
						.getElement());
			}
			perspectives.saveAll();
		} catch (Exception ex) {
			logger.error(
							"Error saving perspectives when exiting the Workbench.",
							ex);
		}
	}

	/**
	 * Calls all the shutdown on all the {@link ShutdownSPI}s. If a shutdown
	 * returns <code>false</code> (meaning that the shutdown process should be
	 * aborted) then this method returns with a value of <code>false</code>
	 * immediately.
	 * 
	 * @return <code>true</code> if all the <code>ShutdownSPIs</code> return
	 *         <code>true</code> and the workbench shutdown should proceed
	 */
	private boolean callShutdownHooks() {
		boolean shutdown = true;
		SPIRegistry<ShutdownSPI> registry = new SPIRegistry<ShutdownSPI>(
				ShutdownSPI.class);
		List<ShutdownSPI> instances = registry.getInstances();
		Collections.sort(instances, new Comparator<ShutdownSPI>() {
			public int compare(ShutdownSPI o1, ShutdownSPI o2) {
				return o1.positionHint() - o2.positionHint();
			}
		});
		for (ShutdownSPI shutdownSPI : instances) {
			if (!shutdownSPI.shutdown()) {
				shutdown = false;
				break;
			}
		}
		return shutdown;
	}

	/**
	 * Store current Workbench position and size.
	 * 
	 * @throws IOException
	 */
	void storeSizeAndLocationPrefs() throws IOException {

		// Store the current Workbench window size and position
		File confDir = new File(appRuntime.getApplicationHomeDir(), "conf");
		File propFile = new File(confDir, "preferences.properties");
		if (!propFile.exists()) {
			propFile.createNewFile();
		}

		Writer writer = new BufferedWriter(new FileWriter(propFile));
		writer.write("width=" + this.getWidth() + "\n");
		writer.write("height=" + this.getHeight() + "\n");
		writer.write("x=" + this.getX() + "\n");
		writer.write("y=" + this.getY() + "\n");
		writer.flush();
		writer.close();
	}

	/**
	 * Loads last saved Workbench position and size.
	 * 
	 * @throws IOException
	 */
	private void loadSizeAndLocationPrefs() {
		File confDir = new File(appRuntime.getApplicationHomeDir(), "conf");
		File propFile = new File(confDir, "preferences.properties");

		// Screen size
		Dimension screen = getToolkit().getScreenSize();

		if (!propFile.exists()) {

			// set default size to 3/4 of width and height
			setSize((int) (screen.getWidth() * 0.75),
					(int) (screen.getHeight() * 0.75));

			// this.setSize(new Dimension(1000, 800));
			this.setLocation(0, 0);
		} else {
			Properties props = new Properties();
			try {
				props.load(propFile.toURI().toURL().openStream());
				String swidth = props.getProperty("width");
				String sheight = props.getProperty("height");
				String sx = props.getProperty("x");
				String sy = props.getProperty("y");

				int width = Integer.parseInt(swidth);
				int height = Integer.parseInt(sheight);
				int x = Integer.parseInt(sx);
				int y = Integer.parseInt(sy);

				// Make sure our window is not too big
				width = Math.min((int) screen.getWidth(), width);
				height = Math.min((int) screen.getHeight(), height);

				// Move to upper left corner if we are too far off
				if (x > (screen.getWidth() - 50) || x < 0) {
					x = 0;
				}
				if (y > (screen.getHeight() - 50) || y < 0) {
					y = 0;
				}

				this.setSize(width, height);
				this.setLocation(x, y);

			} catch (Exception e) {
				logger
						.error(
								"Error loading default Workbench window dimensions.",
								e);
			}
		}
	}

	public static void setLookAndFeel() {
		String defaultLaf = System.getProperty("swing.defaultlaf");
		if (defaultLaf != null) {
			try { 
				UIManager.setLookAndFeel(defaultLaf);
				return;
			} catch (Exception e) {
				logger.info("Can't set requested look and feel -Dswing.defaultlaf=" + defaultLaf, e);
			}
		}
		String os = System.getProperty("os.name");
		if (os.contains("Mac") || os.contains("Windows")) {
			// For OSX and Windows use the system look and feel
			String systemLF = UIManager.getSystemLookAndFeelClassName();
			try {
				UIManager.setLookAndFeel(systemLF);
				logger.info("Using system L&F " + systemLF);
				return;
			} catch (Exception ex2) {
				logger.error("Unable to load system look and feel "
						+ systemLF, ex2);
			}
		}
		// The system look and feel on *NIX
		// (com.sun.java.swing.plaf.gtk.GTKLookAndFeel) looks
		// like Windows 3.1.. try to use Nimbus (Java 6e10 and 
		// later)
		try {
			UIManager.setLookAndFeel(NIMBUS);
			logger.info("Using Nimbus look and feel");
			return;
		} catch (Exception e) {			
		}
		
		// Metal should be better than GTK still
		try {
			String crossPlatform = UIManager.getCrossPlatformLookAndFeelClassName();
			UIManager.setLookAndFeel(crossPlatform);
			logger.info("Using cross platform Look and Feel " + crossPlatform);
		} catch (Exception e){
		}
		
		// Final fallback
		try {
			String systemLF = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(systemLF);
			logger.info("Using system platform Look and Feel " + systemLF);
		} catch (Exception e){
			logger.info("Using default Look and Feel " + UIManager.getLookAndFeel());			
		}
		
		
	}

	public WorkbenchPerspectives getPerspectives() {
		return perspectives;
	}

	private final class SwitchToWorkflowPerspective implements
			Observer<FileManagerEvent> {
		// If we currently are not in the design perspective - switch to it now
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			if (message instanceof SetCurrentDataflowEvent) {
				getPerspectives().setWorkflowPerspective();
			}
		}
	}

	protected class OSXAppListener extends OSXAdapter {
		@Override
		public boolean handleQuit() {
			exit();
			return false;
		}

		@Override
		public boolean hasPreferences() {
			return true;
		}

		@Override
		public boolean handlePreferences() {
			T2ConfigurationFrame.showFrame();
			return true;
		}

		@Override
		public boolean handleOpenFile(String filename) {
			try {
				FileManager.getInstance()
						.openDataflow(null, new File(filename));
				return true;
			} catch (OpenException e) {
				logger.warn("Could not open file " + filename, e);
			} catch (IllegalStateException e) {
				logger.warn("Could not open file " + filename, e);
			}
			return false;
		}
	}

}
