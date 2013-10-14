// Copyright (C) 2008 The University of Manchester, University of Southampton
// and Cardiff University
package net.sf.taverna.t2.component.registry.standard.myexpclient;

import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChild;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChildText;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.makeUser;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.retrieveAttributions;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.retrieveComments;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.retrieveCredits;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.retrieveTags;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Jiten Bhagat, Sergejs Aleksejevs
 */
public class Workflow extends Resource {
	private static final long serialVersionUID = 1156851369689820288L;
	public static final String MIME_TYPE_TAVERNA_1 = "application/vnd.taverna.scufl+xml";
	public static final String MIME_TYPE_TAVERNA_2 = "application/vnd.taverna.t2flow+xml";

	private int accessType;

	private int version;
	private User uploader;
	private URI preview;
	private URI thumbnail;
	private URI thumbnailBig;
	private URI svg;
	private License license;

	private String visibleType;
	private String contentType;
	private URI contentUri;
	byte[] content;

	private List<Tag> tags;
	private List<Comment> comments;
	private List<Resource> credits;
	private List<Resource> attributions;
	private Map<String, List<Map<String, String>>> components;

	public Workflow() {
		super();
		this.setItemType(WORKFLOW);
	}

	public Workflow(Element docRootElement) throws URISyntaxException {
		this();
		// Access type
		setAccessType(Util
				.getAccessType(getChild(docRootElement, "privileges")));

		// URI
		setURI(docRootElement.getAttribute("uri"));

		// Resource URI
		setResource(docRootElement.getAttribute("resource"));

		// Version
		String version = docRootElement.getAttribute("version");
		if (version != null && !version.equals(""))
			setVersion(Integer.parseInt(version));

		// Id
		String id = getChildText(docRootElement, "id");
		if (id == null || id.isEmpty())
			throw new RuntimeException("Error while parsing workflow XML data:"
					+ " no ID provided for workflow with title: \""
					+ getChildText(docRootElement, "title") + "\"");
		setID(id);

		// Title
		setTitle(getChildText(docRootElement, "title"));

		// Description
		setDescription(getChildText(docRootElement, "description"));

		// Uploader
		setUploader(makeUser(getChild(docRootElement, "uploader")));

		// Created at
		String createdAt = getChildText(docRootElement, "created-at");
		if (createdAt != null && !createdAt.equals(""))
			setCreatedAt(createdAt);

		// Updated at
		String updatedAt = getChildText(docRootElement, "updated-at");
		if (updatedAt != null && !updatedAt.equals(""))
			setUpdatedAt(updatedAt);

		// Preview
		String preview = getChildText(docRootElement, "preview");
		if (preview != null && !preview.equals(""))
			setPreview(new URI(preview));

		// Thumbnail
		String thumbnail = getChildText(docRootElement, "thumbnail");
		if (thumbnail != null && !thumbnail.equals(""))
			setThumbnail(new URI(thumbnail));

		// Thumbnail (big)
		String thumbnailBig = getChildText(docRootElement, "thumbnail-big");
		if (thumbnailBig != null && !thumbnailBig.equals(""))
			setThumbnailBig(new URI(thumbnailBig));

		// SVG
		String svg = getChildText(docRootElement, "svg");
		if (svg != null && !svg.equals(""))
			setSvg(new URI(svg));

		// License
		setLicense(License.getInstance(getChildText(docRootElement,
				"license-type")));

		// Content URI
		String contentUri = getChildText(docRootElement, "content-uri");
		if (contentUri != null && !contentUri.equals(""))
			setContentUri(new URI(contentUri));

		// Type and Content-Type
		setVisibleType(getChildText(docRootElement, "type"));
		setContentType(getChildText(docRootElement, "content-type"));

		// Tags
		tags = new ArrayList<Tag>();
		tags.addAll(retrieveTags(docRootElement));

		// Comments
		comments = new ArrayList<Comment>(
				retrieveComments(docRootElement, this));

		// Credits
		credits = new ArrayList<Resource>(retrieveCredits(docRootElement));

		// Attributions
		attributions = new ArrayList<Resource>(
				retrieveAttributions(docRootElement));

		// Components
		components = new HashMap<String, List<Map<String, String>>>();
		extractStructure(docRootElement, components);
	}

