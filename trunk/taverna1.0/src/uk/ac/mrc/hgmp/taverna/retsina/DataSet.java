/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tim Carver, HGMP
 */

package uk.ac.mrc.hgmp.taverna.retsina;

// Utility Imports
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;

import java.lang.String;

import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.baclava.DataThing;


public class DataSet
{

  private String dataSet = "";
  private int id = 1;
  private Hashtable ports = new Hashtable();

  /** Map of name:String -> data:DataThing */
  private HashMap dataMap = new HashMap();

  public DataSet()
  {
  }

  /**
  *
  * Add data to the data set
  *
  */
  public void addData(String name, String type, String value,
                      org.embl.ebi.escience.scufl.Port port)
  {
     dataSet = dataSet+" <data>\n";
     dataSet = dataSet+"   <ID>"+id+"</ID>\n";
     dataSet = dataSet+"   <name>"+name+"</name>\n";
     dataSet = dataSet+"   <type>"+type+"</type>\n";
     dataSet = dataSet+"   <value><![CDATA["+value+"]]></value>\n";
     dataSet = dataSet+" </data>";
     id++;

     ports.put(port,value);
     DataThing dataThing = DataThingFactory.bake(value);
     dataMap.put(name, dataThing);
  }

  /**
  *
  * Use to set a new value in the data set
  * @param name		name of data
  * @param value	value of data
  * @param p		associated port
  *
  */
  public void setDataValue(String name, String value, 
                  org.embl.ebi.escience.scufl.Port p)
  {
    if(ports.containsKey(p))
    {
      int index = dataSet.indexOf(name);
      if( index > -1 )
      {
        String oldValue = (String)ports.get(p);
        int startIndex = dataSet.indexOf("<value><![CDATA["+oldValue,index);
        int endIndex   = dataSet.indexOf("</value>",startIndex);
        dataSet = dataSet.substring(0,startIndex) +
                  "<value><![CDATA["+value+"]]>"  +
                  dataSet.substring(endIndex);
      }
      ports.remove(p);
    }
    ports.put(p,value);
    DataThing dataThing = DataThingFactory.bake(value);
    dataMap.put(name, dataThing);
  }

  /**
  *
  * Get the data set as a String
  * @return	data set
  *
  */
  public String getDataSetString()
  {
    return "<?xml version=\"1.0\"?><dataset>\n"+
           dataSet+"\n</dataset>";
  }

  /**
   * Get the data as a map of Baclava objects
   */ 
  public Map getData() {
    return dataMap;
  }

  /**
  *
  * Determine if the dataset has a data value setting
  * for a port
  * @param p	port
  * @return	true if a value is registered for that port
  *
  */
  public boolean dataContains(org.embl.ebi.escience.scufl.Port p)
  {
    return ports.containsKey(p);
  }

  /**
  *
  * Get the value for a port
  * @param p    Scufl port
  * @return     value for that port or null if none set
  *
  */
  public String getValue(org.embl.ebi.escience.scufl.Port p)
  {
    if(dataContains(p))
      return (String)ports.get(p);

    return null;
  }

  
}

