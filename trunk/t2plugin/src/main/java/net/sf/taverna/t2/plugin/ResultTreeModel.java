package net.sf.taverna.t2.plugin;

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
import net.sf.taverna.t2.workflowmodel.impl.ContextManager;

import org.apache.log4j.Logger;

public class ResultTreeModel extends DefaultTreeModel implements ResultListener {

	private static final long serialVersionUID = 7154527821423588046L;

	private static Logger logger = Logger.getLogger(ResultTreeModel.class);
	private String portName;
	int depth;
	int depthSeen = -1;

	public ResultTreeModel(String portName, int depth) {
		super(new DefaultMutableTreeNode("Results:"));
		this.portName = portName;
		this.depth = depth;
	}

	public void resultTokenProduced(EntityIdentifier token, int[] index,
			String portName, String owningProcess) {
		if (this.portName.equals(portName)) {
			if (depthSeen == -1)
				depthSeen = index.length;

			if (index.length>=depthSeen) {
			
				if (token.getType()==IDType.List) {
					EntityListIdentifier tokenList = (EntityListIdentifier)token;
					try {
						EntityList list = (EntityList)ContextManager.getDataManager(owningProcess).getEntity(tokenList);
						int [] elementIndex=new int[index.length+1];
						for (int indexElement=0 ; indexElement< index.length ; indexElement++) {
							elementIndex[indexElement]=index[indexElement];
						}
						int c=0;
						for (EntityIdentifier id : list) {
							elementIndex[index.length]=c;
							resultTokenProduced(id, elementIndex, portName, owningProcess);
							c++;
						}
						//TODO: display to user.
					} catch (RetrievalException e) {
						logger.error("Error resolving data entity list",e);
					} catch (NotFoundException e) {
						logger.error("Unable to find data entity list",e);
					}
				}
				else  {
					insertNewDataTokenNode(token, index, owningProcess);
				}
			}
		}

	}

	private void insertNewDataTokenNode(EntityIdentifier token, int[] index, String owningProcess) {
		MutableTreeNode parent = (MutableTreeNode) getRoot();
		if (index.length == depth) {
			if (depth == 0) {
				insertNodeInto(
						new DefaultMutableTreeNode(token.toString()),
						parent, 0);
			} else {
				parent = getChildAt(parent, 0);
				parent.setUserObject("List...");
				for (int indexElement = 0; indexElement < depth; indexElement++) {
					MutableTreeNode child = getChildAt(parent,
							index[indexElement]);
					if (indexElement == (depth - 1)) { // leaf
						
						
						if (token.getType()==IDType.Literal) {
							try {
								String value = (String)new DataFacade(ContextManager.getDataManager(owningProcess)).resolve(token,String.class);
								child.setUserObject(value);
							} catch (RetrievalException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (NotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else {
							int childIndex = parent.getIndex(child);
							child = new ResultTreeNode(token, new DataFacade(ContextManager.getDataManager(owningProcess)));
							parent.remove(childIndex);
							parent.insert(child, childIndex);
						}
						
						
					} else { // list
						child.setUserObject("List: "+token.toString());
					}
					nodeChanged(child);
					parent = child;
				}
			}
		}
	}

	private MutableTreeNode getChildAt(MutableTreeNode parent,
			int i) {
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
