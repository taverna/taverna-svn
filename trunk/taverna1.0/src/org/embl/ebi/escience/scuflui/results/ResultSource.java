/*
 * Created on Sep 23, 2004
 */
package org.embl.ebi.escience.scuflui.results;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class ResultSource
{
	private int depth = -1;
	
	private Port sourcePort;
	private DataThing thing;
	
	protected HashMap results = new HashMap();

	protected Collection inputs = new HashSet();
	protected Collection outputs = new HashSet();
	protected Collection outputProcessors = new HashSet();
	
	/**
	 * @param sourcePort
	 * @param thing
	 */
	public ResultSource(Port sourcePort, DataThing thing)
	{
		this.sourcePort = sourcePort;
		this.thing = thing;
	}
	
	public void addInput(ResultSource thing)
	{
		inputs.add(thing);
	}

	public void addOutput(ResultSource thing)
	{
		outputs.add(thing);
	}	
	
	public Processor getProcessor()
	{
		return sourcePort.getProcessor();
	}
	
	public void addOutputProcessor(Processor processor)
	{
		if(!processor.getName().equals("SCUFL_INTERNAL_SINKPORTS"))
		{
			outputProcessors.add(processor);
		}
	}
		
	public int getDepth()
	{
		if(depth == -1)
		{
			if (inputs.isEmpty())
			{
				depth = 0;
			}
			Iterator inputIterator = inputs.iterator();
			while(inputIterator.hasNext())
			{
				ResultSource source = (ResultSource)inputIterator.next();
				depth = Math.max(depth, source.getDepth());
			}
			depth = depth + 1;
		}
		return depth;
	}
	
	public ResultThing getResultThing(String lsid)
	{
		return (ResultThing)results.get(lsid);
	}
	
	public void populateResults(HashMap provenance)
	{
		addResult(thing, provenance, null);
	}
	
	private void addResult(DataThing thing, HashMap provenance, Collection inputLSIDs)
	{
		Collection inputList = new HashSet();
		if (inputLSIDs != null)
		{
			inputList.addAll(inputLSIDs);
		}
		Collection moreInputs = (Collection)provenance.get(thing.getLSID(thing.getDataObject()));
		if(moreInputs != null)
		{
			inputList.addAll(moreInputs);
		}
		String[] lsids = new String[ inputList.size() ];
		inputList.toArray(lsids);
		ResultThing result = new ResultThing(thing, lsids);
		result.source = this;
		results.put(result.getLSID(), result);
		
		if(thing.getDataObject() instanceof Collection)
		{
			Iterator childIterator = thing.childIterator();
			while(childIterator.hasNext())
			{
				DataThing child = (DataThing)childIterator.next();
				addResult(child, provenance, inputList);
			}
		}
	}
	
	public String toString()
	{
		if (sourcePort.getProcessor().getName().equals("SCUFL_INTERNAL_SOURCEPORTS"))
		{
			return sourcePort.getName();
		}
		return sourcePort.getProcessor().getName() + ":" + sourcePort.getName();
	}	
}