package org.embl.ebi.escience.scuflui;

import java.awt.Component;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Parent;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;

/**
 * An extension of the javax.swing.JTree class, constructed with a String of XML
 * and used to display the XML structure as an interactive tree. Derived from
 * original code by Kyle Gabhart from
 * http://www.devx.com/gethelpon/10MinuteSolution/16694/0/page/1
 * And then subsequently heavily rewritten to move to JDOM, and moved lots of the
 * setup code to the renderer to cut down initialisation time.
 * 
 * @author Kyle Gabhart
 * @author Tom Oinn
 * @author Kevin Glover
 */
public class XMLTree extends JTree
{
	private class XMLNode extends DefaultMutableTreeNode
	{
		public XMLNode(Content userObject)
		{
			super(userObject);
		}
	}

	int textSizeLimit = 1000;
	
	/**
	 * Build a new XMLTree from the supplied String containing XML.
	 * @param text
	 * @throws IOException
	 * @throws JDOMException
	 */
	public XMLTree(String text) throws IOException, JDOMException
	{
		super();
		Document document = new SAXBuilder(false).build(new StringReader(text));
		init(document.getRootElement());
	}

	public XMLTree(Document document)
	{
		super();
		init(document.getRootElement());
	}
	
	private void init(Content content)
	{
		// Fix for platforms other than metal which can't otherwise
		// cope with arbitrary size rows
		setRowHeight(0);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setShowsRootHandles(true);
		setEditable(false);		
		setModel(new DefaultTreeModel(createTreeNode(content)));
		setCellRenderer(new DefaultTreeCellRenderer()
		{
			/*
			 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree,
			 *      java.lang.Object, boolean, boolean, boolean, int, boolean)
			 */
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
															boolean expanded, boolean leaf,
															int row, boolean hasFocus)
			{
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				if (value instanceof XMLNode)
				{
					XMLNode node = (XMLNode) value;
					if(node.getUserObject() instanceof Element)
					{
						setIcon(ScuflIcons.xmlNodeIcon);
						Element element = (Element)node.getUserObject();
						StringBuffer nameBuffer = new StringBuffer("<html>" + element.getQualifiedName());						
						boolean addedAnAttribute = false;
						if(element.getParent() instanceof Element)
						{
							Element parent = (Element)element.getParent();
							if(parent.getNamespace(element.getNamespacePrefix()) == null)
							{
								nameBuffer.append(" <font color=\"purple\">xmlns:" + element.getNamespacePrefix()
													+ "</font>=\"<font color=\"green\">" + element.getNamespaceURI() + "</font>\"");
							}
						}
						else
						{
							nameBuffer.append(" <font color=\"purple\">xmlns:" + element.getNamespacePrefix()
												+ "</font>=\"<font color=\"green\">" + element.getNamespaceURI() + "</font>\"");							
						}
						
						Iterator attributes = element.getAttributes().iterator();
						while(attributes.hasNext())
						{
							Attribute attribute = (Attribute)attributes.next();
							String name = attribute.getName().trim();
							String attributeValue = attribute.getValue().trim();
							if (attributeValue != null)
							{
								if (attributeValue.length() > 0)
								{
									if (addedAnAttribute)
									{
										nameBuffer.append(",");
									}
									addedAnAttribute = true;
									nameBuffer.append(" <font color=\"purple\">" + name
											+ "</font>=\"<font color=\"green\">" + attributeValue + "</font>\"");
								}
							}
						}
				
						nameBuffer.append("</html>");
						setText(nameBuffer.toString());
					}
					else if(node.getUserObject() instanceof Text)
					{
						setIcon(ScuflIcons.leafIcon);
						Text text = (Text)node.getUserObject();
						String name = text.getText();
						if(textSizeLimit > -1 && name.length() > textSizeLimit)
						{
							name = name.substring(0, textSizeLimit) + "...";
						}
						setText("<html><pre><font color=\"blue\">" + name.replaceAll("<br>", "\n").replaceAll("<", "&lt;")
						+ "</font></pre></html>");
					}
				}
				return this;
			}
		});
		setAllNodesExpanded();		
	}
	
	public void setAllNodesExpanded()
	{
		synchronized (this.getModel())
		{
			expandAll(this, new TreePath(this.getModel().getRoot()), true);
		}
	}

	private void expandAll(JTree tree, TreePath parent, boolean expand)
	{
		synchronized (this.getModel())
		{
			// Traverse children
			// Ignores nodes who's userObject is a Processor type to
			// avoid overloading the UI with nodes at startup.
			TreeNode node = (TreeNode) parent.getLastPathComponent();
			if (node.getChildCount() >= 0)
			{
				for (Enumeration e = node.children(); e.hasMoreElements();)
				{
					TreeNode n = (TreeNode) e.nextElement();
					TreePath path = parent.pathByAddingChild(n);
					expandAll(tree, path, expand);
				}
			}
			// Expansion or collapse must be done bottom-up
			if (expand)
			{
				tree.expandPath(parent);
			}
			else
			{
				tree.collapsePath(parent);
			}
		}
	}

	public void setTextNodeSizeLimit(int sizeLimit)
	{
		textSizeLimit = sizeLimit;
	}
	
	private XMLNode createTreeNode(Content content)
	{
		XMLNode node = new XMLNode(content);
		if(content instanceof Parent)
		{
			Parent parent = (Parent)content;
			Iterator children = parent.getContent().iterator();
			while(children.hasNext())
			{
				Object child = children.next();
				if(child instanceof Element)
				{
					node.add(createTreeNode((Content)child));
				}
				else if(textSizeLimit != 0 && child instanceof Text)
				{
					Text text = (Text)child;
					if(!text.getTextNormalize().equals(""))
					{
						node.add(createTreeNode(text));						
					}					
				}
			}
		}
		return node;
	}
}