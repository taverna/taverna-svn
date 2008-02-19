package net.sf.taverna.t2.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

public class ResultTreeNode implements MutableTreeNode {

	private MutableTreeNode parent;
	
	private List<MutableTreeNode> children = new ArrayList<MutableTreeNode>();
	
	private EntityIdentifier token;
	
	private DataFacade dataFacade;

	private final String mimeType;
	
	public EntityIdentifier getToken() {
		return token;
	}

	public String getMimeType() {
		return mimeType;
	}

	public ResultTreeNode(EntityIdentifier token, DataFacade dataFacade, String mimeType) {
		this.token = token;
		this.dataFacade = dataFacade;
		this.mimeType = mimeType;
		System.out.println("Mime Type: " + mimeType);
	}
	
	public Enumeration<MutableTreeNode> children() {
		return Collections.enumeration(children);
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public TreeNode getChildAt(int index) {
		if (children.size() == 0) {
			try {
				children.add(new DefaultMutableTreeNode(dataFacade.resolve(token, String.class)));
			} catch (RetrievalException e) {
				//couldn't resolve since it was a blob etc so use renderer instead
				//maybe change icon on the tree to something that isn't folder?
				//what about setting the icon to a thumbnail of the rendered entity?
//				children.add(new DefaultMutableTreeNode("ERROR: " + e.getMessage()));
			} catch (NotFoundException e) {
//				children.add(new DefaultMutableTreeNode("ERROR: " + e.getMessage()));
			}
		}
		return children.get(index);
	}

	public int getChildCount() {
		return 1;
	}

	public int getIndex(TreeNode node) {
		return children.indexOf(node);
	}

	public TreeNode getParent() {
		return parent;
	}

	public boolean isLeaf() {
		return false;
	}

	public void insert(MutableTreeNode node, int index) {
		children.add(index, node);
	}

	public void remove(int index) {
		children.remove(index);
	}

	public void remove(MutableTreeNode node) {
		children.remove(node);
	}

	public void removeFromParent() {
		parent.remove(this);		
	}

	public void setParent(MutableTreeNode node) {
		parent = node;
	}

	public void setUserObject(Object arg0) {
		
	}
	
	public String toString() {
		return token.toString();
	}

	public DataFacade getDataFacade() {
		return dataFacade;
	}

}
