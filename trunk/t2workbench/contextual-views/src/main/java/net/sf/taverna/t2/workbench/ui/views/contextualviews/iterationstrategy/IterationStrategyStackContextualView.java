package net.sf.taverna.t2.workbench.ui.views.contextualviews.iterationstrategy;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategy;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategyStack;

public class IterationStrategyStackContextualView extends ContextualView{
	
	private JPanel panel;
	private JPanel iterations;
	private IterationStrategyStack stack;

	public IterationStrategyStackContextualView(IterationStrategyStack stack) {
		this.stack = stack;
		initialise();
		initView();
	}

	private void initialise() {
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		iterations = new JPanel();
		iterations.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		iterations.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		panel.add(iterations, constraints);
		setIterations();
	}

	private void setIterations() {
		int gridy = 0;
		List<? extends IterationStrategy> strategies = stack.getStrategies();
		for (IterationStrategy strategy:strategies) {
			IterationStrategyContextualView view = new IterationStrategyContextualView(strategy);
			JButton clickIterationButton = new JButton(new ClickIterationAction(view));
			clickIterationButton.setText("Iteration Strategy");
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = gridy;
			constraints.weightx = 0;
			constraints.weighty = 0;
			constraints.fill = GridBagConstraints.NONE;
			iterations.add(clickIterationButton, constraints);
			gridy++;
		}
	}

	@Override
	protected JComponent getMainFrame() {
		return panel;
	}

	@Override
	protected String getViewTitle() {
		return "Iteration Strategy contextual View";
	}
	
public class ClickIterationAction extends AbstractAction {
		
		private final JFrame frame;

		public ClickIterationAction(JPanel panel) {
			frame = new JFrame();
			frame.add(panel);
		}

		public void actionPerformed(ActionEvent e) {
			frame.setVisible(true);
		}
		
	}

}
