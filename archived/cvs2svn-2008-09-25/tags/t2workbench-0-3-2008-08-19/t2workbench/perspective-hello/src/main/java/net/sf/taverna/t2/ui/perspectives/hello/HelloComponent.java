/**
 * 
 */
package net.sf.taverna.t2.ui.perspectives.hello;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

final class HelloComponent extends JPanel implements UIComponentSPI {
	
	public HelloComponent() {
		add(new ShadedLabel("Hello there", ShadedLabel.GREEN), BorderLayout.NORTH);
		add(new JLabel("<html>Welcome to the Taverna 2<br>" + 
				"workbench.</html>"), BorderLayout.CENTER);
	}
	
	public ImageIcon getIcon() {
		return WorkbenchIcons.databaseIcon;
	}

	@Override
	public String getName() {
		return "Another name";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
		
	}

	public void onDispose() {
		// TODO Auto-generated method stub
		
	}
}