/**
 * 
 */
package net.sf.taverna.t2.component.annotation;

import static com.hp.hpl.jena.rdf.model.ModelFactory.createOntologyModel;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static java.awt.BorderLayout.SOUTH;
import static net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils.populateModelFromString;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.sf.taverna.t2.lang.ui.DeselectingButton;
import net.sf.taverna.t2.lang.ui.ReadOnlyTextArea;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;

/**
 * @author alanrw
 * 
 */
@SuppressWarnings("serial")
public class TurtleInputPanel extends JPanel {
	JTextArea turtleTextArea = new JTextArea(30, 80);
	ReadOnlyTextArea errors = new ReadOnlyTextArea(1, 80);
	private OntClass clazz;

	public TurtleInputPanel(OntClass clazz) {
		this.clazz = clazz;

		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(turtleTextArea), CENTER);

		turtleTextArea.setText("<#changeme> a <" + clazz.getURI() + ">\n\n\n.");

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		JButton validateButton = new DeselectingButton(new AbstractAction(
				"Validate") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getContentAsModel();
			}
		});
		buttonPanel.add(errors, CENTER);
		errors.setOpaque(false);
		buttonPanel.add(validateButton, EAST);
		this.add(buttonPanel, SOUTH);
	}

	public OntModel getContentAsModel() {
		OntModel result = createOntologyModel();
		try {
			populateModelFromString(result, getContentAsString());

			// Check it is not still called changeme
			List<Individual> individuals = result.listIndividuals(clazz)
					.toList();
			if (individuals.isEmpty()) {
				errors.setText("No valid individuals");
				return null;
			}
			for (Individual i : individuals)
				if (i.getURI().endsWith("changeme")) {
					errors.setText("Name has not been changed");
					return null;
				}

			errors.setText("No errors found");
			return result;
		} catch (Throwable ex) { // syntax error?
			errors.setText(ex.getMessage());
			return null;
		}
	}

	public String getContentAsString() {
		return turtleTextArea.getText();
	}
}
