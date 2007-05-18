package net.sf.taverna.t2.monitor;

/**
 * Implemented by entities that can be included in the monitor state tree
 * <p>
 * This is now a marker interface, the previous ability to get properties by
 * process identifier was a security leak in that it allowed agents to access
 * properties which didn't belong to them (bad idea)
 * 
 * @author Tom Oinn
 * 
 */
public interface Monitorable {

}
