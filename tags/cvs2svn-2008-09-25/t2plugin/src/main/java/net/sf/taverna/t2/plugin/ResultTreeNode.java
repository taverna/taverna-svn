package net.sf.taverna.t2.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

import org.apache.log4j.Logger;

public class ResultTreeNode implements MutableTreeNode {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ResultTreeNode.class);

	private MutableTreeNode parent;

	private List<MutableTreeNode> children = new ArrayList<MutableTreeNode>();

	private EntityIdentifier token;

	private DataFacade dataFacade;

	private final List<String> mimeTypes;

	public EntityIdentifier getToken() {
		return token;
	}

	public List<String> getMimeTypes() {
		return mimeTypes;
	}

	public ResultTreeNode(EntityIdentifier token, DataFacade dataFacade,
			List<String> mimeTypes) {
		this.token = token;
		this.dataFacade = dataFacade;
		this.mimeTypes = mimeTypes;
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
				// add a node underneath the 'folder' which displays the mime
				// types and can be right clicked on.  Similar to T1 functionality
				children.add(new ResultTreeChildNode(mimeTypes, dataFacade,
						token));
			} catch (Exception e) {

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
