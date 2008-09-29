package uk.org.mygrid.dataplayground;

import java.util.Iterator;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;

public class PlaygroundDataThing extends PlaygroundObject {

	DataThing dataThing;

	// set if this vertex is supposed to be hidden in the graph visualisation;
	boolean hidden;

	public PlaygroundDataThing() {
		super();
		setName("Data Thing");
		DataThing newThing = DataThingFactory.bake(new String("Data"));
		newThing.getMetadata().addMIMEType("text/plain");
		dataThing = newThing;
		// dataThing.g
	}

	public PlaygroundDataThing(DataThing thing, String name) {
		super();
		dataThing = thing;
		setName(name);
		flatten(dataThing);
	}

	public void flatten(DataThing thing) {
		if (thing != null) {
			Iterator<DataThing> i = thing.childIterator();
			if (i.hasNext()) {
				flatten(i.next());
			} else {
				dataThing = thing;
			}
		}
	}

	public DataThing getDataThing() {
		return dataThing;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setDataThing(DataThing dataThing) {
		this.dataThing = dataThing;
		// setName(dataThing.toString());
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public String toString() {
		return getName();
	}
}
