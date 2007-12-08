package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModelEvent;
import net.sf.taverna.t2.cloudone.gui.entity.model.ReferenceSchemeModel;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * The non-editable version of the DataDocument.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
@SuppressWarnings("unchecked")
public class DataDocumentView
		extends
		EntityView<DataDocumentModel, ReferenceSchemeModel, DataDocumentModelEvent> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1597320335876228073L;
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DataDocumentView.class);
	private JPanel views;
	private JLabel noRefs;

	public DataDocumentView(DataDocumentModel model) {
		super(model, null);
		initialise();
		// setBorder(BorderFactory.createLineBorder(Color.BLUE));
	}

	protected void initialise() {
		noRefs = new JLabel("No Reference Schemes have been added");
		setBorder(javax.swing.BorderFactory.createTitledBorder(null,
				"Reference Schemes",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		setLayout(new GridBagLayout());
		add(noRefs);
		// setOpaque(false);
		// JPanel addSchemes = addSchemeButtons();
		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.gridx = 0;
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		// add to outer panel first then the parent?
		views = new JPanel();
		// views.setOpaque(false);
		views.setLayout(new GridBagLayout());
		outerConstraint.weighty = 0.1;
		outerConstraint.weightx = 0.1;
		outerConstraint.fill = GridBagConstraints.BOTH;
		add(views, outerConstraint);
		for (ReferenceSchemeModel ref : getModel().getReferenceSchemeModels()) {
			addModelView(ref);
		}
		JPanel filler = new JPanel();
		// filler.setOpaque(false);
		// filler.setBorder(BorderFactory.createEtchedBorder());
		add(filler, outerConstraint);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent createModelView(final ReferenceSchemeModel refModel) {
		noRefs.setVisible(false);
		JPanel panel = new JPanel();
		JButton removeRef = new JButton(new RemoveViewAction((refModel)));
		// removeRef.setOpaque(false);
		JLabel label = new JLabel(refModel.getStringRepresentation());
		refModel.addObserver(new RefSchemeObserver(label));
		panel.add(label);
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
		if (getModel().getReferenceSchemeModels().isEmpty()) {
			noRefs.setVisible(true);
		}
		views.revalidate();
	}

	/**
	 * The Controller (in Model-View-Controller terms) for removing this view
	 * and model from the parent
	 * 
	 * @author Ian Dunlop
	 * @author Stian Soiland
	 * 
	 */
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

	/**
	 * Changes the text on the label representing a {@link RefSchemeView} to the
	 * string representation of the {@link ReferenceScheme} when the
	 * non-editable view is switched on
	 * 
	 * @author Ian Dunlop
	 * @author Stian Soiland
	 * 
	 */
	public class RefSchemeObserver implements Observer<Object> {

		private final JLabel label;

		public RefSchemeObserver(JLabel label) {
			this.label = label;
		}

		@SuppressWarnings("unchecked")
		public void notify(Observable<Object> sender, Object message) {
			ReferenceSchemeModel refSchemeModel = (ReferenceSchemeModel) sender;
			label.setText(refSchemeModel.getStringRepresentation());
		}

	}

}
