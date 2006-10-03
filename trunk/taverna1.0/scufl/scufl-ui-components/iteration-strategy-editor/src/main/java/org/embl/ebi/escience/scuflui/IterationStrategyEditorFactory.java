package org.embl.ebi.escience.scuflui;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class IterationStrategyEditorFactory implements UIComponentFactorySPI {

	public String getName() {
		return "Iteration strategy editor";
	}

	public ImageIcon getIcon() {
		return IterationStrategyEditor.baclavaIteratorIcon;
	}

	public UIComponentSPI getComponent() {
		return new IterationStrategyEditor();
	}

}
