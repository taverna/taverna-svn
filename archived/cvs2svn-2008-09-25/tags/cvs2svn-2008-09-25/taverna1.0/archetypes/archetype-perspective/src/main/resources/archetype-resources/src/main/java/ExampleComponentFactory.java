package ${packageName};

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class ExampleComponentFactory implements UIComponentFactorySPI {

	public UIComponentSPI getComponent() {
		return new ExampleComponent();
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getName() {
		return "Example";
	}

}
