/**
 * 
 */
package net.sf.taverna.service.executeremotely.ui;

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
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import net.sf.taverna.service.executeremotely.RemoteWorkflowInstance;
import net.sf.taverna.service.rest.client.JobREST;
import net.sf.taverna.service.rest.client.JobsREST;
import net.sf.taverna.service.rest.client.NotSuccessException;
import net.sf.taverna.service.rest.client.RESTContext;
import net.sf.taverna.service.rest.client.RESTException;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ModelMap;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
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
				log("Can't get status for " + job);
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

	private JPanel logPane;

	JobsPanel() {
		super(new GridBagLayout());
		refresh();
	}

	public void setContext(RESTContext context) {
		this.context = context;
		refresh();
	}

	public void refresh() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				removeAll();
				addHeader();
				addJobs();
				addRefreshButton();
				addLogs();
				revalidate();
				repaint();
			}

		});
	}

	private void addHeader() {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = 0;
		add(new ShadedLabel("Jobs at " + context, ShadedLabel.TAVERNA_BLUE), c);

	}

	private void addRefreshButton() {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_END;
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
		c.weightx = 0.2;
		JobsREST jobs;
		try {
			jobs = context.getUser().getJobs();
		} catch (RuntimeException e) {
			logger.warn("Could not load jobs", e);
			return;
		} catch (NotSuccessException e) {
			logger.warn("Could not load jobs", e);
			return;
		}
		for (JobREST job : jobs) {
			add(new Job(job), c);
		}
		// eat blank space
		c.weighty = 0.1;
		add(new JPanel(), c);
	}

	private void addLogs() {
		logPane = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.1;
		c.weighty = 0.1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		add(new JScrollPane(logPane), c);
	}

	public void log(Exception ex) {
		log(ex.toString());
	}

	public void log(String msg) {
		JLabel logLabel = new JLabel("<html><small>" + msg + "</small></html");
		logPane.add(logLabel, 0);
		logPane.revalidate();
	}

	public Element parseXML(String xm) throws JDOMException {
		SAXBuilder builder = new SAXBuilder(false);
		Document document;
		try {
			document = builder.build(new StringReader(xm));
		} catch (IOException ex) {
			// Not expected
			logger.error("Could not read XML from StringReader", ex);
			throw new RuntimeException("Could not read XML from StringReader",
				ex);
		}
		Element element = document.getRootElement();
		return element;
	}
}