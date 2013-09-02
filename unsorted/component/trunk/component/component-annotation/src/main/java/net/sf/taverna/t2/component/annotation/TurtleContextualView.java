/**
 * 
 */
package net.sf.taverna.t2.component.annotation;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.annotationbeans.SemanticAnnotation;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

/**
 * @author alanrw
 *
 */
public class TurtleContextualView extends ContextualView {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3401885589263647202L;
	private JPanel panel;
	private String annotationContent = "";
	
	public TurtleContextualView(Annotated<?> selection) {
		super();

		SemanticAnnotation annotation = SemanticAnnotationUtils.findSemanticAnnotation(selection);
		if (annotation != null) {
			annotationContent = annotation.getContent();
		}
		initialise();
		initView();
	}


	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#getMainFrame()
	 */
	@Override
	public JComponent getMainFrame() {
		return panel;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#getPreferredPosition()
	 */
	@Override
	public int getPreferredPosition() {
		return 512;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#getViewTitle()
	 */
	@Override
	public String getViewTitle() {
		return "Turtle representation";
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#refreshView()
	 */
	@Override
	public void refreshView() {
		initialise();
	}
	
	protected final void initialise() {
		if (panel == null) {
			panel = new JPanel(new BorderLayout());
		} else {
			panel.removeAll();
		}
		JTextArea textArea = new JTextArea(20, 80);
		textArea.setEditable(false);
		textArea.setText(annotationContent);
		panel.add(textArea, BorderLayout.CENTER);
		revalidate();		
	}



}
