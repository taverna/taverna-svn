package org.myexp_whip_plugin.ui;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

import org.myexp_whip_plugin.ui.MainComponent;

public class MainComponent extends JPanel implements WorkflowModelViewSPI {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(MainComponent.class);

	private ScuflModel model;
	
	public MainComponent() {
		//add(new JButton("Example"));
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
