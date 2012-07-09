/**
 * 
 */
package net.sf.taverna.t2.semantic.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;

import net.sf.taverna.t2.semantic.profile.AnnotationProfile;
import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.workbench.file.FileManager;
import uk.ac.manchester.cs.owl.semspreadsheets.model.WorkbookManager;
import uk.ac.manchester.cs.owl.semspreadsheets.ui.ClassHierarchyTreePanel;
import uk.ac.manchester.cs.owl.semspreadsheets.ui.WorkbookFrame;

/**
 * @author alanrw
 *
 */
public class SemanticAnnotationMenuAction extends AbstractContextualMenuAction {

	public static final URI configureSection = URI
    .create("http://taverna.sf.net/2009/contextMenu/configure");
	private static final String SEMANTIC_ANNOTATION = "Configure semantics...";
	
	private static FileManager fm = FileManager.getInstance();

	public SemanticAnnotationMenuAction() {
		super(configureSection, 55);
	}

	@Override
	public boolean isEnabled() {
		return (getContextualSelection() != null);
	}
	

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.ui.menu.AbstractMenuAction#createAction()
	 */
	@Override
	protected Action createAction() {
		return new AbstractAction(SEMANTIC_ANNOTATION) {

			@Override
			public void actionPerformed(ActionEvent e) {
				AnnotationProfile profile = AnnotationProfile.getAnnotationProfile(fm.getCurrentDataflow());
				WorkbookManager wm = profile.getWorkbookManager();
				WorkbookFrame wf = new WorkbookFrame(wm);
				JPanel p = new ClassHierarchyTreePanel(wf);
				p.setPreferredSize(new Dimension(400,300));
				JDialog dialog = new JDialog();
				dialog.add(p);
				dialog.pack();
				p.setPreferredSize(new Dimension(400,300));
				dialog.setLocationRelativeTo((Component) e.getSource());
				dialog.setVisible(true);
			}
		};
	}

}
