/*
 * Created on Dec 8, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.embl.ebi.escience.scuflui.graph.model;

import org.embl.ebi.escience.scufl.Port;

/**
 * COMMENT 
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.1 $
 */
public class DummyProcessor
{
	private Port port;
	
	/**
	 * @param port
	 */
	public DummyProcessor(Port port)
	{
		this.port = port;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if(obj instanceof DummyProcessor)
		{
			return ((DummyProcessor)obj).port.equals(port);
		}
		return super.equals(obj);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return port.toString();
	}
	
	/**
	 * @return the external workflow port
	 */
	public Port getPort()
	{
		return port;
	}
	
	public int hashCode()
	{
		return port.hashCode();
	}
}
