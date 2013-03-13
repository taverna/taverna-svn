/**
 * 
 */
package net.sf.taverna.t2.component.annotation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.annotationbeans.SemanticAnnotation;
import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;

/**
 * @author alanrw
 *
 */
public abstract class AbstractSemanticAnnotationContextualView extends ContextualView {
	
	protected static final String ENCODING = "TURTLE";
	/* Pretend-base for making relative URIs */
	private static String BASE = "widget://4aa8c93c-3212-487c-a505-3e337adf54a3/";
	private final boolean allowChange;

	public AbstractSemanticAnnotationContextualView(boolean allowChange) {
		super();
		this.allowChange = allowChange;
	}


	private static Logger logger = Logger.getLogger(SemanticAnnotationContextualView.class);

	private static EditManager editManager = EditManager.getInstance();
	private static FileManager fileManager = FileManager.getInstance();
	private static Edits edits = editManager.getEdits();

	private JPanel panel;

	private Annotated<?> annotated;

	private List<SemanticAnnotationProfile> semanticAnnotationProfiles;
	private Model model;

	private Resource subject;

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
		return 510;
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
			panel = new JPanel(new GridBagLayout());
		} else {
			panel.removeAll();
		}
		populatePanel(panel);
		revalidate();		
	}

	private void populatePanel(JPanel panel) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.insets = new Insets(5,5,5,5);

		Set<Statement> statements = model.listStatements().toSet();
		for (SemanticAnnotationProfile semanticAnnotationProfile : semanticAnnotationProfiles) {
			OntProperty predicate = semanticAnnotationProfile.getPredicate();
			if (predicate != null) {
				Set<Statement> statementsWithPredicate = new HashSet<Statement>();
				for (Statement statement : statements) {
					if (statement.getPredicate().equals(predicate)) {
						statementsWithPredicate.add(statement);
					}
				}
				if (!statementsWithPredicate.isEmpty() || allowChange) {
				panel.add(new SemanticAnnotationPanel(this, semanticAnnotationProfile,
						statementsWithPredicate, allowChange), gbc);
				panel.add(new JSeparator(), gbc);
				}
				statements.removeAll(statementsWithPredicate);
			} else {
				panel.add(new UnresolveablePredicatePanel(semanticAnnotationProfile), gbc);
				panel.add(new JSeparator(), gbc);
			}
			
		}
		
		if (semanticAnnotationProfiles.isEmpty()) {
			panel.add(new JLabel("No annotations possible"), gbc);
		}
		for (Statement s : statements) {
			panel.add(new UnrecognizedStatmentPanel(s), gbc);
		}

		gbc.weighty = 1;
		panel.add(new JPanel(), gbc);
	}

	public void removeStatement(Statement statement) {
		model.remove(statement);
		initialise();
		repaint();
		updateSemanticAnnotation();
	}

	public void addStatement(Statement statement) {
		model.add(statement);
		initialise();
		updateSemanticAnnotation();
	}
	
	public void changeStatement(Statement origStatement, OntProperty predicate, RDFNode node) {
		model.remove(origStatement);
		model.add(subject, predicate, node);
		initialise();
		updateSemanticAnnotation();
	}

	public void addStatement(OntProperty predicate, RDFNode node) {
		model.add(subject, predicate, node);
		initialise();
		updateSemanticAnnotation();
	}

//	public void addModel(Model model) {
//		this.model.add(model);
//		initialise();
//		updateSemanticAnnotation();
//	}

	private SemanticAnnotation createSemanticAnnotation() {
		SemanticAnnotation semanticAnnotation = new SemanticAnnotation();
		StringWriter stringWriter = new StringWriter();
		model.write(stringWriter, ENCODING, BASE);
		// Workaround for https://issues.apache.org/jira/browse/JENA-132
		String turtle = stringWriter.toString().replace("widget://4aa8c93c-3212-487c-a505-3e337adf54a3/", "");
		semanticAnnotation.setContent(turtle);
		return semanticAnnotation;
	}

	public void updateSemanticAnnotation() {
		Dataflow currentDataflow = fileManager.getCurrentDataflow();
		try {
			editManager.doDataflowEdit(currentDataflow,
					edits.getAddAnnotationChainEdit(annotated, createSemanticAnnotation()));
		} catch (EditException e) {
			logger.warn("Can't set semantic annotation", e);
		}
	}


	public void setAnnotated(Annotated<?> annotated) {
		this.annotated = annotated;
	}


	public void setSemanticAnnotationProfiles(
			List<SemanticAnnotationProfile> semanticAnnotationProfiles) {
		this.semanticAnnotationProfiles = semanticAnnotationProfiles;
	}


	public Model getModel() {
		return model;
	}


	public void populateModel() {
		this.model = ModelFactory.createDefaultModel();
		this.subject = model.createResource(BASE);
		SemanticAnnotation annotation = SemanticAnnotationUtils.findSemanticAnnotation(getAnnotated());
		if (annotation != null && !annotation.getContent().isEmpty()) {
			StringReader stringReader = new StringReader(annotation.getContent());
			getModel().read(stringReader, BASE, ENCODING);
		}

	}


	public Annotated<?> getAnnotated() {
		return annotated;
	}


}
