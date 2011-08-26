package net.sf.taverna.service.executeremotely.ui;

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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.sf.taverna.service.rest.client.RESTContext;

import org.embl.ebi.escience.scuflui.workbench.Workbench;

/**
 * Frame for adding/editing a service
 * 
 * @author Stian Soiland
 */
public class AddEditServiceFrame extends JDialog {

	private ExecuteRemotelyPanel parentPanel;

	public Fields fields;

	public Buttons buttons;

	public RESTContext service;

	/**
	 * Add a new service
	 * 
	 * @param parentPanel
	 */
	public AddEditServiceFrame(ExecuteRemotelyPanel parentPanel) {
		this(parentPanel, null);
	}

	/**
	 * Edit an existing service
	 * 
	 * @param parentPanel
	 * @param service
	 */
	public AddEditServiceFrame(ExecuteRemotelyPanel parentPanel,
		RESTContext service) {
		super(Workbench.getInstance());
		this.parentPanel = parentPanel;
		this.service = service;
		setLocationRelativeTo(parentPanel);
		if (service == null) {
			setTitle("Add a new service");
		} else {
			setTitle("Edit service " + service);
		}
		setLayout(new BorderLayout());
		setSize(new Dimension(450, 200));
		add(new JLabel("Service entrypoint"), BorderLayout.NORTH);
		add(new JPanel(), BorderLayout.WEST);
		add(new JPanel(), BorderLayout.EAST);
		fields = new Fields();
		add(fields, BorderLayout.CENTER);
		buttons = new Buttons();
		add(buttons, BorderLayout.SOUTH);
	}

	class Fields extends JPanel {
		private JTextField nameField;

		private JTextField uriField;

		private JTextField userField;

		private JPasswordField pwField;

		Fields() {
			super(new GridBagLayout());

			int y = 0;

			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.CENTER;
			c.ipadx = 3;
			c.ipady = 3;

			c.gridy = y++;
			c.gridx = 0;
			add(new JLabel("Name:"), c);
			c.gridx = 1;
			c.weightx = 0.1;
			c.anchor = GridBagConstraints.LINE_START;
			nameField = new JTextField(30);
			if (service != null) {
				nameField.setText(service.getName());
			}
			add(nameField, c);
			c.gridwidth = 1;
			c.weightx = 0.0;

			c.gridy = y++;
			c.gridx = 0;
			add(new JLabel("URI:"), c);
			c.gridy = y++;
			c.gridwidth = 2;
			c.weightx = 0.1;
			c.anchor = GridBagConstraints.LINE_START;
			uriField = new JTextField(30);
			if (service != null) {
				uriField.setText(service.getBaseURI().toString());
			}

			add(uriField, c);
			c.gridwidth = 1;
			c.weightx = 0.0;

			c.gridy = y++;
			c.gridx = 0;
			c.anchor = GridBagConstraints.LINE_END;
			add(new JLabel("Username:"), c);
			c.gridx = 1;
			c.anchor = GridBagConstraints.LINE_START;
			userField = new JTextField(15);
			if (service != null) {
				userField.setText(service.getUsername());
			}
			add(userField, c);

			c.gridy = y++;
			c.gridx = 0;
			c.anchor = GridBagConstraints.LINE_END;
			add(new JLabel("Password:"), c);
			c.gridx = 1;
			c.anchor = GridBagConstraints.LINE_START;
			pwField = new JPasswordField(15);
			if (service != null) {
				pwField.setText(service.getPassword());
			}
			add(pwField, c);
		}
	}

	class Buttons extends JPanel {
		Action okAction = new AbstractAction("OK") {
			public void actionPerformed(ActionEvent e) {
				String name = fields.nameField.getText();
				String uri = fields.uriField.getText();
				String username = fields.userField.getText();
				String password = new String(fields.pwField.getPassword());
				if (uri.equals("")) {
					JOptionPane.showMessageDialog(AddEditServiceFrame.this,
						"URI field can't be empty", "Empty URI",
						JOptionPane.ERROR_MESSAGE);
					// Don't dipose of the window
					return;
				}
				if (service == null) {
					parentPanel.addService(name, uri, username, password);
				} else {
					parentPanel.updateService(service, name, uri, username,
						password);
				}
				AddEditServiceFrame.this.dispose();
			}
		};

		Action cancelAction = new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
				AddEditServiceFrame.this.dispose();
			}
		};

		Buttons() {
			super(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			add(new JButton(okAction), c);
			add(new JButton(cancelAction), c);
		}
	}

}
