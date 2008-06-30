package net.sf.taverna.t2.workflowmodel.processor.dispatch;

/**
 * If a layer requires notification of the arrival of new items to the event
 * queues within the dispatcher it should implement this interface.
 * 
 * @author Tom Oinn
 * 
 */
public interface NotifiableLayer {

	/**
	 * Called when a new Job or Completion is added to a queue within the
	 * dispatch stack
	 * 
	 * @param owningProcess
	 */
	public void eventAdded(String owningProcess);

}
