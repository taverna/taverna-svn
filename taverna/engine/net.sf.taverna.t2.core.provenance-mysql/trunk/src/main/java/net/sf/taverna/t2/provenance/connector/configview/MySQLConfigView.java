package net.sf.taverna.t2.provenance.connector.configview;

import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.workbench.provenance.ProvenanceConfiguration;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

public class MySQLConfigView extends ContextualView{
	
	public MySQLConfigView(){
		initView();
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new MySQLConfigAction(this);
	}

	private JPanel panel;
	private JTextField userText;
	private JTextField passwordText;
	private JTextField dbNameText;
	private JTextField dbURL;

	@Override
	public JComponent getMainFrame() {
		refreshView();
		return panel;
	}

	@Override
	public String getViewTitle() {
		return "My SQL Config View";
	}

	@Override
	public void refreshView() {
		panel = new JPanel();
		String dbName = ProvenanceConfiguration.getInstance().getProperty("dbName");
		String user = ProvenanceConfiguration.getInstance().getProperty("dbUser");
		String password = ProvenanceConfiguration.getInstance().getProperty("dbPassword");
		String url = ProvenanceConfiguration.getInstance().getProperty("dbURL");
		
		JLabel userLabel = new JLabel("Name");
		userText = new JTextField();
		if (user != null) {
			userText.setText(user);
		}
		
		userText.setToolTipText("The user name used to access the Provenance database, must have full read and write priveleges");

		JLabel passwordLabel = new JLabel("Password");
		passwordText = new JTextField();
		if (password != null) {
			passwordText.setText(password);
		}
		
		passwordText.setToolTipText("The password for the user name you access the database with");

		JLabel dbLocationLabel = new JLabel("Database Name");
		dbNameText = new JTextField();
		if (dbName != null) {
			dbNameText.setText(dbName);
		}
		dbNameText.setToolTipText("The name of the database you want the provenance data stored in");
		
		JLabel dbURLLabel = new JLabel("Database URL");
		dbURL = new JTextField();
		if (url != null) {
			dbURL.setText(url);
		}
		
		dbURL.setToolTipText("Where the MySQL database is located eg. localhost or an actual URL (no http:// is required)");
		

		JPanel dbChoicesPanel = new JPanel();
		panel.add(dbChoicesPanel);
		GridLayout gridLayout = new GridLayout(0,1);
		dbChoicesPanel.setLayout(gridLayout);

		dbChoicesPanel.add(userLabel);
		dbChoicesPanel.add(userText);
		dbChoicesPanel.add(passwordLabel);
		dbChoicesPanel.add(passwordText);
//		dbChoicesPanel.add(dbLocationLabel);
//		dbChoicesPanel.add(dbNameText);
		dbChoicesPanel.add(dbURLLabel);
		dbChoicesPanel.add(dbURL);
	
		
	}

	
	public String getUserText() {
		return userText.getText();
	}

	

	public String getPasswordText() {
		return passwordText.getText();
	}

	

	public String getDbLocationText() {
		return dbNameText.getText();
	}
	
	public String getDBURL() {
		return dbURL.getText();
	}

}
