package net.sf.taverna.t2.cloudone.gui.entity.model;

import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.lang.observer.Observable;

/**
 * Super class for the {@link ReferenceScheme}s which are modelled
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public abstract class ReferenceSchemeModel<Event> implements Observable<Event> {

	public abstract void remove();

	public abstract String getStringRepresentation();

}
