/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tim Carver, HGMP
 */

package uk.ac.mrc.hgmp.taverna.retsina;


public class DataSet
{

  private String dataSet = "";
  private int id = 1;

  public DataSet()
  {
  }

  /**
  *
  * Add data to the data set
  *
  */
  public void addData(String name, String type,
                      String value)
  {
     dataSet = dataSet+" <data>\n";
     dataSet = dataSet+"   <ID>"+id+"</ID>\n";
     dataSet = dataSet+"   <name>"+name+"</name>\n";
     dataSet = dataSet+"   <type>"+type+"</type>\n";
     dataSet = dataSet+"   <value><![CDATA["+value+"]]></value>\n";
     dataSet = dataSet+" </data>";
     id++;
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

}

