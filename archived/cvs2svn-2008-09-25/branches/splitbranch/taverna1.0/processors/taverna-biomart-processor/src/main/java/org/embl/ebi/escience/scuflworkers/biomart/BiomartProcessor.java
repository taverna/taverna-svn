/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: BiomartProcessor.java,v $
 * Revision           $Revision: 1.1.2.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-07-05 16:40:57 $
 *               by   $Author: davidwithers $
 * Created on 17-Mar-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.biomart;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.biomart.martservice.MartQuery;
import org.biomart.martservice.query.Attribute;
import org.biomart.martservice.query.Filter;
import org.biomart.martservice.query.QueryListener;
import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.MinorScuflModelEvent;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.view.WorkflowSummaryAsHTML;
import org.embl.ebi.escience.scuflworkers.HTMLSummarisableProcessor;

/**
 * 
 * @author Tom Oinn
 * @author David Withers
 * @author Stuart Owen
 */
public class BiomartProcessor extends Processor implements HTMLSummarisableProcessor {
	private MartQuery query;

	private QueryListener queryListener;

	/**
	 * @param model
	 * @param name
	 * @throws ProcessorCreationException
	 * @throws DuplicateProcessorNameException
	 */
	public BiomartProcessor(ScuflModel model, String processorName, MartQuery query) throws ProcessorCreationException,
			DuplicateProcessorNameException {
		super(model, processorName);
		setDescription(query.getMartDataset().getDisplayName());
		this.query = query;

		try {
			// query.getMartService().calculateLinks();
			buildOutputPortsFromQuery();
			buildInputPortsFromQuery();
		} catch (Exception ex) {
			ProcessorCreationException pce = new ProcessorCreationException("Can't build output ports");
			pce.initCause(ex);
			throw pce;
		}

		// Register a query change listener to trap changes to the query object
		// and fire them off as minor model events
		queryListener = new QueryListener() {
			public void attributeAdded(Attribute attribute) {
				updateOutputs();
			}

			public void attributeRemoved(Attribute attribute) {
				updateOutputs();
			}

			public void filterAdded(Filter filter) {
				updateInputs();
			}

			public void filterRemoved(Filter filter) {
				updateInputs();
			}

			public void filterChanged(Filter filter) {
				pingModel();
			}

			private void updateInputs() {
				try {
					buildInputPortsFromQuery();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			private void updateOutputs() {
				try {
					buildOutputPortsFromQuery();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		query.getQuery().addQueryListener(queryListener);
	}

	protected void finalize() throws Throwable {
		query.getQuery().removeQueryListener(queryListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scufl.Processor#getProperties()
	 */
	public Properties getProperties() {
		return new Properties();
	}

	public MartQuery getQuery() {
		return query;
	}

	void pingModel() {
		fireModelEvent(new MinorScuflModelEvent(this, "Filter values changed"));
	}

	private void buildInputPortsFromQuery() throws PortCreationException, DuplicatePortNameException {
		List filters = query.getQuery().getFilters();
		Set<String> filterNames = new HashSet<String>();
		// Create new input ports corresponding to filters
		for (Iterator iter = filters.iterator(); iter.hasNext();) {
			Filter filter = (Filter) iter.next();
			String name = filter.getQualifiedName();
			filterNames.add(name + "_filter");
			try {
				locatePort(name + "_filter");
			} catch (UnknownPortException upe) {
				Port newPort = new InputPort(this, name + "_filter");
				if (filter.isList()) {
					newPort.setSyntacticType("l('text/plain')");
				} else {
					newPort.setSyntacticType("'text/plain'");
				}
				addPort(newPort);
			}
		}
		// Remove any ports which don't have filters with corresponding
		// names
		InputPort[] currentInputs = getInputPorts();
		for (int i = 0; i < currentInputs.length; i++) {
			Port inputPort = currentInputs[i];
			if (filterNames.contains(inputPort.getName()) == false) {
				removePort(inputPort);
			}
		}
	}

	private void buildOutputPortsFromQuery() throws PortCreationException, DuplicatePortNameException {
		List attributes = query.getQuery().getAttributes();
		Set<String> attributeNames = new HashSet<String>();
		// Create new output ports corresponding to attributes
		for (Iterator iter = attributes.iterator(); iter.hasNext();) {
			Attribute attribute = (Attribute) iter.next();
			String name = attribute.getQualifiedName();
			attributeNames.add(name);
			try {
				locatePort(name);
			} catch (UnknownPortException upe) {
				Port newPort = new OutputPort(this, name);
				newPort.setSyntacticType("l('text/plain')");
				addPort(newPort);
			}
		}
		// Remove any ports which don't have attributes with corresponding
		// names
		OutputPort[] ports = getOutputPorts();
		for (int i = 0; i < ports.length; i++) {
			Port outputPort = ports[i];
			if (attributeNames.contains(outputPort.getName()) == false) {
				removePort(outputPort);
			}
		}
	}

	public String getHTMLSummary(List<HTMLSummarisableProcessor> processors, Map<String, Processor> names) {
		StringBuffer sb = new StringBuffer();
		for (HTMLSummarisableProcessor p : processors) {
			sb.append("<tr><td bgcolor=\"d1eeed\" rowspan=\"2\">Biomart</td>");
			BiomartProcessor bp = (BiomartProcessor) p;
			sb.append("<td bgcolor=\"d1eeed\">Dataset Name</td>");
			sb.append("<td bgcolor=\"d1eeed\">Processor</td>");
			sb.append("</tr>");
			sb.append("<tr><td>" + bp.getQuery().getMartService().getLocation() + "</td>");
			sb.append("<td>" + WorkflowSummaryAsHTML.nameFor(names, bp) + "</td></tr>");
		}
		return sb.toString();
	}

	public int htmlTablePlacement() {
		return 5;
	}

}
