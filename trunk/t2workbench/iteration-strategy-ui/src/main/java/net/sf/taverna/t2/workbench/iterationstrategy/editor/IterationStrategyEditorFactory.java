package net.sf.taverna.t2.workbench.iterationstrategy.editor;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class IterationStrategyEditorFactory implements UIComponentFactorySPI {

	public String getName() {
		return "Iteration strategy editor";
	}

	public ImageIcon getIcon() {
		return IterationStrategyEditor.leafnodeicon;
	}

	public UIComponentSPI getComponent() {
		return new IterationStrategyEditor();
	}

}
