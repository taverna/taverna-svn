/*
 * Created on Jan 27, 2005
 */
package org.embl.ebi.escience.scufl;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.1.2.1 $
 */
public class ScuflModelRenameEvent extends ScuflModelEvent {
	String oldName;

	/**
	 * @param source
	 * @param oldName
	 */
	public ScuflModelRenameEvent(Object source, String oldName) {
		super(source, "Renamed " + getClassName(source) + " " + oldName);
	}

	public String getOldName() {
		return oldName;
	}
}
