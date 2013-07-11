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

import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;

/**
 * @author alanrw
 *
 */
public class UnresolveablePredicatePanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6697222230069693882L;

	public UnresolveablePredicatePanel(SemanticAnnotationProfile semanticAnnotationProfile) {
		super();
		setLayout(new BorderLayout());
		setBorder(new AbstractBorder() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -6217562768253967301L;

			@Override
			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				g.setColor(Color.GRAY);
				g.drawLine(x, y+height-1, x+width-1, y+height-1);
			}
		});
		this.add(new JLabel("Unable to resolve " + semanticAnnotationProfile.getPredicateString() + " in the ontology"));
	}

}
