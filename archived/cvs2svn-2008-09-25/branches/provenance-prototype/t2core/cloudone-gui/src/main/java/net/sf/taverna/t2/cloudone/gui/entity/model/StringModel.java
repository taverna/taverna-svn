package net.sf.taverna.t2.cloudone.gui.entity.model;

import java.util.List;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * Represents a Literal String or a Blob containing a string
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class StringModel extends EntityModel implements
		Observable<StringModelEvent> {
	/*
	 * Sends notifications of events to registered observers
	 */
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

	/**
	 * If you want to be notified of events regarding this model. Uses the
	 * {@link MultiCaster}
	 */
	public void addObserver(Observer<StringModelEvent> observer) {
		multiCaster.addObserver(observer);

	}

	/**
	 * If you no longer wish to be informed of events regarding this model. Uses
	 * the {@link MultiCaster}
	 */
	public void removeObserver(Observer<StringModelEvent> observer) {
		multiCaster.removeObserver(observer);

	}

	@Override
	public void remove() {
		super.remove();
		multiCaster.notify(new StringModelEvent(
				StringModelEvent.EventType.REMOVED, string));
	}

	/**
	 * Set the string and notify observers of the change
	 * 
	 * @param string
	 */
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
