package net.sf.taverna.t2.workbench.ui.activitypalette;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;

import net.sf.taverna.t2.partition.PartitionAlgorithm;
import net.sf.taverna.t2.partition.PartitionAlgorithmSetSPI;
import net.sf.taverna.t2.partition.PartitionAlgorithmSetSPIRegistry;
import net.sf.taverna.t2.partition.PropertyExtractorRegistry;
import net.sf.taverna.t2.partition.RootPartition;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class ActivityPaletteComponent extends JPanel implements UIComponentSPI {

	public ActivityPaletteComponent() {
		setLayout(new GridBagLayout());
		GridBagConstraints panelConstraint = new GridBagConstraints();
		panelConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		panelConstraint.gridx = 0;
		panelConstraint.gridy = 0;
		panelConstraint.weightx = 0;
		panelConstraint.weighty = 0;
		panelConstraint.fill = GridBagConstraints.BOTH;
		add(initialise(), panelConstraint);
		JPanel fillerPanel = new JPanel();
		panelConstraint.gridx = 1;
		panelConstraint.gridy = 0;
		panelConstraint.weightx = 0.1;
		panelConstraint.weighty = 0.1;
		add(fillerPanel, panelConstraint);
	}

	private Component initialise() {

		PropertyExtractorRegistry propertyExtractorRegistry = getPropertyExtractorRegistry();

		RootPartition<?> partition = getPartition(propertyExtractorRegistry);
		
		// TODO need to get the activities from something and their partitions
		JTree activityTree = new ActivityTree(partition);
		
		return activityTree;
	}
	
	private JMenu getAlgorithmMenu() {
		JPopupMenu algorithmPopup = new JPopupMenu();
		for (PartitionAlgorithm<?> algorithm:getAlgorithms()) {
			JMenuItem item = new JMenuItem();
			item.addActionListener(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					
				}
				
			});
		}
		return null;
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Activity Palette";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

	private PropertyExtractorRegistry getPropertyExtractorRegistry() {
		
		return new PropertyExtractorRegistry() {

			public Map<String, Object> getAllPropertiesFor(Object target) {
				Map<String, Object> result = new HashMap<String, Object>();
//				if (target instanceof ActivityItem) {
//					ActivityItem ai = (ActivityItem) target;
//					result.put("type", ai.getType());
//					result.put("name", ai.getName());
//				}
//				if (target instanceof WSDLActivityItem) {
//					WSDLActivityItem ai = (WSDLActivityItem) target;
//					result.put("operation", ai.getOperation());
//				}
//				if (target instanceof SoaplabActivityItem) {
//					SoaplabActivityItem ai = (SoaplabActivityItem) target;
//					result.put("category", ai.getCategory());
//					result.put("operation", ai.getOperation());
//				}
//				if (target instanceof LocalWorkerActivityItem) {
//					LocalWorkerActivityItem ai = (LocalWorkerActivityItem) target;
//					result.put("category", ai.getCategory());
//				}
//
				return result;
			}

		};
	}

	private <ActivityItem> RootPartition<?> getPartition(PropertyExtractorRegistry reg) {
		return new RootPartition<ActivityItem>(getAlgorithms(), reg);
	}

	/**
	 * Loop thorugh all the available {@link PartitionAlgorithm}s and create a
	 * master set
	 * 
	 * @return A union of all the {@link PartitionAlgorithm} sets
	 */
	private List<PartitionAlgorithm<?>> getAlgorithms() {
		List<PartitionAlgorithmSetSPI> instances = PartitionAlgorithmSetSPIRegistry
				.getInstance().getInstances();
		Set<PartitionAlgorithm<?>> partitionAlgorithmSet = new HashSet<PartitionAlgorithm<?>>();
		for (PartitionAlgorithmSetSPI instance : instances) {
			Set<PartitionAlgorithm<?>> partitonAlgorithms = instance
					.getPartitionAlgorithms();
			partitionAlgorithmSet.addAll(partitonAlgorithms);
		}
		List<PartitionAlgorithm<?>> partitionAlgorithmList = new ArrayList<PartitionAlgorithm<?>>();
		for (PartitionAlgorithm<?> algorithm:partitionAlgorithmSet) {
			partitionAlgorithmList.add(algorithm);
		}
		return partitionAlgorithmList;
	}

}
