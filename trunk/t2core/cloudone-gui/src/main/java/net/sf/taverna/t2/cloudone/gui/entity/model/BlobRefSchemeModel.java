package net.sf.taverna.t2.cloudone.gui.entity.model;

import java.util.List;

import net.sf.taverna.t2.lang.observer.Observer;

/**
 * Incomplete dummy class
 * 
 * @author Ian Dunlop
 *
 */
public class BlobRefSchemeModel extends ReferenceSchemeModel<String> {

	public BlobRefSchemeModel() {

	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
	}

	@Override
	public String getStringRepresentation() {
		return "(blob)";
	}

	public void addObserver(Observer<String> observer) {
		// TODO Auto-generated method stub
		
	}

	public void removeObserver(Observer<String> observer) {
		// TODO Auto-generated method stub
		
	}

	public List<Observer<String>> getObservers() {
		// TODO Auto-generated method stub
		return null;
	}
}
