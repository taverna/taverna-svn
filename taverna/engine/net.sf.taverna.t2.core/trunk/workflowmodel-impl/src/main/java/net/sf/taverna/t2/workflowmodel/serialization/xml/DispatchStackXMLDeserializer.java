/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
package net.sf.taverna.t2.workflowmodel.serialization.xml;

import java.util.List;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Invoke;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Stop;

import org.jdom.Element;

public class DispatchStackXMLDeserializer extends AbstractXMLDeserializer {
	private static DispatchStackXMLDeserializer instance = new DispatchStackXMLDeserializer();

	private DispatchStackXMLDeserializer() {

	}

	public static DispatchStackXMLDeserializer getInstance() {
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public void deserializeDispatchStack(Processor processor,
			Element dispatchStack) throws ClassNotFoundException, InstantiationException, IllegalAccessException, EditException {
		int layers=0;
		for (Element layer : (List<Element>)dispatchStack.getChildren(DISPATCH_LAYER,T2_WORKFLOW_NAMESPACE)) {
			DispatchLayer<?> dispatchLayer = DispatchLayerXMLDeserializer.getInstance().deserializeDispatchLayer(layer);
			if (dispatchLayer instanceof Invoke) {
				edits.getAddDispatchLayerEdit(processor.getDispatchStack(), new Stop(), layers++).doEdit();
			}
			edits.getAddDispatchLayerEdit(processor.getDispatchStack(), dispatchLayer, layers++).doEdit();
		}
		
	}
}
