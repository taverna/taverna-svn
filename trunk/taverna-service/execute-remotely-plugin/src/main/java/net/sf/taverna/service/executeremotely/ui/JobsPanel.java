/**
 * 
 */
package net.sf.taverna.service.executeremotely.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.sf.taverna.service.executeremotely.UILogger;
import net.sf.taverna.service.rest.client.JobREST;
import net.sf.taverna.service.rest.client.JobsREST;
import net.sf.taverna.service.rest.client.NotSuccessException;
import net.sf.taverna.service.rest.client.RESTContext;
import net.sf.taverna.service.rest.client.RESTException;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;

public class JobsPanel extends JPanel {

	JobInfo lastJobInfo = null;

	private static Logger logger = Logger.getLogger(JobsPanel.class);

	private UILogger uiLog;

	private RESTContext context;

	private static final long serialVersionUID = -8349011048303447414L;

	class Job extends JPanel {
		private static final long serialVersionUID = 3825708919735076714L;

		private String state = "(loading)";

		private JobREST job;

		JLabel line;
		
		private class MouseClickListener extends MouseAdapter {
			@Override
			public void mouseClicked(MouseEvent e) {
				synchronized (JobsPanel.this) {
					if (lastJobInfo != null) {
						Container lastParent = lastJobInfo.getParent();
						lastParent.remove(lastJobInfo);
						lastParent.invalidate();
						lastJobInfo.removeAll();
						lastJobInfo = null;
					}
				}
				GridBagConstraints c = new GridBagConstraints();
				c.anchor = GridBagConstraints.CENTER;
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridwidth = GridBagConstraints.REMAINDER;
				c.gridx = 0;
				c.gridy = 1;
				lastJobInfo = new JobInfo(uiLog, JobsPanel.this, job);
				add(lastJobInfo, c);
				invalidate();
				JobsPanel.this.revalidate();
				JobsPanel.this.repaint();

			}
		}


		private Job(JobREST job) {
			setLayout(new GridBagLayout());
			setOpaque(false);
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.gridx = 0;
			c.gridy = 0;

			line = new JLabel("Parsing job");
			add(line, c);
			this.job = job;
			try {
				state = job.getStatus().toString();
			} catch (RESTException e) {
				logger.warn("Can't get status for " + job, e);
				state = "(unknown)";
				uiLog.log("Can't get status for " + job);
			}
			setBackground(Color.WHITE);
			String title = job.getTitle();
			if (title == null || title.equals("")) {
				title = job.getURI();
			}
			title += " <small>" + job.getCreated() + "</small>";
			// FIXME: Use tables or similar for all this info
			line.setText("<html>" + title + ": <strong>" + state
				+ "</strong></html>");
			addMouseListener(new MouseClickListener());
			
			c.gridy = 2;
			c.weightx = 0.2;
			add(new JPanel(), c); // filler

		}
	}


	JobsPanel(UILogger uiLog) {
		super(new GridBagLayout());
		this.uiLog = uiLog;
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
			uiLog.log("Could not load jobs from " + context);
			logger.warn("Could not load jobs", e);
			return;
		} catch (NotSuccessException e) {
			uiLog.log("Could not load jobs + context");
			logger.warn("Could not load jobs", e);
			return;
		}
		for (JobREST job : jobs) {
			add(new Job(job), c);
		}
		uiLog.log("Loaded jobs from " + context);
		// eat blank space
		c.weighty = 0.1;
		add(new JPanel(), c);
	}

}