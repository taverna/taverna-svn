/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.taverna.t2.portal.myexperiment;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/*
 * Adapted from package net.sf.taverna.t2.ui.perspectives.myexperiment.StyledHTMLEditorKit.
 */
public class StyledHTMLEditorKit extends HTMLEditorKit {

	private final StyleSheet styleSheet;

	public StyledHTMLEditorKit(StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}

	@Override
	public StyleSheet getStyleSheet() {
		return styleSheet;
	}

}

