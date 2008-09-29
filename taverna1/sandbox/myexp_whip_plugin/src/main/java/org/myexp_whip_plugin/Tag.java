// Copyright (C) 2008 The University of Manchester, University of Southampton and Cardiff University
package org.myexp_whip_plugin;

/*
 * @author Jiten Bhagat
 */
public class Tag extends Resource {
	
	private String tagName;
	
	private int count;

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
