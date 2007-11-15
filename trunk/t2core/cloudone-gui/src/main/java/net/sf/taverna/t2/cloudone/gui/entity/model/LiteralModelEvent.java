package net.sf.taverna.t2.cloudone.gui.entity.model;

import org.apache.log4j.Logger;

public class LiteralModelEvent extends ModelEvent<Object>{
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LiteralModelEvent.class);
	
	public LiteralModelEvent(EventType eventType, Object literal) {
		super(eventType, literal);
	}

}
