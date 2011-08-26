package net.sf.taverna.t2.workbench.views.graph;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class GraphViewComponentFactory implements UIComponentFactorySPI {

	public UIComponentSPI getComponent() {
		return new GraphViewComponent();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Graph View";
	}

}
