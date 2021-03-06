package net.sf.taverna.t2.cloudone.gui.entity.model;

import java.util.List;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * Model (in MVC terms) for the
 * {@link net.sf.taverna.t2.cloudone.entity.Literal} being added or removed from
 * a {@link LiteralView}. Interested parties can register with it (delegated to
 * the {@link net.sf.taverna.t2.lang.observer.MultiCaster}) to receive
 * notifications when this model changes.
 * <p>
 * String literals are handled by the {@link StringModel} since they can be
 * either {@link net.sf.taverna.t2.cloudone.entity.Literal}s or
 * {@link net.sf.taverna.t2.cloudone.refscheme.BlobReferenceScheme}s
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class LiteralModel extends EntityModel implements
		Observable<LiteralModelEvent> {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LiteralModel.class);
	/*
	 * Notifies changes to the model to whoever is registered with it
	 */
	private MultiCaster<LiteralModelEvent> multiCaster = new MultiCaster<LiteralModelEvent>(
			this);
	/*
	 * The Literal that this model represents
	 */
	private Object literal = null;
	@SuppressWarnings("unused")
	/*
	 * The parent 'container' for this Literal
	 */
	private EntityListModel parentModel;

	public LiteralModel(EntityListModel parentModel) {
		super(parentModel);
		this.parentModel = parentModel;
	}

	/**
	 * If you want to be notified of changes to the model. Uses the
	 * {@link MultiCaster}
	 */
	public void addObserver(Observer<LiteralModelEvent> observer) {
		multiCaster.addObserver(observer);
	}

	/**
	 * If you no longer want notified of changes. Use the {@link MultiCaster}
	 */
	public void removeObserver(Observer<LiteralModelEvent> observer) {
		multiCaster.removeObserver(observer);
	}

	@Override
	public void remove() {
		super.remove();
		multiCaster.notify(new LiteralModelEvent(
				LiteralModelEvent.EventType.REMOVED, literal));
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
		multiCaster.notify(new LiteralModelEvent(ModelEvent.EventType.ADDED,
				literal));
	}

	public Object getLiteral() {
		return literal;
	}

	public List<Observer<LiteralModelEvent>> getObservers() {
		return multiCaster.getObservers();
	}

}
