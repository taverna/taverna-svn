/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.palette;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import net.sf.taverna.t2.drizzle.model.SubsetKindConfiguration;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.view.subset.ActivitySubsetPanel;

/**
 * @author alanrw
 * 
 */
public final class SubsetKindConfigurationDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -818854898146357591L;

	/**
	 * @param config
	 * @param subsetPanel
	 */
	public SubsetKindConfigurationDialog(final SubsetKindConfiguration config, final ActivitySubsetPanel subsetPanel) {
		this.setLayout(new BorderLayout());

		JPanel choices = new JPanel();
		choices.setLayout(new BorderLayout());
		choices.add(createCheckBoxAndListPanel("tree", config.getKeyList(), config.getTreeListModel()), BorderLayout.NORTH); //$NON-NLS-1$
		choices.add(createCheckBoxAndListPanel("tree table", config.getKeyList(), config.getTreeTableListModel()), BorderLayout.CENTER); //$NON-NLS-1$
		choices.add(createCheckBoxAndListPanel("table", config.getKeyList(), config.getTableListModel()), BorderLayout.SOUTH); //$NON-NLS-1$
		
		JScrollPane choicesScroll = new JScrollPane(choices);
		Dimension fullDimension = Toolkit.getDefaultToolkit().getScreenSize();
		choicesScroll.setMaximumSize(new Dimension(600,(int) (fullDimension.getHeight() * 2 / 3)));
		this.add(choicesScroll, BorderLayout.NORTH);
		JPanel buttonPanel = new JPanel();
		JButton finishButton = new JButton("Finish"); //$NON-NLS-1$
		finishButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				config.setLastChange(System.currentTimeMillis());
				subsetPanel.setModels();
				SubsetKindConfigurationDialog.this.dispose();
			}

		});
		buttonPanel.add(finishButton);
		this.add(buttonPanel, BorderLayout.SOUTH);

		this.pack();
	}
	
	private JPanel createCheckBoxAndListPanel(String name,
			List<PropertyKey> keyList,
			final DefaultListModel listModel) {
		JPanel result = new JPanel();
		result.setLayout(new BorderLayout());
		result.add(new JLabel(name), BorderLayout.NORTH);
		JPanel doublePanel = new JPanel();
		doublePanel.setLayout(new GridLayout(1,2));
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new GridLayout(0,1));
		for (final PropertyKey pk : keyList) {
			final JCheckBox checkBox = new JCheckBox(pk.toString());
			if (listModel.contains(pk)) {
				checkBox.setSelected(true);
			}
				checkBox.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent arg0) {
					if (checkBox.isSelected() && !listModel.contains(pk)) {
						listModel.addElement(pk);
					} else if (listModel.contains(pk)){
						listModel.removeElement(pk);
					}
				}
				
			});
		leftPanel.add(checkBox);
		}
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		JList shownKeyList = new KindKeyList (listModel);
		shownKeyList.setDragEnabled(true);
		JScrollPane scrollPane = new JScrollPane(shownKeyList);
		scrollPane.setViewportView(shownKeyList);
		rightPanel.add(scrollPane);
		doublePanel.add(leftPanel);
		doublePanel.add(rightPanel);
		result.add(doublePanel, BorderLayout.CENTER);
		result.add(new JSeparator(), BorderLayout.SOUTH);
		return result;
	}
}
