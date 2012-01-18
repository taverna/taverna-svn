package idaservicetype.idaservicetype.ui.view;

import java.awt.Frame;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

import idaservicetype.idaservicetype.IDAActivity;
import idaservicetype.idaservicetype.IDAActivityConfigurationBean;
import idaservicetype.idaservicetype.ui.config.IDAConfigureAction;

@SuppressWarnings("serial")
public class ExampleContextualView extends ContextualView {
	private final IDAActivity activity;
	private JLabel description = new JLabel("ads");

	public ExampleContextualView(IDAActivity activity) {
		this.activity = activity;
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		JPanel jPanel = new JPanel();
		jPanel.add(description);
		refreshView();
		return jPanel;
	}

	@Override
	public String getViewTitle() {
		//ExampleActivityConfigurationBean configuration = activity
		//		.getConfiguration();
		return "IDA";
	}

	/**
	 * Typically called when the activity configuration has changed.
	 */
	@Override
	public void refreshView() {
		//ExampleActivityConfigurationBean configuration = activity
		//		.getConfiguration();
		
		description.setText("IDA wizard");
		// TODO: Might also show extra service information looked
		// up dynamically from endpoint/registry
	}

	/**
	 * View position hint
	 */
	@Override
	public int getPreferredPosition() {
		// We want to be on top
		return 100;
	}
	
	@Override
	public Action getConfigureAction(final Frame owner) {
		return new IDAConfigureAction(activity, owner);
	}

}
