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
public class ScuflModelRemoveEvent extends ScuflModelEvent
{
	Object removedObject;
	
	/**
	 * @param source
	 * @param removedObject
	 */
	public ScuflModelRemoveEvent(Object source, Object removedObject)
	{
		super(source, "Removed " + getClassName(removedObject) + " " + removedObject);
		this.removedObject = removedObject;
	}
	
	/**
	 * @return the removed port
	 */
	public Object getRemovedObject()
	{
		return removedObject;
	}
}
