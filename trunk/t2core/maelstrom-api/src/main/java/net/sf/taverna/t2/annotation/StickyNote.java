package net.sf.taverna.t2.annotation;

/**
 * Generic bit of free text to fill the role of a 'postit note' on a bit of the
 * workflow. This is similar to the free text description but intended for less
 * formal documentation. Whereas the free text description can only be applied
 * to certain entities the sticky note can be applied to anything.
 * 
 * @author Tom Oinn
 * 
 */
@AppliesTo(workflowObjectType = {Object.class}, many = true)
public interface StickyNote extends WorkflowAnnotation {

	/**
	 * Returns the descriptive text
	 * 
	 * @return free text description
	 */
	public String getDescription();
	
}
