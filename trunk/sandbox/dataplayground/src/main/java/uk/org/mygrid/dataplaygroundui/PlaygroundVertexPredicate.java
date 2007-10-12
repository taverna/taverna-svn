package uk.org.mygrid.dataplaygroundui;

import org.apache.commons.collections.Predicate;

import uk.org.mygrid.dataplayground.PlaygroundDataObject;
import uk.org.mygrid.dataplayground.PlaygroundDataThing;
import uk.org.mygrid.dataplayground.PlaygroundPortObject;

public class PlaygroundVertexPredicate implements Predicate {

	public boolean evaluate(Object object) {

		if (object instanceof PlaygroundDataObject) {
			return !((PlaygroundDataObject)object).isHidden();
		}
		if (object instanceof PlaygroundPortObject) {
			return !((PlaygroundPortObject)object).isHidden();
		}

		if (object instanceof PlaygroundDataThing) {
			return !((PlaygroundDataThing)object).isHidden();
		}

	return true;
	
	}

}
