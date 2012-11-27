package net.sf.taverna.t2.component.annotation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class SemanticAnnotationPanel extends JPanel {

	private final SemanticAnnotationContextualView semanticAnnotationContextualView;
	private SemanticAnnotationProfile semanticAnnotationProfile;
	private final Set<Statement> statements;

	public SemanticAnnotationPanel(
			SemanticAnnotationContextualView semanticAnnotationContextualView,
			SemanticAnnotationProfile semanticAnnotationProfile, Set<Statement> statements) {
		this.semanticAnnotationContextualView = semanticAnnotationContextualView;
		this.semanticAnnotationProfile = semanticAnnotationProfile;
		this.statements = statements;
		initialize();
	}

	private void initialize() {
		setLayout(new GridBagLayout());
		setBorder(new AbstractBorder() {
			@Override
			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				g.setColor(Color.GRAY);
				g.drawLine(x, y+height-1, x+width-1, y+height-1);
			}
		});

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.SOUTHEAST;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.gridx = 0;

		OntProperty predicate = semanticAnnotationProfile.getPredicate();
		c.gridwidth = 2;
		JLabel label = new JLabel("Predicate : " + getName(predicate));
		label.setBorder(new EmptyBorder(5, 5, 5, 5));
		label.setBackground(Color.WHITE);
		label.setOpaque(true);
		add(label, c);

		c.insets = new Insets(5, 7, 0, 0);
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		if (statements.isEmpty()) {
			c.gridwidth = 2;
			// c.weightx = 1;
			// c.gridy++;
			add(new JLabel("No semantic annotations"), c);
		} else {
			c.gridwidth = 1;
			for (Statement statement : statements) {
				c.gridx = 0;
				c.weightx = 1;
				JLabel predicateLabel = new JLabel(getName(statement.getObject()));
				predicateLabel.setBackground(Color.WHITE);
				predicateLabel.setOpaque(true);
				predicateLabel.setBorder(new EmptyBorder(2,4,2,4));
				add(predicateLabel, c);

				c.gridx = 1;
				c.weightx = 0;
				add(createDeleteButton(statement), c);
			}
		}

		c.gridx = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.SOUTHEAST;
		c.fill = GridBagConstraints.NONE;
		add(createAddButton(predicate), c);

	}

	private JButton createDeleteButton(final Statement statement) {
		return new JButton(new AbstractAction("Delete") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				semanticAnnotationContextualView.removeStatement(statement);
			}
		});
	}

	private JButton createAddButton(final OntProperty predicate) {
		return new JButton(new AbstractAction("Add Annotation") {
			@Override
			public void actionPerformed(ActionEvent e) {
				showAddAnnotationDialog(predicate);
			}
		});
	}

	private void showAddAnnotationDialog(final OntProperty predicate) {
		final JDialog dialog = new JDialog();
		dialog.setTitle("Add Semantic Annotation");
		dialog.setLocationRelativeTo(this);
		dialog.setSize(new Dimension(400, 250));
		dialog.setLayout(new BorderLayout());

		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBorder(new EmptyBorder(5, 5, 0, 0));
		messagePanel.setBackground(Color.WHITE);
		dialog.add(messagePanel, BorderLayout.NORTH);

		JLabel inputLabel = new JLabel("Add Semantic Annotation");
		inputLabel.setBackground(Color.WHITE);
		Font baseFont = inputLabel.getFont();
		inputLabel.setFont(baseFont.deriveFont(Font.BOLD));
		messagePanel.add(inputLabel, BorderLayout.NORTH);

		JTextArea inputText = new JTextArea("Select a value for the predicate '" + getName(predicate) + "'");
		inputText.setMargin(new Insets(5, 10, 10, 10));
		inputText.setMinimumSize(new Dimension(0, 30));
		inputText.setFont(baseFont.deriveFont(11f));
		inputText.setEditable(false);
		inputText.setFocusable(false);
		messagePanel.add(inputText, BorderLayout.CENTER);

		List<Individual> individuals = semanticAnnotationProfile.getIndividuals();
		NamedResource[] namedResources = new NamedResource[individuals.size()];
		for (int i = 0; i < namedResources.length; i++) {
			namedResources[i] = new NamedResource(individuals.get(i));
		}
		final JComboBox resources = new JComboBox(namedResources);
		resources.setEditable(false);
		JPanel resourcePanel = new JPanel(new BorderLayout());
		resourcePanel.add(resources, BorderLayout.NORTH);
		resourcePanel.setBorder(new EmptyBorder(15,5,5,5));
		dialog.add(resourcePanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		dialog.add(buttonPanel, BorderLayout.SOUTH);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				NamedResource selectedItem =  (NamedResource) resources.getSelectedItem();
				semanticAnnotationContextualView.addStatement(predicate, selectedItem.getResource());
				dialog.dispose();
			}
		});
		buttonPanel.add(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);

		dialog.setVisible(true);
	}

	private String getName(RDFNode node) {
		if (node == null) {
			return "unknown";
		} else if (node.isAnon()) {
			return "anon";
		} else if (node.isLiteral()) {
			return node.asLiteral().getLexicalForm();
		} else if (node.isResource()) {
			Resource resource = node.asResource();
			String label = null;
			if (resource instanceof OntResource) {
				OntResource ontResource = (OntResource) resource;
				label = ontResource.getLabel(null);
			}
			if (label != null) {
				return label;
			} else {
				return resource.getLocalName();
			}
		} else {
			return "unknown";
		}
	}

	//
	// private Set<String> getNames(Set<OntResource> resources) {
	// Set<String> names = new HashSet<String>();
	// for (OntResource resource : resources) {
	// names.add(getName(resource));
	// }
	// return names;
	// }

	private class NamedResource {

		private final OntResource resource;

		public NamedResource(OntResource resource) {
			this.resource = resource;
		}

		public OntResource getResource() {
			return resource;
		}

		public String toString() {
			String label = resource.getLabel(null);
			if (label != null) {
				return label;
			} else {
				return resource.getLocalName();
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((resource == null) ? 0 : resource.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NamedResource other = (NamedResource) obj;
			if (resource == null) {
				if (other.resource != null)
					return false;
			} else if (!resource.equals(other.resource))
				return false;
			return true;
		}

	}

}
