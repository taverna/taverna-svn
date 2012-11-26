/**
 * 
 */
package net.sf.taverna.t2.component.registry;

/**
 * @author alanrw
 *
 */
public interface ComponentVersion {
	
	public Integer getVersionNumber();
	
	public String getDescription();

	public String getDataflowString();
	
	public Component getComponent();

}
