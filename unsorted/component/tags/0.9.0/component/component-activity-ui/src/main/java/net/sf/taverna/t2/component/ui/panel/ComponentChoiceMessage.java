/**
 * 
 */
package net.sf.taverna.t2.component.ui.panel;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;

/**
 * @author alanrw
 *
 */
public class ComponentChoiceMessage {

	private final Component chosenComponent;
	private final ComponentFamily componentFamily;

	public ComponentChoiceMessage(ComponentFamily componentFamily, Component chosenComponent) {
		this.componentFamily = componentFamily;
		this.chosenComponent = chosenComponent;
	}

	/**
	 * @return the chosenComponent
	 */
	public Component getChosenComponent() {
		return chosenComponent;
	}

	/**
	 * @return the componentFamily
	 */
	public ComponentFamily getComponentFamily() {
		return componentFamily;
	}

}
