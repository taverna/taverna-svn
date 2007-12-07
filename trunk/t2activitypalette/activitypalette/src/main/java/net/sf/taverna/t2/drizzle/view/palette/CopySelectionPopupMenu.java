/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.palette;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.sf.taverna.t2.drizzle.model.ActivityRegistrySubsetIdentification;
import net.sf.taverna.t2.drizzle.model.ActivityRegistrySubsetModel;
import net.sf.taverna.t2.drizzle.model.ActivityRegistrySubsetSelectionIdentification;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.util.ObjectMembershipFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 * 
 */
public class CopySelectionPopupMenu extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7649573101214082985L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CopySelectionPopupMenu.class);

	public CopySelectionPopupMenu(final ActivityPalettePanel parentPanel, final Set<ProcessorFactoryAdapter> selectedObjects) {
		super("Copy selection"); //$NON-NLS-1$
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
			ActivityRegistrySubsetIdentification ident = subset.getIdent();
			String subsetName = subset.getName();
			int subsetIndex = parentPanel.getIndexOfTab(subsetName);
			if ((subsetIndex != -1) && (!parentPanel.getCurrentTab().getName().equals(subsetName)) && (ident instanceof ActivityRegistrySubsetSelectionIdentification)) {
				JMenuItem subsetItem = new JMenuItem(subset.getName());
				subsetItem.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent arg0) {
						PropertiedObjectFilter<ProcessorFactoryAdapter> newFilter = new ObjectMembershipFilter<ProcessorFactoryAdapter>(selectedObjects);
						subset.addOredFilter(newFilter);
					}

				});
				this.add(subsetItem);
			}
		}
	}
}
