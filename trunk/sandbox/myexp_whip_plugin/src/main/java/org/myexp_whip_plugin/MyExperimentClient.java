package org.myexp_whip_plugin;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class MyExperimentClient {
	
	private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
	
	private static final int EXAMPLE_WORKFLOWS_GROUP_ID = 69;
	
	private Logger logger;
	
	private URL baseUrl;
	
	public MyExperimentClient(Logger logger, URL baseUrl) {
		this.logger = logger;
		this.baseUrl = baseUrl;
	}
	
	public Workflow getWorkflowInfo(int workflowId) {
		Workflow w = null;
		
		try {
			URL workflowUrl = new URL(baseUrl, "workflow.xml?id=" + workflowId + "&all_elements=yes");
			
			w = this.getWorkflowFromXml(workflowId, workflowUrl);
		} catch (Exception e) {
			this.logger.error("Failed to search for workflows", e);
		}
		
		return w;
	}
	
	@SuppressWarnings("unchecked")
	public List<SyndEntry> getLatestWorkflows() throws Exception {
		return this.getLatestWorkflowsRSS().getEntries();
	}
	
	@SuppressWarnings("unchecked")
	public SearchResults searchWorkflows(String keywords) {
		SearchResults results = new SearchResults();
		
		try {
			URL searchUrl = new URL(baseUrl, "search.xml?query=" + keywords + "&type=workflow");
			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(searchUrl);
			
			if (doc != null) {
				List<Element> nodes = doc.getRootElement().getChildren("workflow");
				for (Element e : nodes) {
					int id = this.getWorkflowIdByResourceUrl(e.getAttributeValue("resource"));
					URL url = this.getWorkflowShortXMLUrl(id);
					
					results.getWorkflows().add(this.getWorkflowFromXml(id, url));
				}
			}
		} catch (Exception e) {
			this.logger.error("Failed to search for workflows", e);
		}
		
		logger.debug("Search for keyword(s) '" + keywords + "' returned " + results.getWorkflows().size() + " workflows");
		
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public TagCloud getTagCloud(int size) {
		TagCloud tagCloud = new TagCloud();
		
		try {
			String num = "all";
			if (size > 0) {
				num = "" + size;
			}
				
			URL searchUrl = new URL(baseUrl, "tag-cloud.xml?num=" + num + "&type=workflow");
			
			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(searchUrl);
			
			if (doc != null) {
				List<Element> nodes = doc.getRootElement().getChildren("tag");
				for (Element e : nodes) {
					Resource r = new Resource();
					r.setTitle(e.getText());
					r.setResource(e.getAttributeValue("resource"));
					r.setUri(e.getAttributeValue("uri"));
					
					Tag t = new Tag();
					t.setTitle(e.getText());
					t.setTagName(e.getText());
					t.setResource(e.getAttributeValue("resource"));
					t.setUri(e.getAttributeValue("uri"));
					t.setCount(Integer.parseInt(e.getAttributeValue("count")));
					
					tagCloud.getTags().add(t);
				}
			}
		} catch (Exception e) {
			this.logger.error("Failed to get tag cloud for workflows", e);
		}
		
		logger.debug("Tag cloud retrieval fetched " + tagCloud.getTags().size() + " tags from myExperiment");
		
		return tagCloud;
	}
	
	@SuppressWarnings("unchecked")
	public SearchResults getTagResults(String tagName) {
		SearchResults results = new SearchResults();
		
		try {
			URL searchUrl = new URL(baseUrl, "tagged.xml?tag=" + tagName + "&type=workflow");
			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(searchUrl);
			
			if (doc != null) {
				List<Element> nodes = doc.getRootElement().getChildren("workflow");
				for (Element e : nodes) {
					int id = this.getWorkflowIdByResourceUrl(e.getAttributeValue("resource"));
					URL url = this.getWorkflowShortXMLUrl(id);
					
					results.getWorkflows().add(this.getWorkflowFromXml(id, url));
				}
			}
		} catch (Exception e) {
			this.logger.error("Failed to search for workflows", e);
		}
		
		logger.debug("Tag search for '" + tagName + "' returned " + results.getWorkflows().size() + " workflows");
		
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public List<Workflow> getExampleWorkflows() {
		List<Workflow> workflows = new ArrayList<Workflow>();
		
		try {
			URL exampleWorkflowsGroupUrl = new URL(baseUrl, "group.xml?id=" + EXAMPLE_WORKFLOWS_GROUP_ID + "&all_elements=yes");
			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(exampleWorkflowsGroupUrl);
			
			if (doc != null) {
				List<Element> nodes = doc.getRootElement().getChild("shared-items").getChildren("workflow");
				for (Element e : nodes) {
					int id = this.getWorkflowIdByResourceUrl(e.getAttributeValue("resource"));
					URL url = this.getWorkflowShortXMLUrl(id);
					
					workflows.add(this.getWorkflowFromXml(id, url));
				}
			}
		} catch (Exception e) {
			this.logger.error("Failed to retrieve example workflows", e);
		}
		
		logger.debug(workflows.size() + " example workflows retrieved from myExperiment");
		
		return workflows;
	}
	
	public int getWorkflowIdByResourceUrl(String resourceUrl) {
		String [] s = resourceUrl.split("/");
		return Integer.parseInt(s[s.length-1]);
	}
	
	public URL getWorkflowDownloadURL(int workflowId) throws MalformedURLException {
		return new URL(this.baseUrl, "workflows/" + workflowId + "/download");
	}
	
	private SyndFeed getLatestWorkflowsRSS() throws Exception {
		URL feedUrl = new URL(baseUrl, "workflows.rss");
		
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = input.build(new XmlReader(feedUrl));
		
		return feed;
	}
	
	private URL getWorkflowShortXMLUrl(int workflowId) throws MalformedURLException  {
		return new URL(baseUrl, "workflow.xml?id=" + workflowId + "&elements=id,title,description");
	}
	
	@SuppressWarnings("unchecked")
	private Workflow getWorkflowFromXml(int workflowId, URL workflowXmlUrl) {
		Workflow w = null;
		
		if (workflowXmlUrl != null) {
			try {
				w = new Workflow();
				
				SAXBuilder parser = new SAXBuilder();
				Document doc = parser.build(workflowXmlUrl);
				
				Element root = doc.getRootElement();
				
				// Uri
				w.setUri(root.getAttributeValue("uri"));
				
				// Resource Uri
				w.setResource(root.getAttributeValue("resource"));
				
				// Version
				w.setVersion(Integer.parseInt(root.getAttributeValue("version")));
				
				// Id
				String id = root.getChildText("id");
				if (id == null || id.equals("")) {
					id = "" + workflowId;
				}
				w.setId(Integer.parseInt(id));
				
				// Title
				w.setTitle(root.getChildText("title"));
				
				// Description
				w.setDescription(root.getChildText("description"));
				
				// Uploader
				Element uploaderElement = root.getChild("uploader");
				if (uploaderElement != null) {
					User uploader = new User();
					uploader.setUri(uploaderElement.getAttributeValue("uri"));
					uploader.setResource(uploaderElement.getAttributeValue("resource"));
					uploader.setTitle(uploaderElement.getText());
					uploader.setName(uploaderElement.getText());
					w.setUploader(uploader);
				}
				
				// Created at
				String createdAt = root.getChildText("created-at");
				if (createdAt != null && !createdAt.equals("")) {
					w.setCreatedAt(DATE_FORMATTER.parse(createdAt));
				}
				
				// Updated at
				String updatedAt = root.getChildText("updated-at");
				if (updatedAt != null && !updatedAt.equals("")) {
					w.setUpdatedAt(DATE_FORMATTER.parse(updatedAt));
				}
				
				// Preview
				String preview = root.getChildText("preview");
				if (preview != null && !preview.equals("")) {
					w.setPreview(new URI(preview));
				}
				
				// Thumbnail
				String thumbnail = root.getChildText("thumbnail");
				if (thumbnail != null && !thumbnail.equals("")) {
					w.setThumbnail(new URI(thumbnail));
				}
				
				// Thumbnail (big)
				String thumbnailBig = root.getChildText("thumbnail-big");
				if (thumbnailBig != null && !thumbnailBig.equals("")) {
					w.setThumbnailBig(new URI(thumbnailBig));
				}
				
				// SVG
				String svg = root.getChildText("svg");
				if (svg != null && !svg.equals("")) {
					w.setSvg(new URI(svg));
				}
				
				// License
				w.setLicense(License.getInstance(root.getChildText("license-type")));
				
				// Content URI
				String contentUri = root.getChildText("content-uri");
				if (contentUri != null && !contentUri.equals("")) {
					w.setContentUri(new URI(contentUri));
				}
				
				// Content type
				w.setContentType(root.getChildText("content-type"));
				
				// Tags
				Element tagsElement = root.getChild("tags");
				if (tagsElement != null) {
					List<Element> tagNodes = tagsElement.getChildren();
					for (Element e : tagNodes) {
						Tag t = new Tag();
						t.setTitle(e.getText());
						t.setTagName(e.getText());
						t.setResource(e.getAttributeValue("resource"));
						t.setUri(e.getAttributeValue("uri"));
						
						w.getTags().add(t);
					}
				}
				
				// Credits
				Element creditsElement = root.getChild("credits");
				if (creditsElement != null) {
					List<Element> creditsNodes =  creditsElement.getChildren();
					for (Element e : creditsNodes) {
						User u = new User();
						u.setUri(e.getAttributeValue("uri"));
						u.setResource(e.getAttributeValue("resource"));
						u.setTitle(e.getText());
						u.setName(e.getText());
						
						w.getCredits().add(u);
					}
				}
				
				logger.debug("Found information for worklow with ID: " + w.getId() + ", Title: " + w.getTitle());
			} catch (Exception e) {
				this.logger.error("Failed midway through creating workflow object from XML", e);
			}
		}
		
		return w;
	}
}
