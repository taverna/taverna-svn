package net.sf.taverna.t2.cloudone.gui.entity.model;

import java.util.List;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

public class StringModel extends EntityModel implements
		Observable<StringModelEvent> {

	private MultiCaster<StringModelEvent> multiCaster = new MultiCaster<StringModelEvent>(
			this);
	@SuppressWarnings("unused")
	private EntityListModel parentModel;
	private String string;

	public StringModel(EntityListModel parentModel) {
		super(parentModel);
		this.parentModel = parentModel;
	}

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(StringModel.class);

	public void addObserver(Observer<StringModelEvent> observer) {
		multiCaster.addObserver(observer);

	}

	public void removeObserver(Observer<StringModelEvent> observer) {
		multiCaster.removeObserver(observer);

	}

	@Override
	public void remove() {
		super.remove();
		multiCaster.notify(new StringModelEvent(
				StringModelEvent.EventType.REMOVED, string));
	}

	public void setString(String string) {
		this.string = string;
		multiCaster.notify(new StringModelEvent(
				StringModelEvent.EventType.ADDED, string));
	}

	public String getString() {
		return string;
	}

	public List<Observer<StringModelEvent>> getObservers() {
		return multiCaster.getObservers();
	}
}
