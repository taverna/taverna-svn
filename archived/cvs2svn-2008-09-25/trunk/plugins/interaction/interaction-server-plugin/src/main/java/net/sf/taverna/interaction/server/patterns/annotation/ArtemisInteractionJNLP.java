/*
 * Copyright 2005 Anders Lanzen, Computational Biology Group, BCCS, Univerity of Bergen
 *
 */
package net.sf.taverna.interaction.server.patterns.annotation;

import java.util.HashMap;

/**
 * Generates a new Java Network Launching Protocol (JNLP) XML Document for
 * launching the modified Artemis viewer.
 * 
 * @author andersl
 */

public class ArtemisInteractionJNLP extends JNLPDocument {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The URL of the server hosting the JAR files needed by the application
	 */
	static final String jarLocation = "http://www.bioinfo.no/services/annotation/artemis_interaction/";

	/**
	 * Create JNLP document with the appropriate parameters for this interaction
	 * request
	 * 
	 * @param interactionID
	 *            the ID of the interaction request
	 * @param interactionURL
	 *            the URL of the interaction server
	 */
	public ArtemisInteractionJNLP(String interactionID, String interactionURL) {

		super();

		this.setCodeBase(jarLocation);
		String interactionUrlTrimmed = (interactionURL.charAt(interactionURL
				.length() - 1) == '/') ? interactionURL : interactionURL + "/";
		// this.setSelfLocation(interactionUrlTrimmed +
		// "client/download?id="+interactionID+"&launch=Artemis");
		// Although formally required, self reference is not set since this
		// casues a bug in javaws when launching for the second time
		this.addTitle("Artemis for Interaction Server ID " + interactionID);
		this.addVendor("Bergen Center for Computational Science");
		this.addApplicationWebsite("http://www.sanger.ac.uk/Software/Artemis");
		this
				.addDescription("Modified version of Artemis for Interaction Server");
		this
				.addDescription(
						"Artemis is a DNA sequence viewer and annotation tool. "
								+ "This modified version automatically downloads data relevant for the user "
								+ "from an Interaction Server for review or modification.",
						"short");
		this.addProperty("interaction.id", interactionID);
		this.addProperty("interaction.url", interactionURL);
		this.setMainClass("no.uib.cbu.artemis_interaction.ArtemisLauncher");
		this.addJar("artemis_interaction.jar");

		HashMap j2se = new HashMap();
		j2se.put("version", "1.4.2+");
		j2se.put("initial-heap-size", "32m");
		j2se.put("max-heap-size", "200m");
		this.addJ2SE(j2se);

		this.setAllPermissions(true);
		this.setOfflineAllowed(false);
	}
}
