package net.sf.taverna.t2.workbench.ui.views.contextualviews.dispatchstack;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;

public class DispatchStackContextualView extends ContextualView{
	
	private DispatchStack stack;
	private JPanel panel;
	private JPanel layers;

	public DispatchStackContextualView(DispatchStack stack) {
		this.stack = stack;
		initialise();
		initView();
	}

	private void initialise() {
		panel = new JPanel();
		layers = new JPanel();
		layers.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		layers.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		panel.add(layers, constraints);
		setLayers();
		
	}

	private void setLayers() {
		int gridy = 0;
		for (DispatchLayer<?> layer:stack.getLayers()) {
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = gridy;
			constraints.weightx = 0;
			constraints.weighty = 0;
			constraints.fill = GridBagConstraints.NONE;
			String simpleName = layer.getClass().getSimpleName();
			JTextArea dispatchLayer = new JTextArea(simpleName);
			layers.add(dispatchLayer, constraints);
			gridy++;
		}
	}

	@Override
	protected JComponent getMainFrame() {
		return panel;
	}

	@Override
	protected String getViewTitle() {
		return "Dispatch Stack contextual View";
	}

}
