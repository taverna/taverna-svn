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
import java.util.List;

import javax.naming.NameNotFoundException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import org.apache.log4j.Logger;

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
	
	public SearchResults searchWorkflows(String keywords) {
		SearchResults results = new SearchResults();
		
		try {
			URL searchUrl = new URL(baseUrl, "search.xml?query=" + keywords + "&type=workflow");
			
			/*
			XStream xstream = this.getCustomXStream();

			xstream.alias("search", SearchResults.class);
			xstream.alias("workflow", Resource.class);
			xstream.addImplicitCollection(SearchResults.class, "workflows", "workflow", Resource.class);
			xstream.aliasAttribute(Resource.class, "uri", "uri");
			xstream.aliasAttribute(Resource.class, "resource", "resource");
			xstream.aliasField("workflow", Resource.class, "title");
			
			results = (SearchResults)xstream.fromXML(searchUrl.openStream());
			*/
			
			Document doc = this.getXMLDocument(searchUrl);
			
			if (doc != null) {
				NodeList nodes = doc.getElementsByTagName("workflow");
				for (int i = 0; i < nodes.getLength(); i++) {
					String title = nodes.item(i).getTextContent();
					String uri = null;
					String resource = null;
					NamedNodeMap attribs = nodes.item(i).getAttributes();
					for (int j = 0; j < attribs.getLength(); j++) {
						if (attribs.item(j).getNodeName().equalsIgnoreCase("uri")) {
							uri = attribs.item(j).getTextContent();
						}
						if (attribs.item(j).getNodeName().equalsIgnoreCase("resource")) {
							resource = attribs.item(j).getTextContent();
						}
					}
					
					Resource r = new Resource();
					r.setTitle(title);
					r.setResource(resource);
					r.setUri(uri);
					
					results.getWorkflows().add(r);
				}
			}
		} catch (Exception e) {
			this.logger.error("Failed to search for workflows", e);
		}
		
		logger.debug("Search for keyword(s) '" + keywords + "' returned " + results.getWorkflows().size() + " workflows");
		
		return results;
	}
	
	public TagCloud getTagCloud(int size) {
		TagCloud tagCloud = new TagCloud();
		
		try {
			String num = "all";
			if (size > 0) {
				num = "" + size;
			}
				
			URL searchUrl = new URL(baseUrl, "tag-cloud.xml?num=" + num + "&type=workflow");
			
			Document doc = this.getXMLDocument(searchUrl);
			
			if (doc != null) {
				NodeList nodes = doc.getElementsByTagName("tag");
				for (int i = 0; i < nodes.getLength(); i++) {
					String title = nodes.item(i).getTextContent();
					String uri = null;
					String resource = null;
					int count = 0;
					NamedNodeMap attribs = nodes.item(i).getAttributes();
					for (int j = 0; j < attribs.getLength(); j++) {
						if (attribs.item(j).getNodeName().equalsIgnoreCase("uri")) {
							uri = attribs.item(j).getTextContent();
						}
						if (attribs.item(j).getNodeName().equalsIgnoreCase("resource")) {
							resource = attribs.item(j).getTextContent();
						}
						if (attribs.item(j).getNodeName().equalsIgnoreCase("count")) {
							count = Integer.parseInt(attribs.item(j).getTextContent());
						}
					}
					
					Tag t = new Tag();
					t.setTitle(title);
					t.setTagName(title);
					t.setResource(resource);
					t.setUri(uri);
					t.setCount(count);
					
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
	
	private Document getXMLDocument(URL feedUrl) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		Document doc = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		try {
			URLConnection URLconnection = feedUrl.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) URLconnection;

			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				System.out.println("OK");
				InputStream in = httpConnection.getInputStream();

				try {
					doc = db.parse(in);
				} catch (org.xml.sax.SAXException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("HTTP connection response OK ");

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc;
	}
	
	private XStream getCustomXStream() {
		return new XStream() {

            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {

                    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                        return definedIn != Object.class ? super.shouldSerializeMember(definedIn, fieldName) : false;
                    }
                    
                };
            }
            
        };
	}
}
