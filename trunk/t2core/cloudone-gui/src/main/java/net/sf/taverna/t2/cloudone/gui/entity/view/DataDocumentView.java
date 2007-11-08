package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.cloudone.gui.entity.model.BlobRefSchemeModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModelEvent;
import net.sf.taverna.t2.cloudone.gui.entity.model.HttpRefSchemeModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.ReferenceSchemeModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModelEvent.EventType;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * View (in MVC terms) for a Data Document builder GUI.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 */
public class DataDocumentView extends JFrame {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(DataDocumentView.class);
	/**
	 * A {@link Map} of {@link ReferenceSchemeModel} to
	 * {@link RefSchemeView} to allow tracking of view state and what
	 * model is associated with what view
	 */
	private Map<ReferenceSchemeModel, RefSchemeView> refModelViews = new HashMap<ReferenceSchemeModel, RefSchemeView>();

	private DataDocumentModel model;

	private ModelObserver modelObserver;

	private JPanel refViews;

	/** What {@link RefSchemeView} was the last one to be edited */
	private RefSchemeView lastEditedView;

	private JButton httpButton;
	private JButton blobButton;

	/**
	 * Constructor creates a {@link ModelObserver} and registers it as an
	 * observer with the {@link DataDocumentView} and calls
	 * {@link #initialiseGui()} to create the GUI
	 * 
	 * @param model
	 *            the {@link DataDocumentModel} model part in
	 *            Model-View-Controller terms
	 */
	public DataDocumentView(DataDocumentModel model) {
		this.model = model;
		this.modelObserver = new ModelObserver();
		model.registerObserver(modelObserver);
		// TODO: removeObserver on window close
		initialiseGui();
	}

	/**
	 * Resets buttons/text fields etc to their correct editing states eg, if OK
	 * is clicked and the input is valid then only the Edit & Remove buttons
	 * should be enabled
	 * 
	 * @param model
	 *            the {@link ReferenceSchemeModel} whose state has changed
	 * @throws IllegalStateException
	 *             If the current editable could not set it's fields to the
	 *             model
	 */
	public void edit(ReferenceSchemeModel model) throws IllegalStateException {
		// Might return null, which is OK
		RefSchemeView view = refModelViews.get(model);
		if (lastEditedView != null && lastEditedView != view) {
			try {
				lastEditedView.setEdit(false);
			} catch (IllegalStateException ex) {
				throw new IllegalStateException(ex);
			}
		}
		lastEditedView = view;
		if (view != null) {
			view.setEdit(true);
		}
	}

