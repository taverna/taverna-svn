package net.sf.taverna.t2.workbench.ui.views.contextualviews.datalink;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.Datalink;

/**
 * Contextual view for dataflow's datalinks.
 * 
 * @author Alex Nenadic
 *
 */
public class DatalinkContextualView extends ContextualView {

	private static final long serialVersionUID = -5031256519235454876L;
	
	private Datalink datalink;
	private JPanel datalinkView;

	
	public DatalinkContextualView(Datalink datalink) {
		this.datalink = datalink;
		initView();
	}

	@Override
	protected JComponent getMainFrame() {
		refreshView();
		return datalinkView;
	}

	@Override
	protected String getViewTitle() {
		return "Data link";
	}

	@Override
	public void refreshView() {
		
		datalinkView = new JPanel();
		datalinkView.setBorder(new EmptyBorder(5,5,5,5));
		JLabel datalinkName = new JLabel("Datalink: " + datalink.getSource().getName() + " -> " + datalink.getSink().getName());
		datalinkView.add(datalinkName);		
	}

}
