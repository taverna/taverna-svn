/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.ui.perspectives.results;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.SwingAwareObserver;
import net.sf.taverna.t2.renderers.RendererRegistry;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.selection.events.SelectionManagerEvent;
import net.sf.taverna.t2.workbench.selection.events.WorkflowRunSelectionEvent;
import net.sf.taverna.t2.workbench.ui.Updatable;
import net.sf.taverna.t2.workbench.views.monitor.graph.MonitorGraphComponent;
import net.sf.taverna.t2.workbench.views.results.ResultsComponent;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveIndividualResultSPI;

import org.apache.log4j.Logger;
import org.osgi.service.event.Event;

import uk.org.taverna.platform.run.api.RunService;

/**
 * @author David Withers
 */
public class ResultsPerspectiveComponent extends JPanel implements Updatable {

	private static Logger logger = Logger.getLogger(ResultsPerspectiveComponent.class);

	private static final String RUNS_SELECTED = "RUNS_SELECTED";
	private static final String NO_RUNS_SELECTED = "NO_RUNS_SELECTED";

	private final RunService runService;
	private final SelectionManager selectionManager;
	private final ColourManager colourManager;
	private final WorkbenchConfiguration workbenchConfiguration;

	private List<Updatable> updatables = new ArrayList<>();

	private CardLayout cardLayout;

	private SelectionManagerObserver selectionManagerObserver;

	private MonitorGraphComponent monitorGraphComponent;
	private ResultsComponent resultsComponent;
	private RunSelectorComponent runSelectorComponent;

	public ResultsPerspectiveComponent(RunService runService, SelectionManager selectionManager,
			ColourManager colourManager, WorkbenchConfiguration workbenchConfiguration,
			RendererRegistry rendererRegistry, List<SaveAllResultsSPI> saveAllResultsSPIs,
			List<SaveIndividualResultSPI> saveIndividualResultSPIs) {
		this.runService = runService;
		this.selectionManager = selectionManager;
		this.colourManager = colourManager;
		this.workbenchConfiguration = workbenchConfiguration;

		cardLayout = new CardLayout();
		setLayout(cardLayout);

		JLabel noRunsMessage = new JLabel("No workflows run yet", JLabel.CENTER);
		Font font = noRunsMessage.getFont();
		if (font != null) {
			font = font.deriveFont(Math.round((font.getSize() * 1.5))).deriveFont(Font.BOLD);
			noRunsMessage.setFont(font);
		}
		JPanel noRunsPanel = new JPanel(new BorderLayout());
		noRunsPanel.add(noRunsMessage, BorderLayout.CENTER);
		add(noRunsPanel, NO_RUNS_SELECTED);

		monitorGraphComponent = new MonitorGraphComponent(runService,
				colourManager, workbenchConfiguration, selectionManager);
		resultsComponent = new ResultsComponent(runService, selectionManager,
				rendererRegistry, saveAllResultsSPIs, saveIndividualResultSPIs);

		updatables.add(monitorGraphComponent);
		updatables.add(resultsComponent);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setBorder(null);
		splitPane.setLeftComponent(monitorGraphComponent);
		splitPane.setRightComponent(resultsComponent);
		splitPane.setDividerLocation(200);

		runSelectorComponent = new RunSelectorComponent(runService, selectionManager);

		JPanel runsPanel = new JPanel(new BorderLayout());
		runsPanel.add(runSelectorComponent, BorderLayout.NORTH);
		runsPanel.add(splitPane, BorderLayout.CENTER);
		add(runsPanel, RUNS_SELECTED);

		selectionManagerObserver = new SelectionManagerObserver();
		selectionManager.addObserver(selectionManagerObserver);
	}

	@Override
	protected void finalize() throws Throwable {
		selectionManager.removeObserver(selectionManagerObserver);
	}

	@Override
	public void update() {
		for (Updatable updatable : updatables) {
			updatable.update();
		}
	}

	private class SelectionManagerObserver extends SwingAwareObserver<SelectionManagerEvent> {
		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender,
				SelectionManagerEvent message) {
			if (message instanceof WorkflowRunSelectionEvent) {
				WorkflowRunSelectionEvent workflowRunSelectionEvent = (WorkflowRunSelectionEvent) message;
				String selectedWorkflowRun = workflowRunSelectionEvent.getSelectedWorkflowRun();
				if (selectedWorkflowRun == null) {
					cardLayout.show(ResultsPerspectiveComponent.this, NO_RUNS_SELECTED);
				} else {
					cardLayout.show(ResultsPerspectiveComponent.this, RUNS_SELECTED);
				}
			}
		}
	}

	public void handleEvent(Event event) {
		String topic = event.getTopic();
		switch (topic) {
		case RunService.RUN_CREATED:
			// addWorkflowRun(event.getProperty("RUN_ID").toString());
			break;
		case RunService.RUN_DELETED:
			String workflowRun = event.getProperty("RUN_ID").toString();
			runSelectorComponent.removeObject(workflowRun);
			monitorGraphComponent.removeWorkflowRun(workflowRun);
			resultsComponent.removeWorkflowRun(workflowRun);
			break;
		}
	}

}
