package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class RapidAnalyticsRepositoryBrowser extends JPanel implements
		ActionListener, TreeExpansionListener {

	private RapidAnalyticsRepositoryTree myTreePanel;
	public static JFrame frame;
	
    private int newNodeSuffix = 1;
    private static String ADD_COMMAND = "add";
    private static String REMOVE_COMMAND = "remove";
    private static String CLEAR_COMMAND = "clear";
	private HashMap<Object, String> objectType = new HashMap<Object, String>();
    
	public RapidAnalyticsRepositoryBrowser() {
	
		fillContents();
	
	}
	
	public void fillContents() {
		
		setLayout(new BorderLayout());
		
		setPreferredSize(new Dimension(200, 200));
		
		myTreePanel = new RapidAnalyticsRepositoryTree();
		myTreePanel.myTree.addTreeExpansionListener(this);
		
		// populate tree with root elements
		initialiseTreeContents();
		
		JButton addButton = new JButton("Add");
		addButton.setActionCommand("add");
		addButton.addActionListener(this);
					
		JButton removeButton = new JButton("Remove");
		removeButton.setActionCommand("remove");
		removeButton.addActionListener(this);
		
		JButton clearButton = new JButton("Clear");
        clearButton.setActionCommand("clear");
        clearButton.addActionListener(this);
		
        myTreePanel.setPreferredSize(new Dimension(300, 150));
        add(myTreePanel, BorderLayout.CENTER);

        JPanel panel = new JPanel(new GridLayout(0,3));
        panel.add(addButton);
        panel.add(removeButton); 
        panel.add(clearButton);
        add(panel, BorderLayout.SOUTH);
        		
	}
	
	public void initialiseTreeContents() {
		
		//set the root structure
		populateTree(myTreePanel, getRepositoryStructure(""));

		
	}
	
	public void populateTreeTest(RapidAnalyticsRepositoryTree treePanel) {
		 
			// TEST CASE - to remove
			String p1Name = new String("Parent 1");
	        String p2Name = new String("Parent 2");
	        String c1Name = new String("Child 1");
	        String c2Name = new String("Child 2");

	        DefaultMutableTreeNode p1, p2;

	        p1 = treePanel.addObject(null, p1Name);
	        p2 = treePanel.addObject(null, p2Name);

	        treePanel.addObject(p1, c1Name);
	        treePanel.addObject(p1, c2Name);

	        treePanel.addObject(p2, c1Name);
	        treePanel.addObject(p2, c2Name);
	        	
	}
	
	public void populateTree(RapidAnalyticsRepositoryTree treePanel, Object [] stuff) {
		
		/*
		String p1Name = new String("Parent 1");
        String p2Name = new String("Parent 2");
        String c1Name = new String("Child 1");
        String c2Name = new String("Child 2");

        DefaultMutableTreeNode p1, p2;

        p1 = treePanel.addObject(null, p1Name);
        p2 = treePanel.addObject(null, p2Name);

        treePanel.addObject(p1, c1Name);
        treePanel.addObject(p1, c2Name);

        treePanel.addObject(p2, c1Name);
        treePanel.addObject(p2, c2Name);
		*/
		
        //
        
        String rootNodeName = new String();
        
        String childNodeName = new String();
        
        DefaultMutableTreeNode parentNode;
 
        
        for (Object myParentObject : stuff) {
        	
        	// add root contents to root tree node 
        	parentNode = treePanel.addObject(null, myParentObject);
        	
        	// check whether the parentNode has any children (if it's a folder)
        	if (objectType.get(myParentObject).equals("folder")) {
        		
        		System.out.println("This object is a folder : " + myParentObject);        		
        		
        	}
        	
        	
        }
        
		/*
		for (Object myList : stuff) {
			
			System.out.println(" ABOUT TO POPULATE TREE WITH " + stuff);
			treePanel.addObject(myList);
			
			if (objectType.get(myList).equals("folder")) {
				
				System.out.println(" here's a folder, fetch its contents");
				Object [] contents = getRepositoryStructure((String)myList);
				
				for (Object objectList : contents) {
					
					DefaultMutableTreeNode p1 = new DefaultMutableTreeNode(myList);

					treePanel.addObject(p1 ,objectList);
					
					System.out.println("        the contents " + objectList);
					//treePanel.addObject( p1 , objectList);
					
				}
			}
			
		}
		*/
		
	}
		
	public void actionPerformed(ActionEvent event) {
		// TODO Auto-generated method stub
		 String command = event.getActionCommand();
	        
	        if (ADD_COMMAND.equals(command)) {
	            //Add button clicked
	        	myTreePanel.addObject("New Node " + newNodeSuffix++);
	        } else if (REMOVE_COMMAND.equals(command)) {
	            //Remove button clicked
	        	myTreePanel.removeCurrentNode();
	        } else if (CLEAR_COMMAND.equals(command)) {
	            //Clear button clicked.
	        	myTreePanel.clearNodes();
	        }
	}
	
	public Object[] getRepositoryStructure(String path) {
		
		// get the repository structure

		//http://rpc295.cs.man.ac.uk:8081/RAWS/resources/
		String username = "rishi";
		//String host = "http://rpc295.cs.man.ac.uk";
		String password = "";
		
		String urlBasePath = "http://rpc295.cs.man.ac.uk:8081/RAWS/resources/" + path;
		String urlApiCall = urlBasePath;
		Object content = null;
		
		try {
			
			HttpClient client = new DefaultHttpClient();
			
			AuthScope as = new AuthScope(urlBasePath, 8081);
			
			HttpContext localContext = new BasicHttpContext();
			
			UsernamePasswordCredentials upc = new UsernamePasswordCredentials(
                    username, password);
 
            //  ((DefaultHttpClient) client).getCredentialsProvider()
            //          .setCredentials(as, upc);
            
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope("rpc295.cs.man.ac.uk", 8081, "RapidAnalyticsRealm"), new UsernamePasswordCredentials("rishi", "rishipwd"));
            
            ((DefaultHttpClient) client).setCredentialsProvider(credsProvider);
            
  			BasicScheme basicAuth = new BasicScheme();
			localContext.setAttribute("http", basicAuth);
			
			//HttpHost targetHost= new HttpHost(host, 8081);
			
			HttpGet httpget = new HttpGet(urlApiCall);
						
			httpget.setHeader("Content-Type", "application/xml");

			HttpGet httpGet = new HttpGet(urlBasePath);
			
			HttpRequestBase base = httpGet;
						
			HttpResponse response = client.execute(base,
                    localContext);
			
			System.out.println(" REST OUTPUT IS : " + response.toString());
			
			content = EntityUtils.toString(response.getEntity());
			
			System.out.println(" THE RESPONSE BODY IS : " + content);
					
		} catch (Exception e) {
			
			e.printStackTrace();			
			
		}
		
		return (parseStructureOutput(content.toString()));
		
	}
	
	public Object [] parseStructureOutput(String outputData) {
		
		Object [] myList;
		NodeList folders = null;
		NodeList children = null;
		
		try { 
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(outputData));
			
			Document doc = db.parse(is);
			folders = doc.getElementsByTagName("contents");
			children = doc.getElementsByTagName("entry");
					
			System.out.println(" The number of elements parsed from REST output: " + folders.getLength() + " , " + children.getLength());

		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		String location;
		myList = new Object [children.getLength()];
		
		for (int i = 0; i < children.getLength(); i++) {
			
			Element line = (Element) children.item(i);
			
			System.out.println(" TO PUT IN LIST " + getCharacterDataFromElement(line) + " attribute " + line.getAttribute("type"));
			
			myList[i] = getCharacterDataFromElement(line);
			objectType.put(myList[i], line.getAttribute("type"));
			
		}
		
   		return myList;
		
	}
	
	public static String getCharacterDataFromElement(Element e) {
		
	    Node child = e.getFirstChild();
	    
	    if (child instanceof CharacterData) {
	    	
	       CharacterData cd = (CharacterData) child;
	       return cd.getData();
	       
	    }
	    
	    return "?";
	    
	  }
			
	public void treeCollapsed(TreeExpansionEvent arg0) {
		
		// TODO Auto-generated method stub
		System.out.println("A node on the tree has been collapsed. " + arg0.getPath().toString());
		
	}

	public void treeExpanded(TreeExpansionEvent arg0) {
		
		// TODO Auto-generated method stub
		System.out.println("A node on the tree has been expanded." + arg0.getPath().toString());
		
	}

}
