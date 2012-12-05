package net.sf.taverna.t2.component.annotation;

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

import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;
import net.sf.taverna.t2.spi.SPIRegistry;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class SemanticAnnotationPanel extends JPanel {

	private final SemanticAnnotationContextualView semanticAnnotationContextualView;
	private SemanticAnnotationProfile semanticAnnotationProfile;
	private final Set<Statement> statements;

	protected SPIRegistry<AddSemanticAnnotationDialogSPI> addAnnotationDialogRegistry = new SPIRegistry<AddSemanticAnnotationDialogSPI>(
			AddSemanticAnnotationDialogSPI.class);

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
		JLabel label = new JLabel("Predicate : " + SemanticAnnotationUtils.getDisplayName(predicate));
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
				JLabel predicateLabel = new JLabel(SemanticAnnotationUtils.getDisplayName(statement.getObject()));
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
				getAddAnnotationDialog(predicate).setVisible(true);
			}
		});
	}

	private JDialog getAddAnnotationDialog(OntProperty predicate) {
		List<AddSemanticAnnotationDialogSPI> instances = addAnnotationDialogRegistry.getInstances();
		for (AddSemanticAnnotationDialogSPI addSemanticAnnotationDialogSPI : instances) {
			if (addSemanticAnnotationDialogSPI.canHandleSemanticAnnotation(semanticAnnotationProfile)) {
				return addSemanticAnnotationDialogSPI.getSemanticAnnotationDialog(semanticAnnotationContextualView, semanticAnnotationProfile);
			}
		}
		return new DefaultAddSemanticAnnotationDialog(semanticAnnotationContextualView, semanticAnnotationProfile);
	}

}
