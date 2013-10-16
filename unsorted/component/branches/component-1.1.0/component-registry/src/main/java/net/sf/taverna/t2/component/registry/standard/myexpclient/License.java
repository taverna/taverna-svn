// Copyright (C) 2008 The University of Manchester, University of Southampton
// and Cardiff University
package net.sf.taverna.t2.component.registry.standard.myexpclient;


/*
 * @author Jiten Bhagat, Emmanuel Tagarira
 */
public enum License {
	BY_ND("by-nd", "Creative Commons Attribution-NoDerivs 3.0 License",
			"http://creativecommons.org/licenses/by-nd/3.0/"), BY("by",
			"Creative Commons Attribution 3.0 License",
			"http://creativecommons.org/licenses/by/3.0/"), BY_SA("by-sa",
			"Creative Commons Attribution-Share Alike 3.0 License",
			"http://creativecommons.org/licenses/by-sa/3.0/"), BY_NC_ND(
			"by-nc-nd",
			"Creative Commons Attribution-Noncommercial-NoDerivs 3.0 License",
			"http://creativecommons.org/licenses/by-nc-nd/3.0/"), BY_NC(
			"by-nc", "Creative Commons Attribution-Noncommercial 3.0 License",
			"http://creativecommons.org/licenses/by-nc/3.0/"), BY_NC_SA(
			"by-nc-sa",
			"Creative Commons Attribution-Noncommercial-Share Alike 3.0 License",
			"http://creativecommons.org/licenses/by-nc-sa/3.0/");
	private String type;
	private String text;
	private String link;
	public static License[] SUPPORTED_TYPES = { BY_ND, BY, BY_SA, BY_NC_ND,
			BY_NC, BY_NC_SA };
	public static License DEFAULT_LICENSE = BY_SA;

	private License(String type, String text, String link) {
		this.type = type;
		this.text = text;
		this.link = link;
	}

	public String getType() {
		return type;
	}

	public String getText() {
		return text;
	}

	public String getLink() {
		return link;
	}

	public static License getInstance(String type) {
		if (type == null)
			return null;

		if (type.equalsIgnoreCase("by-nd")) {
			return BY_ND;
		} else if (type.equalsIgnoreCase("by")) {
			return BY;
		} else if (type.equalsIgnoreCase("by-sa")) {
			return BY_SA;
		} else if (type.equalsIgnoreCase("by-nc-nd")) {
			return BY_NC_ND;
		} else if (type.equalsIgnoreCase("by-nc")) {
			return BY_NC;
		} else if (type.equalsIgnoreCase("by-nc-sa")) {
			return BY_NC_SA;
		} else {
			return null;
		}
	}
}
