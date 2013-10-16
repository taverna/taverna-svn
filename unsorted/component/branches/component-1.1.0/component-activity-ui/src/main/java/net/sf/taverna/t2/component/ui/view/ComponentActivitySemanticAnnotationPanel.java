package net.sf.taverna.t2.component.ui.view;

import static net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils.getDisplayName;
import static net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils.getObjectName;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Statement;

public class ComponentActivitySemanticAnnotationPanel extends JPanel {
	private static final long serialVersionUID = 3599768150252711758L;
	private SemanticAnnotationProfile semanticAnnotationProfile;
	private final Set<Statement> statements;

	public ComponentActivitySemanticAnnotationPanel(
			ComponentActivitySemanticAnnotationContextualView semanticAnnotationContextualView,
			SemanticAnnotationProfile semanticAnnotationProfile,
			Set<Statement> statements) {
		this.semanticAnnotationProfile = semanticAnnotationProfile;
		this.statements = statements;
		initialize();
	}

	private void initialize() {
		setLayout(new GridBagLayout());
		setBorder(new AbstractBorder() {
			private static final long serialVersionUID = -5921448975807056953L;

			@Override
			public void paintBorder(Component c, Graphics g, int x, int y,
					int width, int height) {
				g.setColor(Color.GRAY);
				g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
			}
		});

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.SOUTHEAST;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.gridx = 0;

		OntProperty predicate = semanticAnnotationProfile.getPredicate();
		c.gridwidth = 2;
		JLabel label = new JLabel("Annotation type : "
				+ getDisplayName(predicate));
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
				JTextArea value = new JTextArea(getObjectName(statement));
				value.setBackground(Color.WHITE);
				value.setOpaque(true);
				value.setBorder(new EmptyBorder(2, 4, 2, 4));
				add(value, c);
			}
		}

	}

}
