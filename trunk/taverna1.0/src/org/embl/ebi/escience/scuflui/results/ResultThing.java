/*
 * Created on Sep 23, 2004
 */
package org.embl.ebi.escience.scuflui.results;

import java.util.Collection;
import java.util.HashSet;

import org.embl.ebi.escience.baclava.DataThing;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class ResultThing
{
	private DataThing thing;
	protected String[] inputLSIDs;
	protected ResultSource source;
	
	protected Collection inputs = new HashSet();
	protected Collection outputs = new HashSet();
	
	/**
	 * @param thing
	 * @param inputLSIDs
	 */
	public ResultThing(DataThing thing, String[] inputLSIDs)
	{
		super();
		this.thing = thing;
		this.inputLSIDs = inputLSIDs;
	}
	
	public void addInput(ResultThing thing)
	{
		inputs.add(thing);
	}

	public void addOutput(ResultThing thing)
	{
		outputs.add(thing);
	}	
	
	public DataThing getDataThing()
	{
		return thing;
	}
	
	public String getLSID()
	{
		return thing.getLSID(thing.getDataObject());
	}
	
	public String toString()
	{
		return thing.getDataObject().toString();
	}
}
