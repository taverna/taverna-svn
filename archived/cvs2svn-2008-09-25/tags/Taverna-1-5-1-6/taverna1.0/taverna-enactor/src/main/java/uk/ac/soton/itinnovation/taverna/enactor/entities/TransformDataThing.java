////////////////////////////////////////////////////////////////////////////////
//
// (c) University of Nottingham, 2004
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2.1
// of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation Inc, 59 Temple Place, Suite 330, Boston MA 02111-1307 USA.
//
//      Created By          :   Chris Greenhalgh
//      Created Date        :   2004/10/01
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: sowen70 $
//                              $Date: 2006-07-10 14:05:58 $
//                              $Revision: 1.2 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;

/** DataThing transformations, currently just replacelsid special case.
 * @author Chris Greenhalgh
 */
public class TransformDataThing 
{
    /** go through outputs named 'replacelsid...' and for any string-type
     * values replace 'replacelsid:'<inputname>('['<index>']')* with
     * actual lsid of corresponding input (part). Allows processors to generate
     * metadata and webpages refering to other values in the workflow.
     * Chris Greenhalgh
     */
    public static DataThing replacelsid(DataThing outdt, Map inputMap, Map outputMap) 
    {
	try 
	{
	    //System.out.println("replacelsid on "+outdt.getLSID(outdt.getDataObject()));
	    Object newValue = replaceLSIDsInDataThingValues(inputMap, outputMap, outdt, outdt.getDataObject());
	    DataThing newdt = new DataThing(newValue);
	    newdt.copyMetadataFrom(outdt);
	    return newdt;
	} 
	catch (Exception e) 
	{
	    logger.error("replacelsid failed", e);
	    return outdt;
	}
    }
    private static Logger logger = Logger.getLogger(TransformDataThing.class);
    /** recursive worker for replacelsid
     */
    protected static Object replaceLSIDsInDataThingValues(Map inputMap, Map outputMap, DataThing dt, Object value) 
    {
	Object newValue = value;
	if (value instanceof String) 		
	{
	    String asString = (String)value;
	    int index = asString.indexOf("replacelsid:");
	    if (index<0)
		// unchanged
		return value;
	    StringBuffer buffer = new StringBuffer((String)value);
	    while (index >= 0) 			
	    {
		int from = index+"replacelsid:".length();
		int len = 0;
		while (from+len < buffer.length() &&
		    Character.isLetterOrDigit(buffer.charAt(from+len))) 
		    len++;
		String portname = buffer.substring(from, from+len);
		DataThing portvalue = (DataThing)inputMap.get(portname);
		if (portvalue==null)
		    portvalue = (DataThing)outputMap.get(portname);
		if (portvalue==null) 				
		{
		    logger.error("replacedlsid unknown port '"+portname+"'");
		} 
		else 
		{
		    boolean error = false;
		    Object elementvalue = portvalue.getDataObject();
		    while (from+len<buffer.length() && buffer.charAt(from+len)=='[') 
		    {
			// index into value if appropriate
			len++;
			int numlen = 0;
			while(from+len+numlen<buffer.length() &&
			    Character.isDigit(buffer.charAt(from+len+numlen)))
			    numlen++;
			if (from+len+numlen>=buffer.length() || buffer.charAt(from+len+numlen)!=']') 
			{
			    logger.error("replacelsid with ill-formed array index: "+
				buffer.substring(from+len));
			    error = true;
			    break;
			}
			int arrayindex = 0;
			try 
			{
			    arrayindex = new Integer(buffer.substring(from+len, from+len+numlen)).intValue();
			} 
			catch (Exception e) 
			{
			    logger.error("replacelsid with ill-formed array index: "+
				buffer.substring(from+len));
			    error = true;
			    break;
			}
			len = len+numlen+1;
			// index into value
			if (elementvalue.getClass().isArray()) 
			{
			    if (arrayindex<0 || arrayindex>=java.lang.reflect.Array.getLength(elementvalue)) 
			    {
				logger.error("replacelsid with array index ["+arrayindex+
				    "] out of bounds (0.."+java.lang.reflect.Array.getLength(elementvalue)+")");
				error = true;
				break;
			    }
			    elementvalue = java.lang.reflect.Array.get(elementvalue, arrayindex);
			} 
			else if (elementvalue instanceof List) 
			{
			    if (arrayindex<0 || arrayindex>=((List)elementvalue).size()) 
			    {
				logger.error("replacelsid with array index ["+arrayindex+
				    "] out of bounds (0.."+((List)elementvalue).size()+")");
				error = true;
				break;
			    }
			    elementvalue = ((List)elementvalue).get(arrayindex);
			} 
			else 
			{
			    logger.error("replacelsid with array index ["+arrayindex+
				"] for non-array/List value, type "+elementvalue.getClass().getName());
			    error = true;
			    break;
			}
		    }
		    if (!error) 
		    {
			String elementlsid = portvalue.getLSID(elementvalue);
			if (elementlsid==null || elementlsid.length()==0) 
			    logger.error("replacelsid could not find LSID for "+elementvalue);
			else 
			{
			    //System.out.println("Replace "+buffer.substring(index, from+len)+" with "+elementlsid);
			    buffer.replace(index, from+len, elementlsid);
			}
		    }
		}
		index = buffer.indexOf("replacelsid:", index+1);
	    }
	    newValue = buffer.toString();
	}
	else if (value instanceof byte[] || value instanceof byte[][])
	    return value;

	else if (value.getClass().isArray()) 
	{
	    for (int i=0; i<java.lang.reflect.Array.getLength(value); i++)
		java.lang.reflect.Array.set(value, i, 
		    replaceLSIDsInDataThingValues(inputMap, outputMap, dt, 
		    java.lang.reflect.Array.get(value, i)));
	} 
	else if (value instanceof List) 
	{
	    ArrayList newList = new ArrayList((List)value);
	    for (int i=0; i<newList.size(); i++) 
		newList.set(i, replaceLSIDsInDataThingValues(inputMap, outputMap, dt, newList.get(i)));
	    newValue = newList;
	}
	else
	    logger.warn("replacelsid ignoring a "+value.getClass().getName()+" - sorry");

	if (!(newValue==value)) 
	{
	    dt.setLSID(newValue, dt.getLSID(value));
	}
	return newValue;
    }
}

