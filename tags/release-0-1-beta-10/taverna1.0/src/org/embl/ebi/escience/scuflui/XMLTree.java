package org.embl.ebi.escience.scuflui;

import javax.swing.JTree;
import javax.swing.tree.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// IO Imports
import java.awt.Component;
import java.io.ByteArrayInputStream;

import java.lang.Exception;
import java.lang.String;
import java.lang.StringBuffer;
import java.lang.System;
import java.util.*;



/**
 * An extension of the javax.swing.JTree class, constructed
 * with a String of XML and used to display the XML structure
 * as an interactive tree.
 * Derived from original code by Kyle Gabhart from 
 * http://www.devx.com/gethelpon/10MinuteSolution/16694/0/page/1
 * @author Kyle Gabhart
 * @author Tom Oinn
 */
public class XMLTree extends JTree {
       
	private class XMLNode extends DefaultMutableTreeNode
	{
		short nodeType;
		
		public XMLNode(Object userObject, short nodeType)
		{
			super(userObject);
			this.nodeType = nodeType;
		}
	}
	
    /**
     * Build a new XMLTree from the supplied String containing XML.
     */
    public XMLTree(String text) throws ParserConfigurationException {
	super();
	// Fix for platforms other than metal which can't otherwise
	// cope with arbitrary size rows
	setRowHeight(0);
	getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	setShowsRootHandles(true);
	setEditable(false);
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	dbf.setValidating(false);
	DocumentBuilder db = dbf.newDocumentBuilder();
	// Take the DOM root node and convert it to a Tree model for the JTree
	DefaultMutableTreeNode treeNode = createTreeNode(parseXml(text, db));
	setModel(new DefaultTreeModel(treeNode));
	setCellRenderer(new DefaultTreeCellRenderer()
	{
		/*
		 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree,
		 *      java.lang.Object, boolean, boolean, boolean, int, boolean)
		 */
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus)
		{
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			if(value instanceof XMLNode)
			{
				XMLNode node = (XMLNode)value;
				switch (node.nodeType)
					{
						case Node.ELEMENT_NODE:
							setIcon(ScuflIcons.xmlNodeIcon);
							break;

						case Node.TEXT_NODE:
							setIcon(ScuflIcons.leafIcon);
							break;
							
						default:
							break;
					}
			}
			return this;
		}		
	});
	setAllNodesExpanded();
    } 
    public void setAllNodesExpanded() {
	synchronized(this.getModel()) {
	    expandAll(this, new TreePath(this.getModel().getRoot()), true);
	}
    }
    private void expandAll(JTree tree, TreePath parent, boolean expand) {
	synchronized(this.getModel()) {
	    // Traverse children
	    // Ignores nodes who's userObject is a Processor type to
	    // avoid overloading the UI with nodes at startup.
	    TreeNode node = (TreeNode)parent.getLastPathComponent();
	    if (node.getChildCount() >= 0) {
		for (Enumeration e=node.children(); e.hasMoreElements(); ) {
		    TreeNode n = (TreeNode)e.nextElement();
		    TreePath path = parent.pathByAddingChild(n);
		    expandAll(tree, path, expand);
		}
	    }
	    // Expansion or collapse must be done bottom-up
	    if (expand) {
		tree.expandPath(parent);
	    } else {
		tree.collapsePath(parent);
	    }
	}
    }
    /**
     * This takes a DOM Node and recurses through the children until each one is added
     * to a DefaultMutableTreeNode. The JTree then uses this object as a tree model.
     * @return Returns a DefaultMutableTreeNode object based on the root Node passed in
     */
    private XMLNode createTreeNode(Node root) {
	XMLNode       treeNode = null;
	String        type, value;
	NamedNodeMap  attribs;
	Node          attribNode;
	// Get data from root node
	type = getNodeType( root );
	StringBuffer nameBuffer = new StringBuffer("<html>"+root.getNodeName());
	value = root.getNodeValue();
	
	// Append any attributes to the text of the node that contains them
	attribs = root.getAttributes();
	if (attribs != null) {
	    boolean addedAnAttribute = false;
	    for(int i = 0; i < attribs.getLength(); i++) {
		attribNode = attribs.item(i);
		String name = attribNode.getNodeName().trim();
		value = attribNode.getNodeValue().trim();
		if (value!=null) {
		    if ( value.length() > 0 ) {
			if (addedAnAttribute) {
			    nameBuffer.append(",");
			}
			addedAnAttribute = true;
			nameBuffer.append(" <font color=\"purple\">" + name + "</font>=\"<font color=\"green\">" + value + "</font>\"");
		    } 
		} 
	    } 
	} 
	nameBuffer.append("</html>");
	treeNode = new XMLNode( root.getNodeType() == Node.TEXT_NODE ? 
					       "<html><pre><font color=\"green\">"+value+"</font></pre></html>" 
					       :
					       nameBuffer.toString(), root.getNodeType() );
	
	// Recurse children nodes if any exist
	if(root.hasChildNodes()) {
	    NodeList             children;
	    int                  numChildren;
	    Node                 node;
	    String               data;
	    children = root.getChildNodes();
	    // Only recurse if Child Nodes are non-null
	    if( children != null ) {
		numChildren = children.getLength();
		for (int i=0; i < numChildren; i++) {
		    node = children.item(i);
		    if(node!=null) {
			if(node.getNodeType()==Node.ELEMENT_NODE) {
			    treeNode.add(createTreeNode(node));
			} 
			data = node.getNodeValue();
			if(data!=null) {
			    data = data.trim();
			    if (!data.equals("\n") && !data.equals("\r\n") && data.length() > 0) {
				treeNode.add(createTreeNode(node));
			    } 
			} 
		    } 
		} 
	    } 
	} 
	return treeNode;
    } 
    
    /**
     * This method returns a string representing the type of node passed in.
     *
     * @param node org.w3c.Node.Node
     *
     * @return Returns a String representing the node type
     */
    private String getNodeType( Node node ) {
	String type;
	switch( node.getNodeType() )
	    {
	    case Node.ELEMENT_NODE:
		{
		    type = "Element";
		    break;
		}
	    case Node.ATTRIBUTE_NODE:
		{
		    type = "Attribute";
		    break;
		}
	    case Node.TEXT_NODE:
		{
		    type = "Text";
		    break;
		}
	    case Node.CDATA_SECTION_NODE:
		{
		    type = "CData section";
		    break;
		}
	    case Node.ENTITY_REFERENCE_NODE:
		{
		    type = "Entity reference";
		    break;
		}
	    case Node.ENTITY_NODE:
		{
		    type = "Entity";
		    break;
		}
	    case Node.PROCESSING_INSTRUCTION_NODE:
		{
		    type = "Processing instruction";
		    break;
		}
	    case Node.COMMENT_NODE:
		{
		    type = "Comment";
		    break;
		}
	    case Node.DOCUMENT_NODE:
		{
		    type = "Document";
		    break;
		}
	    case Node.DOCUMENT_TYPE_NODE:
		{
		    type = "Document type";
		    break;
		}
	    case Node.DOCUMENT_FRAGMENT_NODE:
		{
		    type = "Document fragment";
		    break;
		}
	    case Node.NOTATION_NODE:
		{
		    type = "Notation";
		    break;
		}
	    default:
		{
		    type = "???";
		    break;
		}
	    }// end switch( node.getNodeType() )
	return type;
    } //end getNodeType()
    
    /**
     * This method performs the actual parsing of the XML text
     *
     * @param text A String representing an XML document
     * @return Returns an org.w3c.Node.Node object
     */
    private Node parseXml(String text, DocumentBuilder db) {
	ByteArrayInputStream byteStream = new ByteArrayInputStream(text.getBytes());
	Document doc = null;
	try {
	    doc = db.parse( byteStream );
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.exit(0);
	}
	return (Node)doc.getDocumentElement();
    } 
    
} 
