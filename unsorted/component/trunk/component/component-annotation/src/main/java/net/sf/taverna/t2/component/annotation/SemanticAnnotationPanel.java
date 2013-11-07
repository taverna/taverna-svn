package net.sf.taverna.t2.component.annotation;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.Color.WHITE;
import static java.awt.Font.BOLD;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.SOUTHEAST;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.String.format;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils.getDisplayName;
import static net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils.getObjectName;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;
import net.sf.taverna.t2.lang.ui.DeselectingButton;
import net.sf.taverna.t2.spi.SPIRegistry;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

public class SemanticAnnotationPanel extends JPanel {
	private static final long serialVersionUID = -5949183295606132775L;

	private final AbstractSemanticAnnotationContextualView semanticAnnotationContextualView;
	private final SemanticAnnotationProfile semanticAnnotationProfile;
	private final Set<Statement> statements;
	protected final SPIRegistry<PropertyPanelFactorySPI> propertyPanelFactoryRegistry = new SPIRegistry<PropertyPanelFactorySPI>(
			PropertyPanelFactorySPI.class);
	private final boolean allowChange;
	private final PropertyPanelFactorySPI bestFactory;

	public SemanticAnnotationPanel(
			AbstractSemanticAnnotationContextualView semanticAnnotationContextualView,
			SemanticAnnotationProfile semanticAnnotationProfile,
			Set<Statement> statements, boolean allowChange) {
		this.semanticAnnotationContextualView = semanticAnnotationContextualView;
		this.semanticAnnotationProfile = semanticAnnotationProfile;
		this.statements = statements;
		this.allowChange = allowChange;
		this.bestFactory = findBestPanelFactory();
		initialise();
	}

	private void initialise() {
		setLayout(new GridBagLayout());
		// setBorder(new AbstractBorder() {
		// @Override
		// public void paintBorder(Component c, Graphics g, int x, int y, int
		// width, int height) {
		// g.setColor(Color.GRAY);
		// g.drawLine(x, y+height-1, x+width-1, y+height-1);
		// }
		// });

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = SOUTHEAST;
		c.fill = BOTH;
		c.weightx = 1;
		c.gridx = 0;

		OntProperty predicate = semanticAnnotationProfile.getPredicate();
		c.gridwidth = 3;
		JLabel label = new JLabel(format("Annotation type : %s",
				getDisplayName(predicate)));
		label.setBorder(new EmptyBorder(5, 5, 5, 5));
		label.setBackground(WHITE);
		label.setOpaque(true);
		add(label, c);

		c.insets = new Insets(7, 0, 0, 0);
		c.anchor = EAST;
		c.fill = HORIZONTAL;
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
				if (bestFactory != null) {
					add(bestFactory.getDisplayComponent(
							semanticAnnotationProfile, statement), c);
				} else {
					JTextArea value = new JTextArea(getObjectName(statement));
					value.setLineWrap(true);
					value.setWrapStyleWord(true);
					value.setEditable(false);
					value.setBackground(WHITE);
					value.setOpaque(true);
					value.setBorder(new EmptyBorder(2, 4, 2, 4));
					add(value, c);
				}
				if (allowChange) {
					c.gridx = 1;
					c.weightx = 0;
					add(createChangeButton(statement), c);

					c.gridx = 2;
					add(createDeleteButton(statement), c);
				}
			}
		}

		if (allowChange
				&& !enoughAlready(statements,
						semanticAnnotationProfile.getMaxOccurs())) {
			c.gridx = 0;
			c.gridwidth = 3;
			c.anchor = SOUTHEAST;
			c.fill = NONE;
			add(createAddButton(), c);
		}
	}

	private boolean enoughAlready(Set<Statement> statements, Integer maxOccurs) {
		return (maxOccurs != null) && (statements.size() >= maxOccurs);
	}

	private JButton createChangeButton(final Statement statement) {
		return new DeselectingButton("Change", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addOrChangeAnnotation(statement);
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

	private JButton createAddButton() {
		return new DeselectingButton("Add Annotation", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addOrChangeAnnotation(null);
			}
		});
	}

	private void addOrChangeAnnotation(Statement statement) {
		JPanel annotationPanel = null;
		JComponent inputComponent = null;

		if (bestFactory != null) {
			inputComponent = bestFactory.getInputComponent(
					semanticAnnotationProfile, statement);
			annotationPanel = getPropertyPanel(
					getDisplayName(semanticAnnotationProfile.getPredicate()),
					inputComponent);
		}

		if (annotationPanel == null) {
			showMessageDialog(null, format("Unable to handle %s",
					semanticAnnotationProfile.getPredicateString()),
					"Annotation problem", ERROR_MESSAGE);
			return;
		}

		int answer = showConfirmDialog(null, annotationPanel,
				"Add/change annotation", OK_CANCEL_OPTION);
		if (answer == OK_OPTION) {
			RDFNode response = bestFactory.getNewTargetNode(statement,
					inputComponent);
			if (response == null)
				return;
			if (statement != null)
				semanticAnnotationContextualView.changeStatement(statement,
						semanticAnnotationProfile.getPredicate(), response);
			else
				semanticAnnotationContextualView.addStatement(
						semanticAnnotationProfile.getPredicate(), response);
		}
	}

	private PropertyPanelFactorySPI findBestPanelFactory() {
		PropertyPanelFactorySPI result = null;
		List<PropertyPanelFactorySPI> instances = propertyPanelFactoryRegistry
				.getInstances();
		int currentRating = MIN_VALUE;
		for (PropertyPanelFactorySPI factory : instances) {
			int ratingForSemanticAnnotation = factory
					.getRatingForSemanticAnnotation(semanticAnnotationProfile);
			if (ratingForSemanticAnnotation > currentRating) {
				currentRating = ratingForSemanticAnnotation;
				result = factory;
			}
		}
		return result;
	}

	public static JPanel getPropertyPanel(String displayName,
			Component inputComponent) {
		JPanel result = new JPanel();
		result.setLayout(new BorderLayout());
		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBorder(new EmptyBorder(5, 5, 0, 0));
		messagePanel.setBackground(WHITE);
		result.add(messagePanel, NORTH);

		JLabel inputLabel = new JLabel("Enter a value for the annotation");
		inputLabel.setBackground(WHITE);
		Font baseFont = inputLabel.getFont();
		inputLabel.setFont(baseFont.deriveFont(BOLD));
		messagePanel.add(inputLabel, NORTH);

		JTextArea messageText = new JTextArea(format(
				"Enter a value for the annotation '%s'", displayName));
		messageText.setMargin(new Insets(5, 10, 10, 10));
		messageText.setMinimumSize(new Dimension(0, 30));
		messageText.setFont(baseFont.deriveFont(11f));
		messageText.setEditable(false);
		messageText.setFocusable(false);
		messagePanel.add(messageText, CENTER);

		result.add(new JScrollPane(inputComponent), CENTER);
		return result;
	}
}
