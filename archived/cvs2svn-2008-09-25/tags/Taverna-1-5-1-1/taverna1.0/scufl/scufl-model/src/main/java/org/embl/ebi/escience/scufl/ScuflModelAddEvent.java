/*
 * Created on Jan 18, 2005
 */
package org.embl.ebi.escience.scufl;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.2 $
 */
public class ScuflModelAddEvent extends ScuflModelEvent {
	Object addedObject;

	/**
	 * @param source
	 * @param addedObject
	 */
	public ScuflModelAddEvent(Object source, Object addedObject) {
		super(source, "Added " + getClassName(addedObject) + " " + addedObject);
		this.addedObject = addedObject;
	}

	/**
	 * @return get the added object
	 */
	public Object getAddedObject() {
		return addedObject;
	}
}
