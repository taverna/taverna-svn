package uk.ac.manchester.cs.elico.utilities.repositorybrowser;

import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import uk.ac.manchester.cs.elico.utilities.configuration.RapidAnalyticsPreferences;
import uk.ac.manchester.cs.elico.utilities.configuration.RapidMinerPluginConfiguration;
import uk.ac.manchester.cs.elico.utilities.csvimporter.CSVImporter;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;

/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Rishi Ramgolam<br>
 * Date: Jul 13, 2011<br>
 * The University of Manchester<br>
 **/

public class RapidAnalyticsRepositoryBrowser extends JPanel implements
        ActionListener, TreeExpansionListener {

	private RapidAnalyticsRepositoryTree myTreePanel;

    JButton useButton;

	private JLabel myIconLabel;
	
    private String repositoryUsername;
    private static String UPLOAD_COMMAND = "upload";
    private static String NEWFOLDER_COMMAND = "newfolder";

    private String CSV_HEADER = "text/csv";
    private String ARFF_HEADER = "application/arff";
    private String RAPIDMINER_PROCESS_HEADER = "application/vnd.rapidminer.rmp+xml";
    private String RAPIDMINER_BINARY_HEADER = "application/vnd.rapidminer.ioo";

    private boolean populated = false;
	private HashMap<Object, String> objectType = new HashMap<Object, String>();
	public String returnedRepositoryLocation;

    private UsernamePassword username_password;

	public RapidAnalyticsPreferences preferences;

	private JPanel titlePanel;

	private JLabel titleLabel;

	private JLabel titleIcon;

	private DialogTextArea titleMessage;
	
	char delimiter = 0;
	boolean userCancel = false;

	public RapidAnalyticsRepositoryBrowser() {
		
		fillContents();
        preferences = getPreferences();
        if (preferences != null) {
            CredentialManager credManager;
            try {
                credManager = CredentialManager.getInstance();
                username_password = credManager.getUsernameAndPasswordForService(URI.create(preferences.getBrowserServiceLocation()), true, null);
         
                preferences.setUsername(username_password.getUsername());
                preferences.setPassword(username_password.getPasswordAsString());
            } catch (CMException e) {
                e.printStackTrace();

            }
        }
        else {
            JOptionPane.showMessageDialog(new JFrame(),
                    new JLabel("<html>Please set the Rapid Analytics repository location <br> " +
                            " and flora location in the preferences panel</html>"));
        }
	}

    private RapidAnalyticsPreferences getPreferences() {

        RapidMinerPluginConfiguration config = RapidMinerPluginConfiguration.getInstance();
        String repos = config.getProperty(RapidMinerPluginConfiguration.RA_REPOSITORY_LOCATION);
        System.err.println("Got repository location: " + repos);
        if (repos.equals("")) {
            return null;
        }

        RapidAnalyticsPreferences pref = new RapidAnalyticsPreferences();
        pref.setRepositoryLocation(repos);
        return pref;

    }

	public RapidAnalyticsRepositoryBrowser(RapidAnalyticsPreferences pref) {

		preferences = pref;
        username_password = new UsernamePassword(preferences.getUsername(), preferences.getPasswordAsString());
		fillContents();
		
	}
	
	public void fillContents() {
		
		setLayout(new BorderLayout());
		
//		setPreferredSize(new Dimension(200, 400));
		myTreePanel = new RapidAnalyticsRepositoryTree();
		myTreePanel.myTree.addTreeExpansionListener(this);
		
		Icon leafIcon = new ImageIcon(getClass().getResource("/file.png"));
		Icon openIcon = new ImageIcon(getClass().getResource("/folder-open.png"));
		Icon closedIcon = new ImageIcon(getClass().getResource("/folder-closed.png"));
		
		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)myTreePanel.myTree.getCellRenderer();
		renderer.setLeafIcon(leafIcon);
		renderer.setClosedIcon(closedIcon);
		renderer.setOpenIcon(openIcon);
		
		// title panel
		titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBackground(Color.WHITE);
		addDivider(titlePanel, SwingConstants.BOTTOM, true);
		
		// title
		titleLabel = new JLabel("RapidAnalytics Repository Browser / Uploader");
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13.5f));
		
		titleIcon = new JLabel("");
		titleMessage = new DialogTextArea("Here you can browse and upload files to your repository.");
		titleMessage.setMargin(new Insets(5, 10, 10, 10));
		titleMessage.setFont(titleMessage.getFont().deriveFont(11f));
		titleMessage.setEditable(false);
		titleMessage.setFocusable(false);
		
		// add title panel 
		// title panel
		titlePanel.setBorder(new CompoundBorder(titlePanel.getBorder(), new EmptyBorder(10, 10, 0, 10)));
		titlePanel.add(titleLabel, BorderLayout.NORTH);
		titlePanel.add(titleIcon, BorderLayout.WEST);
		titlePanel.add(titleMessage, BorderLayout.CENTER);
		add(titlePanel, BorderLayout.NORTH);
		
		// populate tree with root elements
