package net.sf.taverna.t2.component.ui.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils;
import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;
import net.sf.taverna.t2.spi.SPIRegistry;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Statement;

public class ComponentActivitySemanticAnnotationPanel extends JPanel {

	private final ComponentActivitySemanticAnnotationContextualView semanticAnnotationContextualView;
	private SemanticAnnotationProfile semanticAnnotationProfile;
	private final Set<Statement> statements;

	public ComponentActivitySemanticAnnotationPanel(
			ComponentActivitySemanticAnnotationContextualView semanticAnnotationContextualView,
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
		JLabel label = new JLabel("Annotation type : " + SemanticAnnotationUtils.getDisplayName(predicate));
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
		}

	}

}
