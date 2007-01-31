package uk.org.mygrid.tavernaservice.wsdl;

import java.io.Serializable;

import org.embl.ebi.escience.baclava.DataThing;

public class ResultBean implements Serializable {

	private static final long serialVersionUID = -3774365822183427437L;
	String name;
    DataThingBean datathing;
	
    ResultBean(String name, DataThing datathing) {
    	this.name = name;
    	this.datathing = new DataThingBean(datathing);
    }
    
	public String getName() {
		return this.name;	
	}
	
	public DataThingBean getValue() {
		return this.datathing;
	}
	
}
