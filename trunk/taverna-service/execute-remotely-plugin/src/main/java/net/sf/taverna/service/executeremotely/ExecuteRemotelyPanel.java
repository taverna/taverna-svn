package net.sf.taverna.service.executeremotely;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.rpc.ServiceException;

import net.sf.taverna.service.wsdl.client.Taverna;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

public class ExecuteRemotelyPanel extends JPanel implements
	WorkflowModelViewSPI {

	private static Logger logger = Logger.getLogger(ExecuteRemotelyPanel.class);

	private ScuflModel model;

	private URL endpoint = null;

	private JLabel endpointStatus = new JLabel();
	
	private EndpointField endpointField = new EndpointField();

	private Taverna taverna = null;
	
	private Jobs jobs = new Jobs();

	public ExecuteRemotelyPanel() {
		super(new GridBagLayout());
		addHeader();
		addEndpoint();
		addRunButton();
		addJobs();
		addFiller();
		logger.info("showing ourselves");
	}

	public synchronized void setEndpoint(URL url) {
		URL oldUrl = endpoint;
		endpoint = url;
		endpointField.updateEndpoint();
		endpointField.notice("Loading..");
		try {
			taverna = TavernaService.connect(url);
		} catch (ServiceException ex) {
			logger.warn("Could not connect to "+ url);
			endpointField.warning(ex.toString());
			endpoint = oldUrl; // flip back, but don't modify the field
		}
		endpointField.notice("Loaded");
		jobs.refresh();
		logger.info("Connected to " + url);
	}
	
	public synchronized URL getEndpoint() {
		return endpoint;
	}

	public ImageIcon getIcon() {
		return TavernaIcons.runIcon;
	}

	public String getName() {
		logger.info("Woohuu");
		return "Execute remotely";
	}

	public void onDisplay() {
	}

	public void attachToModel(ScuflModel model) {
		this.model = model;
	}

	public void detachFromModel() {
		this.model = null;
	}

	public void onDispose() {
	}

	private void addHeader() {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(new ShadedLabel("Execute remotely", ShadedLabel.TAVERNA_GREEN), c);
	}

	private void addRunButton() {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		add(new JButton(new RunWorkflowAction(this)), c);
	}


	private void addEndpoint() {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.anchor = GridBagConstraints.FIRST_LINE_END;
		add(new JLabel("Endpoint URL:"), c);
		c.gridx = GridBagConstraints.RELATIVE;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 0.1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(endpointField, c);
		c.ipadx = 5;
		add(endpointStatus, c);
	}
	
	private void addJobs() {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridy = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.1;
		c.weighty = 0.1;
		add(jobs, c);		
	}

	private void addFiller() {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.1;
		c.weighty = 0.1;
		c.anchor = GridBagConstraints.SOUTHEAST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(new JPanel(), c);
	}


	private class Jobs extends JPanel {
		public class RefreshAction extends AbstractAction {
			public RefreshAction() {
				putValue(SMALL_ICON, TavernaIcons.refreshIcon);
				putValue(NAME, "Refresh");
				putValue(SHORT_DESCRIPTION, "Refresh list of jobs from server");
			}

			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		}
		
		Jobs() {
			super(new GridBagLayout());
			refresh();
		}
		
		public void refresh() {
			removeAll();
			addHeader();
			addJobs();
			addRefreshButton();
			revalidate();
		}
		
		private void addHeader() {
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.gridx = 0;
			c.gridy = 0;
			if (taverna == null) {
				add(new JLabel("Not connected"), c);
			} else {
				add(new JLabel("Jobs at " + getEndpoint()), c);
			}
		}
		
		private void addRefreshButton() {
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.fill = GridBagConstraints.NONE;
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.gridx = 0;
			c.gridy = GridBagConstraints.RELATIVE;
			add(new JButton(new RefreshAction()), c);
		}
		
		private void addJobs() {
			if (taverna == null) {
				return;
			}
			String[] jobs;
			try {
				jobs = taverna.jobs().split("\n");
			} catch (RemoteException e) {
				logger.warn("Could not list jobs at " + getEndpoint(), e);
				endpointField.warning(e.toString());
				return;
			}
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.gridx = 0;
			c.gridy = GridBagConstraints.RELATIVE;
			for (String job : jobs) {
				add(new JLabel(job), c);
			}
		}		
	}

	public class RunWorkflowAction extends AbstractAction {

		private Component parentComponent;

		public RunWorkflowAction(Component parentComponent) {
			this.parentComponent = parentComponent;
			putValue(SMALL_ICON, TavernaIcons.runIcon);
			putValue(NAME, "Run workflow remotely...");
			putValue(SHORT_DESCRIPTION, "Run the current workflow remotely");
		}

		public void actionPerformed(ActionEvent e) {
			if (taverna == null || model == null) {
				logger.info("Can't run workflow without connection or current workflow");
				return;
			}
			String scufl = XScuflView.getXMLText(model);
			// FIXME: Support input parameters
			try {
				taverna.runWorkflow(scufl, "");
			} catch (RemoteException ex) {
				logger.warn("Could not execute " + model + " at " + getEndpoint(), ex);
				return;
			}
			jobs.refresh();
		}

	}

	private class EndpointField extends JTextField {
		public EndpointField() {
			addFocusListener(new SetEndpointListener());
			updateEndpoint();
		}

		public void updateEndpoint() {
			if (endpoint == null) {
				setText("");
				warning("Unspecified");
			} else {
				setText(endpoint.toExternalForm());
				notice("");
			}
		}

		public void warning(String msg) {
			logger.info("Oh noes " + endpointStatus);
			endpointStatus.setText(msg);
			endpointStatus.setForeground(Color.RED);
		}

		public void notice(String msg) {
			endpointStatus.setText(msg);
			endpointStatus.setForeground(Color.BLACK);
		}

		private class SetEndpointListener implements FocusListener {
			public void focusGained(FocusEvent e) {
				// Would be confusing to say "Invalid URL" or "Loading" when the
				// user is entering a new URL
				notice("");
			}

			public void focusLost(FocusEvent e) {
				try {
					setEndpoint(new URL(endpointField.getText()));
				} catch (MalformedURLException ex) {
					warning("Invalid URL");
				}
			}
		}
	}

}
