package org.myexp_whip_plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import javax.naming.NameNotFoundException;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

public class MyExperimentClient {
	
	private Logger logger;
	
	private URL baseUrl;
	
	public MyExperimentClient(Logger logger, URL baseUrl) {
		this.logger = logger;
		this.baseUrl = baseUrl;
	}
	
	@SuppressWarnings("unchecked")
	public List<SyndEntry> getLatestWorkflows() throws Exception {
		return this.getRSS().getEntries();
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
				List<Element> nodes = doc.getRootElement().getChildren("workflow");
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
		
		return tagCloud;
	}
	
	private SyndFeed getRSS() throws Exception {
		URL feedUrl = new URL(baseUrl, "workflows.rss");
		
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = input.build(new XmlReader(feedUrl));
		
		return feed;
	}
}
