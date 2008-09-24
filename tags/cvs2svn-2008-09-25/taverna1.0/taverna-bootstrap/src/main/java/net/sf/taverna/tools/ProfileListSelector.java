package net.sf.taverna.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * When provided with a URL to the XML that defines a Profile list, the XML is parsed to produce a list of Profile Definitions.
 * 
 * Its possible to store the first profile in the list to a local File, which is used when Taverna is first run and no local profile is defined.
 * 
 * @author Stuart Owen
 *
 */
public class ProfileListSelector {
	
	private List<ProfileDef> profiles = new ArrayList<ProfileDef>();
	private URL listUrl;
	
	public ProfileListSelector(URL listURL) throws Exception {	
		this.listUrl=listURL;
		processList(listURL);
	}
	
	private void processList(URL listURL) throws Exception {
		InputStream inputStream = listURL.openStream();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(inputStream);
		inputStream.close();
		NodeList list = document.getElementsByTagName("profile");
		for (int i=0;i<list.getLength();i++) {
			processNode(listURL,list.item(i));
		}
	}
	
	/**
	 * Provides a list of the Profile definitions listed, ordered by version ascending
	 * @return
	 */
	public List<ProfileDef> getProfileList() {
		return profiles;
	}
	
	private void processNode(URL listURL,Node profileNode) {
		ProfileDef def = new ProfileDef();
		def.version=getChildNodeValue(profileNode,"version");
		def.location=getChildNodeValue(profileNode,"location");
		try {
			def.location=new URL(listURL,def.location).toExternalForm();
			profiles.add(def);
		}
		catch(MalformedURLException e) {
			e.printStackTrace();
			System.out.println("Theres an error with the profile url:"+def.location);
		}
	}
	
	private String getChildNodeValue(Node node, String elementName) {
		String result = null;
		Node child = node.getFirstChild();
		while (child!=null) {
			if (child.getNodeType()==Node.ELEMENT_NODE) {
				if (child.getNodeName().equals(elementName)) {
					result=child.getFirstChild().getNodeValue();
					break;
				}
			}
			child=child.getNextSibling();
		}
		return result;
	}
	
	/**
	 * Stores the first profile version in the list to the destination
	 * @param destinationFile
	 */
	public void storeFirst(File destinationFile) throws Exception {
		if (profiles.size()>0) {
			store(destinationFile,profiles.get(0));
		}
		else {
			throw new Exception("No profiles found in list:"+listUrl.toExternalForm());
		}
	}
	
	private void store(File destinationFile, ProfileDef profile) throws IOException {
		URL profileURL = new URL(profile.location);
		InputStream in = profileURL.openStream();
		
		if (!destinationFile.exists()) destinationFile.createNewFile();
		OutputStream out = new FileOutputStream(destinationFile);
		
		byte [] buffer = new byte[255];
		int len;
		while ((len = in.read(buffer))!=-1) {
			out.write(buffer,0,len);
		}
		
		in.close();
		out.close();
		
	}
	
	class ProfileDef {
		String location;
		String version;
	}
	
}
