// Copyright (C) 2008 The University of Manchester, University of Southampton and Cardiff University 
package net.sf.taverna.t2.ui.perspectives.myexperiment.model;

import java.io.Serializable;

/*
 * @author Jiten Bhagat
 */
public class License implements Serializable
{	
	private String type;
	
	private String text;
	
	private String link;
	
	private License() {
		
	}
	
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
			return new License(type, "Creative Commons Attribution-NoDerivs 3.0 License", "http://creativecommons.org/licenses/by-nd/3.0/");
		}
		else if (type.equalsIgnoreCase("by")) {
			return new License(type, "Creative Commons Attribution 3.0 License", "http://creativecommons.org/licenses/by/3.0/");
		}
		else if (type.equalsIgnoreCase("by-sa")) {
			return new License(type, "Creative Commons Attribution-Share Alike 3.0 License", "http://creativecommons.org/licenses/by-sa/3.0/");
		}
		else {
			return null;
		}
	}
}
