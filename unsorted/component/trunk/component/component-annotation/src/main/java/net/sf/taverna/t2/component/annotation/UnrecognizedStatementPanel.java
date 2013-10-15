/**
 * 
 */
package net.sf.taverna.t2.component.annotation;

import static java.lang.String.format;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.hp.hpl.jena.rdf.model.Statement;

/**
 * @author alanrw
 * 
 */
@SuppressWarnings("serial")
public class UnrecognizedStatementPanel extends JPanel {
	public UnrecognizedStatementPanel(Statement statement) {
		setLayout(new BorderLayout());
		setBorder(new GreyBorder());
		add(new JLabel(format("Unable to find %s in the profile",
				statement.getPredicate())));
	}
}
