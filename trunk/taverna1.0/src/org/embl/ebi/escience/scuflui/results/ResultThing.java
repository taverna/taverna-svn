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
	
	protected Collection inputs = new HashSet();
	protected Collection outputs = new HashSet();
	
	/**
	 * @param thing
	 */
	public ResultThing(DataThing thing)
	{
		super();
		this.thing = thing;
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
}
