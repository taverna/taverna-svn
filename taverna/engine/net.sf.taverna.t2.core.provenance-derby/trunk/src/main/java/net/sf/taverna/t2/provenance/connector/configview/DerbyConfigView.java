package net.sf.taverna.t2.provenance.connector.configview;

import java.awt.Frame;
import java.io.File;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTextArea;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

public class DerbyConfigView extends ContextualView {

	public DerbyConfigView() {
		initView();
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new DerbyConfigAction(this);
	}

	@Override
	public JComponent getMainFrame() {
		JTextArea label = new JTextArea();
		File applicationHomeDir = ApplicationRuntime.getInstance()
				.getApplicationHomeDir();
		File file = new File(applicationHomeDir, "db");
		label.setText("Provenance will be stored in: \n" + file.toString());
		label.setEditable(false);
		return label;
	}

	@Override
	public String getViewTitle() {
		// TODO Auto-generated method stub
		return "Derby provenance config view";
	}

	@Override
	public void refreshView() {
		// TODO Auto-generated method stub

	}

	public String getDBURL() {

		File applicationHomeDir = ApplicationRuntime.getInstance()
				.getApplicationHomeDir();
		File dbFile = new File(applicationHomeDir, "db");
		String jdbcString = "jdbc:derby:" + dbFile.toString()
				+ ";create=true;upgrade=true";
		return jdbcString;
	}

}
