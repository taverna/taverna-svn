package net.sf.taverna.t2.reference.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * A simple workflow launch panel, uses a tabbed layout to display a set of
 * named InputConstructionPanel instances, and enables a 'run workflow' button
 * when all have been registered and locked.
 * 
 * @author Tom Oinn
 * 
 */
public abstract class WorkflowLaunchPanel extends JPanel {

	private final ImageIcon launchIcon = new ImageIcon(getClass().getResource(
			"/icons/start_task.gif"));

	private final ImageIcon notReadyIcon = new ImageIcon(getClass()
			.getResource("/icons/invalid_build_tool.gif"));

	private final ImageIcon readyIcon = new ImageIcon(getClass().getResource(
			"/icons/complete_status.gif"));

	// An action enabled when all inputs are enabled and used to trigger the
	// handleLaunch method
	private final Action launchAction;

	// Hold the current map of name->reference
	private final Map<String, T2Reference> inputMap = new HashMap<String, T2Reference>();

	private final JTabbedPane tabs;
	private final Map<String, Component> tabComponents = new HashMap<String, Component>();

	private final ReferenceService referenceService;
	private final ReferenceContext referenceContext;

	@SuppressWarnings("serial")
	public WorkflowLaunchPanel(ReferenceService rs, ReferenceContext context) {
		super(new BorderLayout());

		this.referenceService = rs;
		this.referenceContext = context;

		launchAction = new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {
				handleLaunch(inputMap);
			}
		};
		launchAction.putValue(Action.SMALL_ICON, launchIcon);
		launchAction.putValue(Action.NAME, "Launch workflow");

		// Construct tab container
		tabs = new JTabbedPane();
		add(tabs, BorderLayout.CENTER);

		// Construct tool bar
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(new JButton(launchAction));
		add(toolBar, BorderLayout.NORTH);
	}

	@SuppressWarnings("serial")
	public synchronized void addInputTab(final String inputName,
			final int inputDepth) {
		// Don't do anything if we already have this tab
		if (inputMap.containsKey(inputName)) {
			return;
		} else {
			InputConstructionPanel inputPanel = new InputConstructionPanel(
					inputDepth, referenceService, referenceContext) {
				@Override
				public void inputDataCleared() {
					inputMap.put(inputName, null);
					tabs.setIconAt(tabs.indexOfTab(inputName), notReadyIcon);
					checkLaunchValid();
				}

				@Override
				public void inputDataRegistered(T2Reference reference) {
					inputMap.put(inputName, reference);
					tabs.setIconAt(tabs.indexOfTab(inputName), readyIcon);
					checkLaunchValid();
				}
			};
			inputMap.put(inputName, null);
			tabComponents.put(inputName, inputPanel);
			tabs.addTab(inputName, notReadyIcon, inputPanel);
			checkLaunchValid();
		}
	}

	public synchronized void removeInputTab(final String inputName) {
		// Only do something if we have this tab to begin with
		if (inputMap.containsKey(inputName) == false) {
			return;
		} else {
			Component component = tabComponents.get(inputName);
			tabComponents.remove(inputName);
			inputMap.remove(inputName);
			tabs.remove(component);
			checkLaunchValid();
		}
	}

	/**
	 * We can enable the launch if and only if all keys in the inputMap have
	 * non-null entries.
	 */
	private synchronized void checkLaunchValid() {
		boolean enabled = true;
		for (T2Reference ref : inputMap.values()) {
			if (ref == null) {
				enabled = false;
				break;
			}
		}
		launchAction.setEnabled(enabled);
	}

	/**
	 * Called when the run workflow action has been performed
	 * 
	 * @param workflowInputs
	 *            a map of named inputs in the form of T2Reference instances
	 */
	public abstract void handleLaunch(Map<String, T2Reference> workflowInputs);

}
