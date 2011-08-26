package org.embl.ebi.escience.scuflui;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class WorkflowInstanceContainerFactory implements UIComponentFactorySPI {

	public String getName() {
		return "Workflow instance container";
	}

	public ImageIcon getIcon() {
		return null;
	}

	public UIComponentSPI getComponent() {
		return new WorkflowInstanceContainer();
	}

}
