/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tim Carver, HGMP
 */

package uk.ac.mrc.hgmp.taverna.retsina;

import com.jgraph.graph.Port; // ambiguous with: org.embl.ebi.escience.scufl.Port
import java.util.Hashtable;

public class DataSet
{

  private String dataSet = "";
  private int id = 1;
  private Hashtable ports = new Hashtable();

  public DataSet()
  {
  }

  /**
  *
  * Add data to the data set
  *
  */
  public void addData(String name, String type, String value,
                      Port port)
  {
     dataSet = dataSet+" <data>\n";
     dataSet = dataSet+"   <ID>"+id+"</ID>\n";
     dataSet = dataSet+"   <name>"+name+"</name>\n";
     dataSet = dataSet+"   <type>"+type+"</type>\n";
     dataSet = dataSet+"   <value><![CDATA["+value+"]]></value>\n";
     dataSet = dataSet+" </data>";
     id++;

     ports.put(port,value);
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

  public boolean dataContains(Port p)
  {
    return ports.containsKey(p);
  }

  public String getValue(Port p)
  {
    if(dataContains(p))
      return (String)ports.get(p);

    return null;
  }

  
}

