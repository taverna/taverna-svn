/**
 * 
 */
package net.sf.taverna.t2.component.annotation;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.FlowLayout.TRAILING;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.lang.ui.DeselectingButton;
import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityPort;

/**
 * @author alanrw
 * 
 */
public class AnnotateSemanticsMenuAction extends AbstractContextualMenuAction {
	private static final String ANNOTATE_SEMANTICS = "Annotate semantics...";
	private static final URI configureSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/configure");
	private static final FileManager fileManager = FileManager.getInstance();

	public AnnotateSemanticsMenuAction() {
		super(configureSection, 45);
	}

	@Override
	public boolean isEnabled() {
		Object selection = getContextualSelection().getSelection();
		Object dataflowSource = fileManager.getDataflowSource(fileManager
				.getCurrentDataflow());
		if (dataflowSource instanceof ComponentVersionIdentification)
			return (selection instanceof Annotated)
					&& !(selection instanceof Activity || selection instanceof ActivityPort);
		return false;
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction(ANNOTATE_SEMANTICS) {
			@Override
			public void actionPerformed(ActionEvent e) {
				showAnnotateSemanticsPanel();
			}
		};
	}

	private void showAnnotateSemanticsPanel() {
		SemanticAnnotationContextualView view = new SemanticAnnotationContextualView(
				(Annotated<?>) getContextualSelection().getSelection());

		final JDialog dialog = new JDialog((Frame) null, "Annotate semantics");
		dialog.setLayout(new BorderLayout());

		dialog.add(new JScrollPane(view), CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(TRAILING));

		buttonPanel.add(new DeselectingButton("OK", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		}));

		dialog.add(buttonPanel, SOUTH);
		dialog.setSize(new Dimension(400, 300));
		dialog.setLocationRelativeTo(null);
		dialog.setModal(true);
		dialog.setVisible(true);
	}
}
