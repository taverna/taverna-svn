package net.sf.taverna.t2.provenance.connector.configview;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workbench.provenance.ProvenanceConfiguration;

public class DerbyConfigAction extends AbstractAction {

	private final DerbyConfigView derbyConfigView;

	public DerbyConfigAction(DerbyConfigView derbyConfigView) {
		super("Derby provenance config action");
		this.derbyConfigView = derbyConfigView;
	}

	public void actionPerformed(ActionEvent e) {
		ProvenanceConfiguration.getInstance().setProperty("dbURL", derbyConfigView.getDBURL());
	}

}
