package net.sf.taverna.service.executeremotely;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.service.rest.client.JobREST;
import net.sf.taverna.service.rest.client.JobsREST;
import net.sf.taverna.service.rest.client.NotSuccessException;
import net.sf.taverna.service.rest.client.RESTContext;
import net.sf.taverna.service.rest.client.WorkflowREST;
import net.sf.taverna.service.rest.client.WorkflowsREST;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

public class ExecuteRemotelyPanel extends JPanel implements
	WorkflowModelViewSPI {

	private static final long serialVersionUID = -3941327167079803885L;

	static Logger logger = Logger.getLogger(ExecuteRemotelyPanel.class);

	private ScuflModel model;

	private RESTContext context;

	private ExecuteRemotelyConf conf = ExecuteRemotelyConf.getInstance();

	private JobsPanel jobs = new JobsPanel();

	public ExecuteRemotelyPanel() {
		super(new GridBagLayout());
		addHeader();
		addConnection();
		addRunButton();
		addJobs();
		addFiller();
		logger.info("showing ourselves");
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

	private void addConnection() {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.anchor = GridBagConstraints.LINE_END;
		c.ipadx = 5;
		c.ipady = 5;
		add(new JLabel("Taverna service:"), c);

		c.weightx = 0.1;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = GridBagConstraints.RELATIVE;

		JComboBox services = new JComboBox(conf.getServices());
		services.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				setContext((RESTContext) cb.getSelectedItem());
			}
		});
		add(services, c);

		Action addService = new AbstractAction("New", TavernaIcons.newIcon) {
			public void actionPerformed(ActionEvent e) {
				AddServiceFrame addFrame =
					new AddServiceFrame(ExecuteRemotelyPanel.this);
				addFrame.setVisible(true);
			}
		};
		add(new JButton(addService), c);

	}

	public void setContext(RESTContext context) {
		this.context = context;
		jobs.setContext(context);
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

	public class RunWorkflowAction extends AbstractAction {

		private static final long serialVersionUID = -621606487976284994L;

		private Component parentComponent;

		public RunWorkflowAction(Component parentComponent) {
			this.parentComponent = parentComponent;
			putValue(SMALL_ICON, TavernaIcons.runIcon);
			putValue(NAME, "Run workflow remotely...");
			putValue(SHORT_DESCRIPTION, "Run the current workflow remotely");
		}

		public void actionPerformed(ActionEvent ev) {
			if (context == null || model == null) {
				logger.info("Can't run workflow without connection or current workflow");
				return;
			}
			String scufl = XScuflView.getXMLText(model);
			WorkflowsREST userWfs;
			try {
				userWfs = context.getUser().getWorkflows();
			} catch (NotSuccessException e) {
				logger.warn("Could not find user", e);
				return;
			}
			WorkflowREST wf;
			try {
				wf = userWfs.add(scufl);
			} catch (NotSuccessException e) {
				logger.warn("Could not add workflow " + model, e);
				return;
			}
			if (wf == null) {
				return;
			}
			logger.info("Created new workflow " + wf);
			JobsREST userJobs;
			try {
				userJobs = context.getUser().getJobs();
			} catch (NotSuccessException e) {
				logger.warn("Could not find user", e);
				return;
			}
			JobREST job;
			try {
				job = userJobs.add(wf);
			} catch (NotSuccessException e) {
				logger.warn("Could not add job " + wf, e);
				return;
			}
			logger.info("Created new job " + job);
			jobs.refresh();
		}
	}

}
