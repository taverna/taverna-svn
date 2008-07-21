package net.sf.taverna.t2.workbench.ui.activitypalette;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class ActivityPaletteConfigurationPanel extends JPanel {

	private static Logger logger = Logger
			.getLogger(ActivityPaletteConfigurationPanel.class);
	
	private Map<String,List<String>> values = new HashMap<String, List<String>>();
	private DefaultComboBoxModel model;
	private DefaultListModel listModel;
	private JList propertyListItems;
	private String selectedKey;
	
	
	public ActivityPaletteConfigurationPanel() {
		super();
		
		Configurable config = ActivityPaletteConfiguration.getInstance();
		setLayout(new BorderLayout());
		
		model = new DefaultComboBoxModel();
		for (String key : config.getPropertyMap().keySet()) {
			if (key.startsWith("taverna.")) {
				if (config.getProperty(key)!=null) {
					model.addElement(key);
					values.put(key,(List<String>)config.getProperty(key));
				}
			}
		}
		
		final JComboBox comboBox = new JComboBox(model);
		comboBox.setRenderer(new DefaultListCellRenderer() {

			
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				if (value!=null && value instanceof String) {
					String name = ActivityPaletteConfiguration.getInstance().getPropertyName((String)value);
					if (name!=null) {
						value=name;
					}
				}
				return super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
			}
			
		});
		
		comboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (comboBox.getSelectedItem()!=null && comboBox.getSelectedItem() instanceof String) {
					selectedKey = (String)comboBox.getSelectedItem();
					List<String> selectedList = values.get(selectedKey);
					populateList(selectedList);
				}
			}
			
		});
		
		JPanel propertySelectionPanel = new JPanel();
		propertySelectionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		propertySelectionPanel.add(new JLabel("Activity type:"));
		propertySelectionPanel.add(comboBox);
		add(propertySelectionPanel,BorderLayout.NORTH);
		
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BorderLayout());
		listModel=new DefaultListModel();
		propertyListItems = new JList(listModel);
		propertyListItems.setBorder(new BevelBorder(BevelBorder.LOWERED));
		
		
		listPanel.add(propertyListItems,BorderLayout.CENTER);
		listPanel.add(listButtons(),BorderLayout.EAST);
		
		add(listPanel,BorderLayout.CENTER);
		
		add(applyButtonPanel(),BorderLayout.SOUTH);
		
		if (model.getSize()>0) {
			comboBox.setSelectedItem(model.getElementAt(0));
		}
	}
	

	private void populateList(List<String> selectedList) {
		listModel.removeAllElements();
		for (String item : selectedList) {
			listModel.addElement(item);
		}
	}
	
	private JPanel applyButtonPanel() {
		JPanel applyPanel = new JPanel();
		applyPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton applyButton = new JButton("Apply");
		
		applyButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Configurable config = ActivityPaletteConfiguration.getInstance();
				for (String key : values.keySet()) {
					List<String> properties = values.get(key);
					config.setProperty(key, properties);
				}
				try {
					ConfigurationManager.getInstance().store(config);
				} catch (Exception e1) {
					logger.error("There was an error storing the configuration:"+config.getName()+"(UUID="+config.getUUID()+")",e1);
				}
			}
			
		});
		
		applyPanel.add(applyButton);
		return applyPanel;
	}
	
	private JPanel listButtons() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		JButton addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String value = JOptionPane.showInputDialog(ActivityPaletteConfigurationPanel.this,"Provide new value for:"+selectedKey,"New property",JOptionPane.INFORMATION_MESSAGE);
				if (value!=null) {
					listModel.addElement(value);
					values.get(selectedKey).add(value);
				}
			}
			
		});
		
		JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Object value = propertyListItems.getSelectedValue();
				int ret=JOptionPane.showConfirmDialog(ActivityPaletteConfigurationPanel.this,"Are you sure you wish to remove "+value+" ?","Confirm removal",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
				if (ret==JOptionPane.YES_OPTION) {
					listModel.removeElement(value);
					values.get(selectedKey).remove(value);
				}
				
			}
			
		});
		
		panel.add(addButton);
		panel.add(deleteButton);
		
		return panel;
	}
	
	private JButton getAddTypeButton() {
		JButton result = new JButton("Add");
		result.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String val = JOptionPane.showInputDialog(ActivityPaletteConfigurationPanel.this, "New property value");
				if (val!=null) {
					if (values.get(val)==null) {
						model.addElement(val);
						values.put(val,new ArrayList<String>());
					}
					else {
						JOptionPane.showMessageDialog(ActivityPaletteConfigurationPanel.this, "This property already exists");
					}
				}
			}
			
		});
		return result;
	}

}
