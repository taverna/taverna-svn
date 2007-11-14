package net.sf.taverna.t2.cloudone.gui.entity.model;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

public class StringModel extends EntityModel implements Observable<StringModelEvent>{
	
	private MultiCaster<StringModelEvent> multiCaster = new MultiCaster<StringModelEvent>(this);
	private EntityListModel parentModel;
	
	
	public StringModel(EntityListModel parentModel) {
		super(parentModel);
		this.parentModel = parentModel;
	}

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(StringModel.class);

	public void registerObserver(Observer<StringModelEvent> observer) {
		multiCaster.registerObserver(observer);
		
	}

	public void removeObserver(Observer<StringModelEvent> observer) {
		multiCaster.removeObserver(observer);
		
	}
	
	public void remove() {
		parentModel.removeEntityModel(this);
		multiCaster.notify(new StringModelEvent(StringModelEvent.EventType.REMOVED, this));
	}
}
