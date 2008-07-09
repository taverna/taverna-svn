package org.myexp_whip_plugin;

import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.DateFormatter;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.whipplugin.http.Base64;

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
	
	@SuppressWarnings("unchecked")
	public Workflow getWorkflowInfo(int workflowId) {
		Workflow w = null;
		
		try {
			URL workflowUrl = new URL(baseUrl, "workflow.xml?id=" + workflowId + "&all_elements=yes");
			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(workflowUrl);
			
			w = new Workflow();
			
			if (doc != null) {
				Element root = doc.getRootElement();
				
				w.setUri(root.getAttributeValue("uri"));
				w.setResource(root.getAttributeValue("resource"));
				w.setVersion(Integer.parseInt(root.getAttributeValue("version")));
				
				// Id
				w.setId(Integer.parseInt(root.getChildText("id")));
				
				// Title
				w.setTitle(root.getChildText("title"));
				
				// Description
				w.setDescription(root.getChildText("description"));
				
				// Uploader
				Element uploaderElement = root.getChild("uploader");
				User uploader = new User();
				uploader.setUri(uploaderElement.getAttributeValue("uri"));
				uploader.setResource(uploaderElement.getAttributeValue("resource"));
				uploader.setTitle(uploaderElement.getText());
				uploader.setName(uploaderElement.getText());
				w.setUploader(uploader);
				
				// Created at
				w.setCreatedAt(DATE_FORMATTER.parse(root.getChildText("created-at")));
				
				// Updated at
				w.setUpdatedAt(DATE_FORMATTER.parse(root.getChildText("updated-at")));
				
				// Preview
				w.setPreview(new URI(root.getChildText("preview")));
				
				// Thumbnail
				w.setThumbnail(new URI(root.getChildText("thumbnail")));
				
				// Thumbnail (big)
				w.setThumbnailBig(new URI(root.getChildText("thumbnail-big")));
				
				// SVG
				w.setSvg(new URI(root.getChildText("svg")));
				
				// License
				w.setLicense(License.getInstance(root.getChildText("license-type")));
				
				// Content URI
				w.setContentUri(new URI(root.getChildText("content-uri")));
				
				// Content type
				w.setContentType(root.getChildText("content-type"));
				
				// Tags
				List<Element> tagNodes = root.getChild("tags").getChildren();
				for (Element e : tagNodes) {
					Tag t = new Tag();
					t.setTitle(e.getText());
					t.setTagName(e.getText());
					t.setResource(e.getAttributeValue("resource"));
					t.setUri(e.getAttributeValue("uri"));
					
					w.getTags().add(t);
				}
				
				// Credits
				List<Element> creditsNodes =  root.getChild("credits").getChildren();
				for (Element e : creditsNodes) {
					User u = new User();
					u.setUri(e.getAttributeValue("uri"));
					u.setResource(e.getAttributeValue("resource"));
					u.setTitle(e.getText());
					u.setName(e.getText());
					
					w.getCredits().add(u);
				}
				
				logger.debug("Found information for worklow with ID: " + w.getId() + ", Title: " + w.getTitle());
			}
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
					Resource r = new Resource();
					r.setTitle(e.getText());
					r.setResource(e.getAttributeValue("resource"));
					r.setUri(e.getAttributeValue("uri"));
					
					results.getWorkflows().add(r);
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
					Resource r = new Resource();
					r.setTitle(e.getText());
					r.setResource(e.getAttributeValue("resource"));
					r.setUri(e.getAttributeValue("uri"));
					
					results.getWorkflows().add(r);
				}
			}
		} catch (Exception e) {
			this.logger.error("Failed to search for workflows", e);
		}
		
		logger.debug("Tag search for '" + tagName + "' returned " + results.getWorkflows().size() + " workflows");
		
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public List<Resource> getExampleWorkflows() {
		List<Resource> workflows = new ArrayList<Resource>();
		
		try {
			URL exampleWorkflowsGroupUrl = new URL(baseUrl, "group.xml?id=" + EXAMPLE_WORKFLOWS_GROUP_ID + "&all_elements=yes");
			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(exampleWorkflowsGroupUrl);
			
			if (doc != null) {
				List<Element> nodes = doc.getRootElement().getChild("shared-items").getChildren();
				for (Element e : nodes) {
					Resource r = new Resource();
					r.setTitle(e.getText());
					r.setResource(e.getAttributeValue("resource"));
					r.setUri(e.getAttributeValue("uri"));
					
					workflows.add(r);
				}
			}
		} catch (Exception e) {
			this.logger.error("Failed to retriece example workflows", e);
		}
		
		logger.debug(workflows.size() + " example workflows retrieved from myExperiment");
		
		return workflows;
	}
	
	private SyndFeed getLatestWorkflowsRSS() throws Exception {
		URL feedUrl = new URL(baseUrl, "workflows.rss");
		
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = input.build(new XmlReader(feedUrl));
		
		return feed;
	}
}
