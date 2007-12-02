/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;
import org.embl.ebi.escience.scuflworkers.ScavengerHelperRegistry;
import org.embl.ebi.escience.scuflworkers.web.WebScavengerHelper;

/**
 * @author alanrw
 * 
 */
public class ScavengerAdderPopupMenu extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7610314984575558130L;

	private static Logger logger = Logger
			.getLogger(ScavengerAdderPopupMenu.class);

	ActivityPalettePanel parentPanel = null;

	public ScavengerAdderPopupMenu(ActivityPalettePanel parentPanel) {
		super("Add scavenger"); //$NON-NLS-1$
		if (parentPanel == null) {
			throw new NullPointerException("parentPanel cannot be null"); //$NON-NLS-1$
		}
		this.parentPanel = parentPanel;
		
		ScavengerHelper webScavengerHelper = null;
		// Iterate over the scavenger creator list from the
		// ProcessorHelper class

		List<ScavengerHelper> scavengerHelpers = ScavengerHelperRegistry
				.instance().getScavengerHelpers();

		// sort alphabetically
		Collections.sort(scavengerHelpers, new Comparator<ScavengerHelper>() {
			public int compare(ScavengerHelper o1, ScavengerHelper o2) {
				if (o1.getScavengerDescription() == null)
					return -1;
				if (o2.getScavengerDescription() == null)
					return 1;
				return o1.getScavengerDescription().compareTo(
						o2.getScavengerDescription());
			}
		});

		for (ScavengerHelper scavengerHelper : scavengerHelpers) {
			// Instantiate a ScavengerHelper...
			try {
				// webscavenger helper is added after the
				// seperator
				if (scavengerHelper instanceof WebScavengerHelper) {
					webScavengerHelper = scavengerHelper;
				} else {
					addScavengerHelperToMenu(this, scavengerHelper);
				}
			} catch (Exception ex) {
				logger.error("Exception adding scavenger helper to scavenger tree"); //$NON-NLS-1$
			}
		}
		// if (!parentPanel.getPaletteModel().isPopulating()) {
		// TODO
		this.addSeparator();

		if (webScavengerHelper != null) {
			addScavengerHelperToMenu(this, webScavengerHelper);
		}

		/*
		JMenuItem collect = new JMenuItem("Collect scavengers from model", //$NON-NLS-1$
				TavernaIcons.importIcon);
		this.add(collect);
		collect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					ScavengerAdderPopupMenu.this.parentPanel.getPaletteModel()
							.addScavengersFromModel();
				} catch (ScavengerCreationException sce) {
					JOptionPane
							.showMessageDialog(null,
									"Unable to import scavengers!\n" //$NON-NLS-1$
											+ sce.getMessage(), "Exception!", //$NON-NLS-1$
									JOptionPane.ERROR_MESSAGE);
				}
			}
		}); */
	}

	/**
	 * Adapted from ScavengerTreePopupHandler
	 */
	private void addScavengerHelperToMenu(JPopupMenu menu,
			ScavengerHelper scavengerHelper) {
		if (menu == null) {
			throw new NullPointerException("menu cannot be null"); //$NON-NLS-1$
		}
		if (scavengerHelper == null) {
			throw new NullPointerException("scavengerHelper cannot be null"); //$NON-NLS-1$
		}
		String scavengerDescription = scavengerHelper.getScavengerDescription();
		if (scavengerDescription != null) {
			JMenuItem scavengerMenuItem = new JMenuItem(scavengerDescription,
					scavengerHelper.getIcon());
			scavengerMenuItem.addActionListener(scavengerHelper
					.getListener(this.parentPanel.getPaletteModel().getAdapter()));
			menu.add(scavengerMenuItem);
		}
	}

}
