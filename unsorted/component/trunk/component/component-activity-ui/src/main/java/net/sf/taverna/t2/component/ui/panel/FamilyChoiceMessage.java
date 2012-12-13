/**
 * 
 */
package net.sf.taverna.t2.component.ui.panel;

import net.sf.taverna.t2.component.registry.ComponentFamily;

/**
 * @author alanrw
 *
 */
public class FamilyChoiceMessage {

	private final ComponentFamily chosenFamily;

	public FamilyChoiceMessage(ComponentFamily chosenFamily) {
		this.chosenFamily = chosenFamily;
	}

	/**
	 * @return the chosenFamily
	 */
	public ComponentFamily getChosenFamily() {
		return chosenFamily;
	}

}
