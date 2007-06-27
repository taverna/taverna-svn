package net.sf.taverna.service.executeremotely.ui;

import java.io.InputStream;

import javax.swing.ImageIcon;

import net.sf.taverna.perspectives.AbstractPerspective;

public class ExecuteRemotelyPerspective extends AbstractPerspective {

	static String NAME = "Execute remotely";
	
	static ImageIcon ICON = new ImageIcon(ExecuteRemotelyFactory.class.getResource("/package_network.png"));
	
	@Override
	public ImageIcon getButtonIcon() {
		return ICON;
	}

	@Override
	protected InputStream getLayoutResourceStream() {
		return getClass().getResourceAsStream("/execute_remotely-perspective.xml");
	}

	@Override
	public String getText() {
		return NAME;
	}
}
