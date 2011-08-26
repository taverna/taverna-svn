package net.sf.taverna.t2.annotation;

public enum CurationEventType {

	/**
	 * The curation event asserts that the event it is attached to was correct,
	 * effectively signing off an approval on the attached event.
	 */
	VALIDATION,

	/**
	 * The curation event repudiates the information in the attached event,
	 * denying its validity.
	 */
	REPUDIATION,

	/**
	 * The curation event neither validates nor repudiates the information in
	 * the attached event.
	 */
	NEUTRAL;

}
