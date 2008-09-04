/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.processor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.dispatchstack.DispatchStackContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.iterationstrategy.IterationStrategyStackContextualView;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class ProcessorContextualView extends ContextualView {

	public class ClickActivityAction extends AbstractAction {

		private final JFrame frame;

		public ClickActivityAction(JPanel panel) {
			frame = new JFrame();
			frame.add(panel);
		}

		public void actionPerformed(ActionEvent e) {
			frame.setVisible(true);
		}

	}
	public class ClickDispatchStackAction extends AbstractAction {

		private final JFrame frame;

		public ClickDispatchStackAction(JPanel panel) {
			frame = new JFrame();
			frame.add(panel);
		}

		public void actionPerformed(ActionEvent e) {
			frame.setVisible(true);
		}

	}
	public class ClickIterationStackAction extends AbstractAction {

		private final JFrame frame;

		public ClickIterationStackAction(JPanel panel) {
			frame = new JFrame();
			frame.add(panel);
		}

		public void actionPerformed(ActionEvent e) {
			frame.setVisible(true);
		}

	}
	private Processor processor;
	private JPanel panel;
	private JPanel activities;
	private JPanel annotations;
	private JPanel dispatchStack;

	private JPanel inputPorts;

	private JPanel outputPorts;

	private JPanel iterationStrategy;

	public ProcessorContextualView(Processor processor) {
		super();
		this.processor = processor;
		initialise();
		initView();
	}

	@Override
	protected JComponent getMainFrame() {
		return panel;
	}

	@Override
	protected String getViewTitle() {
		return "Processor Contextual View";
	}

	private void initialise() {
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		activities = new JPanel();
		activities.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
				"Activities",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		activities.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		panel.add(activities, constraints);
		setActivities();
		annotations = new JPanel();
		dispatchStack = new JPanel();
		dispatchStack.setLayout(new GridBagLayout());
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridy = 1;
		panel.add(dispatchStack, constraints);
		setDispatchStack();
		inputPorts = new JPanel();
		outputPorts = new JPanel();
		iterationStrategy = new JPanel();
		iterationStrategy.setLayout(new GridBagLayout());
		setIterationStrategy();
		constraints.gridy = 2;
		panel.add(iterationStrategy, constraints);
	}

	@Override
	public void refreshView() {
		initialise();
	}

	private void setActivities() {
		// TODO Auto-generated method stub
		int gridy = 0;
		for (Activity activity : processor.getActivityList()) {
			ContextualViewFactory viewFactoryForBeanType = ContextualViewFactoryRegistry
					.getInstance().getViewFactoryForObject(activity);
			ContextualView view = viewFactoryForBeanType.getView(activity);
			JButton clickActivityView = new JButton(
					new ClickDispatchStackAction(view));
			clickActivityView.setText("View "
					+ activity.getClass().getSimpleName());
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = gridy;
			constraints.weightx = 0;
			constraints.weighty = 0;
			constraints.fill = GridBagConstraints.NONE;
			activities.add(clickActivityView, constraints);
			gridy++;
		}
	}

	private void setDispatchStack() {
		// JPanel dispatchStackPanel = new JPanel();
		// dispatchStackPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0.1;
		constraints.weighty = 0.1;
		constraints.fill = GridBagConstraints.NONE;
		DispatchStackContextualView view = new DispatchStackContextualView(
				processor.getDispatchStack());
		JButton clickDispatchStackButton = new JButton(
				new ClickDispatchStackAction(view));
		clickDispatchStackButton.setText("Dispatch Stack");
		// dispatchStackPanel.setSize(300, 100);
		// dispatchStackPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
		// "Dispatch Stack Here!",
		// javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
		// javax.swing.border.TitledBorder.DEFAULT_POSITION,
		// new java.awt.Font("Lucida Grande", 1, 12)));
		// dispatchStackPanel.add(new JTextArea("I am the dispatch stack"));
		dispatchStack.add(clickDispatchStackButton, constraints);

	}

	private void setIterationStrategy() {
		// JPanel iterationStrategyPanel = new JPanel();
		// iterationStrategyPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0.1;
		constraints.weighty = 0.1;
		constraints.fill = GridBagConstraints.NONE;
		// iterationStrategyPanel.setSize(300, 100);
		// iterationStrategyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
		// "Iteration Strategy Here!",
		// javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
		// javax.swing.border.TitledBorder.DEFAULT_POSITION,
		// new java.awt.Font("Lucida Grande", 1, 12)));
		IterationStrategyStackContextualView iterationStrategyStackContextualView = new IterationStrategyStackContextualView(
				processor.getIterationStrategy());
		JButton clickIterationStackButton = new JButton(
				new ClickIterationStackAction(
						iterationStrategyStackContextualView));
		clickIterationStackButton.setText("Iteration Strategy Stack");
		// iterationStrategyPanel.add(clickIterationStackButton, constraints);
		iterationStrategy.add(clickIterationStackButton, constraints);
	}

}
