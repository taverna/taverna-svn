/**
 * 
 */
package net.sf.taverna.t2.component.annotation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.taverna.raven.log.Log;
import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.lang.ui.DeselectingButton;
import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.ui.menu.ContextualSelection;
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

	private static Log logger = Log.getLogger(AnnotateSemanticsMenuAction.class);

	public AnnotateSemanticsMenuAction() {
		super(configureSection, 45);
	}

	@Override
	public boolean isEnabled() {
		Object selection = getContextualSelection().getSelection();
		Object dataflowSource = fileManager.getDataflowSource(fileManager.getCurrentDataflow());
		if (dataflowSource instanceof ComponentVersionIdentification) {
			boolean b = (selection instanceof Annotated) && !(selection instanceof Activity || selection instanceof ActivityPort);
			return b;
		}
		return false;
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction(ANNOTATE_SEMANTICS) {
			public void actionPerformed(ActionEvent e) {
				SemanticAnnotationContextualView view = new SemanticAnnotationContextualView((Annotated) getContextualSelection().getSelection());
				
				final JDialog dialog = new JDialog((Frame) null, "Annotate semantics");
				dialog.setLayout(new BorderLayout());
				
				JScrollPane scrollPane = new JScrollPane(view);

				dialog.add(scrollPane, BorderLayout.CENTER);
				
				JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));

				JButton okButton = new DeselectingButton("OK",
						new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								dialog.setVisible(false);
							}
						});
				buttonPanel.add(okButton);

				dialog.add(buttonPanel, BorderLayout.SOUTH);
				dialog.setSize(new Dimension(400,300));
				dialog.setLocationRelativeTo(null);
				dialog.setModal(true);
				dialog.setVisible(true);
			}
		};
	}

}
