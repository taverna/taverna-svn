// Copyright (C) 2008 The University of Manchester, University of Southampton and Cardiff University
package org.myexp_whip_plugin;

/*
 * @author Jiten Bhagat
 */
public class Resource {
	
	private String uri;
	
	private String resource;
	
	private String title;

	public String getUri() {
		return uri;
	}

	public String getResource() {
		return resource;
	}

	public String getTitle() {
		return title;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