//		initialiseTreeContents();
		
//		fc = new JFileChooser();
//		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		JButton addButton = new JButton("Upload File");
		addButton.setActionCommand("upload");
		addButton.addActionListener(this);
					
		//JButton newFolderButton = new JButton("New Folder");
		//newFolderButton.setActionCommand("newfolder");
		//newFolderButton.addActionListener(this);
		
		this.useButton = new JButton(new AbstractAction("Select file") {

            public void actionPerformed(ActionEvent actionEvent) {
                fileSelectedButtonPress();
            }
        });
		useButton.setActionCommand("use");

		ImageIcon icon = new ImageIcon(getClass().getResource("/loading.gif"),
        "your file is being uploaded");
		myIconLabel = new JLabel(icon);
		myIconLabel.setVisible(false);
		
        myTreePanel.setPreferredSize(new Dimension(200, 150));
        add(myTreePanel, BorderLayout.CENTER);

        JPanel panel = new JPanel(new GridLayout(0,4));
        panel.add(addButton);
        //panel.add(newFolderButton); 
        panel.add(useButton);
        panel.add(myIconLabel);
        add(panel, BorderLayout.SOUTH);
       
	}
	
	public void setPreferences(RapidAnalyticsPreferences pref) {
		
		preferences = pref;
		
	}

    public void fileSelectedButtonPress () {

        // set a variable of the chosen respository structure
        TreePath myPath = myTreePanel.myTree.getSelectionPath();
        returnedRepositoryLocation = parseRepositoryTreePath(myPath);
        //[DEBUG] System.out.println(" the parsed repository path is " + parsedPath);
//        returnedRepositoryLocation = parsedPath;

        // rip out last slash (for individual files)
        //int i = returnedRepositoryLocation.lastIndexOf("/");
        //returnedRepositoryLocation = returnedRepositoryLocation.substring(0, i);



    }

	public void initialiseTreeContents() {
	
		//set the root structure
		//[debug]System.err.println("am i here 1");
		populateTree(myTreePanel, getRepositoryStructure(""));
		//[debug]System.err.println("am i here 2");
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
        		//[debug]System.out.println("This object is a folder : " + myParentObject);        		
        		
        		// add contents to parents 
        		Object [] contents = getRepositoryStructure(parentNode.toString());
        		
        			for (Object myChildObject : contents) {
        				
        				//[debug]System.out.println("	contents of parent " + contents.toString());
        				userNode = treePanel.addObject(parentNode, myChildObject);     
        				
        				if (!userNode.toString().equals(repositoryUsername)) {
            				dummy = treePanel.addObject(userNode, "dummy");
        				}
        				        				
        				// now fill in contents for the users directory from repositoryUsername
        				if (myChildObject.equals(repositoryUsername)) {
        					        					
        					//[debug]System.out.println("	Got the username stuff");
        								
            				Object [] userContents = getRepositoryStructure(parentNode.toString() + "/" + myChildObject);
            				
            				//[debug]System.out.println("user contents" + userContents + " length " + userContents.length);
            				
            				if (userContents.length == 0) {
            					
            					treeNode = userNode.getPath();
            				}
            				
            					for (Object userObject : userContents) {
            						            						
            						//[debug]System.out.println("	contents of user object : " + userObject);
            						childNode = treePanel.addObject(userNode, userObject);
            						treeNode = childNode.getPath();
            						
            						if (objectType.get(userObject).equals("folder")) {
            							//[debug]System.out.println("*** ADDING " + userObject + " to objectType");
            							myTreePanel.addObject(childNode, "dummy");
            						}
            					            						
            					}
        				}
        				
        			}
        		
        	}
        	
        }
        
        //System.out.println(" TREE LENGTH " + treeNode.length);
        
        if (treeNode != null) {
        	 
        	 TreePath path = new TreePath(treeNode);
        	 treePanel.myTree.setExpandsSelectedPaths(true);
        	 treePanel.myTree.setSelectionPath(path);
        	
        }
        
	}
	
	public void populateTreeTest(RapidAnalyticsRepositoryTree treePanel) {
		 
			// TEST CASE - to remove
			String p1Name = "Parent 1";
	        String p2Name = "Parent 2";
	        String c1Name = "Child 1";
	        String c2Name = "Child 2";

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
	    String filePath;
	   
	        if (UPLOAD_COMMAND.equals(command)) {
	        	
	            //Add button clicked
	        	//	myTreePanel.addObject("New Node " + newNodeSuffix++);

                FileDialog fd = new FileDialog(new JFrame(), "Choose a file");
                fd.setVisible(true);

                String dir = fd.getDirectory();
                String file = fd.getFile();

	            if (file != null) {

                    File f = new File(dir, file);
	                filePath = f.getPath();
	                //This is where a real application would open the file.
	              //[debug]System.out.println("Opening: " + file + " path " + dir);
	                
	              //[debug]System.out.println(" FILE EXTENSION " + getFileExtension(filePath) + " filepath " + filePath);

	                
	                // if it's a csv file, then open the CSV importer and get the chosen delimiter
	                	                
	                if (getFileExtension(filePath).equals("csv")) {
	                	
	                	final JDialog csvFrame = new JDialog((JDialog)SwingUtilities.getAncestorOfClass(JDialog.class, RapidAnalyticsRepositoryBrowser.this), "CSV Importer");
	                    
		                CSVImporter importer = new CSVImporter(filePath) {
		        			
		        			public void getChosenFileDelimiter() {
		        				
		        				delimiter = getChosenDelimiter();
		        				csvFrame.dispose();
		        			}
		        			
		        			public void closeImporter() {
		        				
		        				csvFrame.dispose();
		        				userCancel = true;
		        			}
		        			
		        		};

		        		//final JFrame frame = new JFrame();
		        		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		                   
		        		csvFrame.setLocation(dim.width / 2 - getWidth() / 2, dim.height / 2 - getHeight() / 2);
		        		csvFrame.setResizable(false);
	                    csvFrame.add(importer);
	                    csvFrame.setModal(true);
	                    csvFrame.pack();
	                    csvFrame.setVisible(true);
		        		
	                	
	                }
                    
	               
                  //[debug]System.out.println(" the chosen delimiter is : " + delimiter);
	                if (!userCancel) {
	                	
	                	uploadFile(filePath, delimiter);
	                }
	                
	                
	        	                
	            } else {
	            		
	            	//[debug]System.out.println("Open command cancelled by user.");
	
	            }
	              	
	        } else if (NEWFOLDER_COMMAND.equals(command)) {
	        	
	            //Remove button clicked
	        	//myTreePanel.removeCurrentNode();
	        	
	        }

//            else if (USE_COMMAND.equals(command)) {
//
//	           // set a variable of the chosen respository structure
//	        	TreePath myPath = myTreePanel.myTree.getSelectionPath();
//	        	String parsedPath = parseRepositoryTreePath(myPath);
//	        	//[DEBUG] System.out.println(" the parsed repository path is " + parsedPath);
//	        	returnedRepositoryLocation = parsedPath;
//
//	        	// rip out last slash (for individual files)
//	        		//int i = returnedRepositoryLocation.lastIndexOf("/");
//	        		//returnedRepositoryLocation = returnedRepositoryLocation.substring(0, i);
//
//
//	        }
	}

    public void dispose () {
        JDialog dialog = (JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, this);
        dialog.dispose();

    }

	public String getChosenRepositoryPath() {
		return returnedRepositoryLocation;
	}
	
	public void setRepositoryLocation(String value) {
		returnedRepositoryLocation = value;
	}
	
	public int getPortFromString(String value) {
		
		URL myURL = null;
		try {
			myURL = new URL(value);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return myURL.getPort();
	}
	
	public Object[] getRepositoryStructure(String path) {
		
		// get the repository structure

		//http://rpc295.cs.man.ac.uk:8081/RAWS/resources/


		String username = this.username_password.getUsername();
		//String host = "http://rpc295.cs.man.ac.uk";
        repositoryUsername = username;
		String password = this.username_password.getPasswordAsString();

		String urlBasePath = preferences.getBrowserServiceLocation() + path;
        URL urlBase = null;

		try {
			
			urlBase = new URL(preferences.getBrowserServiceLocation());
			
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Object content = null;
		
		try {
			
			HttpClient client = new DefaultHttpClient();
			
//			AuthScope as = new AuthScope(urlBasePath, urlBase.getPort());
			
			HttpContext localContext = new BasicHttpContext();
			
			//UsernamePasswordCredentials upc = new UsernamePasswordCredentials(
           //         username, password);
 
            //  ((DefaultHttpClient) client).getCredentialsProvider()
            //          .setCredentials(as, upc);
			
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            assert urlBase != null;

            //credsProvider.setCredentials(new AuthScope(urlBase.getHost(), urlBase.getPort(), "Spring Security Application"), new UsernamePasswordCredentials(username, password));
            credsProvider.setCredentials(new AuthScope(urlBase.getHost(), urlBase.getPort(), AuthScope.ANY_REALM), new UsernamePasswordCredentials(username, password));

            //[debug-old RA] credsProvider.setCredentials(new AuthScope(urlBase.getHost(), urlBase.getPort(), "RapidAnalyticsRealm"), new UsernamePasswordCredentials(username, password));
            
            ((DefaultHttpClient) client).setCredentialsProvider(credsProvider);
            
  			BasicScheme basicAuth = new BasicScheme();
			localContext.setAttribute("http", basicAuth);
			
			//HttpHost targetHost= new HttpHost(host, 8081);
			
			HttpGet httpget = new HttpGet(urlBasePath);
						
			httpget.setHeader("Content-Type", "application/xml");

			HttpGet httpGet;
            httpGet = new HttpGet(urlBasePath);

            HttpRequestBase base = httpGet;
						
			HttpResponse response = client.execute(base,
                    localContext);
			
			//[debug]System.out.println(" REST OUTPUT IS : " + response.toString());
			//[debug]System.out.println(" REST Status Line : " + response.getStatusLine().toString());
			if (response.getStatusLine().toString().equals("HTTP/1.1 403 Forbidden")) {
				return null;
			}
			
			
			content = EntityUtils.toString(response.getEntity());
			
			//[debug]System.out.println(" THE RESPONSE BODY IS : " + content);
					
		} catch (Exception e) {
			
			e.printStackTrace();			
			//[debug]System.out.println(" just caught an exception ");
			
		}

		//[debug]System.out.println("content brought back : " + content.toString());
		
		return parseStructureOutput(content.toString());

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
		
 		//System.out.println(" [debug] output data" + outputData);
 		
		Object [] myList;
		NodeList folders;
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
					
			//[debug]System.out.println(" The number of elements parsed from REST output: " + folders.getLength() + " , " + children.getLength());

		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		myList = new Object [children.getLength()];
		
		for (int i = 0; i < children.getLength(); i++) {
			
			Element line = (Element) children.item(i);
			
			//[debug]System.out.println(" TO PUT IN LIST " + getCharacterDataFromElement(line) + " attribute " + line.getAttribute("type"));
			
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
		//[debug]System.out.println(" The path is " + treePath);
		
		Object [] objects = treePath.getPath();
		boolean first =  true;
		String path = "";
		
		for (Object myObject : objects) {
			
			if (!first) {
				
                path += "/";
				path += myObject;		
			}
			
			first = false;
		}
		
		//[debug]System.out.println(" The path is now " + path);
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
				
				//[debug]System.out.println("	THIS NODE CONTAINS A DUMMY ");
				
				myTreePanel.myTree.setSelectionPath(path.pathByAddingChild(nodeOfPath.getFirstChild()));
				nodeOfPath.getFirstChild();
				
				myTreePanel.removeCurrentNode();
				
				//myTreePanel.addObject(nodeOfPath, " not a dummy!");	// add the contents here
				addObjectsToPath(path, nodeOfPath);
				
				myTreePanel.myTree.expandPath(event.getPath());
				
			}
		
			//[debug]System.out.println("	EXPANDED " + path + " NODE OF PATH " + nodeOfPath);
		
		}
				
	}
	
	public String getFileExtension(String fileName) {
		
	    String fname;
	    String ext;
	    int mid= fileName.lastIndexOf(".");
	    fname=fileName.substring(0,mid);
	    ext=fileName.substring(mid+1,fileName.length());  
	  //[debug]System.out.println("File name ="+fname);
	  //[debug]System.out.println("Extension ="+ext);   
	    
	    return ext;
	}
	
	public String getFileName(String path) {
	
		String fname;
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

        if (extension.equals("csv")) {

            return CSV_HEADER;

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

	public void uploadFile(String filePath, char delimiter) {
	
		// get the users selected node on the tree then get its path
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) myTreePanel.myTree.getLastSelectedPathComponent();
		TreePath selectionPath = new TreePath(node.getPath());
		String updatedSelectionPath = parseRepositoryTreePath(selectionPath);
		//[debug]System.out.println(" updated selection path " + updatedSelectionPath);
		
		String fileExtension = getFileExtension(filePath);
		String contentType = getContentType(fileExtension);
		String fname = getFileName(filePath);
		String fileURI = preferences.getRepositoryLocation() + "/RAWS/resources" + updatedSelectionPath + "/" + fname;
        if (contentType.equals(CSV_HEADER)) {
            fileURI = fileURI + "?column_separators=" + delimiter;
        }
      //[debug]System.out.println(" THE FILENAME TO APPEND IS : " + fname);		
		
      //[debug]System.out.println(" THE CONTENT TYPE IS : " + contentType);
			
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
		
		// [ THREAD SWITCH ] doRequest(httpPut, contentType, fileURI, node);
		UploaderThread thread = new UploaderThread();
		thread.setContentType(contentType);
		thread.setHttpRequest(httpPut);
		thread.setURI(fileURI);
		thread.setParentNode(node);
		
		thread.execute();
	}
	
	public void doRequest(HttpRequestBase httpRequest, String contentType, String URI, DefaultMutableTreeNode parentNode) {
		
		String urlBasePath = URI;
		URL urlBaseTemp = null;
		try {
			urlBaseTemp = new URL(urlBasePath);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		httpRequest.setHeader("Content-Type", contentType);
		
		try {
		      
			HttpClient httpClient = new DefaultHttpClient();
			//AuthScope as = new AuthScope(urlBasePath, urlBaseTemp.getPort());
			
			HttpContext localContext = new BasicHttpContext();
			
			//	UsernamePasswordCredentials upc = new UsernamePasswordCredentials(
            //         "rishi", "");
            //  ((DefaultHttpClient) client).getCredentialsProvider()
            //          .setCredentials(as, upc);
            
            CredentialsProvider credsProvider = new BasicCredentialsProvider();

            credsProvider.setCredentials(new AuthScope(urlBaseTemp.getHost(), urlBaseTemp.getPort(), "RapidAnalyticsRealm"), new UsernamePasswordCredentials(preferences.getUsername(), preferences.getPasswordAsString()));
            
            ((DefaultHttpClient) httpClient).setCredentialsProvider(credsProvider);
            
  			BasicScheme basicAuth = new BasicScheme();
			localContext.setAttribute("http", basicAuth);
			            
			HttpResponse response = httpClient.execute(httpRequest, localContext);
			
			myIconLabel.setVisible(false);
			
			Object content = EntityUtils.toString(response.getEntity());
			
			//[debug]System.out.println(" REST UPLOAD RESPONSE : " + content);
			
			JOptionPane.showMessageDialog(this, content);
			
			// refresh tree branch
			updateTreePath(parentNode);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
	}

	class UploaderThread extends SwingWorker<String, Object> {
		
		   private HttpRequestBase httpRequest;
		   private String contentType;
		   private String URI;
		   private DefaultMutableTreeNode parentNode;
		
	       @Override
	       public String doInBackground() {
	    	   
	    	   doRequest(httpRequest, contentType, URI, parentNode);
	           return null;
	       }

	       @Override
	       protected void done() {
	           try { 
	              // label.setText(get());
	        	   myIconLabel.setVisible(false);
	           } catch (Exception ignore) {
	           }
	       }
	       
	   	public void doRequest(HttpRequestBase httpRequest, String contentType, String URI, DefaultMutableTreeNode parentNode) {
	   		
	   		myIconLabel.setVisible(true);
			String urlBasePath = URI;
			URL urlBaseTemp = null;
			try {
				urlBaseTemp = new URL(urlBasePath);
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			httpRequest.setHeader("Content-Type", contentType);
			
			try {
			      
				HttpClient httpClient = new DefaultHttpClient();
				//AuthScope as = new AuthScope(urlBasePath, urlBaseTemp.getPort());
				
				HttpContext localContext = new BasicHttpContext();
				
				//	UsernamePasswordCredentials upc = new UsernamePasswordCredentials(
	            //         "rishi", "");
	            //  ((DefaultHttpClient) client).getCredentialsProvider()
	            //          .setCredentials(as, upc);
	            
	            CredentialsProvider credsProvider = new BasicCredentialsProvider();

	            credsProvider.setCredentials(new AuthScope(urlBaseTemp.getHost(), urlBaseTemp.getPort(), AuthScope.ANY_REALM), new UsernamePasswordCredentials(preferences.getUsername(), preferences.getPasswordAsString()));
	            
	            ((DefaultHttpClient) httpClient).setCredentialsProvider(credsProvider);
	            
	  			BasicScheme basicAuth = new BasicScheme();
				localContext.setAttribute("http", basicAuth);
				        
				HttpResponse response = httpClient.execute(httpRequest, localContext);
				
				myIconLabel.setVisible(false);
				
				Object content = EntityUtils.toString(response.getEntity());
				
				//[debug]System.out.println(" REST UPLOAD RESPONSE : " + content);
				
				JOptionPane.showMessageDialog(null, content.toString());
				
				// refresh tree branch
				updateTreePath(parentNode);
				
			} catch (Exception e) {
				
				e.printStackTrace();
				
			}
			
		}

		public void setHttpRequest(HttpRequestBase httpRequest) {
			this.httpRequest = httpRequest;
		}

		public HttpRequestBase getHttpRequest() {
			return httpRequest;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

		public String getContentType() {
			return contentType;
		}

		public void setURI(String uRI) {
			URI = uRI;
		}

		public String getURI() {
			return URI;
		}

		public void setParentNode(DefaultMutableTreeNode parentNode) {
			this.parentNode = parentNode;
		}

		public DefaultMutableTreeNode getParentNode() {
			return parentNode;
		}
	}
	
	public void updateTreePath(DefaultMutableTreeNode parentNode) {
	
		// RE-FACTOR
		//[debug]System.out.println(" TO UPDATE NODE " + parentNode.getPath());
		String path = parseRepositoryTreePath(new TreePath(parentNode.getPath()));
		
		// get new objects
		Object [] myObjects = getRepositoryStructure(path);
		//[debug]System.out.println(" TO UPDATE NODE WITH " + myObjects.toString());
		
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
	
	/**
	 * Adds a light gray or etched border to the top or bottom of a JComponent.
	 * 
	 * @param component
     * @param position
     * @param etched
	 */
	protected void addDivider(JComponent component, final int position, final boolean etched) {
		component.setBorder(new Border() {
			private final Color borderColor = new Color(.6f, .6f, .6f);
			
			public Insets getBorderInsets(Component c) {
				if (position == SwingConstants.TOP) {
					return new Insets(5, 0, 0, 0);
				} else {
					return new Insets(0, 0, 5, 0);
				}
			}

			public boolean isBorderOpaque() {
				return false;
			}

			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				if (position == SwingConstants.TOP) {
					if (etched) {
						g.setColor(borderColor);
						g.drawLine(x, y, x + width, y);
						g.setColor(Color.WHITE);
						g.drawLine(x, y + 1, x + width, y + 1);
					} else {
						g.setColor(Color.LIGHT_GRAY);
						g.drawLine(x, y, x + width, y);
					}
				} else {
					if (etched) {
						g.setColor(borderColor);
						g.drawLine(x, y + height - 2, x + width, y + height - 2);
						g.setColor(Color.WHITE);
						g.drawLine(x, y + height - 1, x + width, y + height - 1);
					} else {
						g.setColor(Color.LIGHT_GRAY);
						g.drawLine(x, y + height - 1, x + width, y + height - 1);
					}
				}
			}

		});
	}

}
