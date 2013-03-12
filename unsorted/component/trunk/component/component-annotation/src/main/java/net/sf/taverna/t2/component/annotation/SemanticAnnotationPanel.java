package net.sf.taverna.t2.component.annotation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;
import net.sf.taverna.t2.lang.ui.DeselectingButton;
import net.sf.taverna.t2.spi.SPIRegistry;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

public class SemanticAnnotationPanel extends JPanel {

	private final SemanticAnnotationContextualView semanticAnnotationContextualView;
	private final SemanticAnnotationProfile semanticAnnotationProfile;
	private final Set<Statement> statements;

	protected SPIRegistry<PropertyPanelFactorySPI> propertyPanelFactoryRegistry = new SPIRegistry<PropertyPanelFactorySPI>(
			PropertyPanelFactorySPI.class);

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
		c.gridwidth = 3;
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
		} else {
			c.gridwidth = 1;
			for (Statement statement : statements) {
				c.gridx = 0;
				c.weightx = 1;
				JTextArea value = new JTextArea(SemanticAnnotationUtils.getDisplayName(statement.getObject()));
				value.setLineWrap(true);
				value.setWrapStyleWord(true);
				value.setEditable(false);
				value.setBackground(Color.WHITE);
				value.setOpaque(true);
				value.setBorder(new EmptyBorder(2,4,2,4));
				add(value, c);

				c.gridx = 1;
				c.weightx = 0;
				add(createChangeButton(predicate, statement), c);
				
				c.gridx = 2;
				add(createDeleteButton(statement), c);
			}
		}

		c.gridx = 0;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.SOUTHEAST;
		c.fill = GridBagConstraints.NONE;
		add(createAddButton(predicate), c);

	}

	private JButton createChangeButton(final OntProperty predicate, final Statement statement) {
		return new DeselectingButton("Change", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addOrChangeAnnotation(predicate, statement);
			}
		});
	}

	private JButton createDeleteButton(final Statement statement) {
		return new DeselectingButton("Delete", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				semanticAnnotationContextualView.removeStatement(statement);
			}
		});
	}

	private JButton createAddButton(final OntProperty predicate) {
		return new DeselectingButton("Add Annotation", new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				addOrChangeAnnotation(predicate, null);
			}
		});
	}

	private void addOrChangeAnnotation(final OntProperty predicate, Statement statement) {
		JPanel annotationPanel = null;
		PropertyPanelFactorySPI chosenFactory = null;
		JComponent inputComponent = null;
		List<PropertyPanelFactorySPI> instances = propertyPanelFactoryRegistry.getInstances();
		for (PropertyPanelFactorySPI factory : instances) {
			if (factory.canHandleSemanticAnnotation(semanticAnnotationProfile)) {
				chosenFactory = factory;
				inputComponent = factory.getInputComponent(semanticAnnotationProfile, statement);
				annotationPanel = getPropertyPanel(SemanticAnnotationUtils.getDisplayName(semanticAnnotationProfile.getPredicate()), inputComponent);
				break;
			}
		}

		if (annotationPanel != null) {
			int answer = JOptionPane.showConfirmDialog(null, annotationPanel, "Add/change annotation", JOptionPane.OK_CANCEL_OPTION);
			if (answer == JOptionPane.OK_OPTION) {
				RDFNode response = chosenFactory.getNewTargetNode(inputComponent);
				if (response != null) {
					if (statement != null) {
						semanticAnnotationContextualView.changeStatement(statement, semanticAnnotationProfile.getPredicate(), response);
					}
					else {
						semanticAnnotationContextualView.addStatement(semanticAnnotationProfile.getPredicate(),
								response);
					}
				}
			}
		} else {
			JOptionPane.showMessageDialog(null, "Unable to handle annotation", "Annotation problem", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public static JPanel getPropertyPanel(
			String displayName,
			JComponent inputComponent) {
		JPanel result = new JPanel();
		result.setLayout(new BorderLayout());
		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBorder(new EmptyBorder(5, 5, 0, 0));
		messagePanel.setBackground(Color.WHITE);
		result.add(messagePanel, BorderLayout.NORTH);

		JLabel inputLabel = new JLabel("Enter a value for the annotation");
		inputLabel.setBackground(Color.WHITE);
		Font baseFont = inputLabel.getFont();
		inputLabel.setFont(baseFont.deriveFont(Font.BOLD));
		messagePanel.add(inputLabel, BorderLayout.NORTH);

		JTextArea messageText = new JTextArea("Enter a value for the annotation '"
				+ displayName
				+ "'");
		messageText.setMargin(new Insets(5, 10, 10, 10));
		messageText.setMinimumSize(new Dimension(0, 30));
		messageText.setFont(baseFont.deriveFont(11f));
		messageText.setEditable(false);
		messageText.setFocusable(false);
		messagePanel.add(messageText, BorderLayout.CENTER);
		
		result.add(new JScrollPane(inputComponent), BorderLayout.CENTER);
		return result;
	}



}
