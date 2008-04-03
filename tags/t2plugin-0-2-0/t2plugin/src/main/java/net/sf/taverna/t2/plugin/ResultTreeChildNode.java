package net.sf.taverna.t2.plugin;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

public class ResultTreeChildNode extends DefaultMutableTreeNode{
	
	private static final long serialVersionUID = 1L;
	private final DataFacade dataFacade;
	private final EntityIdentifier entityIdentifier;
	private final List<String> mimeTypes;

	public ResultTreeChildNode(List<String> mimeTypes, DataFacade dataFacade, EntityIdentifier entityIdentifier) {
		super(mimeTypes);
		this.mimeTypes = mimeTypes;
		this.dataFacade = dataFacade;
		this.entityIdentifier = entityIdentifier;
	}

	public DataFacade getDataFacade() {
		return dataFacade;
	}

	public EntityIdentifier getEntityIdentifier() {
		return entityIdentifier;
	}

	public List<String> getMimeTypes() {
		return mimeTypes;
	}

}
