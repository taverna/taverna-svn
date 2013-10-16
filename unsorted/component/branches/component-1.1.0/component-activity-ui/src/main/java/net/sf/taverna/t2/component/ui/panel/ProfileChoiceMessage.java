/**
 * 
 */
package net.sf.taverna.t2.component.ui.panel;

import net.sf.taverna.t2.component.api.Profile;

/**
 * @author alanrw
 *
 */
public class ProfileChoiceMessage {

	private final Profile chosenProfile;

	public ProfileChoiceMessage(Profile chosenProfile) {
		this.chosenProfile = chosenProfile;
	}

	/**
	 * @return the chosenProfile
	 */
	public Profile getChosenProfile() {
		return chosenProfile;
	}

}
