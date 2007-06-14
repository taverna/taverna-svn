/**
 * 
 */
package net.sf.taverna.service.executeremotely;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.service.rest.client.JobREST;
import net.sf.taverna.service.rest.client.JobsREST;
import net.sf.taverna.service.rest.client.NotSuccessException;
import net.sf.taverna.service.rest.client.RESTContext;
import net.sf.taverna.service.rest.client.RESTException;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ModelMap;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

class JobsPanel extends JPanel {

	private static Logger logger = Logger.getLogger(JobsPanel.class);

	private static final long serialVersionUID = -8349011048303447414L;

	private class Job extends JLabel {
		private static final long serialVersionUID = 3825708919735076714L;

		private class MouseClickListener extends MouseAdapter {
			@Override
			public void mouseClicked(MouseEvent e) {
				// Create and add a Workflow instance
				RemoteWorkflowInstance instance =
					new RemoteWorkflowInstance(job);
				ModelMap.getInstance().setModel(job.getURI(), instance);
			}
		}

		private String state;

		private JobREST job;

		private Job(JobREST job) {
			super("Parsing job");
			this.job = job;
			try {
				state = job.getStatus().toString();
			} catch (RESTException e) {
				logger.warn("Can't get status for " + job, e);
				state = "(unknown)";
			}
			setBackground(Color.WHITE);
			setText(job.getURI() + ": " + state);
			addMouseListener(new MouseClickListener());
		}
	}

	public class RefreshAction extends AbstractAction {

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

	private RESTContext context;

	JobsPanel() {
		super(new GridBagLayout());
		refresh();
	}

	public void setContext(RESTContext context) {
		this.context = context;
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
		// if (taverna == null) {
		// add(new ShadedLabel("Not connected",
		// ShadedLabel.TAVERNA_ORANGE), c);
		// } else {
		// add(new ShadedLabel("Jobs at " + getEndpoint(),
		// ShadedLabel.TAVERNA_BLUE), c);
		// }
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
		if (context == null) {
			return;
		}
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		JobsREST jobs;
		try {
			jobs = context.getUser().getJobs();
		} catch (NotSuccessException e) {
			ExecuteRemotelyPanel.logger.warn("Could not load jobs", e);
			return;
		}
		for (JobREST job : jobs) {
			add(new Job(job), c);
		}
	}

	public Element parseXML(String xm) throws JDOMException {
		SAXBuilder builder = new SAXBuilder(false);
		Document document;
		try {
			document = builder.build(new StringReader(xm));
		} catch (IOException ex) {
			// Not expected
			ExecuteRemotelyPanel.logger.error(
				"Could not read XML from StringReader", ex);
			throw new RuntimeException("Could not read XML from StringReader",
				ex);
		}
		Element element = document.getRootElement();
		return element;
	}
}