package net.sf.taverna.t2.cloudone.entity.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.entity.ErrorDocument;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;

public class ErrorDocumentPanel extends AbstractEntityPanel {

	private static final long serialVersionUID = 1L;
	private JLabel messageLabel;
	private JLabel stackLabel;
	private ErrorDocumentIdentifier id;
	private ErrorDocument errDoc;

	public ErrorDocumentPanel(DataManager dataManager,
			ErrorDocumentIdentifier id) throws RetrievalException,
			NotFoundException {

		this.id = id;
		errDoc = (ErrorDocument) dataManager.getEntity(id);
		buildPanel();
	}

	@Override
	public JComponent createDetails() {
		JPanel details = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 1;
		c.weightx = 0.1;
		c.fill = GridBagConstraints.HORIZONTAL;

		if (errDoc.getMessage() != null) {
			messageLabel = new JLabel(errDoc.getMessage());
			details.add(messageLabel, c);
		}
		// show the stack trace if it exists
		if (errDoc.getStackTrace() != null) {
			stackLabel = new JLabel("<html><pre>" + errDoc.getStackTrace()
					+ "</pre></html>");
			details.add(stackLabel, c);
		}
		return details;
	}

	@Override
	public JComponent createHeader() {
		return createLabel("ErrorDocument", id);
	}

}
