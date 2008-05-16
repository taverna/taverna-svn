package net.sf.taverna.t2.workbench.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

/**
 * The main workbench frame.
 * 
 * @author David Withers
 */
public class Workbench extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(Workbench.class);

	private static Workbench instance;

	private ActionManager actionManager;
	
	private Workbench() {
		setLayout(new BorderLayout());
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		setLookAndFeel();		

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JSplitPane toolBarPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		add(toolBarPanel, BorderLayout.NORTH);
		
		JToolBar toolBar = new JToolBar();
		toolBarPanel.add(toolBar);
		
		actionManager = new ActionManager(menuBar, toolBar);
		
		setSize(new Dimension(500, 500));
		
	}
	
	public static final Workbench getInstance() {
		if (instance == null) {
			instance = new Workbench();
		}
		return instance;
	}
	
	public void exit() {
		System.exit(0);
	}
	
	private void setLookAndFeel() {
//		String landf = MyGridConfiguration
//				.getProperty("taverna.workbench.themeclass");
		boolean set = false;

//		if (landf != null) {
//			try {
//				UIManager.setLookAndFeel(landf);
//				logger.info("Using " + landf + " Look and Feel");
//				set = true;
//			} catch (Exception ex) {
//				logger.error(
//						"Error using theme defined by taverna.workbench.themeclass as "
//								+ landf, ex);
//			}
//		}

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
					} else {
						logger.info("Using default Look and Feel");
					}
				} catch (Exception ex2) {
					ex2.printStackTrace();
				}

			}
		}
	}
	
	public static void main(String[] args) {
		System.setProperty("raven.eclipse", "true");
		Workbench workbench = getInstance();
		workbench.setVisible(true);
	}

}
