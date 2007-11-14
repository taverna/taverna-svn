package net.sf.taverna.t2.cloudone.gui.entity.model;

import org.apache.log4j.Logger;

public class StringModelEvent extends ModelEvent<StringModel>{
	public StringModelEvent(EventType eventType, StringModel entityModel) {
		super(eventType, entityModel);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(StringModelEvent.class);
}
