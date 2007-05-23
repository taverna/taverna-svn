package net.sf.taverna.service.executeremotely;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;

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
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ModelMap;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

public class ExecuteRemotelyPanel extends JPanel implements
	WorkflowModelViewSPI {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3941327167079803885L;

	private static Logger logger = Logger.getLogger(ExecuteRemotelyPanel.class);

	private ScuflModel model;

	private URL endpoint = null;

	private JLabel endpointStatus = new JLabel();
	
	private EndpointField endpointField = new EndpointField();
	
	private Jobs jobs = new Jobs();
	
	private Namespace serviceNS = Namespace.getNamespace("http://service.taverna.sf.net/");	

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
		

		jobs.refresh();
		logger.info("Connected to " + url);
	}
	
	public synchronized URL getEndpoint() {
		return endpoint;
	}

	// FIXME: Replace with a "Run remotely" icon
	public ImageIcon getIcon() {
		return TavernaIcons.runIcon;
	}

	@Override
	public String getName() {
		return "Execute remotely";
	}

	public void onDisplay() {
	}

	public void attachToModel(ScuflModel model) {
		this.model = model;
	}

	public void detachFromModel() {
		model = null;
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
		c.anchor = GridBagConstraints.LINE_END;
		c.ipadx = 5;
		c.ipady = 5;
		add(new JLabel("Endpoint URL:"), c);

		c.weightx = 0.1;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = GridBagConstraints.RELATIVE;
		add(endpointField, c);
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
		/**
		 * 
		 */
		private static final long serialVersionUID = -8349011048303447414L;

		private class Job extends JLabel {
			/**
			 * 
			 */
			private static final long serialVersionUID = 3825708919735076714L;

			private class MouseClickListener extends MouseAdapter {
				@Override
				public void mouseClicked(MouseEvent e) {
					// Create and add a Workflow instance
					RemoteWorkflowInstance instance = new RemoteWorkflowInstance(taverna, job_id);
					ModelMap.getInstance().setModel(job_id, instance);
				}
			}

			private String job_id;
			private String state;

			private Job(Element jobElement) {
				super("Parsing job");
				job_id = jobElement.getAttribute("id").getValue();
				state = jobElement.getChildText("status", serviceNS);
				setBackground(Color.WHITE);
				setText(job_id +": " + state);
				// FIXME: Should be Action so it also works with keyboard
				addMouseListener(new MouseClickListener());
			}
		}

		public class RefreshAction extends AbstractAction {
			/**
			 * 
			 */
			private static final long serialVersionUID = -4718304414344585132L;

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
				add(new ShadedLabel("Not connected", 
						ShadedLabel.TAVERNA_ORANGE), c);
			} else {
				add(new ShadedLabel("Jobs at " + getEndpoint(),
					ShadedLabel.TAVERNA_BLUE), c);
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
		
		@SuppressWarnings("unchecked")
		private void addJobs() {
			if (taverna == null) {
				return;
			}
			String jobs;
			try {
				jobs = taverna.jobs();
			} catch (RemoteException e) {
				logger.warn("Could not list jobs at " + getEndpoint(), e);
				endpointField.warning(e.toString());
				return;
			}
			Element jobsElement;
			try {
				jobsElement = parseXML(jobs);
			} catch (JDOMException e) {
				logger.warn("Could not parse XML for jobs: " + jobs, e);
				return;
			}
			if (! jobsElement.getNamespace().equals(serviceNS) || 
				!jobsElement.getName().equals("jobs")) {
				logger.warn("Unknown XML element for jobs: " + jobsElement);
				endpointField.warning("Unknown XML for jobs");
				return;
			}
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.gridx = 0;
			c.gridy = GridBagConstraints.RELATIVE;
			List<Element> children = jobsElement.getChildren("job", serviceNS);
			for (Element job : children) {
				if (! job.equals("")) {
					add(new Job(job), c);
				}
			}
		}

		public Element parseXML(String xm) throws JDOMException {
			SAXBuilder builder = new SAXBuilder(false);
			Document document;
			try {
				document = builder.build(new StringReader(xm));
			} catch (IOException ex) {
				// Not expected
				logger.error("Could not read XML from StringReader", ex);
				throw new RuntimeException("Could not read XML from StringReader", ex);
			}
			Element element = document.getRootElement();
			return element;
		}		
	}

	public class RunWorkflowAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -621606487976284994L;
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
			//try {
				//taverna.runWorkflow("", "", scufl, "");
			//} catch (RemoteException ex) {
				//logger.warn("Could not execute " + model + " at " + getEndpoint(), ex);
			//	return;
			//}
			jobs.refresh();
		}

	}

	private class EndpointField extends JTextField {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7139620617907195405L;

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
