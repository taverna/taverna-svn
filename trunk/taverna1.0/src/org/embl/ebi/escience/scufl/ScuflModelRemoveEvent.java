/*
 * Created on Jan 18, 2005
 */
package org.embl.ebi.escience.scufl;

/**
 * COMMENT 
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.1 $
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
	
	private static String getClassName(Object addedObject)
	{
		String name = addedObject.getClass().getName();
		int index = name.lastIndexOf('.');
		if(index != -1)
		{
			name = name.substring(index+1);
		}
		return name;
	}	
	
	/**
	 * @return the removed port
	 */
	public Object getRemovedObject()
	{
		return removedObject;
	}
}
