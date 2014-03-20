/**
 * 
 */
package net.sf.taverna.t2.component.annotation;

import static java.awt.BorderLayout.CENTER;
import static net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils.findSemanticAnnotation;

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
	private static final long serialVersionUID = -3401885589263647202L;
	private JPanel panel;
	private String annotationContent = "";

	public TurtleContextualView(Annotated<?> selection) {
		SemanticAnnotation annotation = findSemanticAnnotation(selection);
		if (annotation != null) 
			annotationContent = annotation.getContent();
		initialise();
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		return panel;
	}

	@Override
	public int getPreferredPosition() {
		return 512;
	}

	@Override
	public String getViewTitle() {
		return "Turtle representation";
	}

	@Override
	public void refreshView() {
		initialise();
	}

	protected final void initialise() {
		if (panel == null)
			panel = new JPanel(new BorderLayout());
		else
			panel.removeAll();
		JTextArea textArea = new JTextArea(20, 80);
		textArea.setEditable(false);
		textArea.setText(annotationContent);
		panel.add(textArea, CENTER);
		revalidate();
	}
}
