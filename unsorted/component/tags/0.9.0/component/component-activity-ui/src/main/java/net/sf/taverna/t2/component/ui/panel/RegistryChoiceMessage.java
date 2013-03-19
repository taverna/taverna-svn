/**
 * 
 */
package net.sf.taverna.t2.component.ui.panel;

import net.sf.taverna.t2.component.registry.ComponentRegistry;

/**
 * @author alanrw
 *
 */
public class RegistryChoiceMessage {

	private final ComponentRegistry chosenRegistry;

	public RegistryChoiceMessage(ComponentRegistry chosenRegistry) {
		this.chosenRegistry = chosenRegistry;
	}

	/**
	 * @return the chosenRegistry
	 */
	public ComponentRegistry getChosenRegistry() {
		return chosenRegistry;
	}

}
