/**
 * 
 */
package net.sf.taverna.t2.component.annotation;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.io.StringReader;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.annotationbeans.SemanticAnnotation;
import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * @author alanrw
 *
 */
public class TurtleContextualView extends ContextualView {
	
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
		return "N3 annotations";
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
