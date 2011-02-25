package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
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
import org.xml.sax.SAXParseException;

public class RapidAnalyticsRepositoryBrowser extends JPanel implements
		ActionListener, TreeExpansionListener {

	private RapidAnalyticsRepositoryTree myTreePanel;
	public static JFrame frame;
	
	private JLabel myIconLabel;
	
    private int newNodeSuffix = 1;
    private String repositoryUsername;    
    private static String UPLOAD_COMMAND = "upload";
    private static String NEWFOLDER_COMMAND = "newfolder";
    private static String USE_COMMAND = "use";
     
    private String ARFF_HEADER = "application/arff";
    private String RAPIDMINER_PROCESS_HEADER = "application/vnd.rapidminer.rmp+xml";
    private String RAPIDMINER_BINARY_HEADER = "application/vnd.rapidminer.ioo";

    private boolean populated = false;
	private HashMap<Object, String> objectType = new HashMap<Object, String>();
    private String password;	
	public String returnedRepositoryLocation;
    
	JFileChooser fc;
	 
	public RapidAnalyticsRepositoryBrowser() {
	
		fillContents();
		
	}
	
	public void fillContents() {
		
		setLayout(new BorderLayout());
		
		setPreferredSize(new Dimension(300, 400));
		
		myTreePanel = new RapidAnalyticsRepositoryTree();
		myTreePanel.myTree.addTreeExpansionListener(this);
		
		Icon leafIcon = new ImageIcon(getClass().getResource("/file.png"));
		Icon openIcon = new ImageIcon(getClass().getResource("/folder-open.png"));
		Icon closedIcon = new ImageIcon(getClass().getResource("/folder-closed.png"));
		
		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)myTreePanel.myTree.getCellRenderer();
		renderer.setLeafIcon(leafIcon);
		renderer.setClosedIcon(closedIcon);
		renderer.setOpenIcon(openIcon);
		
		// populate tree with root elements
		initialiseTreeContents();
		
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		JButton addButton = new JButton("Upload File");
		addButton.setActionCommand("upload");
		addButton.addActionListener(this);
					
		JButton newFolderButton = new JButton("New Folder");
		newFolderButton.setActionCommand("newfolder");
		newFolderButton.addActionListener(this);
		
		JButton useButton = new JButton("Use Location");
		useButton.setActionCommand("use");
		useButton.addActionListener(this);
	
		ImageIcon icon = new ImageIcon(getClass().getResource("/loading.gif"),
        "your file is being uploaded");
		myIconLabel = new JLabel(icon);
		myIconLabel.setVisible(false);
		
        myTreePanel.setPreferredSize(new Dimension(300, 150));
        add(myTreePanel, BorderLayout.CENTER);

        JPanel panel = new JPanel(new GridLayout(0,4));
        panel.add(addButton);
        panel.add(newFolderButton); 
        panel.add(useButton);
        panel.add(myIconLabel);
        add(panel, BorderLayout.SOUTH);
        		
	}

	public void initialiseTreeContents() {
	
		//set the root structure
		populateTree(myTreePanel, getRepositoryStructure(""));
		populated = true;
		
	}
	
	public void populateTree(RapidAnalyticsRepositoryTree treePanel, Object [] stuff) {
		  
        DefaultMutableTreeNode parentNode, userNode, childNode, dummy;
      
        TreeNode[] treeNode = null;
        
        for (Object myParentObject : stuff) {
        	
        	// add root contents to root tree node 
        	parentNode = treePanel.addObject(null, myParentObject);
    	
        	// check whether the parentNode has any children (if it's a folder)
        	if (objectType.get(myParentObject).equals("folder")) {
        		
        		// add the folder contents to 
        		System.out.println("This object is a folder : " + myParentObject);        		
        		
        		// add contents to parents 
        		Object [] contents = getRepositoryStructure(parentNode.toString());
        		
        			for (Object myChildObject : contents) {
        				
        				System.out.println("	contents of parent " + contents.toString());
        				userNode = treePanel.addObject(parentNode, myChildObject);     
        				
        				if (!userNode.toString().equals(repositoryUsername)) {
            				dummy = treePanel.addObject(userNode, "dummy");
        				}
        				        				
        				// now fill in contents for the users directory from repositoryUsername
        				if (myChildObject.equals(repositoryUsername)) {
        					        					
        					System.out.println("	Got the username stuff");
        								
            				Object [] userContents = getRepositoryStructure(parentNode.toString() + "/" + myChildObject);
            				
            					for (Object userObject : userContents) {
            						            						
            						System.out.println("	contents of user object : " + userObject);
            						childNode = treePanel.addObject(userNode, userObject);
            						treeNode = childNode.getPath();
            						
            						if (objectType.get(userObject).equals("folder")) {
            							System.out.println("*** ADDING " + userObject + " to objectType");
            							myTreePanel.addObject(childNode, "dummy");
            						}
            					            						
            					}
        				}
        				
        			}
        		
        	}
        	
        }
        
        TreePath path = new TreePath(treeNode);
        treePanel.myTree.setExpandsSelectedPaths(true);
        treePanel.myTree.setSelectionPath(path);
        
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
			
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
	    String filePath = null;
	    
	        if (UPLOAD_COMMAND.equals(command)) {
	        	
	            //Add button clicked
	        	//	myTreePanel.addObject("New Node " + newNodeSuffix++);
	        	
	        	int returnVal = fc.showOpenDialog(this);

	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	            	
	                File file = fc.getSelectedFile();
	                filePath = file.getPath();
	                //This is where a real application would open the file.
	                System.out.println("Opening: " + file.getName() + " path " + file.getPath());
	                
	                System.out.println(" FILE EXTENSION " + getFileExtension(filePath));
	                myIconLabel.setVisible(true);
	                uploadFile(filePath);
	                myIconLabel.setVisible(false);
	                
	            } else {
	            		
	            	 System.out.println("Open command cancelled by user.");
	
	            }
	              	
	        } else if (NEWFOLDER_COMMAND.equals(command)) {
	        	
	            //Remove button clicked
	        	//myTreePanel.removeCurrentNode();
	        	
	        } else if (USE_COMMAND.equals(command)) {
	        	
	           // set a variable of the chosen respository structure
	        	TreePath myPath = myTreePanel.myTree.getSelectionPath();
	        	String parsedPath = parseRepositoryTreePath(myPath);
	        	//[DEBUG] System.out.println(" the parsed repository path is " + parsedPath);
	        	returnedRepositoryLocation = parsedPath;
	        	
	        	// rip out last slash (for individual files)
	        		//int i = returnedRepositoryLocation.lastIndexOf("/");
	        		//returnedRepositoryLocation = returnedRepositoryLocation.substring(0, i);
	        	
	        	JDialog dialog = (JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, this);
	        	dialog.dispose();
	        
	        }
	}
	
	public String getChosenRepositoryPath() {
		return returnedRepositoryLocation;
	}
	
	public void setRepositoryLocation(String value) {
		returnedRepositoryLocation = value;
	}
	
	public Object[] getRepositoryStructure(String path) {
		
		// get the repository structure

		//http://rpc295.cs.man.ac.uk:8081/RAWS/resources/
		String username = "rishi";
		repositoryUsername = username;
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
            credsProvider.setCredentials(new AuthScope("rpc295.cs.man.ac.uk", 8081, "RapidAnalyticsRealm"), new UsernamePasswordCredentials("rishi", ""));
            
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
			System.out.println(" REST Status Line : " + response.getStatusLine().toString());
			if (response.getStatusLine().toString().equals("HTTP/1.1 403 Forbidden")) {
				return null;
			}
			
			
			content = EntityUtils.toString(response.getEntity());
			
			System.out.println(" THE RESPONSE BODY IS : " + content);
					
		} catch (Exception e) {
			
			e.printStackTrace();			
			System.out.println(" just caught an exception ");
			
		}
		
		return (parseStructureOutput(content.toString()));
		
	}
	
	public void addObjectsToPath(TreePath path, DefaultMutableTreeNode node) {
		
		String repositoryFolderPath = parseRepositoryTreePath(path);
		Object [] pathObjects = getRepositoryStructure(repositoryFolderPath);
		if (pathObjects == null) {
			return;
		}
		
		for (Object myObject : pathObjects) {
		
			DefaultMutableTreeNode addedNode = myTreePanel.addObject(node, myObject);
					
			if (objectType.get(myObject).equals("folder")) {
				myTreePanel.addObject(addedNode, "dummy");
			}
				
		}
		
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
	
	public String parseRepositoryTreePath(TreePath treePath) {

		// the path is:
		System.out.println(" The path is " + treePath);
		
		Object [] objects = treePath.getPath();
		boolean first =  true;
		String path = "/";
		
		for (Object myObject : objects) {
			
			if (!first) {
				
				path += myObject;		
				path += "/";
			}
			
			first = false;
		}
		
		System.out.println(" The path is now " + path);
		return path;
	}
				
	public void treeCollapsed(TreeExpansionEvent event) {
		
	}

	public void treeExpanded(TreeExpansionEvent event) {

		// after the tree has been populated with the users root directory already expanded on initialisation

		if (populated) {
			
			TreePath path = event.getPath();
			DefaultMutableTreeNode nodeOfPath = (DefaultMutableTreeNode) path.getLastPathComponent();
			
			if (nodeOfPath.getChildCount() == 1 && nodeOfPath.getFirstChild().toString().equals("dummy")) {
				
				System.out.println("	THIS NODE CONTAINS A DUMMY ");
				
				myTreePanel.myTree.setSelectionPath(path.pathByAddingChild(nodeOfPath.getFirstChild()));
				nodeOfPath.getFirstChild();
				
				myTreePanel.removeCurrentNode();
				
				//myTreePanel.addObject(nodeOfPath, " not a dummy!");	// add the contents here
				addObjectsToPath(path, nodeOfPath);
				
				myTreePanel.myTree.expandPath(event.getPath());
				
			}
		
			System.out.println("	EXPANDED " + path + " NODE OF PATH " + nodeOfPath);
		
		}
				
	}
	
	public String getFileExtension(String fileName) {
		
	    String fname="";
	    String ext="";
	    int mid= fileName.lastIndexOf(".");
	    fname=fileName.substring(0,mid);
	    ext=fileName.substring(mid+1,fileName.length());  
	    System.out.println("File name ="+fname);
	    System.out.println("Extension ="+ext);   
	    
	    return ext;
	}
	
	public String getFileName(String path) {
	
		String fname = "";
		int mid = path.lastIndexOf("/");
		fname = path.substring(mid+1, path.length());
		
		return fname;
	}
	
	public String getContentType(String extension) {
	
		if (extension.equals("arff")) {
			
			return ARFF_HEADER;
			
		}
		
		if (extension.equals("ioo")) {
			
			return RAPIDMINER_BINARY_HEADER;
		}
		
		if (extension.equals("xml")) {
			
			return RAPIDMINER_PROCESS_HEADER;
			
		}
				
		// if there is no match set it as a blob
		return "application/blob"; 
		
	}
	
	public static byte[] getBytesFromFile(File file) throws IOException {

        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
       
        try {
        	
             while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                
            	 offset += numRead;		
            
             }
             
        } catch (Exception e) {
        	
        	e.printStackTrace();
        	
        }
       
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
       
        	throw new IOException("Could not completely read file "+file.getName());
      
        }
    
        // Close the input stream and return bytes
        is.close();
        
        return bytes;
        
    }

	public void uploadFile(String filePath) {
	
		// get the users selected node on the tree then get its path
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) myTreePanel.myTree.getLastSelectedPathComponent();
		TreePath selectionPath = new TreePath(node.getPath());
		String updatedSelectionPath = parseRepositoryTreePath(selectionPath);
		System.out.println(" updated selection path " + updatedSelectionPath);
		
		String fileExtension = getFileExtension(filePath);
		String contentType = getContentType(fileExtension);
		String fname = getFileName(filePath);
		String fileURI = "http://rpc295.cs.man.ac.uk:8081/RAWS/resources" + updatedSelectionPath + fname;
		System.out.println(" THE FILENAME TO APPEND IS : " + fname);		
		
		System.out.println(" THE CONTENT TYPE IS : " + contentType);
			
		HttpPut httpPut = new HttpPut(fileURI);
		httpPut.setHeader("Content-Type", contentType);
		Object inputMessageBody;
		File myFile = new File(filePath);
		inputMessageBody = myFile;
		
		byte[] b = null;
		
		try {
			//b = getBytesFromFile(myFile);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			
			//InputStream is = new FileInputStream(myFile);
		
		    // Get the size of the file
		    //long length = myFile.length();
			//InputStreamEntity myStreamEntity = new InputStreamEntity(is, length);
			
			//System.out.println("[debug] INPUT STREAM REPEATIBLE " + myStreamEntity.isRepeatable());
			//HttpEntity entity = null;
			
			 //entity = myStreamEntity;
			 //httpPut.setEntity(myStreamEntity);
			
			FileEntity fileEntity = new FileEntity(myFile, contentType);
			httpPut.setEntity(fileEntity);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		doRequest(httpPut, contentType, fileURI, node);
	
	}
	
	public void doRequest(HttpRequestBase httpRequest, String contentType, String URI, DefaultMutableTreeNode parentNode) {
		
		String urlBasePath = URI;

		httpRequest.setHeader("Content-Type", contentType);
		
		try {
		      
			HttpClient httpClient = new DefaultHttpClient();
			AuthScope as = new AuthScope(urlBasePath, 8081);
			
			HttpContext localContext = new BasicHttpContext();
			
			UsernamePasswordCredentials upc = new UsernamePasswordCredentials(
                    "rishi", "");
 
            //  ((DefaultHttpClient) client).getCredentialsProvider()
            //          .setCredentials(as, upc);
            
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope("rpc295.cs.man.ac.uk", 8081, "RapidAnalyticsRealm"), new UsernamePasswordCredentials("rishi", ""));
            
            ((DefaultHttpClient) httpClient).setCredentialsProvider(credsProvider);
            
  			BasicScheme basicAuth = new BasicScheme();
			localContext.setAttribute("http", basicAuth);
			
			HttpResponse response = httpClient.execute(httpRequest, localContext);
			
			Object content = EntityUtils.toString(response.getEntity());
			
			System.out.println(" REST UPLOAD RESPONSE : " + content);
			
			// refresh tree branch
			updateTreePath(parentNode);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
	}

	public void updateTreePath(DefaultMutableTreeNode parentNode) {
	
		// RE-FACTOR
		System.out.println(" TO UPDATE NODE " + parentNode.getPath());
		String path = parseRepositoryTreePath(new TreePath(parentNode.getPath()));
		
		// get new objects
		Object [] myObjects = getRepositoryStructure(path);
		System.out.println(" TO UPDATE NODE WITH " + myObjects.toString());
		
		//System.out.println(" DEBUG TREE PATH : " + myTreePanel.getNodeAt(new TreePath(parentNode.getPath())).getPath());
		DefaultMutableTreeNode lastNode = myTreePanel.getNodeAt(new TreePath(parentNode.getPath()));
		lastNode.removeAllChildren();
		myTreePanel.myTreeModel.reload();
	        
		addObjectsToPath(new TreePath(parentNode.getPath()), parentNode);
		
		 TreePath lastPath = new TreePath(lastNode.getPath());
	        myTreePanel.myTree.setExpandsSelectedPaths(true);
	        myTreePanel.myTree.setSelectionPath(lastPath);
	        myTreePanel.updateUI();
	}
	
}
