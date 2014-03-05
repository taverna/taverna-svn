/**
 * 
 */
package net.sf.taverna.t2.component.annotation;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NORTHWEST;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils.createBaseResource;
import static net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils.createSemanticAnnotation;
import static net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils.getDisplayName;
import static org.apache.log4j.Logger.getLogger;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

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
public abstract class AbstractSemanticAnnotationContextualView extends
		ContextualView {
	private static final long serialVersionUID = 3567849347002793442L;
	private static final Logger logger = getLogger(SemanticAnnotationContextualView.class);
	private static final EditManager editManager = EditManager.getInstance();
	private static final FileManager fileManager = FileManager.getInstance();
	private static final Edits edits = editManager.getEdits();

	public AbstractSemanticAnnotationContextualView(boolean allowChange) {
		super();
		this.allowChange = allowChange;
	}

	private final boolean allowChange;
	private JPanel panel;
	private Annotated<?> annotated;
	private List<SemanticAnnotationProfile> semanticAnnotationProfiles;
	private Model model;
	private Resource subject;

	private static Comparator<SemanticAnnotationProfile> comparator = new Comparator<SemanticAnnotationProfile>() {
		@Override
		public int compare(SemanticAnnotationProfile arg0,
				SemanticAnnotationProfile arg1) {
			String d0 = getDisplayName(arg0.getPredicate());
			String d1 = getDisplayName(arg1.getPredicate());
			return CASE_INSENSITIVE_ORDER.compare(d0, d1);
		}
	};

	@Override
	public JComponent getMainFrame() {
		return panel;
	}

	@Override
	public int getPreferredPosition() {
		return 510;
	}

	protected final void initialise() {
		populateModel();
		if (panel == null)
			panel = new JPanel(new GridBagLayout());
		else
			panel.removeAll();
		populatePanel(panel);
	}

	public void removeStatement(Statement statement) {
		model.remove(statement);
		// populatePanel(panel);
		updateSemanticAnnotation();
	}

	public void addStatement(Statement statement) {
		model.add(statement);
		// populatePanel(panel);
		updateSemanticAnnotation();
	}

	public void changeStatement(Statement origStatement, OntProperty predicate,
			RDFNode node) {
		if (predicate == null)
			return;
 		model.remove(origStatement);
		model.add(subject, predicate, node);
		// populatePanel(panel);
		updateSemanticAnnotation();
	}

	public void addStatement(OntProperty predicate, RDFNode node) {
		if (predicate == null)
			return;
		model.add(subject, predicate, node);
		// populatePanel(panel);
		updateSemanticAnnotation();
	}

	@Override
	public void refreshView() {
		populatePanel(panel);
	}

	// public void addModel(Model model) {
	// this.model.add(model);
	// initialise();
	// updateSemanticAnnotation();
	// }

	public void updateSemanticAnnotation() {
		try {
			editManager.doDataflowEdit(fileManager.getCurrentDataflow(), edits
					.getAddAnnotationChainEdit(annotated,
							createSemanticAnnotation(getModel())));
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
		this.subject = createBaseResource(this.model);
	}

	public Annotated<?> getAnnotated() {
		return annotated;
	}

	private void populatePanel(JPanel panel) {
		panel.removeAll();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = NORTHWEST;
		gbc.fill = HORIZONTAL;
		gbc.gridx = 0;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.insets = new Insets(5, 5, 5, 5);
		panel.add(new JLabel("Reading semantic annotations"), gbc);
		revalidate();
		initView();
		new StatementsReader().execute();
	}

	private Set<Statement> listStatements(OntProperty predicate) {
		if (predicate == null)
			return Collections.emptySet();
		return model.listStatements(subject, predicate, (RDFNode) null).toSet();
	}

	private class StatementsReader extends SwingWorker<String, Object> {
		private Map<SemanticAnnotationProfile, Set<Statement>> profileStatements;
		private Set<Statement> statements;
		private Set<SemanticAnnotationProfile> unresolvablePredicates;

		@Override
		protected String doInBackground() throws Exception {
			try {
				parseStatements();
			} catch (Exception e) {
				logger.error("failed to parse annotation statements", e);
				throw e;
			}
			return null;
		}

		private void parseStatements() {
			statements = listStatements(null);
			profileStatements = new TreeMap<SemanticAnnotationProfile, Set<Statement>>(
					comparator);
			unresolvablePredicates = new HashSet<SemanticAnnotationProfile>();
			for (SemanticAnnotationProfile semanticAnnotationProfile : semanticAnnotationProfiles) {
				OntProperty predicate = semanticAnnotationProfile
						.getPredicate();
				if (predicate == null) {
					unresolvablePredicates.add(semanticAnnotationProfile);
					continue;
				}

				Set<Statement> statementsWithPredicate = listStatements(predicate);
				profileStatements.put(semanticAnnotationProfile,
						statementsWithPredicate);
				statements.removeAll(statementsWithPredicate);
			}
		}

		@Override
		protected void done() {
			panel.removeAll();
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = NORTHWEST;
			gbc.fill = HORIZONTAL;
			gbc.gridx = 0;
			gbc.weightx = 1;
			gbc.weighty = 0;
			gbc.insets = new Insets(5, 5, 5, 5);
			try {
				get();
				for (Entry<SemanticAnnotationProfile, Set<Statement>> entry : profileStatements
						.entrySet()) {
					panel.add(
							new SemanticAnnotationPanel(
									AbstractSemanticAnnotationContextualView.this,
									entry.getKey(), entry.getValue(), allowChange),
							gbc);
					panel.add(new JSeparator(), gbc);
				}
				for (SemanticAnnotationProfile semanticAnnotationProfile : unresolvablePredicates) {
					panel.add(new UnresolveablePredicatePanel(
							semanticAnnotationProfile), gbc);
					panel.add(new JSeparator(), gbc);
				}

				if (semanticAnnotationProfiles.isEmpty())
					panel.add(new JLabel("No annotations possible"), gbc);
				for (Statement s : statements)
					panel.add(new UnrecognizedStatementPanel(s), gbc);

				gbc.weighty = 1;
				panel.add(new JPanel(), gbc);
			} catch (ExecutionException | InterruptedException e) {
				logger.error(e);
				panel.add(new JLabel("Unable to read semantic annotations"), gbc);
			}


			revalidate();
			initView();
		}
	}
}
