package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.sf.taverna.t2.cloudone.gui.entity.model.HttpRefSchemeModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.ReferenceSchemeModel;
import net.sf.taverna.t2.cloudone.impl.http.HttpReferenceScheme;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

/**
 * View (in Model-View-Controller pattern) for an {@link HttpReferenceScheme}.
 * Registers with a {@link HttpRefSchemeModel} to be informed when changes to
 * the model take place
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class HttpRefSchemeView extends ReferenceSchemeView implements
		Observer<URL> {

	private static final long serialVersionUID = 1L;
	private final HttpRefSchemeModel model;
	private JTextField textField;
	private JButton okButton;
	private JButton editButton;
	private JButton removeButton;
	private OKAction okAction = new OKAction();
	private EditAction editAction = new EditAction();
	private RemoveAction removeAction = new RemoveAction();
	private DataDocumentView parentView;

	/**
	 * Register as an observer with a {@link HttpRefSchemeModel} and initialise
	 * the GUI
	 * 
	 * @param model
	 * @param parentView
	 */
	public HttpRefSchemeView(HttpRefSchemeModel model,
			DataDocumentView parentView) {
		this.model = model;
		this.parentView = parentView;
		model.registerObserver(this);
		initialise();
	}

	/**
	 * The {@link HttpRefSchemeModel} notifies the {@link HttpRefSchemeView}
	 * that the URL has been changed ans must be updated in the view
	 */
	public void notify(Observable<URL> sender, URL url) {
		textField.setText(url.toString());
	}

	/**
	 * Handles the state of components on the form eg. disable/enable buttons
	 * 
	 * @throws IllegalStateException
	 *             If the current URL was malformed. The field will remain
	 *             editable.
	 */
	@Override
	public void setEdit(boolean editable) throws IllegalStateException {
		if (!editable) {
			// We are no longer to be editable
			setURLFromField();
		}
		setFieldsEditable(editable);
		if (editable) {
			textField.requestFocusInWindow();
		}
	}

	public void setURLFromField() throws IllegalStateException {
		try {
			model.setURL(textField.getText());
		} catch (MalformedURLException e) {
			JOptionPane.showMessageDialog(HttpRefSchemeView.this, textField
					.getText()
					+ " is not a valid url", "Invalid URL",
					JOptionPane.WARNING_MESSAGE);
			textField.requestFocusInWindow();
			throw new IllegalStateException(e);
		}
	}

	private void initialise() {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		setLayout(new GridBagLayout());
		textField = new JTextField(20);
		textField.setMinimumSize(new Dimension(250, 20));
		add(textField, c);
		okButton = new JButton(okAction);
		add(okButton);
		editButton = new JButton(editAction);
		add(editButton);
		removeButton = new JButton(removeAction);
		add(removeButton);

		editAction.setEnabled(false);
		textField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// dataDocumentBuilder.edit(HttpRefSchemePanel.this);
			}
		});
		textField.addActionListener(okAction);
		// By default, a new view is not editable
		setFieldsEditable(false);
	}

	/**
				 * Enable/disable fields and buttons to reflect editable status.
				 * Used by {@link #setEdit(boolean)} and the constructor, and
				 * does not check if the URL is valid.
				 * 
				 * @param editable
				 *            True if the fields are to be editable
				 */
	private void setFieldsEditable(boolean editable) {
		textField.setEditable(editable);
		textField.setEnabled(editable);
		editAction.setEnabled(!editable);
		okAction.setEnabled(editable);
	}

	/**
	 * Controller for edit button action. Asks parent view to deal with the
	 * button enable/disable etc. through the
	 * {@link DataDocumentView#edit(ReferenceSchemeModel)}
	 * 
	 * @author Stian Soiland
	 * @author Ian Dunlop
	 * 
	 */
	public class EditAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public EditAction() {
			super("Edit");
		}

		public void actionPerformed(ActionEvent e) {
			parentView.edit(model);
		}
	}

	/**
	 * Controller for the OK button action. Asks the {@link HttpRefSchemeModel}
	 * to set the URL. If the URL is invalid a dialogue box will be popped up
	 * 
	 * @author Stian soiland
	 * @author Ian Dunlop
	 * 
	 */
	public class OKAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public OKAction() {
			super("OK");
		}

		public void actionPerformed(ActionEvent e) {
			try {
				setURLFromField();
				parentView.edit(null);
			} catch (IllegalStateException ex) {
				// Warning box already shown, won't do edit(null)
			}
		}
	}

	/**
	 * Controller for the removal of a {@link HttpReferenceScheme} from the
	 * view. Asks the {@link HttpRefSchemeModel} to remove itself
	 * 
	 * @author Ian Dunlop
	 * @author Stian Soiland
	 * 
	 */
	public class RemoveAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public RemoveAction() {
			super("Remove");
		}

		public void actionPerformed(ActionEvent e) {
			model.remove();
		}
	}

}
