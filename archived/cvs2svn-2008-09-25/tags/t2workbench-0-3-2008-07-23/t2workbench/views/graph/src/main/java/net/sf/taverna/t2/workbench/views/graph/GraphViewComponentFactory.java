package net.sf.taverna.t2.workbench.views.graph;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

/**
 * 
 * 
 * @author David Withers
 */
public class GraphViewComponentFactory implements UIComponentFactorySPI {

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI#getComponent()
	 */
	public UIComponentSPI getComponent() {
		return new GraphViewComponent();
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI#getIcon()
	 */
	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI#getName()
	 */
	public String getName() {
		return "Graph View";
	}

}
