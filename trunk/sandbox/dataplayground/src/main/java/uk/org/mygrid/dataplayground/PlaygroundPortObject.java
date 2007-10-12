package uk.org.mygrid.dataplayground;

import java.util.Iterator;
import java.util.Set;

import org.embl.ebi.escience.scufl.Port;

public class PlaygroundPortObject extends PlaygroundObject {

	Port port;
	
	//	set if this vertex is supposed to be hidden in the graph visualisation;
	boolean hidden;
	//set if we never want to see this port object in the visualisation;
	boolean invisible;
	
	//Holds the playground object mapped to this port , could be a PlaygroundDataThing or PlaygroundDataObject 
	PlaygroundObject mappedObject;
	
	public PlaygroundPortObject(Port p){
		
		 port = p;
		 setName(p.getName());
	}

	public Port getPort() {
		return port;
	}

	public void setP(Port p) {
		port = p;
	}

	public PlaygroundObject getMappedObject() {
		
		Set predecessors = getPredecessors();
		Iterator i = predecessors.iterator();
		
		if(i.hasNext())
			return (PlaygroundObject)i.next();
		
		return null;
	}

	public void setMappedObject(PlaygroundObject mappedObject) {
		this.mappedObject = mappedObject;
	}
	
	public String toString() {

		return getName();
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isInvisible() {
		return invisible;
	}

	public void setInvisible(boolean invisible) {
		this.invisible = invisible;
	}

	
	
	
	
}
