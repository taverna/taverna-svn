package net.sf.taverna.t2.reference.ui;

import java.awt.CardLayout;

import javax.swing.JPanel;

import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * A JPanel used to switch back and forth between editing and previewing a
 * single piece of input data. The edit view allows for construction of a POJO
 * using the PreRegistrationPanel, if the registration action is invoked this
 * component switches to a newly constructed instance of T2ReferencePanel. If
 * the edit operation on that preview panel is invoked it switches back, both of
 * these transitions are messaged through the two abstract methods.
 * 
 * @author Tom Oinn
 * 
 */
public abstract class InputConstructionPanel extends JPanel {

	private final CardLayout layout;
	private final PreRegistrationPanel preRegistrationPanel;

	/**
	 * Construct a new InputConstructionPanel
	 * 
	 * @param inputDepth
	 *            the collection depth to construct
	 * @param referenceService
	 *            a ReferenceService to use to register the constructed POJOs
	 * @param context
	 *            a ReferenceContext to use when registering, can be null in
	 *            which case is treated as an empty reference context.
	 */
	@SuppressWarnings("serial")
	public InputConstructionPanel(final int inputDepth,
			final ReferenceService referenceService,
			final ReferenceContext context) {
		super(new CardLayout());
		layout = (CardLayout) getLayout();

		// Construct a new pre-registration panel and set it so a registration
		// uses the reference service to register the specified input then
		// switch the layout to the preview view.
		preRegistrationPanel = new PreRegistrationPanel(inputDepth) {

			@SuppressWarnings("serial")
			@Override
			/**
			 * Any exceptions here will be caught and shown in the status bar,
			 * so no need to handle them explicitly.
			 */
			public void handleRegistration(Object pojo) {
				// Register with the reference service, engaging the automatic
				// value to reference translator system.
				final T2Reference ref = referenceService.register(pojo,
						inputDepth, true, context);
				// Create a new preview window and show it
				T2ReferencePanel t2preview = new T2ReferencePanel(
						referenceService, ref) {
					@Override
					public void handleEdit() {
						// Throw back to the edit view, removing the preview
						// from the parent. This automatically switches the view
						// to the next lowest card, which is always the editor
						layout.removeLayoutComponent(this);
						inputDataCleared();
					}
				};
				layout.addLayoutComponent(t2preview, "preview");
				layout.show(InputConstructionPanel.this, "preview");
				// Message that data has been registered
				inputDataRegistered(ref);
			}

		};
		layout.addLayoutComponent(preRegistrationPanel, "editor");
		layout.show(this, "editor");

	}

	/**
	 * Called when the user has constructed and registered a piece of input data
	 * in the reference service.
	 * 
	 * @param reference
	 *            the T2Reference of the newly registered input data
	 */
	public abstract void inputDataRegistered(T2Reference reference);

	/**
	 * Called when the user has cleared the previously registered input,
	 * normally when moving from the T2Reference tree view back to the edit
	 * view.
	 */
	public abstract void inputDataCleared();

}
