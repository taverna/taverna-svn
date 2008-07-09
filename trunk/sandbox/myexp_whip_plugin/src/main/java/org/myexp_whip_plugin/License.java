package org.myexp_whip_plugin;

public class License {
	
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
