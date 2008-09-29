package net.sf.taverna.t2.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.IDType;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.invocation.WorkflowDataToken;

import org.apache.log4j.Logger;

public class ResultTreeModel extends DefaultTreeModel implements ResultListener {

	private static final long serialVersionUID = 7154527821423588046L;

	private static Logger logger = Logger.getLogger(ResultTreeModel.class);
	private String portName;
	int depth;
	int depthSeen = -1;

	private final Map<String, List<String>> mimeTypes;

	public ResultTreeModel(String portName, int depth, Map<String, List<String>> mimeTypes2) {
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
				DataFacade dataFacade = new DataFacade(dataToken.getContext()
						.getDataManager());
				EntityIdentifier entityToken = dataToken.getData();

				if (entityToken.getType() == IDType.List) {
					EntityListIdentifier tokenList = (EntityListIdentifier) entityToken;
					try {

						EntityList list = (EntityList) dataToken.getContext()
								.getDataManager().getEntity(tokenList);
						int[] elementIndex = new int[index.length + 1];
						for (int indexElement = 0; indexElement < index.length; indexElement++) {
							elementIndex[indexElement] = index[indexElement];
						}
						int c = 0;
						for (EntityIdentifier id : list) {
							elementIndex[index.length] = c;
							resultTokenProduced(new WorkflowDataToken(dataToken
									.getOwningProcess(), elementIndex, id,
									dataToken.getContext()), portName);
							c++;
						}
						// TODO: display to user.
					} catch (RetrievalException e) {
						logger.error("Error resolving data entity list", e);
					} catch (NotFoundException e) {
						logger.error("Unable to find data entity list", e);
					}
				} else {
					insertNewDataTokenNode(entityToken, index, dataToken.getOwningProcess(),
							dataFacade);
				}
			}
		}

	}

	private void insertNewDataTokenNode(EntityIdentifier token, int[] index,
			String owningProcess, DataFacade dataFacade) {
		MutableTreeNode parent = (MutableTreeNode) getRoot();
		if (index.length == depth) {
			if (depth == 0) {
				MutableTreeNode child = getChildAt(parent, 0);
				child = updateChildNodeWithData(token, owningProcess, parent,
						child, dataFacade);
				nodeChanged(child);
			} else {
				parent = getChildAt(parent, 0);
				parent.setUserObject("List...");
				for (int indexElement = 0; indexElement < depth; indexElement++) {
					MutableTreeNode child = getChildAt(parent,
							index[indexElement]);
					if (indexElement == (depth - 1)) { // leaf
						child = updateChildNodeWithData(token, owningProcess,
								parent, child, dataFacade);
					} else { // list
						child.setUserObject("List...");
					}
					nodeChanged(child);
					parent = child;
				}
			}
		}
	}

	private MutableTreeNode updateChildNodeWithData(EntityIdentifier token,
			String owningProcess, MutableTreeNode parent,
			MutableTreeNode child, DataFacade dataFacade) {
		if (token.getType() == IDType.Literal) {
			try {
				String value = (String) dataFacade.resolve(token, String.class);
				child.setUserObject(value);
			} catch (RetrievalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			List<String> mimeType = mimeTypes.get(this.portName);
			int childIndex = parent.getIndex(child);
			List<String> mimeTypeList = new ArrayList<String>();
			for(String type:mimeType) {
				mimeTypeList.add(type);
			}
			child = new ResultTreeNode(token, dataFacade, mimeTypeList);
			parent.remove(childIndex);
			parent.insert(child, childIndex);
		}
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
