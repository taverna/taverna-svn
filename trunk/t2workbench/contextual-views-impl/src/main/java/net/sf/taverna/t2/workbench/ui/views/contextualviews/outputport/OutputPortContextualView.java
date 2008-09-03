package net.sf.taverna.t2.workbench.ui.views.contextualviews.outputport;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.impl.ActivityOutputPortImpl;

/**
 * Contextual view for dataflow procerssor's output ports.
 * 
 * @author Alex Nenadic
 *
 */
public class OutputPortContextualView extends ContextualView{

	private static final long serialVersionUID = -7743029534480678624L;
	
	private ActivityOutputPortImpl outputPort;
	private JPanel outputPortView;

	public OutputPortContextualView(ActivityOutputPortImpl outputport) {
		this.outputPort = outputport;
		initView();
	}

	@Override
	protected JComponent getMainFrame() {
		refreshView();
		return outputPortView;
	}

	@Override
	protected String getViewTitle() {
		return " Output port";
	}

	@Override
	public void refreshView() {
		outputPortView = new JPanel();
		outputPortView.setBorder(new EmptyBorder(5,5,5,5));
		JLabel outputPortName = new JLabel("Output port name: " + outputPort.getName());
		outputPortView.add(outputPortName);
	}

}