	public int getAccessType() {
		return this.accessType;
	}

	public void setAccessType(int accessType) {
		this.accessType = accessType;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public User getUploader() {
		return uploader;
	}

	public void setUploader(User uploader) {
		this.uploader = uploader;
	}

	public URI getPreview() {
		return preview;
	}

	public void setPreview(URI preview) {
		this.preview = preview;
	}

	public URI getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(URI thumbnail) {
		this.thumbnail = thumbnail;
	}

	public URI getSvg() {
		return svg;
	}

	public void setSvg(URI svg) {
		this.svg = svg;
	}

	public License getLicense() {
		return license;
	}

	public void setLicense(License license) {
		this.license = license;
	}

	public URI getContentUri() {
		return contentUri;
	}

	public void setContentUri(URI contentUri) {
		this.contentUri = contentUri;
	}

	public String getVisibleType() {
		return this.visibleType;
	}

	public void setVisibleType(String visibleType) {
		this.visibleType = visibleType;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getContent() {
		return this.content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public List<Resource> getCredits() {
		return credits;
	}

	public List<Resource> getAttributions() {
		return this.attributions;
	}

	public Map<String, List<Map<String, String>>> getComponents() {
		return this.components;
	}

	public URI getThumbnailBig() {
		return thumbnailBig;
	}

	public void setThumbnailBig(URI thumbnailBig) {
		this.thumbnailBig = thumbnailBig;
	}

	/**
	 * Determines whether the current instance of the workflow is a Taverna 1 or
	 * Taverna 2 workflow
	 */
	public boolean isTavernaWorkflow() {
		return contentType.equals(MIME_TYPE_TAVERNA_1)
				|| contentType.equals(MIME_TYPE_TAVERNA_2);
	}

	public boolean isTaverna1Workflow() {
		return contentType.equals(MIME_TYPE_TAVERNA_1);
	}

	public boolean isTaverna2Workflow() {
		return contentType.equals(MIME_TYPE_TAVERNA_2);
	}

	/**
	 * A helper method to return a set of API elements that are needed to
	 * satisfy request of a particular type - e.g. creating a listing of
	 * resources or populating full preview, etc.
	 * 
	 * @param iRequestType
	 *            A constant value from Resource class.
	 * @return Comma-separated string containing values of required API
	 *         elements.
	 */
	public static String getRequiredAPIElements(int iRequestType) {
		String elements = "";

		// cases higher up in the list are supersets of those that come below -
		// hence no "break" statements are required, because 'falling through'
		// the switch statement is the desired behaviour in this case
		//
		// cases that follow after the first 'break' statement are to be treated
		// separately - these require individual processing and have nothing to
		// do with joining different elements for various listings / previews
		switch (iRequestType) {
		case REQUEST_FULL_PREVIEW:
			elements += "created-at,updated-at,preview,thumbnail-big,svg,license-type,content-uri,"
					+ "tags,comments,ratings,credits,attributions,components,";
		case REQUEST_FULL_LISTING:
			elements += "uploader,type,";
		case REQUEST_SHORT_LISTING:
			elements += "id,title,thumbnail,description,privileges,content-type";
			break;
		case REQUEST_WORKFLOW_CONTENT_ONLY:
			elements += "type,content-type,content";
			break;
		}

		return elements;
	}

	public static Workflow buildFromXML(Document doc, Logger logger) {
		// if no XML document was supplied, return NULL
		if (doc == null)
			return null;

		// call main method which parses XML document starting from root element
		return buildFromXML(doc.getDocumentElement(), logger);
	}

	// class method to build a workflow instance from XML
	public static Workflow buildFromXML(Element docRootElement, Logger logger) {
		// return null to indicate an error if XML document contains no root
		// element
		if (docRootElement == null)
			return null;

		Workflow w = new Workflow();
		try {
			w = new Workflow(docRootElement);
			logger.debug("Found information for worklow with ID: " + w.getID()
					+ ", Title: " + w.getTitle());
		} catch (Exception e) {
			logger.error(
					"Failed midway through creating workflow object from XML",
					e);
		}

		// return created workflow instance
		return w;
	}

	private static void extractStructure(Element docRootElement,
			Map<String, List<Map<String, String>>> components) {
		Element componentsElement = getChild(docRootElement, "components");
		if (componentsElement == null)
			return;

		// ** inputs **
		Element sourcesElement = getChild(componentsElement, "sources");
		if (sourcesElement != null) {
			List<Map<String, String>> inputs = new ArrayList<Map<String, String>>();
			NodeList sourcesNodes = sourcesElement.getChildNodes();
			for (int i = 0; i < sourcesNodes.getLength(); i++) {
				Element e = (Element) sourcesNodes.item(i);
				Map<String, String> curInput = new HashMap<String, String>();
				curInput.put("name", getChildText(e, "name"));
				curInput.put("description", getChildText(e, "description"));
				inputs.add(curInput);
			}

			// put all inputs that were found into the overall component
			// collection
			components.put("inputs", inputs);
		}

		// ** outputs **
		Element outputsElement = getChild(componentsElement, "sinks");
		if (outputsElement != null) {
			List<Map<String, String>> sinks = new ArrayList<Map<String, String>>();
			NodeList outputsNodes = outputsElement.getChildNodes();
			for (int i = 0; i < outputsNodes.getLength(); i++) {
				Element e = (Element) outputsNodes.item(i);
				Map<String, String> curOutput = new HashMap<String, String>();
				curOutput.put("name", getChildText(e, "name"));
				curOutput.put("description", getChildText(e, "description"));
				sinks.add(curOutput);
			}

			// put all outputs that were found into the overall
			// component collection
			components.put("outputs", sinks);
		}

		// ** processors **
		Element processorsElement = getChild(componentsElement, "processors");
		if (processorsElement != null) {
			List<Map<String, String>> processors = new ArrayList<Map<String, String>>();
			NodeList processorsNodes = processorsElement.getChildNodes();
			for (int i = 0; i < processorsNodes.getLength(); i++) {
				Element e = (Element) processorsNodes.item(i);
				Map<String, String> curProcessor = new HashMap<String, String>();
				curProcessor.put("name", getChildText(e, "name"));
				curProcessor.put("type", getChildText(e, "type"));
				curProcessor.put("description", getChildText(e, "description"));
				processors.add(curProcessor);
			}

			// put all processors that were found into the overall
			// component collection
			components.put("processors", processors);
		}

		// ** links **
		Element linksElement = getChild(componentsElement, "links");
		if (linksElement != null) {
			List<Map<String, String>> links = new ArrayList<Map<String, String>>();
			NodeList linksNodes = linksElement.getChildNodes();
			for (int i = 0; i < linksNodes.getLength(); i++) {
				Element e = (Element) linksNodes.item(i);
				Map<String, String> curLink = new HashMap<String, String>();
				String sourcePort = getChildText(e, "source", "port");
				String strSource = getChildText(e, "source", "node")
						+ (sourcePort == null ? "" : ":" + sourcePort);
				curLink.put("source", strSource);
				String sinkPort = getChildText(e, "sink", "port");
				String strSink = getChildText(e, "sink", "node")
						+ (sinkPort == null ? "" : ":" + sinkPort);
				curLink.put("sink", strSink);
				links.add(curLink);
			}

			// put all links that were found into the overall component
			// collection
			components.put("links", links);
		}
	}
}
