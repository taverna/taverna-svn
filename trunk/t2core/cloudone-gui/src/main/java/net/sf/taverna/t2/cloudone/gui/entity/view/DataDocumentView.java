package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModelEvent;
import net.sf.taverna.t2.cloudone.gui.entity.model.ReferenceSchemeModel;

import org.apache.log4j.Logger;

public class DataDocumentView extends
		EntityView<DataDocumentModel, ReferenceSchemeModel, DataDocumentModelEvent> {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DataDocumentView.class);
	private JPanel views;

	public DataDocumentView(DataDocumentModel model) {
		super(model);
		initialise();
	}

	protected void initialise() {
		setLayout(new GridBagLayout());
		// JPanel addSchemes = addSchemeButtons();
		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.gridx = 0;
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		// add to outer panel first then the parent?
		views = new JPanel();
		views.setLayout(new GridBagLayout());
		outerConstraint.weighty = 0.1;
		outerConstraint.weightx = 0.1;
		outerConstraint.fill = GridBagConstraints.BOTH;
		add(views, outerConstraint);
		for (ReferenceSchemeModel ref : getParentModel()
				.getReferenceSchemeModels()) {
			addModelView(ref);
		}
		JPanel filler = new JPanel();
		// filler.setBorder(BorderFactory.createEtchedBorder());
		add(filler, outerConstraint);
	}

	@Override
	protected JComponent createModelView(ReferenceSchemeModel refModel) {
		return new JLabel(refModel.toString());
	}

	@Override
	protected void placeViewComponent(JComponent view) {
		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outerConstraint.gridx = 1;
		outerConstraint.weighty = 0.1;
		outerConstraint.weightx = 0.1;
		outerConstraint.fill = GridBagConstraints.BOTH;
		views.add(view, outerConstraint);
	}

	@Override
	protected void removeViewComponent(JComponent refModel) {
		views.remove(refModel);
	}

}