	/**
	 * Refreshes the view after a model has been added
	 * 
	 * @param refView
	 */
	private void addReferenceView(RefSchemeView refView) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		refViews.add(refView, c);
		refViews.revalidate();
	}

	/**
	 * Adds a {@link ReferenceSchemeModel} to the view. Triggered by the
	 * {@link ModelObserver} being notified by the {@link DataDocumentModel}
	 * that something has been added
	 * 
	 * @param refModel
	 *            the type of {@link ReferenceSchemeModel} that has been added
	 *            eg {@link HttpRefSchemeModel}
	 */
	private void addRefSchemeModel(ReferenceSchemeModel refModel) {
		RefSchemeView refView = null;
		if (refModel instanceof HttpRefSchemeModel) {
			refView = new HttpRefSchemeView((HttpRefSchemeModel) refModel, this);
		} else {
			logger.warn("Unsupported reference model " + refModel);
			return;
		}
		refModelViews.put(refModel, refView);
		addReferenceView(refView);
	}

	private JPanel createdRefs() {
		JPanel refs = new JPanel();
		refs.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		// TODO: Add existing from model
		model.getReferenceSchemeModels();
		return refs;
	}

	private void initialiseGui() {
		setLayout(new GridBagLayout());

		JPanel addSchemes = addSchemeButtons();
		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.gridx = 0;
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		add(addSchemes, outerConstraint);

		refViews = createdRefs();
		add(refViews, outerConstraint);

		outerConstraint.gridx = 1;
		outerConstraint.gridy = 100;
		outerConstraint.weighty = 0.1;
		outerConstraint.weightx = 0.1;
		outerConstraint.fill = GridBagConstraints.BOTH;
		JPanel filler = new JPanel();
		// filler.setBorder(BorderFactory.createEtchedBorder());
		add(filler, outerConstraint);
	}

	/**
	 * Removes a {@link ReferenceSchemeModel} from the view. Triggered by the
	 * {@link ModelObserver} being notified by the {@link DataDocumentModel}
	 * that something has been removed. Refreshes the view.
	 * 
	 * @param refModel
	 *            the type of {@link ReferenceSchemeModel} that has been removed
	 *            eg {@link HttpRefSchemeModel}
	 */
	private void removeRefSchemeModel(ReferenceSchemeModel refModel) {
		RefSchemeView view = refModelViews.remove(refModel);
		refViews.remove(view);
		if (lastEditedView == view) {
			lastEditedView = null;
		}
		refViews.revalidate();
	}

	protected void addCreatedRefModel(ReferenceSchemeModel refModel) {
		model.addReferenceScheme(refModel);
		try {
			// Make it editable
			edit(refModel);
		} catch (IllegalStateException ex) {
			// Could not change editable, remove fresh model
			model.removeReferenceScheme(refModel);
		}
	}

	protected JPanel addSchemeButtons() {
		JPanel addSchemes = new JPanel();
		addSchemes.setLayout(new GridBagLayout());
		GridBagConstraints cLabel = new GridBagConstraints();
		cLabel.gridx = 0;
		cLabel.weightx = 0.1;
		cLabel.fill = GridBagConstraints.HORIZONTAL;

		GridBagConstraints cButton = new GridBagConstraints();
		cButton.gridx = 1;
		cButton.fill = GridBagConstraints.HORIZONTAL;

		JLabel httpRefLabel = new JLabel("Http Reference Scheme");
		JLabel blobRefLabel = new JLabel("Blob Reference Scheme");
		CreateHttpAction createHttpAction = new CreateHttpAction(model);
		CreateBlobAction createBlobAction = new CreateBlobAction(model);
		httpButton = new JButton(createHttpAction);
		blobButton = new JButton(createBlobAction);

		addSchemes.add(httpRefLabel, cLabel);
		addSchemes.add(httpButton, cButton);
		addSchemes.add(blobRefLabel, cLabel);
		addSchemes.add(blobButton, cButton);
		return addSchemes;
	}

	/**
	 * The Controller (in Model-View-Controller terms) for adding a
	 * {@link BlobReferenceSchemeReferenceScheme} via clicking the appropriate
	 * button
	 * 
	 * @author Stian Soiland
	 * @author Ian Dunlop
	 * 
	 */
	public class CreateBlobAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		@SuppressWarnings("unused")
		private DataDocumentModel dataDocmodel;

		public CreateBlobAction(DataDocumentModel model) {
			super("Create blob reference");
			this.dataDocmodel = model;
		}

		public void actionPerformed(ActionEvent e) {
			// this.dataDocumentView.controller.createReference(BlobRefSchemeModel.class);
			BlobRefSchemeModel refModel = new BlobRefSchemeModel();
			addCreatedRefModel(refModel);
		}
	}

	/**
	 * The Controller (in Model-View-Controller terms) for adding a
	 * {@link HttpReferenceScheme} via clicking the appropriate button
	 * 
	 * @author Stian Soiland
	 * @author Ian Dunlop
	 * 
	 */
	public class CreateHttpAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		private DataDocumentModel dataDocModel;

		public CreateHttpAction(DataDocumentModel dataDocModel) {
			super("Create HTTP reference");
			this.dataDocModel = dataDocModel;
		}

		public void actionPerformed(ActionEvent e) {
			HttpRefSchemeModel refModel = new HttpRefSchemeModel(dataDocModel);
			addCreatedRefModel(refModel);
		}
	}

	/**
	 * Observes the changes in a {@link DataDocumentModel} and is notified by
	 * the {@link DataDocumentModel} whenit wants to inform it of a change to
	 * the underlying data
	 * 
	 * @author Stian Soiland
	 * @author Ian Dunlop
	 * 
	 */
	private final class ModelObserver implements
			Observer<DataDocumentModelEvent> {

		public void notify(Observable<DataDocumentModelEvent> sender,
				DataDocumentModelEvent event) {
			EventType eventType = event.getEventType();
			if (eventType.equals(EventType.ADDED)) {
				ReferenceSchemeModel refModel = event.getRefSchemeModel();
				addRefSchemeModel(refModel);
			} else if (eventType.equals(EventType.REMOVED)) {
				ReferenceSchemeModel refModel = event.getRefSchemeModel();
				removeRefSchemeModel(refModel);
			} else {
				logger.warn("Unsupported event type " + eventType);
			}
		}
	}
}
