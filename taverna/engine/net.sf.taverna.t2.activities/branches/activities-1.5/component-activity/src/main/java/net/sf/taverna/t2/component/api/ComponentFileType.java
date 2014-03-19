/**
 * 
 */
package net.sf.taverna.t2.component.api;

import net.sf.taverna.t2.workbench.file.FileType;

/**
 * @author alanrw
 * 
 */
public class ComponentFileType extends FileType {
	static final String COMPONENT_MIMETYPE = "application/vnd.taverna.component";
	public static final FileType instance = new ComponentFileType();

	private ComponentFileType() {}

	@Override
	public String getDescription() {
		return "Taverna component";
	}

	// Not really used
	@Override
	public String getExtension() {
		return "component";
	}

	@Override
	public String getMimeType() {
		return COMPONENT_MIMETYPE;
	}
}
