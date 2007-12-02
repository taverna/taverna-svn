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

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ShowSubsetPopupMenu extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7649573101214082985L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ShowSubsetPopupMenu.class);

	public ShowSubsetPopupMenu(final ActivityPalettePanel parentPanel) {
		super("Add subset"); //$NON-NLS-1$
		if (parentPanel == null) {
			throw new NullPointerException("parentPanel cannot be null"); //$NON-NLS-1$
		}
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
