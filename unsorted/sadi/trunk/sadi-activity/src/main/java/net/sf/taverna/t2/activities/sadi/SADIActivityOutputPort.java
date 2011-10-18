/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.sadi;

import net.sf.taverna.t2.workflowmodel.AbstractOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import ca.wilkinsonlab.sadi.beans.RestrictionBean;
import ca.wilkinsonlab.sadi.client.Service;
import ca.wilkinsonlab.sadi.rdfpath.RDFPath;
import ca.wilkinsonlab.sadi.rdfpath.RDFPathElement;
import ca.wilkinsonlab.sadi.utils.LabelUtils;
import ca.wilkinsonlab.sadi.utils.OwlUtils;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * A {@link SADIActivity} output port.
 * 
 * @author David Withers
 */
public class SADIActivityOutputPort extends AbstractOutputPort implements ActivityOutputPort,
		SADIActivityPort {

	private final SADIActivity sadiActivity;
	private final RDFPath rdfPath;
	private final String valuesFromURI;
	private final String valuesFromLabel;

	/**
	 * Constructs a new SADIActivityOutputPort.
	 * 
	 * @param sadiActivity
	 * @param rdfPath
	 * @param name
	 * @param portDepth
	 */
	public SADIActivityOutputPort(SADIActivity sadiActivity, RDFPath rdfPath, String name,
			int portDepth) {
		this(sadiActivity, rdfPath, name, portDepth, portDepth);
	}

	/**
	 * Constructs a new SADIActivityOutputPort.
	 * 
	 * @param sadiActivity
	 * @param rdfPath
	 * @param name
	 * @param portDepth
	 * @param granularDepth
	 */
	public SADIActivityOutputPort(SADIActivity sadiActivity, RDFPath rdfPath, String name,
			int portDepth, int granularDepth) {
		super(name, portDepth, granularDepth);
		this.sadiActivity = sadiActivity;
		this.rdfPath = rdfPath;
		if (rdfPath.isEmpty()) {
			/* if the RDFPath is empty this port produces instances of the 
			 * service's output class...
			 */
			Service service = sadiActivity.getService();
			if (service != null) {
				valuesFromURI = service.getOutputClassURI();
				valuesFromLabel = SADIUtils.getOutputClassLabel(service);
			} else {
				// this shouldn't happen...
				valuesFromURI = null;
				valuesFromLabel = null;
			}
		} else {
			/* if the RDFPath isn't empty, this port consumes instances of
			 * the last path element's valuesFrom...
			 */
			RDFPathElement element = rdfPath.getLastPathElement();
			if (element.getClass() != null) {
				RestrictionBean bean = element.toRestrictionBean();
				valuesFromURI = bean.getValuesFromURI();
				valuesFromLabel = bean.getValuesFromLabel();
			} else {
				/* use the range of the property...
				 */
				Property p = element.getProperty();
				if (p.canAs(OntProperty.class)) {
					OntClass range = OwlUtils.getUsefulRange(p.as(OntProperty.class));
					valuesFromURI = range.getURI();
					valuesFromLabel = LabelUtils.getLabel(range);
				} else {
					valuesFromURI = RDFS.Resource.getURI();
					valuesFromLabel = "Resource";
				}
			}
		}
	}

	public SADIActivity getSADIActivity() {
		return sadiActivity;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.sadi.SADIActivityPort#getRDFPath()
	 */
	public RDFPath getRDFPath()
	{
		return rdfPath;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.sadi.SADIActivityPort#getValuesFromURI()
	 */
	public String getValuesFromURI()
	{
		// TODO Auto-generated method stub
		return valuesFromURI;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.sadi.SADIActivityPort#getValuesFromLabel()
	 */
	public String getValuesFromLabel() {
		// TODO Auto-generated method stub
		return valuesFromLabel;
	}
}
