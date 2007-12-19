package net.sf.taverna.t2.cloudone.gui.entity.model;

import org.apache.log4j.Logger;

/**
 * What changed and why
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class StringModelEvent extends ModelEvent<String> {
	public StringModelEvent(EventType eventType, String entityModel) {
		super(eventType, entityModel);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(StringModelEvent.class);
}
