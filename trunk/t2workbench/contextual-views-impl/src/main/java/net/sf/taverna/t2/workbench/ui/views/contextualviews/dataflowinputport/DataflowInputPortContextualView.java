package net.sf.taverna.t2.workbench.ui.views.contextualviews.dataflowinputport;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.impl.DataflowInputPortImpl;

/**
 * Contextual view for dataflow's input ports.
 * 
 * @author Alex Nenadic
 *
 */
public class DataflowInputPortContextualView extends ContextualView{
	
	private static final long serialVersionUID = -8746856072335775933L;
	private DataflowInputPortImpl dataflowInputPort;
	private JPanel dataflowInputPortView;
	
	public DataflowInputPortContextualView(DataflowInputPortImpl inputport) {
		this.dataflowInputPort = inputport;
		initView();
	}

	@Override
	protected JComponent getMainFrame() {
		refreshView();
		return dataflowInputPortView;
	}

	@Override
	protected String getViewTitle() {
		return "Dataflow input port";
	}

	@Override
	public void refreshView() {
		dataflowInputPortView = new JPanel();
		dataflowInputPortView.setBorder(new EmptyBorder(5,5,5,5));
		JLabel outputPortName = new JLabel("Dataflow input port name: " + dataflowInputPort.getName());
		dataflowInputPortView.add(outputPortName);
	}

}
