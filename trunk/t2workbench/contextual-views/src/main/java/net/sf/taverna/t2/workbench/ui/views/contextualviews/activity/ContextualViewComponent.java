package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class ContextualViewComponent extends JPanel implements UIComponentSPI{
	
	public ContextualViewComponent() {
		initialise();
	}

	private void initialise() {
		add(new JLabel("this is a contextual view!"));
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Contextual View";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
		
	}

	public void onDispose() {
		// TODO Auto-generated method stub
		
	}

}
