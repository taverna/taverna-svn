/**
 * 
 */
package net.sf.taverna.t2.component.ui.panel;

import net.sf.taverna.t2.component.profile.ComponentProfile;

/**
 * @author alanrw
 *
 */
public class ProfileChoiceMessage {

	private final ComponentProfile chosenProfile;

	public ProfileChoiceMessage(ComponentProfile chosenProfile) {
		this.chosenProfile = chosenProfile;
	}

	/**
	 * @return the chosenProfile
	 */
	public ComponentProfile getChosenProfile() {
		return chosenProfile;
	}

}
