/**
 * 
 */
package net.sf.taverna.t2.component.annotation;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import net.sf.taverna.t2.lang.ui.DeselectingButton;
import net.sf.taverna.t2.lang.ui.ReadOnlyTextArea;

import com.hp.hpl.jena.graph.query.regexptrees.PerlPatternParser.SyntaxException;
import com.hp.hpl.jena.n3.turtle.TurtleParseException;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author alanrw
 *
 */
public class TurtleInputPanel extends JPanel {
	
	JTextArea turtleTextArea = new JTextArea(30, 80);
	ReadOnlyTextArea errors = new ReadOnlyTextArea(1, 80)
;
	private OntClass clazz;	
	public TurtleInputPanel(OntClass clazz) {
		super();
		this.clazz = clazz;
		
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(turtleTextArea), BorderLayout.CENTER);
		
		turtleTextArea.setText("<#changeme> a <" + clazz.getURI() + ">\n\n\n.");
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		JButton validateButton = new DeselectingButton(new AbstractAction("Validate") {
			public void actionPerformed(ActionEvent arg0) {
				getContentAsModel();
			}
		});
		buttonPanel.add(errors, BorderLayout.CENTER);
		errors.setOpaque(false);
		buttonPanel.add(validateButton, BorderLayout.EAST);
		this.add (buttonPanel, BorderLayout.SOUTH);
	}

	public OntModel getContentAsModel() {
		OntModel result = ModelFactory.createOntologyModel();
		try {
			SemanticAnnotationUtils.populateModelFromString(result, getContentAsString());
			
			// Check it is not still called changeme
			List<Individual> individuals = result.listIndividuals(clazz).toList();
			if (individuals.isEmpty()) {
				errors.setText("No valid individuals");
				return null;
			}
			for (Individual i : individuals) {
				String individualUri = i.getURI();
				if (individualUri.endsWith("changeme")) {
					errors.setText ("Name has not been changed");
					return null;
				}
			}
			errors.setText("No errors found");		
			return result;
		} catch (Throwable ex) {	// syntax error?
			errors.setText(ex.getMessage());
			return null;
		}

	}

	public String getContentAsString() {
		return turtleTextArea.getText();
	}
}
