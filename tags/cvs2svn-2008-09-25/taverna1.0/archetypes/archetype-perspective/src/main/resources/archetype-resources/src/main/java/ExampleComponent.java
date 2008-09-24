package ${packageName};

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

public class ExampleComponent extends JPanel implements WorkflowModelViewSPI {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(ExampleComponent.class);

	private ScuflModel model;
	
	public ExampleComponent() {
		add(new JButton("Example"));
	}
	
	public void attachToModel(ScuflModel model) {
		this.model = model;
	}

	public void detachFromModel() {

	}

	public ImageIcon getIcon() {
		return null;
	}

	public void onDisplay() {

	}

	public void onDispose() {

	}

}
