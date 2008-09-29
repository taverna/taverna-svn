/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.tools.Bootstrap;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scuflui.ResultItemPanel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.results.ResultMapSaveRegistry;
import org.embl.ebi.escience.scuflui.shared.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.spi.ResultMapSaveSPI;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

/**
 * A simple data thing viewer tool that can be launched from the command line or
 * LSID launchpad to view the XML form of data thing object Map. The main method
 * takes a single filename as its command lines argument and creates a JFrame
 * with the data renderer in.
 * 
 * @author Tom Oinn
 */
public class DataThingViewer extends JFrame {

	private JTabbedPane tabs;

	private JToolBar toolbar;

	private Map resultMap;

	final JFileChooser fc = new JFileChooser();

	public static void main(String[] args) throws Exception {
		// Set the local repository for the SPI registry
		Repository repository = LocalRepository.getRepository(new File(Bootstrap.TAVERNA_CACHE));
		if (repository != null) {
			TavernaSPIRegistry.setRepository(repository);
		}

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		DataThingViewer viewer = new DataThingViewer();
		if (args.length == 1) {
			viewer.load(new File(args[0]));
		}
	}

	public DataThingViewer() {
		super("DataThing Viewer");
		setBounds(100, 100, 500, 500);
		// Quit this app when the big window closes.
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		this.tabs = new JTabbedPane();
		getContentPane().add(this.tabs);
		this.toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		toolbar.setMaximumSize(new Dimension(2000, 30));
		toolbar.setBorderPainted(true);
		getContentPane().add(this.toolbar, BorderLayout.NORTH);
		toolbar.add(Box.createHorizontalGlue());
		setJMenuBar(createMenuBar());
		clear();
		setVisible(true);
	}

	private void clear() {
		toolbar.removeAll();
		tabs.removeAll();
		resultMap = new HashMap();
		ResultMapSaveSPI[] savePlugins = ResultMapSaveRegistry.plugins();
		for (int i = 0; i < savePlugins.length; i++) {
			JButton saveAction = new JButton(savePlugins[i].getName(),
					savePlugins[i].getIcon());
			saveAction.addActionListener(savePlugins[i].getListener(resultMap,
					null));
			saveAction.setEnabled(false);
			toolbar.add(saveAction);
			if (i < savePlugins.length - 1) {
				toolbar.addSeparator();
			}
		}
	}

	private void load(File f) throws Exception {
		InputStream is = new FileInputStream(f);
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(is);
		tabs.removeAll();
		resultMap = DataThingXMLFactory.parseDataDocument(doc);
		for (Iterator i = resultMap.keySet().iterator(); i.hasNext();) {
			String resultName = (String) i.next();
			DataThing resultValue = (DataThing) resultMap.get(resultName);
			this.tabs.add(resultName, new ResultItemPanel(resultValue));
		}
		toolbar.removeAll();
		ResultMapSaveSPI[] savePlugins = ResultMapSaveRegistry.plugins();
		for (int i = 0; i < savePlugins.length; i++) {
			JButton saveAction = new JButton(savePlugins[i].getName(),
					savePlugins[i].getIcon());
			saveAction.addActionListener(savePlugins[i].getListener(resultMap,
					null));
			toolbar.add(saveAction);
			if (i < savePlugins.length - 1) {
				toolbar.addSeparator();
			}
		}
		toolbar.add(Box.createHorizontalGlue());
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		// File menu to allow saving of the data thing to disk
		JMenu fileMenu = new JMenu("File");
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		JMenuItem reset = new JMenuItem("Reset", TavernaIcons.deleteIcon);
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clear();
			}
		});
		JMenuItem load = new JMenuItem("Load", TavernaIcons.openIcon);
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Preferences prefs = Preferences
						.userNodeForPackage(DataThingViewer.class);
				String curDir = prefs.get("currentDir", System
						.getProperty("user.home"));
				fc.resetChoosableFileFilters();
				fc
						.setFileFilter(new ExtensionFileFilter(
								new String[] { "xml" }));
				fc.setCurrentDirectory(new File(curDir));
				int returnVal = fc.showOpenDialog(DataThingViewer.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					prefs
							.put("currentDir", fc.getCurrentDirectory()
									.toString());
					try {
						DataThingViewer.this.load(fc.getSelectedFile());
					} catch (Exception ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(DataThingViewer.this,
								"Cannot open XML : \n\n" + ex.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		fileMenu.add(load);
		fileMenu.add(reset);
		fileMenu.add(exit);
		menuBar.add(fileMenu);
		return menuBar;
	}

}
