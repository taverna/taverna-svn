package net.sf.taverna.service.executeremotely;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.service.rest.client.RESTContext;

import org.apache.log4j.Logger;

public class AddServiceFrame extends JDialog {

	ExecuteRemotelyConf conf = ExecuteRemotelyConf.getInstance();
	
	private static Logger logger = Logger.getLogger(AddServiceFrame.class);

	private ExecuteRemotelyPanel parentPanel;

	Fields fields = new Fields();

	Buttons buttons = new Buttons();

	public AddServiceFrame(ExecuteRemotelyPanel parentPanel) {
		this.parentPanel = parentPanel;
		setLocationRelativeTo(parentPanel);
		setTitle("Add a new service");
		setLayout(new BorderLayout());
		setSize(new Dimension(350, 140));
		add(new JLabel("Add a new service entrypoint"), BorderLayout.NORTH);
		add(fields, BorderLayout.CENTER);
		add(buttons, BorderLayout.SOUTH);
	}

	class Fields extends JPanel {
		private JTextField uriField;
		private JTextField userField;
		private JTextField pwField;

		Fields() {
			super(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.gridy=0;
			c.gridx=0;
			add(new JLabel("URI"), c);
			c.gridy=1;
			uriField = new JTextField(40);
			add(uriField, c);

			c.gridy = 2;
			c.gridx = 0;
			add(new JLabel("Username"), c);
			c.gridx = 1;
			userField = new JTextField(20);
			add(userField, c);

			c.gridy = 3;
			c.gridx = 0;
			add(new JLabel("Password"), c);
			c.gridx = 1;
			pwField = new JTextField(20);
			add(pwField, c);
		}
	}

	class Buttons extends JPanel {
		Action okAction = new AbstractAction("OK") {
			public void actionPerformed(ActionEvent e) {
				String uri = fields.uriField.getText();
				String username = fields.userField.getText();
				String password = fields.pwField.getText();
				RESTContext service = new RESTContext(uri, username, password);
				conf.addService(service);
				AddServiceFrame.this.dispose();
				parentPanel.setContext(service);
			}
		};

		Action cancelAction = new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
				AddServiceFrame.this.dispose();
			}
		};

		Buttons() {
			super(new BorderLayout());
			add(new JButton(okAction) ,BorderLayout.LINE_START);
			add(new JButton(cancelAction), BorderLayout.LINE_END);
		}
	}

}
