package net.sf.taverna.t2.cloudone.gui.entity.model;

import net.sf.taverna.t2.cloudone.entity.Literal;

import org.apache.log4j.Logger;

/**
 * What happened and to what {@link Literal}
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class LiteralModelEvent extends ModelEvent<Object> {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LiteralModelEvent.class);

	public LiteralModelEvent(EventType eventType, Object literal) {
		super(eventType, literal);
	}

}
