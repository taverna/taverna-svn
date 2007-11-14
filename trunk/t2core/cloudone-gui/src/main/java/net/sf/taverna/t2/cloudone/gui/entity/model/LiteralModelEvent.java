package net.sf.taverna.t2.cloudone.gui.entity.model;

import org.apache.log4j.Logger;

public class LiteralModelEvent extends ModelEvent<LiteralModel>{
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LiteralModelEvent.class);
	
	public LiteralModelEvent(EventType eventType, LiteralModel entityModel) {
		super(eventType, entityModel);
		// TODO Auto-generated constructor stub
	}

}
