/**
 * 
 */
package net.sf.taverna.t2.component.ui.panel;

import net.sf.taverna.t2.component.api.Family;

/**
 * @author alanrw
 * 
 */
public class FamilyChoiceMessage {

	private final Family chosenFamily;

	public FamilyChoiceMessage(Family chosenFamily) {
		this.chosenFamily = chosenFamily;
	}

	/**
	 * @return the chosenFamily
	 */
	public Family getChosenFamily() {
		return chosenFamily;
	}

}
