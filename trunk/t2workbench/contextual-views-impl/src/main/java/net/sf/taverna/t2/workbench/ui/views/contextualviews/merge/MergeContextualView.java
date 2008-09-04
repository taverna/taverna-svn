package net.sf.taverna.t2.workbench.ui.views.contextualviews.merge;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;

/**
 * Contextual view for dataflow's merges.
 * 
 * @author Alex Nenadic
 *
 */
public class MergeContextualView extends ContextualView{

	
	private static final long serialVersionUID = -8726212237088362797L;
	private Merge merge;
	private JPanel mergeView;

	
	public MergeContextualView(Merge merge) {
		this.merge = merge;
		initView();
	}

	@Override
	protected JComponent getMainFrame() {
		refreshView();
		return mergeView;
	}

	@Override
	protected String getViewTitle() {
		return "Data link";
	}

	@Override
	public void refreshView() {
		
		mergeView = new JPanel();
		mergeView.setLayout(new BoxLayout(mergeView, BoxLayout.PAGE_AXIS));
		mergeView.setBorder(new EmptyBorder(5, 5, 5, 5));
		mergeView.add(new JLabel("Merge: " + merge.getLocalName()));
		mergeView.add(Box.createRigidArea(new Dimension(0,5)));
		mergeView.add(new JLabel("Inputs: "));
		for (MergeInputPort mergeInputPort : merge.getInputPorts()) {
			mergeView.add(new JLabel(mergeInputPort.getName()));
		}
		mergeView.add(Box.createRigidArea(new Dimension(0,5)));
		mergeView.add(new JLabel("Outputs: "));
		mergeView.add(new JLabel(merge.getOutputPort().getName()));
	}
}
