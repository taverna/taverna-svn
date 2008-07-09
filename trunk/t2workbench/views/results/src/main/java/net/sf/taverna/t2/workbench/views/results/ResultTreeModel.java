package net.sf.taverna.t2.workbench.views.results;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;

import org.apache.log4j.Logger;

public class ResultTreeModel extends DefaultTreeModel implements ResultListener {

	private static final long serialVersionUID = 7154527821423588046L;

	private static Logger logger = Logger.getLogger(ResultTreeModel.class);
	private String portName;
	int depth;
	int depthSeen = -1;

	private final Map<String, List<String>> mimeTypes;

	public ResultTreeModel(String portName, int depth,
			Map<String, List<String>> mimeTypes2) {
		super(new DefaultMutableTreeNode("Results:"));
		this.portName = portName;
		this.depth = depth;
		this.mimeTypes = mimeTypes2;
	}

	public void resultTokenProduced(WorkflowDataToken dataToken, String portName) {
		int[] index = dataToken.getIndex();
		if (this.portName.equals(portName)) {
			if (depthSeen == -1)
				depthSeen = index.length;

			if (index.length >= depthSeen) {
				T2Reference entityToken = dataToken.getData();

				if (entityToken.getReferenceType() == T2ReferenceType.IdentifiedList) {

					try {
						IdentifiedList<T2Reference> list = dataToken
								.getContext().getReferenceService()
								.getListService().getList(entityToken);
						int[] elementIndex = new int[index.length + 1];
						for (int indexElement = 0; indexElement < index.length; indexElement++) {
							elementIndex[indexElement] = index[indexElement];
						}
						int c = 0;
						for (T2Reference id : list) {
							elementIndex[index.length] = c;
							resultTokenProduced(new WorkflowDataToken(dataToken
									.getOwningProcess(), elementIndex, id,
									dataToken.getContext()), portName);
							c++;
						}
						// TODO: display to user.
					} catch (Exception e) {
						logger.error("Error resolving data entity list "
								+ entityToken, e);
					}
				} else {
					insertNewDataTokenNode(entityToken, index, dataToken
							.getOwningProcess(), dataToken.getContext());
				}
			}
		}

	}

	private void insertNewDataTokenNode(T2Reference token, int[] index,
			String owningProcess, InvocationContext context) {
		MutableTreeNode parent = (MutableTreeNode) getRoot();
		if (index.length == depth) {
			if (depth == 0) {
				MutableTreeNode child = getChildAt(parent, 0);
				child = updateChildNodeWithData(token, owningProcess, parent,
						child, context);
				nodeChanged(child);
			} else {
				parent = getChildAt(parent, 0);
				parent.setUserObject("List...");
				for (int indexElement = 0; indexElement < depth; indexElement++) {
					MutableTreeNode child = getChildAt(parent,
							index[indexElement]);
					if (indexElement == (depth - 1)) { // leaf
						child = updateChildNodeWithData(token, owningProcess,
								parent, child, context);
					} else { // list
						child.setUserObject("List...");
					}
					nodeChanged(child);
					parent = child;
				}
			}
		}
	}

	private MutableTreeNode updateChildNodeWithData(T2Reference token,
			String owningProcess, MutableTreeNode parent,
			MutableTreeNode child, InvocationContext context) {

		List<String> mimeType = mimeTypes.get(this.portName);
		int childIndex = parent.getIndex(child);
		List<String> mimeTypeList = new ArrayList<String>();
//		for (String type : mimeType) {
//			mimeTypeList.add(type);
//		}
//		child = new ResultTreeNode(token, context, mimeTypeList);
		child = new ResultTreeNode(token, context, java.util.Collections.singletonList("text/plain"));

		parent.remove(childIndex);
		parent.insert(child, childIndex);

		return child;
	}

	private MutableTreeNode getChildAt(MutableTreeNode parent, int i) {
		int childCount = getChildCount(parent);
		if (childCount <= i) {
			for (int x = childCount; x <= i; x++) {
				insertNodeInto(new DefaultMutableTreeNode("Waiting for data"),
						parent, x);
			}
		}

		return (MutableTreeNode) parent.getChildAt(i);
	}

}
