package net.sf.taverna.t2.workbench.views.results;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.T2Reference;

public class ResultTreeChildNode extends DefaultMutableTreeNode{
	
	private static final long serialVersionUID = 1L;
	private final T2Reference reference;
	private final List<String> mimeTypes;
	private final InvocationContext context;

	public ResultTreeChildNode(List<String> mimeTypes, InvocationContext context, T2Reference reference) {
		super(mimeTypes);
		this.mimeTypes = mimeTypes;
		this.context = context;
		this.reference = reference;
	}

	public InvocationContext getContext() {
		return context;
	}

	public T2Reference getT2Reference() {
		return reference;
	}

	public List<String> getMimeTypes() {
		return mimeTypes;
	}

}
