/**
 * 
 */
package net.sf.taverna.t2.component.annotation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.AbstractBorder;

import com.hp.hpl.jena.rdf.model.Statement;

/**
 * @author alanrw
 *
 */
public class UnrecognizedStatementPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 513310371296907202L;

	public UnrecognizedStatementPanel(Statement statement) {
		super();
		setLayout(new BorderLayout());
		setBorder(new AbstractBorder() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 4069500537350893389L;

			@Override
			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				g.setColor(Color.GRAY);
				g.drawLine(x, y+height-1, x+width-1, y+height-1);
			}
		});
		this.add(new JLabel("Unable to find " + statement.getPredicate().toString() + " in the profile"));
	}

}
