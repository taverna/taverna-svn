// Copyright (C) 2008 The University of Manchester, University of Southampton
// and Cardiff University
package net.sf.taverna.t2.component.registry.standard.myexpclient;

import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.Type.TAG;

import java.io.Serializable;

/**
 * @author Jiten Bhagat, Sergejs Aleksejevs
 */
public class Tag extends Resource implements Serializable {
	private static final long serialVersionUID = -8199085099046578707L;
	private String tagName;
	private int count;

	public Tag() {
		super(TAG);
	}

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

	/**
	 * This makes sure that things like instanceOf() and remove() in List
	 * interface work properly - this way resources are treated to be the same
	 * if they store identical data, rather than they simply hold the same
	 * reference.
	 */
	@Override
	public boolean equals(Object other) {
		// could only be equal to another Tag object, not anything else
		if (!(other instanceof Tag))
			return false;

		/*
		 * 'other' object is a Tag; equality is based on the data stored in the
		 * current and 'other' Tag instances
		 */
		Tag otherTag = (Tag) other;
		return count == otherTag.count && tagName.equals(otherTag.tagName);
	}

	@Override
	public String toString() {
		return "Tag (" + tagName + ", " + count + ")";
	}

	/**
	 * A helper method to return a set of API elements that are needed to
	 * satisfy request of a particular type - e.g. creating a listing of
	 * resources or populating full preview, etc.
	 * 
	 * @param requestType
	 *            A constant value from Resource class.
	 * @return Comma-separated string containing values of required API
	 *         elements.
	 */
	@SuppressWarnings("incomplete-switch")
	public static String getRequiredAPIElements(RequestType requestType) {
		String elements = "";

		/*
		 * cases higher up in the list are supersets of those that come below -
		 * hence no "break" statements are required, because 'falling through'
		 * the switch statement is the desired behaviour in this case
		 */
		switch (requestType) {
		case DEFAULT:
			elements += ""; // no change needed - defaults will be used
		}

		return elements;
	}

	/**
	 * Instantiates a Tag object from action command string that is used to
	 * trigger tag search events in the plugin. These action commands should
	 * look like "tag:<tag_name>".
	 * 
	 * @param actionCommand
	 *            The action command to parse.
	 * @return A Tab object instance or null if action command was invalid.
	 */
	public static Tag instantiateTagFromActionCommand(String actionCommand) {
		if (!actionCommand.startsWith("tag:"))
			return null;

		// instantiate the Tag object, strip out the leading "tag:" and
		// return result
		Tag t = new Tag();
		t.setTagName(actionCommand.replaceFirst("tag:", ""));
		return t;
	}

}
