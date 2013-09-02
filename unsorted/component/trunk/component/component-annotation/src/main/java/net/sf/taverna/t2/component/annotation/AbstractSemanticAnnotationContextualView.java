/**
 * 
 */
package net.sf.taverna.t2.component.annotation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingWorker;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * @author alanrw
 *
 */
public abstract class AbstractSemanticAnnotationContextualView extends ContextualView {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3567849347002793442L;

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
	
	private static Comparator<SemanticAnnotationProfile> comparator = new Comparator<SemanticAnnotationProfile>() {

		@Override
		public int compare(SemanticAnnotationProfile arg0,
				SemanticAnnotationProfile arg1) {
			String d0 = SemanticAnnotationUtils.getDisplayName(arg0.getPredicate());
			String d1 = SemanticAnnotationUtils.getDisplayName(arg1.getPredicate());
			return String.CASE_INSENSITIVE_ORDER.compare(d0,d1);
		}};

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

	protected final void initialise() {
		populateModel();
		if (panel == null) {
			panel = new JPanel(new GridBagLayout());
		} else {
			panel.removeAll();
		}
		populatePanel(panel);
	}


	public void removeStatement(Statement statement) {
		model.remove(statement);
//		populatePanel(panel);
		updateSemanticAnnotation();
	}

	public void addStatement(Statement statement) {
		model.add(statement);
//		populatePanel(panel);
		updateSemanticAnnotation();
	}
	
	public void changeStatement(Statement origStatement, OntProperty predicate, RDFNode node) {
		model.remove(origStatement);
		model.add(subject, predicate, node);
//		populatePanel(panel);
		updateSemanticAnnotation();
	}

	public void addStatement(OntProperty predicate, RDFNode node) {
		model.add(subject, predicate, node);
//		populatePanel(panel);
		updateSemanticAnnotation();
	}
	
	@Override
	public void refreshView() {
		populatePanel(panel);
	}



//	public void addModel(Model model) {
//		this.model.add(model);
//		initialise();
//		updateSemanticAnnotation();
//	}

	public void updateSemanticAnnotation() {
		Dataflow currentDataflow = fileManager.getCurrentDataflow();
		try {
			editManager.doDataflowEdit(currentDataflow,
					edits.getAddAnnotationChainEdit(annotated, SemanticAnnotationUtils.createSemanticAnnotation(getModel())));
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


	private void populateModel() {
		this.model = SemanticAnnotationUtils.populateModel(getAnnotated());
		this.subject = SemanticAnnotationUtils.createBaseResource(this.model);
	}


	public Annotated<?> getAnnotated() {
		return annotated;
	}

	private void populatePanel(JPanel panel) {
		panel.removeAll();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.insets = new Insets(5,5,5,5);
		panel.add(new JLabel("Reading semantic annotations"), gbc);
		AbstractSemanticAnnotationContextualView.this.revalidate();		
		AbstractSemanticAnnotationContextualView.this.initView();
		(new StatementsReader()).execute();
	}
	
	private class StatementsReader extends SwingWorker<String, Object> {
		
		private Map<SemanticAnnotationProfile, Set<Statement>> statementsWithPredicateMap;
		private Set<Statement> statements;
		private Set<SemanticAnnotationProfile> unresolvablePredicates;

		@Override
		protected String doInBackground() throws Exception {
			try {
			statements = model.listStatements(subject, null, (RDFNode) null).toSet();
			statementsWithPredicateMap = new TreeMap<SemanticAnnotationProfile, Set<Statement>>(comparator);
			unresolvablePredicates = new HashSet<SemanticAnnotationProfile>();
			for (SemanticAnnotationProfile semanticAnnotationProfile : semanticAnnotationProfiles) {
				OntProperty predicate = semanticAnnotationProfile.getPredicate();
				if (predicate != null) {
					Set<Statement> statementsWithPredicate = model.listStatements(subject, predicate, (RDFNode) null).toSet();
					statementsWithPredicateMap.put(semanticAnnotationProfile, statementsWithPredicate);
					statements.removeAll(statementsWithPredicate);
				} else {
					unresolvablePredicates.add(semanticAnnotationProfile);
				}
				
			}
			}
			catch (Exception e) {
				logger.error(e);
			}
			return null;
		}

		@Override
	    protected void done() {
			panel.removeAll();
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.weightx = 1;
			gbc.weighty = 0;
			gbc.insets = new Insets(5,5,5,5);

			for (SemanticAnnotationProfile semanticAnnotationProfile : statementsWithPredicateMap.keySet()) {
					panel.add(new SemanticAnnotationPanel(AbstractSemanticAnnotationContextualView.this, semanticAnnotationProfile,
							statementsWithPredicateMap.get(semanticAnnotationProfile), allowChange), gbc);
					panel.add(new JSeparator(), gbc);
					}
			for (SemanticAnnotationProfile semanticAnnotationProfile : unresolvablePredicates) {
					panel.add(new UnresolveablePredicatePanel(semanticAnnotationProfile), gbc);
					panel.add(new JSeparator(), gbc);
				}
			
			if (semanticAnnotationProfiles.isEmpty()) {
				panel.add(new JLabel("No annotations possible"), gbc);
			}
			for (Statement s : statements) {
				panel.add(new UnrecognizedStatementPanel(s), gbc);
			}

			gbc.weighty = 1;
			panel.add(new JPanel(), gbc);
			AbstractSemanticAnnotationContextualView.this.revalidate();		
			AbstractSemanticAnnotationContextualView.this.initView();

	}
	}

}
