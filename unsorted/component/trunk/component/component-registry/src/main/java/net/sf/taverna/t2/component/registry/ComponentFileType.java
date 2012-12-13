/**
 * 
 */
package net.sf.taverna.t2.component.registry;

import net.sf.taverna.t2.workbench.file.FileType;

/**
 * @author alanrw
 *
 */
public class ComponentFileType extends FileType {
	
	static final String COMPONENT_MIMETYPE="application/vnd.taverna.component";
	


	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.file.FileType#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Taverna component";
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.file.FileType#getExtension()
	 * 
	 * Not really used
	 */
	@Override
	public String getExtension() {
		return "component";
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.file.FileType#getMimeType()
	 */
	@Override
	public String getMimeType() {
		return COMPONENT_MIMETYPE;
	}

}
