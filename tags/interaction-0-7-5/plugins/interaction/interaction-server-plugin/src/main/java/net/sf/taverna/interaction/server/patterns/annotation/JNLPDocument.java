/*
 * Copyright 2005 Anders Lanzen, Computational Biology Group, BCCS, Univerity of Bergen
 *
 */
package net.sf.taverna.interaction.server.patterns.annotation;

import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.jdom.Element;
import org.jdom.filter.ElementFilter;

/**
 * An empty well-formed JNLP XML document for launching a Java Web Start
 * Application, including all the required root elements. It's main purpose is
 * to provide and interface to creating new JNLP documents. Does not support
 * multiple platforms or applets. <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/javaws/developersguide/syntax.html">
 * More information about the JNLP syntax.</a>
 * 
 * @author andersl
 * 
 */
public abstract class JNLPDocument extends org.jdom.Document {

	private Element root = new Element("jnlp");;

	private Element information = new Element("information");

	private Element security = new Element("security");

	private Element resources = new Element("resources");

	private Element application_desc = new Element("application-desc");

	/**
	 * Create an empty well-formed JNLP XML document for launching a Java Web
	 * Start Application, including all the required root elements.
	 */

	public JNLPDocument() {
		super();
		root = new Element("jnlp");
		this.setRootElement(root);
		root.setAttribute("spec", "1.0+");
		root.addContent(information);
		root.addContent(security);
		root.addContent(resources);
		root.addContent(application_desc);
	}

	/**
	 * Returns the application-desc element.
	 */
	public Element getApplication_desc() {
		return this.application_desc;
	}

	/**
	 * Returns the information element.
	 */
	public Element getInformation() {
		return this.information;
	}

	/**
	 * Returns the resources element.
	 */
	public Element getResources() {
		return this.resources;
	}

	/**
	 * Returns the security element.
	 */
	public Element getSecurity() {
		return this.security;
	}

	/**
	 * Returns whether or not the application declares a requirement of
	 * all-permissions
	 */
	public boolean hasAllPermissions() {
		return (security.getChild("all-permissions") != null);
	}

	/**
	 * Returns whether or not the application may run offline
	 */
	public boolean offlineAllowed() {
		return (information.getChild("offline-allowed") != null);
	}

	/**
	 * Add the website address for information about this application.
	 */
	public void addApplicationWebsite(String url) {
		Element homepage = new Element("homepage");
		homepage.setAttribute("href", url);
		information.addContent(homepage);
	}

	/**
	 * Set the Code Base Location (URL) where the JAR files of this JWS
	 * application reside
	 */
	public void setCodeBase(String CodeLocation) {
		root.setAttribute("codebase", CodeLocation);
	}

	/**
	 * Set the Self Location (URL) of this JNLP document (Note: this is not
	 * strictly required and may lead to problems with caching of dynamically
	 * generated JNLPS such as this one when used.
	 */
	public void setSelfLocation(String url) {
		root.setAttribute("href", url);
	}

	/**
	 * Add title of the JWS application to the Information Element
	 */

	public void addTitle(String title) {
		Element new_element = new Element("title");
		new_element.addContent(title);
		information.addContent(new_element);
	}

	/**
	 * Add description element with a "kind" attribute to Information Element
	 * 
	 * @param description
	 *            the description text
	 * @param kind
	 *            the description type - accepted values are "one-line", "short"
	 *            and "tooltip"
	 */

	public void addDescription(String description, String kind) {
		Element new_element = new Element("decsription");
		new_element.addContent(description);
		information.addContent(new_element);
		new_element.setAttribute("kind", kind);
	}

	/**
	 * Add description element without a "kind" attribute to Information Element
	 */

	public void addDescription(String description) {
		Element new_element = new Element("decsription");
		new_element.addContent(description);
		information.addContent(new_element);
	}

	/**
	 * Add a vendor element with the name of the vendor of the application
	 * launched
	 */

	public void addVendor(String vendor) {
		Element new_element = new Element("vendor");
		new_element.addContent(vendor);
		information.addContent(new_element);
	}

	/**
	 * Add an icon during JWS loading.
	 */

	public void addIcon(String location, boolean splash) {
		Element icon = new Element("icon");
		if (splash)
			icon.setAttribute("kind", "splash");
		icon.setAttribute("href", location);
		information.addContent(icon);
	}

	/**
	 * Add a j2se attribute to resources with the attributes specified in a Map
	 */

	public void addJ2SE(Map attributes) {
		Element j2se = new Element("j2se");
		resources.addContent(j2se);
		Iterator i = attributes.keySet().iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			j2se.setAttribute(key, ((String) attributes.get(key)));
		}
	}

	/**
	 * Add JAR required for the JWS Application
	 */
	public void addJar(String location) {
		Element new_element = new Element("jar");
		new_element.setAttribute("href", location);
		resources.addContent(new_element);
	}

	/**
	 * Add arguments to be sent to the main method of the JWS application
	 * 
	 * @param args
	 *            all arguments sent as a string
	 */

	public void addArguments(String args) {
		StringTokenizer st = new StringTokenizer(args);
		while (st.hasMoreTokens()) {
			Element newArg = new Element("argument");
			application_desc.addContent(newArg);
		}
	}

	/**
	 * Add System property to be sent to the JWS Application
	 */
	public void addProperty(String name, String value) {
		Element prop = new Element("property");
		prop.setAttribute("name", name);
		prop.setAttribute("value", value);
		resources.addContent(prop);
	}

	/**
	 * Set Main Class for launch of application
	 * 
	 * @param mainClass
	 *            the class whose Main method will be executed
	 */

	public void setMainClass(String mainClass) {
		application_desc.setAttribute("main-class", mainClass);
	}

	/**
	 * Set whether the JWS application is allowed to run offline. Default false.
	 * Inserts the offline-allowed element if true.
	 */
	public void setOfflineAllowed(boolean offline) {
		if (this.offlineAllowed() && !offline) {
			// remove security element
			information.removeContent(new ElementFilter("offline-allowed"));
		} else if (!this.offlineAllowed() && offline) {
			// add security element
			Element off = new Element("offline-allowed");
			information.addContent(off);
		}
	}

	/**
	 * Set the value of all-permissions, i.e. whether the application needs full
	 * access to the users computer. Default is false. If true, an
	 * all-permissions element enclosed in a security element must be created.
	 * 
	 * @param value
	 *            true if the applications should request all permissions, false
	 *            otherwise
	 */
	public void setAllPermissions(boolean value) {
		if (this.hasAllPermissions() && value == false) {
			// remove security element
			security.removeContent(new ElementFilter("all-permissions"));
		} else if (!this.hasAllPermissions() && value == true) {
			// add security element
			Element allP = new Element("all-permissions");
			security.addContent(allP);
		}
	}
}
