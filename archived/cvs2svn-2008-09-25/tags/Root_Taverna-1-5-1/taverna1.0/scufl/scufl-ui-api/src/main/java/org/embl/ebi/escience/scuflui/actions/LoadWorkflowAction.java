/*
 * Created on May 18, 2005
 */
package org.embl.ebi.escience.scuflui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ExtensionFileFilter;

/**
 * Outdated action for loading a workflow into a current model.
 * See OpenWorkflowFromFileAction and OpenWorkflowFromURLAction instead.
 * 
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision$
 */
public class LoadWorkflowAction extends ScuflModelAction {
	final JFileChooser fc = new JFileChooser();

	/**
	 * @param model
	 */
	public LoadWorkflowAction(ScuflModel model) {
		super(model);
		putValue(SMALL_ICON, TavernaIcons.openMenuIcon);
		putValue(NAME, "Load");
		putValue(SHORT_DESCRIPTION, "Load a workflow...");
	}

	public void actionPerformed(ActionEvent e) {
		
		JPopupMenu menu = new JPopupMenu("Load");
		JMenuItem fromFile = new JMenuItem("Load from a file");
		fromFile.setIcon(TavernaIcons.openIcon);
		fromFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadFromFile();

			}
		});
		menu.add(fromFile);

		JMenuItem fromWeb = new JMenuItem("Load from the web");
		fromWeb.setIcon(TavernaIcons.openurlIcon);
		fromWeb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadFromWeb();
			}
		});
		menu.add(fromWeb);

		Component sourceComponent = (Component) e.getSource();
		menu.show(sourceComponent, 0, sourceComponent.getHeight());
	}

	/*
	 * Asks for a url and loads a workflow from the xml based at that url
	 */
	protected void loadFromWeb() {
		try {
			String name = (String) JOptionPane.showInputDialog(null,
					"Enter the URL of a workflow definition to load",
					"Workflow URL", JOptionPane.QUESTION_MESSAGE, null, null,
					"http://");
			if (name != null) {
				XScuflParser
						.populate((new URL(name)).openStream(), model, null);
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null,
					"Problem opening workflow from web : \n" + ex.getMessage(),
					"Error!", JOptionPane.ERROR_MESSAGE);
		}
	}

	/*
	 * Prompts for a file and loads a workflow from that file
	 */
	protected void loadFromFile() {
		Preferences prefs = Preferences.userNodeForPackage(TavernaIcons.class);
		String curDir = prefs
				.get("currentDir", System.getProperty("user.home"));
		fc.setDialogTitle("Open Workflow");
		fc.resetChoosableFileFilters();
		fc.setFileFilter(new ExtensionFileFilter(new String[] { "xml" }));
		fc.setCurrentDirectory(new File(curDir));
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			prefs.put("currentDir", fc.getCurrentDirectory().toString());
			final File file = fc.getSelectedFile();
			// mrp Refactored to do the heavy-lifting in a new thread
			new Thread(new Runnable() {
				public void run() {
					try {
						// todo: does the update need running in the AWT thread?
						// perhaps this thread should be spawned in populate?
						XScuflParser.populate(file.toURL().openStream(), model,
								null);						
					} catch (Exception ex) {
						JOptionPane
								.showMessageDialog(
										null,
										"Problem opening workflow from file : \n\n"
												+ ex.getMessage()
												+ "\n\nTo load this workflow try setting offline mode, this will allow you to load and remove any defunct operations.",
										"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}).start();

		}
	}

}
