package net.sf.taverna.t2.workbench.ui.views.contextualviews.dataflowoutputport;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.impl.DataflowOutputPortImpl;

public class DataflowOutputPortContextualView extends ContextualView{

	private static final long serialVersionUID = 5496014085110553051L;
	private DataflowOutputPortImpl dataflowOutputPort;
	private JPanel dataflowOutputPortView;

	public DataflowOutputPortContextualView(DataflowOutputPortImpl outputport) {
		this.dataflowOutputPort = outputport;
		initView();
	}

	@Override
	protected JComponent getMainFrame() {
		refreshView();
		return dataflowOutputPortView;
	}

	@Override
	protected String getViewTitle() {
		return "Dataflow output port";
	}

	@Override
	public void refreshView() {
		dataflowOutputPortView = new JPanel();
		dataflowOutputPortView.setBorder(new EmptyBorder(5,5,5,5));
		JLabel outputPortName = new JLabel("Dataflow output port name: " + dataflowOutputPort.getName());
		dataflowOutputPortView.add(outputPortName);
	}
}
