/**
 * 
 */
package net.sf.taverna.t2.component.ui.panel;

import net.sf.taverna.t2.component.api.Registry;

/**
 * @author alanrw
 * 
 */
public class RegistryChoiceMessage {

	private final Registry chosenRegistry;

	public RegistryChoiceMessage(Registry chosenRegistry) {
		this.chosenRegistry = chosenRegistry;
	}

	/**
	 * @return the chosenRegistry
	 */
	public Registry getChosenRegistry() {
		return chosenRegistry;
	}

}
