package net.sf.taverna.t2.workbench.ui.impl;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.zaria.ZBasePane;

/**
 * The default ZBasePane used within the Taverna Workbench
 * 
 * @author Stuart Owen
 * @author Stian Soiland-Reyes
 */
@SuppressWarnings("serial")
public class WorkbenchZBasePane extends ZBasePane {

	public WorkbenchZBasePane() {
		super();
		setKnownSPINames(new String[] { UIComponentFactorySPI.class
				.getCanonicalName() });
	}

	public JMenuItem getMenuItem(Class theClass) {
		try {
			UIComponentFactorySPI factory = (UIComponentFactorySPI) theClass
					.newInstance();
			Icon icon = factory.getIcon();
			if (icon != null) {
				return new JMenuItem(factory.getName(), factory.getIcon());
			} else {
				return new JMenuItem(factory.getName());
			}
		} catch (InstantiationException e) {
			return new JMenuItem("Instantiation exception!");
		} catch (IllegalAccessException e) {
			return new JMenuItem("Illegal access exception!");
		}
	}

	@Override
	public JComponent getComponent(Class theClass) {
		UIComponentFactorySPI factory;
		try {
			factory = (UIComponentFactorySPI) theClass.newInstance();
			return (JComponent) factory.getComponent();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return new JPanel();
	}

	public void discard() {

	}

}
