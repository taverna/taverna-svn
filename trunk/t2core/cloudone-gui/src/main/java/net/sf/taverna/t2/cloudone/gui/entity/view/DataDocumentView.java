package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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
		super(model, null);
		initialise();
		//setBorder(BorderFactory.createLineBorder(Color.BLUE));
	}

	protected void initialise() {
		setLayout(new GridBagLayout());
//		setOpaque(false);
		// JPanel addSchemes = addSchemeButtons();
		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.gridx = 0;
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		// add to outer panel first then the parent?
		views = new JPanel();
//		views.setOpaque(false);
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
//		filler.setOpaque(false);
		// filler.setBorder(BorderFactory.createEtchedBorder());
		add(filler, outerConstraint);
	}

	@Override
	protected JComponent createModelView(final ReferenceSchemeModel refModel) {
		JPanel panel = new JPanel();
//		JLabel removeRef = new JLabel("<html><a href='#'>remove</a></html>");
//		removeRef.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseClicked(MouseEvent e) {
//				refModel.remove();
//			}
//		});
		
		JButton removeRef = new JButton(new RemoveViewAction((refModel)));
//		removeRef.setOpaque(false);
		JLabel lable = new  JLabel(refModel.toString());
		panel.add(lable);
		panel.add(removeRef);
		return panel;
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
		views.revalidate();
	}
	
	public class RemoveViewAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private final ReferenceSchemeModel model;

		public RemoveViewAction(ReferenceSchemeModel model) {
			super("Remove");
			this.model = model;
		}

		public void actionPerformed(ActionEvent e) {
			model.remove();
		}
	}

	@Override
	public void setEdit(boolean editable) {
		// TODO Auto-generated method stub
		
	}

}
