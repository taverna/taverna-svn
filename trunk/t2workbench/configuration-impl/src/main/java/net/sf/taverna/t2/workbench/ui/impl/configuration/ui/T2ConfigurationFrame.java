package net.sf.taverna.t2.workbench.ui.impl.configuration.ui;

import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIRegistry;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class T2ConfigurationFrame extends JFrame {
	
	private static Logger logger = Logger.getLogger(T2ConfigurationFrame.class);
	JSplitPane splitPane;
	
	
	public T2ConfigurationFrame () {
		setLayout(new BorderLayout());
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		add(splitPane,BorderLayout.CENTER);
		JList list = getConfigurationList();
		list.setBorder(new BevelBorder(BevelBorder.LOWERED));
		splitPane.setLeftComponent(list);
		splitPane.setRightComponent(new JPanel());
		splitPane.setDividerLocation(0.4);
		
		//select first item if one exists
		if (list.getModel().getSize()>0) {
			list.setSelectedValue(list.getModel().getElementAt(0), true);
		}
		
		pack();
	}
	
	private JList getConfigurationList() {
		final JList list = new JList();
		DefaultListModel listModel = new DefaultListModel();
		list.setModel(listModel);
		list.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (list.getSelectedValue() instanceof ConfigurableItem) {
					ConfigurableItem item = (ConfigurableItem)list.getSelectedValue();
					setMainPanel(item.getPanel());
				}
			}
		});
		
		for (ConfigurationUIFactory fac : ConfigurationUIRegistry.getInstance().getConfigurationUIFactories()) {
			String name=fac.getConfigurable().getName();
			if (name!=null) {
				logger.info("Adding configurable for name:"+name);
				listModel.addElement(new ConfigurableItem(fac));
			}
			else {
				logger.warn("The configurable "+fac.getConfigurable().getClass()+" has a null name");
			}
		}
		
		return list;
	}
	
	private void setMainPanel(JPanel panel) {
		splitPane.setRightComponent(panel);
	}
	
	class ConfigurableItem {
		

		private final ConfigurationUIFactory factory;

		public ConfigurableItem(ConfigurationUIFactory factory) {
			this.factory = factory;
			
		}

		public JPanel getPanel() {
			return factory.getConfigurationPanel();
		}
		
		@Override
		public String toString() {
			return factory.getConfigurable().getName();
		}
		
	}
}
