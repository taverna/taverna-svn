package net.sf.taverna.t2.cloudone.gui.entity.model;

import java.util.List;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;


public class LiteralModel extends EntityModel implements Observable<LiteralModelEvent>{
	

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LiteralModel.class);
	
	private MultiCaster<LiteralModelEvent> multiCaster = new MultiCaster<LiteralModelEvent>(this);
	
	private Object literal = null;
	@SuppressWarnings("unused")
	private EntityListModel parentModel;
	
	public LiteralModel(EntityListModel parentModel) {
		super(parentModel);
		this.parentModel = parentModel;
	}
	
	public void addObserver(Observer<LiteralModelEvent> observer) {
		multiCaster.addObserver(observer);
	}

	public void removeObserver(Observer<LiteralModelEvent> observer) {
		multiCaster.removeObserver(observer);
	}
	
	@Override
	public void remove() {
		super.remove();
		multiCaster.notify(new LiteralModelEvent(LiteralModelEvent.EventType.REMOVED, literal));
	}
	
	public void setLiteral(Integer literal) {
		setLiteralObject(literal);
	}
	
	public void setLiteral(Float literal) {
		setLiteralObject(literal);
	}
	public void setLiteral(Double literal) {
		setLiteralObject(literal);
	}
	public void setLiteral(Boolean literal) {
		setLiteralObject(literal);
	}
	public void setLiteral(Long literal) {
		setLiteralObject(literal);
	}
	
	private void setLiteralObject(Object literal) {
		if (literal == null) {
			throw new NullPointerException();
		}
		this.literal = literal;
		multiCaster.notify(new LiteralModelEvent(ModelEvent.EventType.ADDED, literal));
	}

	public Object getLiteral() {
		return literal;
	}

	public List<Observer<LiteralModelEvent>> getObservers() {
		return multiCaster.getObservers();
	}
	
}
