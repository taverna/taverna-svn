package org.embl.ebi.escience.scuflui.graph;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class WorkflowEditorFactory implements UIComponentFactorySPI{

	public UIComponentSPI getComponent() {
		return new WorkflowEditor();
	}

	public ImageIcon getIcon() {
		return TavernaIcons.windowDiagram;
	}

	public String getName() {
		return "Interactive Workflow Editor";
	}

}
