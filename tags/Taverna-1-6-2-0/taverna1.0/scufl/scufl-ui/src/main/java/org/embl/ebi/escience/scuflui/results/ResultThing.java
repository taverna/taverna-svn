/*
 * Created on Sep 23, 2004
 */
package org.embl.ebi.escience.scuflui.results;

import java.util.Collection;
import java.util.HashSet;

import org.embl.ebi.escience.baclava.DataThing;

/**
 * A result from a workflow. Wraps a {@link DataThing DataThing} object to keep
 * track of provenance information link the {@link ResultSource source} of the
 * result, and the results that created or were created by this one.
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 */
public class ResultThing
{
	private DataThing thing;
	protected String[] inputLSIDs;
	private ResultSource source;

	private Collection inputs = new HashSet();
	private Collection outputs = new HashSet();

	/**
	 * @param source
	 *            the ResultSource that this result came from
	 * @param thing
	 *            the underlying DataThing that is the result
	 * @param inputLSIDs
	 *            a array of LSIDs that created this result
	 */
	public ResultThing(ResultSource source, DataThing thing, String[] inputLSIDs)
	{
		super();
		this.source = source;
		this.thing = thing;
		this.inputLSIDs = inputLSIDs;
	}

	/**
	 * Adds a <code>ResultThing</code> as an input to this result.
	 * 
	 * @param thing
	 *            a <code>ResultThing</code> which
	 */
	protected void addInput(ResultThing thing)
	{
		inputs.add(thing);
	}

	/**
	 * Adds a <code>ResultThing</code> as an output to this result.
	 * 
	 * @param thing
	 *            a ResultThing which was created by the workflow using this
	 *            result.
	 */
	protected void addOutput(ResultThing thing)
	{
		outputs.add(thing);
	}

	/**
	 * @return the underlying DataThing that is the result
	 * @see DataThing
	 */
	public DataThing getDataThing()
	{
		return thing;
	}

	/**
	 * @return the LSID of this result
	 */
	public String getLSID()
	{
		return thing.getLSID(thing.getDataObject());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return thing.getDataObject().toString();
	}

	/**
	 * @return the ResultSource that this result came from
	 * @see ResultSource
	 */
	public ResultSource getSource()
	{
		return source;
	}
}
