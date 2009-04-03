package net.sf.taverna.t2.provenance.connector.configview;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workbench.provenance.ProvenanceConfiguration;

public class MySQLConfigAction extends AbstractAction{

	private final MySQLConfigView mySQLConfigView;

	public MySQLConfigAction(MySQLConfigView mySQLConfigView) {
		super("MySQL Provenance Config Action");
		this.mySQLConfigView = mySQLConfigView;		
	}

	public void actionPerformed(ActionEvent e) {
		
//		ProvenanceConfiguration.getInstance().setProperty("dbName", mySQLConfigView.getDbLocationText());
		ProvenanceConfiguration.getInstance().setProperty("dbUser", mySQLConfigView.getUserText());
		ProvenanceConfiguration.getInstance().setProperty("dbPassword", mySQLConfigView.getPasswordText());
		ProvenanceConfiguration.getInstance().setProperty("dbURL", mySQLConfigView.getDBURL());
	}

}
