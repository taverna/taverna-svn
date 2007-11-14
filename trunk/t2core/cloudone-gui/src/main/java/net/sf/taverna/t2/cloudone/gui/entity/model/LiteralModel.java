package net.sf.taverna.t2.cloudone.gui.entity.model;

import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;


public class LiteralModel extends EntityModel implements Observable<LiteralModelEvent>{
	

	private static Logger logger = Logger.getLogger(LiteralModel.class);
	
	private MultiCaster<LiteralModelEvent> multiCaster = new MultiCaster<LiteralModelEvent>(this);
	
	private Literal literal = null;
	private EntityListModel parentModel;
	
	public LiteralModel(EntityListModel parentModel) {
		super(parentModel);
		this.parentModel = parentModel;
		// TODO Auto-generated constructor stub
	}
	
	public void registerObserver(Observer<LiteralModelEvent> observer) {
		multiCaster.registerObserver(observer);
	}

	public void removeObserver(Observer<LiteralModelEvent> observer) {
		multiCaster.removeObserver(observer);
	}
	
	public void removeLiteral() {
		parentModel.removeEntityModel(this);
		multiCaster.notify(new LiteralModelEvent(LiteralModelEvent.EventType.REMOVED, this));
	}
}
