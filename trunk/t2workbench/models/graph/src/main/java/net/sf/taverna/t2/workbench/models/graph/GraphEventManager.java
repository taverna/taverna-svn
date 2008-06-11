package net.sf.taverna.t2.workbench.models.graph;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;

public class GraphEventManager {

	private DataflowSelectionModel dataflowSelectionModel;

	public GraphEventManager(DataflowSelectionModel dataflowSelectionModel) {
		this.dataflowSelectionModel = dataflowSelectionModel;
	}

	public void mouseClicked(final GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y) {
		if (button == 0) {
			dataflowSelectionModel.addSelection(graphElement.getDataflowObject());
		} else if (button == 2) {
//			JOptionPane.showMessageDialog(null, "Right click");
			final JPopupMenu menu = new JPopupMenu(graphElement.getLabel());
			if (graphElement.isSelected()) {
				menu.add(new AbstractAction("Deselect") {

					public void actionPerformed(ActionEvent arg0) {
						dataflowSelectionModel.removeSelection(graphElement.getDataflowObject());
						menu.setVisible(false);
					}

				});
			} else {
				menu.add(new AbstractAction("Select") {

					public void actionPerformed(ActionEvent arg0) {
						dataflowSelectionModel.addSelection(graphElement.getDataflowObject());
						menu.setVisible(false);
					}

				});
			}
			menu.setLocation(x, y);
			menu.setVisible(true);
		}
	}

}
