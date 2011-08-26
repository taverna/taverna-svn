/**
 * 
 */
package uk.org.mygrid.datalineage.model;

import java.util.List;


public interface DataVertex {

	public String getDataId();
	
	public String getDataLsid();
	
	public String getName();
	
	public void setName(String name);
	
	public List<String> getInputNames();
	
	public void setInputNames(List<String> inputNames);

}