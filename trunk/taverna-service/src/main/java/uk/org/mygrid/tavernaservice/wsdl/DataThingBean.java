package uk.org.mygrid.tavernaservice.wsdl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.embl.ebi.escience.baclava.DataThing;

public class DataThingBean implements Serializable {
	
	private DataThing thing;

	public DataThingBean(DataThing thing) {
		this.thing = thing;
	}
	
	public boolean isCollection() {
		return thing.getDataObject() instanceof Collection;
	}
	public String getStringValue() {
		if (isCollection()) {
			return null;
		}
		return thing.getDataObject().toString();
	}
	
	public DataThingBean[] getChildren() {
		if (! isCollection()) {
			return null;
		}
		List<DataThingBean> children = new ArrayList<DataThingBean>();
		Iterator<DataThing> things = thing.childIterator();
		while (things.hasNext()) {
			DataThing thing = things.next();
			DataThingBean bean = new DataThingBean(thing);
			children.add(bean);
		}
		return children.toArray(new DataThingBean[0]);
	}
}
