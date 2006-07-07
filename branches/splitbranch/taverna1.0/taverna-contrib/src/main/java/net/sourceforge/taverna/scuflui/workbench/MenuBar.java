package net.sourceforge.taverna.scuflui.workbench;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import net.sourceforge.taverna.publish.actions.DeleteRepositoryAction;
import net.sourceforge.taverna.publish.actions.NewRepositoryAction;
import net.sourceforge.taverna.publish.actions.UpdateRepositoryAction;
import net.sourceforge.taverna.scuflui.actions.AboutAction;
import net.sourceforge.taverna.scuflui.actions.ClearWorkflowAction;
import net.sourceforge.taverna.scuflui.actions.CopyAction;
import net.sourceforge.taverna.scuflui.actions.CutAction;
import net.sourceforge.taverna.scuflui.actions.ExplorerAction;
import net.sourceforge.taverna.scuflui.actions.GenerateWorkflowDocAction;
import net.sourceforge.taverna.scuflui.actions.HelpAction;
import net.sourceforge.taverna.scuflui.actions.NewSubWorkflowAction;
import net.sourceforge.taverna.scuflui.actions.NewWorkflowAction;
import net.sourceforge.taverna.scuflui.actions.OpenWorkflowAction;
import net.sourceforge.taverna.scuflui.actions.OpenWorkflowFromWebAction;
import net.sourceforge.taverna.scuflui.actions.PasteAction;
import net.sourceforge.taverna.scuflui.actions.QuitAction;
import net.sourceforge.taverna.scuflui.actions.RedoAction;
import net.sourceforge.taverna.scuflui.actions.RunWorkflowAction;
import net.sourceforge.taverna.scuflui.actions.SaveWorkflowAction;
import net.sourceforge.taverna.scuflui.actions.ServicesAction;
import net.sourceforge.taverna.scuflui.actions.UndoAction;
import net.sourceforge.taverna.scuflui.actions.WorkflowHelpAction;
import net.sourceforge.taverna.scuflui.actions.WorkflowViewAction;

import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import org.embl.ebi.escience.scuflui.UIComponentRegistry;
import org.embl.ebi.escience.scuflui.UIUtils;

/**
 * This class
 * 
 * File New Workflow New Subworkflow Open Workflow - resets the current workflow
 * prior to opening an existing workflow. Open Workflow From Web Open Document -
 * would open a document that you had saved earlier, such as a FASTA file or
 * alignment.
 * 
 * Import Workflow Import Subworkflow Import Service Export Diagram... Save
 * Workflow Save Workflow As... Save Diagram As...
 * 
 * Print Diagram Exit
 * 
 * Edit Cut Copy Paste Undo Redo
 * 
 * Options... - would allow you to specify defaults that are currently only
 * settable in properties file, and would allow you to specify a default
 * workflow directory.
 * 
 * Workflow Run workflow Debug workflow - opens the debug windows prior to
 * running the workflow.
 * 
 * View Advanced Model Explorer Available Services Workflow Diagram
 * 
 * Help About Taverna Help Contents
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 */
public class MenuBar extends JMenuBar {

	public MenuBar() {
		this.add(createFileMenu());
		this.add(createEditMenu());
		this.add(createWorkflowMenu());
		this.add(createViewMenu());
		this.add(createRepositoryMenu());
		this.add(createServicesMenu());
		this.add(createHelpMenu());
	}

	/**
	 * This method creates the workflow menu.
	 * 
	 * @return
	 */
	private JMenu createWorkflowMenu() {
		JMenu menu = new JMenu("Workflow");
		menu.add(new JMenuItem(new GenerateWorkflowDocAction()));
		menu.add(new JMenuItem(new RunWorkflowAction()));
		return menu;
	}

	/**
	 * This method creates the file menu
	 * 
	 * @return
	 */
	private JMenu createFileMenu() {
		JMenu menu = new JMenu("File");

		menu.add(new JMenuItem(new NewWorkflowAction()));
		menu.add(new JMenuItem(new NewSubWorkflowAction()));
		menu.add(new JMenuItem(new OpenWorkflowAction()));
		menu.add(new JMenuItem(new OpenWorkflowFromWebAction()));
		menu.add(new JMenuItem(new SaveWorkflowAction()));
		menu.addSeparator();
		menu.add(new JMenuItem(new ClearWorkflowAction()));
		menu.add(new JSeparator());
		menu.add(new JMenuItem(new QuitAction()));

		return menu;
	}

	/**
	 * This menu creates the edit menu.
	 * 
	 * @return
	 */
	private JMenu createEditMenu() {
		JMenu menu = new JMenu("Edit");
		menu.add(new JMenuItem(new CutAction()));
		menu.add(new JMenuItem(new CopyAction()));
		menu.add(new JMenuItem(new PasteAction()));

		menu.addSeparator();
		menu.add(new JMenuItem(new UndoAction()));
		menu.add(new JMenuItem(new RedoAction()));
		// menu.addSeparator();
		// menu.add(new JMenuItem(new PreferencesAction()));
		return menu;

	}

	/**
	 * This method creates the view menu.
	 * 
	 * @return
	 */
	private JMenu createViewMenu() {
		JMenu menu = new JMenu("View");
		menu.add(new JMenuItem(new ExplorerAction()));
		menu.add(new JMenuItem(new ServicesAction()));
		menu.add(new JMenuItem(new WorkflowViewAction()));
		return menu;

	}

	private JMenu createPublishMenu() {
		JMenu menu = new JMenu("Publish");
		menu.add(new JMenuItem());

		return menu;
	}

	private JMenu createServicesMenu() {
		JMenu menu = new JMenu("Services");
		UIComponentRegistry registry = UIComponentRegistry.instance();
		for (Iterator i = registry.getComponents().keySet().iterator(); i.hasNext();) {
			final String itemName = (String) i.next();
			try {
				final Class itemClass = Class.forName((String) registry.getComponents().get(itemName));
				final ImageIcon itemIcon = (ImageIcon) registry.getIcons().get(itemName);
				JMenuItem menuItem = null;
				if (itemIcon == null) {
					menuItem = new JMenuItem(itemName);
				} else {
					menuItem = new JMenuItem(itemName, itemIcon);
				}
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						try {
							ScuflUIComponent thing = (ScuflUIComponent) itemClass.newInstance();
							UIUtils.createFrame(Workbench.model, thing, 100, 100, 400, 400);

						} catch (InstantiationException ie) {
							//
						} catch (IllegalAccessException iae) {
							//
						}
					}
				});
				menu.add(menuItem);
			} catch (Exception ex) {
				//
			}
		}

		return menu;
	}

	/**
	 * This method creates the help menu.
	 * 
	 * @return
	 */
	private JMenu createHelpMenu() {
		JMenu menu = new JMenu("Help");
		menu.add(new JMenuItem(new AboutAction()));
		menu.add(new JMenuItem(new HelpAction()));
		menu.add(new JMenuItem(new WorkflowHelpAction()));
		return menu;

	}

	private JMenu createRepositoryMenu() {
		JMenu menu = new JMenu("Repository");
		menu.add(new JMenuItem(new NewRepositoryAction()));
		menu.add(new JMenuItem(new UpdateRepositoryAction()));
		menu.add(new JMenuItem(new DeleteRepositoryAction()));
		return menu;
	}

}
