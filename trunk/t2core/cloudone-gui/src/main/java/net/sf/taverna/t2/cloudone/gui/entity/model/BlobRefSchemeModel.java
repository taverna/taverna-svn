package net.sf.taverna.t2.cloudone.gui.entity.model;

import net.sf.taverna.t2.lang.observer.Observer;

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

	public void registerObserver(Observer<String> observer) {
		// TODO Auto-generated method stub
		
	}

	public void removeObserver(Observer<String> observer) {
		// TODO Auto-generated method stub
		
	}
}
