/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;
import org.embl.ebi.escience.scuflworkers.ScavengerHelperRegistry;
import org.embl.ebi.escience.scuflworkers.web.WebScavengerHelper;

/**
 * @author alanrw
 * 
 */
public class ShowSubsetPopupMenu extends JPopupMenu {

	private static Logger logger = Logger.getLogger(ShowSubsetPopupMenu.class);

	private ActivityPalettePanel parentPanel = null;

	public ShowSubsetPopupMenu(final ActivityPalettePanel parentPanel) {
		super("Add subset");
		this.parentPanel = parentPanel;

		List<ActivityRegistrySubsetModel> subsets = new ArrayList<ActivityRegistrySubsetModel>(
				parentPanel.getPaletteModel().getSubsetModels());

		Collections.sort(subsets,
				new Comparator<ActivityRegistrySubsetModel>() {
					public int compare(ActivityRegistrySubsetModel arg0,
							ActivityRegistrySubsetModel arg1) {
						return (arg0.getName().compareTo(arg1.getName()));
					}

				});

		for (final ActivityRegistrySubsetModel subset : subsets) {
			if (parentPanel.getIndexOfTab(subset.getName()) == -1) {
				JMenuItem subsetItem = new JMenuItem(subset.getName());
				subsetItem.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent arg0) {
						parentPanel.subsetModelAdded(null, subset);
					}

				});
				this.add(subsetItem);
			}
		}
	}
}
